package ch.mvurdorf.platform.passkey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Associates the native apps with the relying party domain, so that they may use its passkeys.
 *
 * @see <a href="https://developer.apple.com/documentation/xcode/supporting-associated-domains">Apple associated domains</a>
 * @see <a href="https://developer.android.com/identity/sign-in/credential-manager#add-support-dal">Android Digital Asset Links</a>
 */
@RestController
@RequestMapping(value = "/.well-known", produces = MediaType.APPLICATION_JSON_VALUE)
public class WellKnownEndpoint {

    private final List<String> appleAppIds;
    private final String androidPackage;
    private final List<String> androidCertFingerprints;

    public WellKnownEndpoint(@Value("${passkey.apple-app-ids}") List<String> appleAppIds,
                             @Value("${passkey.android-package}") String androidPackage,
                             @Value("${passkey.android-cert-fingerprints}") List<String> androidCertFingerprints) {
        this.appleAppIds = appleAppIds;
        this.androidPackage = androidPackage;
        this.androidCertFingerprints = androidCertFingerprints.stream().map(String::strip).filter(it -> !it.isEmpty()).toList();
    }

    @GetMapping("/apple-app-site-association")
    public Map<String, Object> appleAppSiteAssociation() {
        return Map.of("webcredentials", Map.of("apps", appleAppIds));
    }

    @GetMapping("/assetlinks.json")
    public List<Map<String, Object>> assetLinks() {
        return List.of(Map.of("relation", List.of("delegate_permission/common.handle_all_urls", "delegate_permission/common.get_login_creds"),
                              "target", Map.of("namespace", "android_app",
                                               "package_name", androidPackage,
                                               "sha256_cert_fingerprints", androidCertFingerprints)));
    }
}
