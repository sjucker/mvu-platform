package ch.mvurdorf.platform.passkey;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class PasskeyConfiguration {

    /**
     * The relying party identifier, i.e. the domain the passkeys are bound to. The app must be associated with this
     * domain (see {@link WellKnownEndpoint}).
     */
    @Bean
    RelyingParty relyingParty(PasskeyCredentialRepository credentialRepository,
                              @Value("${passkey.rp-id}") String rpId,
                              @Value("${passkey.rp-name}") String rpName,
                              @Value("${passkey.android-cert-fingerprints}") List<String> androidCertFingerprints) {
        return RelyingParty.builder()
                           .identity(RelyingPartyIdentity.builder().id(rpId).name(rpName).build())
                           .credentialRepository(credentialRepository)
                           .origins(origins(rpId, androidCertFingerprints))
                           .build();
    }

    /**
     * Native Android apps don't send a web origin, but {@code android:apk-key-hash:<base64url(SHA-256 of signing cert)>}.
     */
    static Set<String> origins(String rpId, List<String> androidCertFingerprints) {
        var origins = new LinkedHashSet<String>();
        origins.add("https://" + rpId);
        androidCertFingerprints.stream()
                               .filter(fingerprint -> !fingerprint.isBlank())
                               .map(fingerprint -> HexFormat.ofDelimiter(":").parseHex(fingerprint.strip().toLowerCase()))
                               .map(hash -> "android:apk-key-hash:" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash))
                               .forEach(origins::add);
        return origins;
    }
}
