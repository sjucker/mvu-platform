package ch.mvurdorf.platform.passkey;

import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.jooq.tables.pojos.Login;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static ch.mvurdorf.platform.jooq.Tables.PASSKEY_CHALLENGE;
import static ch.mvurdorf.platform.jooq.Tables.PASSKEY_CREDENTIAL;
import static ch.mvurdorf.platform.utils.DateUtil.now;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Registers passkeys for logins and verifies passkey assertions (WebAuthn relying party).
 * Pending requests (containing the challenge) are stored in the database, since the API is stateless.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasskeyService {

    static final Duration REQUEST_VALIDITY = Duration.ofMinutes(5);
    private static final int USER_HANDLE_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DSLContext jooqDsl;
    private final LoginDao loginDao;
    private final RelyingParty relyingParty;
    private final PasskeyCredentialRepository credentialRepository;

    @Transactional
    public PasskeyOptionsDto startRegistration(String email) {
        var login = findActiveLogin(email);
        var userHandle = credentialRepository.getUserHandleForUsername(email).orElseGet(PasskeyService::newUserHandle);

        var request = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                                        .user(UserIdentity.builder()
                                                          .name(email)
                                                          .displayName(login.getName())
                                                          .id(userHandle)
                                                          .build())
                                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                                                                                              .residentKey(ResidentKeyRequirement.REQUIRED)
                                                                                              .userVerification(UserVerificationRequirement.REQUIRED)
                                                                                              .build())
                                        .build());

        try {
            var requestId = storeRequest(login.getId(), request.toJson());
            return new PasskeyOptionsDto(requestId, request.toCredentialsCreateJson());
        } catch (JsonProcessingException e) {
            throw new PasskeyException("could not serialize registration request", e);
        }
    }

    @Transactional(noRollbackFor = PasskeyException.class)
    public void finishRegistration(String email, UUID requestId, String credentialJson, String name) {
        var login = findActiveLogin(email);
        var pending = consumeRequest(requestId);
        if (!Objects.equals(pending.fkLogin(), login.getId())) {
            throw new PasskeyException("registration request %s does not belong to %s".formatted(requestId, email));
        }

        try {
            var request = PublicKeyCredentialCreationOptions.fromJson(pending.requestJson());
            var result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                                                                                  .request(request)
                                                                                  .response(PublicKeyCredential.parseRegistrationResponseJson(credentialJson))
                                                                                  .build());

            jooqDsl.insertInto(PASSKEY_CREDENTIAL)
                   .set(PASSKEY_CREDENTIAL.FK_LOGIN, login.getId())
                   .set(PASSKEY_CREDENTIAL.CREDENTIAL_ID, result.getKeyId().getId().getBytes())
                   .set(PASSKEY_CREDENTIAL.USER_HANDLE, request.getUser().getId().getBytes())
                   .set(PASSKEY_CREDENTIAL.PUBLIC_KEY_COSE, result.getPublicKeyCose().getBytes())
                   .set(PASSKEY_CREDENTIAL.SIGNATURE_COUNT, result.getSignatureCount())
                   .set(PASSKEY_CREDENTIAL.NAME, isBlank(name) ? "Passkey" : name.strip())
                   .set(PASSKEY_CREDENTIAL.CREATED_AT, now())
                   .execute();
            log.info("registered passkey for {}", email);
        } catch (IOException | RegistrationFailedException e) {
            throw new PasskeyException("could not register passkey for " + email, e);
        }
    }

    @Transactional
    public PasskeyOptionsDto startAssertion() {
        // no username: the passkey is discoverable, the user chooses it on the device
        var request = relyingParty.startAssertion(StartAssertionOptions.builder()
                                                                       .userVerification(UserVerificationRequirement.REQUIRED)
                                                                       .build());
        try {
            var requestId = storeRequest(null, request.toJson());
            return new PasskeyOptionsDto(requestId, request.toCredentialsGetJson());
        } catch (JsonProcessingException e) {
            throw new PasskeyException("could not serialize assertion request", e);
        }
    }

    /**
     * @return the email of the login the passkey belongs to
     */
    @Transactional(noRollbackFor = PasskeyException.class)
    public String finishAssertion(UUID requestId, String credentialJson) {
        var pending = consumeRequest(requestId);
        try {
            var result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                                                                            .request(AssertionRequest.fromJson(pending.requestJson()))
                                                                            .response(PublicKeyCredential.parseAssertionResponseJson(credentialJson))
                                                                            .build());
            if (!result.isSuccess()) {
                throw new PasskeyException("passkey assertion was not successful");
            }

            jooqDsl.update(PASSKEY_CREDENTIAL)
                   .set(PASSKEY_CREDENTIAL.SIGNATURE_COUNT, result.getSignatureCount())
                   .set(PASSKEY_CREDENTIAL.LAST_USED_AT, now())
                   .where(PASSKEY_CREDENTIAL.CREDENTIAL_ID.eq(result.getCredential().getCredentialId().getBytes()))
                   .execute();

            var email = result.getUsername();
            findActiveLogin(email);
            log.info("passkey login for {}", email);
            return email;
        } catch (IOException | AssertionFailedException e) {
            throw new PasskeyException("could not verify passkey", e);
        }
    }

    public List<PasskeyDto> findPasskeys(String email) {
        var login = findActiveLogin(email);
        return jooqDsl.selectFrom(PASSKEY_CREDENTIAL)
                      .where(PASSKEY_CREDENTIAL.FK_LOGIN.eq(login.getId()))
                      .orderBy(PASSKEY_CREDENTIAL.CREATED_AT)
                      .fetch(it -> new PasskeyDto(it.getId(), it.getName(), it.getCreatedAt(), it.getLastUsedAt()));
    }

    /**
     * @return whether the passkey existed (and belonged to the login)
     */
    public boolean deletePasskey(String email, Long id) {
        var login = findActiveLogin(email);
        var deleted = jooqDsl.deleteFrom(PASSKEY_CREDENTIAL)
                             .where(PASSKEY_CREDENTIAL.ID.eq(id))
                             .and(PASSKEY_CREDENTIAL.FK_LOGIN.eq(login.getId()))
                             .execute();
        log.info("deleted passkey {} of {}: {}", id, email, deleted > 0);
        return deleted > 0;
    }

    private Login findActiveLogin(String email) {
        return loginDao.fetchOptionalByEmail(email)
                       .filter(Login::getActive)
                       .orElseThrow(() -> new PasskeyException("no active login found for " + email));
    }

    private UUID storeRequest(Long fkLogin, String requestJson) {
        jooqDsl.deleteFrom(PASSKEY_CHALLENGE)
               .where(PASSKEY_CHALLENGE.EXPIRES_AT.lt(now()))
               .execute();

        var requestId = UUID.randomUUID();
        jooqDsl.insertInto(PASSKEY_CHALLENGE)
               .set(PASSKEY_CHALLENGE.ID, requestId)
               .set(PASSKEY_CHALLENGE.FK_LOGIN, fkLogin)
               .set(PASSKEY_CHALLENGE.REQUEST_JSON, requestJson)
               .set(PASSKEY_CHALLENGE.EXPIRES_AT, now().plus(REQUEST_VALIDITY))
               .execute();
        return requestId;
    }

    private record PendingRequest(Long fkLogin, String requestJson) {}

    /**
     * Removes the pending request, so that every challenge can only be used once.
     */
    private PendingRequest consumeRequest(UUID requestId) {
        return jooqDsl.deleteFrom(PASSKEY_CHALLENGE)
                      .where(PASSKEY_CHALLENGE.ID.eq(requestId))
                      .and(PASSKEY_CHALLENGE.EXPIRES_AT.ge(now()))
                      .returning(PASSKEY_CHALLENGE.FK_LOGIN, PASSKEY_CHALLENGE.REQUEST_JSON)
                      .fetchOptional(it -> new PendingRequest(it.get(PASSKEY_CHALLENGE.FK_LOGIN), it.get(PASSKEY_CHALLENGE.REQUEST_JSON)))
                      .orElseThrow(() -> new PasskeyException("unknown or expired passkey request " + requestId));
    }

    private static ByteArray newUserHandle() {
        var bytes = new byte[USER_HANDLE_LENGTH];
        RANDOM.nextBytes(bytes);
        return new ByteArray(bytes);
    }
}
