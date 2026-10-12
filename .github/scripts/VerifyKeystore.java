import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Key;
import java.security.MessageDigest;
import java.security.cert.Certificate;

public class VerifyKeystore {
    private static final String EXPECTED_SHA1 = "10:C5:EC:FE:27:83:37:8B:DE:55:E0:6A:29:E2:48:7B:EC:AA:F3:AD";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("::error::Keystore path argument missing.");
            System.exit(1);
        }

        String ksPath = args[0];
        File ksFile = new File(ksPath);
        if (!ksFile.exists() || ksFile.length() == 0) {
            System.err.println("::error::Keystore file does not exist or is empty: " + ksPath);
            System.exit(1);
        }

        String storePass = System.getenv("RELEASE_STORE_PASSWORD");
        if (storePass == null) storePass = System.getenv("KEYSTORE_PASSWORD");

        String alias = System.getenv("RELEASE_KEY_ALIAS");
        if (alias == null) alias = System.getenv("KEY_ALIAS");
        if (alias == null || alias.trim().isEmpty()) alias = "rivava";

        String keyPass = System.getenv("RELEASE_KEY_PASSWORD");
        if (keyPass == null) keyPass = System.getenv("KEY_PASSWORD");

        if (storePass == null || storePass.isEmpty() || keyPass == null || keyPass.isEmpty()) {
            System.err.println("::error::RELEASE_STORE_PASSWORD or RELEASE_KEY_PASSWORD environment variable is missing.");
            System.exit(1);
        }

        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(ksFile)) {
                ks.load(fis, storePass.toCharArray());
            }
        } catch (Exception e1) {
            try {
                ks = KeyStore.getInstance("PKCS12");
                try (FileInputStream fis = new FileInputStream(ksFile)) {
                    ks.load(fis, storePass.toCharArray());
                }
            } catch (Exception e2) {
                System.err.println("::error::Failed to load keystore with provided RELEASE_STORE_PASSWORD: " + e2.getMessage());
                System.exit(1);
            }
        }

        try {
            if (!ks.containsAlias(alias)) {
                System.err.println("::error::Alias '" + alias + "' not found in keystore.");
                System.exit(1);
            }

            Certificate cert = ks.getCertificate(alias);
            if (cert == null) {
                System.err.println("::error::Certificate for alias '" + alias + "' is null.");
                System.exit(1);
            }

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(cert.getEncoded());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                if (i > 0) sb.append(":");
                sb.append(String.format("%02X", digest[i]));
            }
            String actualSha1 = sb.toString();

            if (!EXPECTED_SHA1.equalsIgnoreCase(actualSha1)) {
                System.err.println("::error::Wrong upload certificate fingerprint! Expected SHA-1: " + EXPECTED_SHA1 + "; found: " + actualSha1);
                System.exit(1);
            }

            Key key = ks.getKey(alias, keyPass.toCharArray());
            if (key == null) {
                System.err.println("::error::Private key for alias '" + alias + "' could not be recovered using RELEASE_KEY_PASSWORD.");
                System.exit(1);
            }

            System.out.println("Confirmed: Keystore is readable, private key for alias '" + alias + "' is accessible, and upload key SHA-1 matches " + EXPECTED_SHA1);

        } catch (Exception e) {
            System.err.println("::error::Keystore verification failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
