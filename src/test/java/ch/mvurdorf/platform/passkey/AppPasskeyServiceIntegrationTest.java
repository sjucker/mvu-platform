package ch.mvurdorf.platform.passkey;

import ch.mvurdorf.platform.jooq.tables.daos.LoginDao;
import ch.mvurdorf.platform.testing.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static ch.mvurdorf.platform.jooq.Tables.LOGIN;
import static ch.mvurdorf.platform.jooq.Tables.PASSKEY_CHALLENGE;
import static ch.mvurdorf.platform.jooq.Tables.PASSKEY_CREDENTIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppPasskeyServiceIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL = "test@mvurdorf.ch";
    private static final String RP_ID = "platform.mvurdorf.ch";

    private AppPasskeyService appPasskeyService;
    private SoftwareAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        var credentialRepository = new PasskeyCredentialRepository(jooqDsl);
        var relyingParty = new PasskeyConfiguration().relyingParty(credentialRepository, RP_ID, "MVU", List.of());
        appPasskeyService = new AppPasskeyService(jooqDsl, new LoginDao(jooqDsl.configuration()), relyingParty, credentialRepository);
        authenticator = new SoftwareAuthenticator("https://" + RP_ID);

        insertLogin(EMAIL, true);
    }

    @Test
    void registerAndLogin() throws Exception {
        register("iPhone");

        assertThat(appPasskeyService.findPasskeys(EMAIL)).singleElement()
                                                      .satisfies(passkey -> {
                                                          assertThat(passkey.name()).as("name").isEqualTo("iPhone");
                                                          assertThat(passkey.lastUsedAt()).as("not used yet").isNull();
                                                      });

        var login = appPasskeyService.startAssertion();
        var email = appPasskeyService.finishAssertion(login.requestId(), authenticator.get(login.options()));

        assertThat(email).isEqualTo(EMAIL);
        assertThat(appPasskeyService.findPasskeys(EMAIL).getFirst().lastUsedAt()).as("last used").isNotNull();
        assertThat(jooqDsl.fetchCount(PASSKEY_CHALLENGE)).as("requests are consumed").isZero();
    }

    @Test
    void registrationOptionsRequireDiscoverablePasskeyWithUserVerification() {
        var options = appPasskeyService.startRegistration(EMAIL).options();

        assertThat(options).contains("\"rp\":{\"name\":\"MVU\",\"id\":\"" + RP_ID + "\"}")
                           .contains("\"residentKey\":\"required\"")
                           .contains("\"userVerification\":\"required\"");
    }

    @Test
    void loginRequestCanOnlyBeUsedOnce() throws Exception {
        register("iPhone");
        var login = appPasskeyService.startAssertion();
        var credential = authenticator.get(login.options());
        appPasskeyService.finishAssertion(login.requestId(), credential);

        assertThatThrownBy(() -> appPasskeyService.finishAssertion(login.requestId(), credential)).isInstanceOf(PasskeyException.class);
    }

    @Test
    void loginWithUnknownRequestFails() throws Exception {
        register("iPhone");
        var login = appPasskeyService.startAssertion();
        var credential = authenticator.get(login.options());

        assertThatThrownBy(() -> appPasskeyService.finishAssertion(UUID.randomUUID(), credential)).isInstanceOf(PasskeyException.class);
    }

    @Test
    void loginFailsForInactiveLogin() throws Exception {
        register("iPhone");
        jooqDsl.update(LOGIN).set(LOGIN.ACTIVE, false).execute();

        var login = appPasskeyService.startAssertion();
        var credential = authenticator.get(login.options());

        assertThatThrownBy(() -> appPasskeyService.finishAssertion(login.requestId(), credential)).isInstanceOf(PasskeyException.class);
    }

    @Test
    void loginFailsWithDeletedPasskey() throws Exception {
        register("iPhone");
        var id = appPasskeyService.findPasskeys(EMAIL).getFirst().id();
        assertThat(appPasskeyService.deletePasskey(EMAIL, id)).as("deleted").isTrue();

        var login = appPasskeyService.startAssertion();
        var credential = authenticator.get(login.options());

        assertThatThrownBy(() -> appPasskeyService.finishAssertion(login.requestId(), credential)).isInstanceOf(PasskeyException.class);
    }

    @Test
    void registrationRequestOfOtherLoginIsRejected() throws Exception {
        insertLogin("other@mvurdorf.ch", true);
        var registration = appPasskeyService.startRegistration(EMAIL);
        var credential = authenticator.create(registration.options());

        assertThatThrownBy(() -> appPasskeyService.finishRegistration("other@mvurdorf.ch", registration.requestId(), credential, "iPhone"))
                .isInstanceOf(PasskeyException.class);
        assertThat(jooqDsl.fetchCount(PASSKEY_CREDENTIAL)).as("no passkey stored").isZero();
    }

    @Test
    void passkeyOfOtherLoginCanNotBeDeleted() throws Exception {
        insertLogin("other@mvurdorf.ch", true);
        register("iPhone");
        var id = appPasskeyService.findPasskeys(EMAIL).getFirst().id();

        assertThat(appPasskeyService.deletePasskey("other@mvurdorf.ch", id)).as("deleted").isFalse();
        assertThat(appPasskeyService.findPasskeys(EMAIL)).hasSize(1);
    }

    private void register(String name) throws Exception {
        var registration = appPasskeyService.startRegistration(EMAIL);
        appPasskeyService.finishRegistration(EMAIL, registration.requestId(), authenticator.create(registration.options()), name);
    }

    private void insertLogin(String email, boolean active) {
        jooqDsl.insertInto(LOGIN)
               .set(LOGIN.NAME, "Test")
               .set(LOGIN.EMAIL, email)
               .set(LOGIN.ACTIVE, active)
               .set(LOGIN.CALENDAR_TOKEN, UUID.randomUUID().toString())
               .execute();
    }
}
