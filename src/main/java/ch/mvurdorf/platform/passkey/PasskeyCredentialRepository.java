package ch.mvurdorf.platform.passkey;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static ch.mvurdorf.platform.jooq.Tables.LOGIN;
import static ch.mvurdorf.platform.jooq.Tables.PASSKEY_CREDENTIAL;

/**
 * Gives the WebAuthn library access to the stored passkeys. The username is the email of the login.
 */
@Component
@RequiredArgsConstructor
public class PasskeyCredentialRepository implements CredentialRepository {

    private final DSLContext jooqDsl;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return jooqDsl.select(PASSKEY_CREDENTIAL.CREDENTIAL_ID)
                      .from(PASSKEY_CREDENTIAL)
                      .join(LOGIN).on(LOGIN.ID.eq(PASSKEY_CREDENTIAL.FK_LOGIN))
                      .where(LOGIN.EMAIL.eq(username))
                      .fetchSet(it -> PublicKeyCredentialDescriptor.builder()
                                                                   .id(new ByteArray(it.value1()))
                                                                   .build());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return jooqDsl.select(PASSKEY_CREDENTIAL.USER_HANDLE)
                      .from(PASSKEY_CREDENTIAL)
                      .join(LOGIN).on(LOGIN.ID.eq(PASSKEY_CREDENTIAL.FK_LOGIN))
                      .where(LOGIN.EMAIL.eq(username))
                      .limit(1)
                      .fetchOptional(it -> new ByteArray(it.value1()));
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return jooqDsl.select(LOGIN.EMAIL)
                      .from(PASSKEY_CREDENTIAL)
                      .join(LOGIN).on(LOGIN.ID.eq(PASSKEY_CREDENTIAL.FK_LOGIN))
                      .where(PASSKEY_CREDENTIAL.USER_HANDLE.eq(userHandle.getBytes()))
                      .limit(1)
                      .fetchOptional(LOGIN.EMAIL);
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return jooqDsl.selectFrom(PASSKEY_CREDENTIAL)
                      .where(PASSKEY_CREDENTIAL.CREDENTIAL_ID.eq(credentialId.getBytes()))
                      .and(PASSKEY_CREDENTIAL.USER_HANDLE.eq(userHandle.getBytes()))
                      .fetchOptional(this::toRegisteredCredential);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return jooqDsl.selectFrom(PASSKEY_CREDENTIAL)
                      .where(PASSKEY_CREDENTIAL.CREDENTIAL_ID.eq(credentialId.getBytes()))
                      .fetchSet(this::toRegisteredCredential);
    }

    private RegisteredCredential toRegisteredCredential(Record it) {
        return RegisteredCredential.builder()
                                   .credentialId(new ByteArray(it.get(PASSKEY_CREDENTIAL.CREDENTIAL_ID)))
                                   .userHandle(new ByteArray(it.get(PASSKEY_CREDENTIAL.USER_HANDLE)))
                                   .publicKeyCose(new ByteArray(it.get(PASSKEY_CREDENTIAL.PUBLIC_KEY_COSE)))
                                   .signatureCount(it.get(PASSKEY_CREDENTIAL.SIGNATURE_COUNT))
                                   .build();
    }
}
