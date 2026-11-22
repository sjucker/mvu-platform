package ch.mvurdorf.platform.passkey;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PasskeyConfigurationTest {

    @Test
    void originsContainWebAndAndroidOrigins() {
        var fingerprint = "FA:C6:17:45:DC:09:03:78:6F:B9:ED:E6:2A:96:2B:39:9F:73:48:F0:BB:6F:89:9B:83:32:66:75:91:03:3B:9C";

        assertThat(PasskeyConfiguration.origins("platform.mvurdorf.ch", List.of(fingerprint, " ")))
                .containsExactly("https://platform.mvurdorf.ch",
                                 "android:apk-key-hash:-sYXRdwJA3hvue3mKpYrOZ9zSPC7b4mbgzJmdZEDO5w");
    }
}
