package ch.mvurdorf.platform.passkey;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal WebAuthn authenticator (EC P-256, "none" attestation) holding a single passkey, to test the relying party
 * without a device.
 */
class SoftwareAuthenticator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64URL_DECODER = Base64.getUrlDecoder();

    private final String origin;
    private final byte[] credentialId = "software-authenticator-credential".getBytes(StandardCharsets.UTF_8);
    private KeyPair keyPair;
    private String rpId;
    private byte[] userHandle;

    SoftwareAuthenticator(String origin) {
        this.origin = origin;
    }

    /**
     * @param optionsJson as returned by the relying party (<code>{"publicKey": {...}}</code>)
     * @return the JSON serialized {@code PublicKeyCredential}
     */
    String create(String optionsJson) throws Exception {
        var options = MAPPER.readTree(optionsJson).get("publicKey");
        rpId = options.get("rp").get("id").asText();
        userHandle = BASE64URL_DECODER.decode(options.get("user").get("id").asText());

        var generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        keyPair = generator.generateKeyPair();

        var clientDataJson = clientDataJson("webauthn.create", options);
        var authenticatorData = new ByteArrayOutputStream();
        authenticatorData.writeBytes(authenticatorDataHeader(0x45)); // user present, user verified, attested credential data
        authenticatorData.writeBytes(new byte[16]); // AAGUID
        authenticatorData.writeBytes(ByteBuffer.allocate(2).putShort((short) credentialId.length).array());
        authenticatorData.writeBytes(credentialId);
        authenticatorData.writeBytes(coseKey());

        var attestationObject = new LinkedHashMap<Object, Object>();
        attestationObject.put("fmt", "none");
        attestationObject.put("attStmt", Map.of());
        attestationObject.put("authData", authenticatorData.toByteArray());

        return MAPPER.writeValueAsString(Map.of("id", BASE64URL.encodeToString(credentialId),
                                                "rawId", BASE64URL.encodeToString(credentialId),
                                                "type", "public-key",
                                                "response", Map.of("clientDataJSON", BASE64URL.encodeToString(clientDataJson),
                                                                   "attestationObject", BASE64URL.encodeToString(Cbor.encode(attestationObject)),
                                                                   "transports", new String[]{"internal"}),
                                                "clientExtensionResults", Map.of()));
    }

    /**
     * @param optionsJson as returned by the relying party (<code>{"publicKey": {...}}</code>)
     * @return the JSON serialized {@code PublicKeyCredential}
     */
    String get(String optionsJson) throws Exception {
        var options = MAPPER.readTree(optionsJson).get("publicKey");
        var clientDataJson = clientDataJson("webauthn.get", options);
        var authenticatorData = authenticatorDataHeader(0x05); // user present, user verified

        var signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(authenticatorData);
        signature.update(sha256(clientDataJson));

        return MAPPER.writeValueAsString(Map.of("id", BASE64URL.encodeToString(credentialId),
                                                "rawId", BASE64URL.encodeToString(credentialId),
                                                "type", "public-key",
                                                "response", Map.of("clientDataJSON", BASE64URL.encodeToString(clientDataJson),
                                                                   "authenticatorData", BASE64URL.encodeToString(authenticatorData),
                                                                   "signature", BASE64URL.encodeToString(signature.sign()),
                                                                   "userHandle", BASE64URL.encodeToString(userHandle)),
                                                "clientExtensionResults", Map.of()));
    }

    private byte[] clientDataJson(String type, JsonNode options) throws Exception {
        return MAPPER.writeValueAsBytes(Map.of("type", type,
                                               "challenge", options.get("challenge").asText(),
                                               "origin", origin));
    }

    private byte[] authenticatorDataHeader(int flags) throws GeneralSecurityException {
        return ByteBuffer.allocate(37)
                         .put(sha256(rpId.getBytes(StandardCharsets.UTF_8)))
                         .put((byte) flags)
                         .putInt(0) // signature counter (not supported, like most passkey providers)
                         .array();
    }

    private byte[] coseKey() {
        var point = ((ECPublicKey) keyPair.getPublic()).getW();
        var key = new LinkedHashMap<Object, Object>();
        key.put(1, 2); // kty: EC2
        key.put(3, -7); // alg: ES256
        key.put(-1, 1); // crv: P-256
        key.put(-2, unsigned32(point.getAffineX()));
        key.put(-3, unsigned32(point.getAffineY()));
        return Cbor.encode(key);
    }

    private static byte[] unsigned32(BigInteger value) {
        var bytes = value.toByteArray();
        var result = new byte[32];
        var length = Math.min(bytes.length, 32);
        System.arraycopy(bytes, bytes.length - length, result, 32 - length, length);
        return result;
    }

    private static byte[] sha256(byte[] data) throws GeneralSecurityException {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    /**
     * Encodes the few CBOR types needed for attestation objects and COSE keys.
     */
    private static final class Cbor {

        static byte[] encode(Object value) {
            var out = new ByteArrayOutputStream();
            write(out, value);
            return out.toByteArray();
        }

        private static void write(ByteArrayOutputStream out, Object value) {
            switch (value) {
                case Integer i when i >= 0 -> header(out, 0, i);
                case Integer i -> header(out, 1, -1 - i);
                case byte[] bytes -> {
                    header(out, 2, bytes.length);
                    out.writeBytes(bytes);
                }
                case String text -> {
                    var bytes = text.getBytes(StandardCharsets.UTF_8);
                    header(out, 3, bytes.length);
                    out.writeBytes(bytes);
                }
                case Map<?, ?> map -> {
                    header(out, 5, map.size());
                    map.forEach((k, v) -> {
                        write(out, k);
                        write(out, v);
                    });
                }
                default -> throw new IllegalArgumentException("unsupported CBOR value " + value);
            }
        }

        private static void header(ByteArrayOutputStream out, int majorType, int length) {
            if (length < 24) {
                out.write(majorType << 5 | length);
            } else if (length < 256) {
                out.write(majorType << 5 | 24);
                out.write(length);
            } else {
                out.write(majorType << 5 | 25);
                out.write(length >> 8);
                out.write(length & 0xff);
            }
        }
    }
}
