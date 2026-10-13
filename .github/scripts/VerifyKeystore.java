import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Key;
import java.security.MessageDigest;
import java.security.cert.Certificate;

public class VerifyKeystore {
    private static final String EXPECTED_SHA1 = "B2:A7:7A:4B:B3:5F:93:7A:00:11:24:ED:3B:03:76:2F:17:F5:30:95";

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

        String rawStorePass = System.getenv("RELEASE_STORE_PASSWORD");
        if (rawStorePass == null) rawStorePass = System.getenv("KEYSTORE_PASSWORD");

        String rawAlias = System.getenv("RELEASE_KEY_ALIAS");
        if (rawAlias == null) rawAlias = System.getenv("KEY_ALIAS");
        if (rawAlias == null || rawAlias.trim().isEmpty()) rawAlias = "rivava-upload";

        String rawKeyPass = System.getenv("RELEASE_KEY_PASSWORD");
        if (rawKeyPass == null) rawKeyPass = System.getenv("KEY_PASSWORD");

        if (rawStorePass == null || rawStorePass.isEmpty() || rawKeyPass == null || rawKeyPass.isEmpty()) {
            System.err.println("::error::RELEASE_STORE_PASSWORD or RELEASE_KEY_PASSWORD environment variable is missing.");
            System.exit(1);
        }

        String[] storePassCandidates = rawStorePass.equals(rawStorePass.trim())
                ? new String[]{rawStorePass}
                : new String[]{rawStorePass, rawStorePass.trim()};

        KeyStore ks = null;
        String loadedType = null;
        String workingStorePass = null;

        // Try PKCS12 first, then JKS
        String[] types = new String[]{"PKCS12", "JKS"};

        for (String type : types) {
            for (String pass : storePassCandidates) {
                try {
                    KeyStore candidate = KeyStore.getInstance(type);
                    try (FileInputStream fis = new FileInputStream(ksFile)) {
                        candidate.load(fis, pass.toCharArray());
                    }
                    ks = candidate;
                    loadedType = type;
                    workingStorePass = pass;
                    break;
                } catch (Exception ignored) {
                }
            }
            if (ks != null) break;
        }

        if (ks == null) {
            System.err.println("::error::Failed to load keystore with provided RELEASE_STORE_PASSWORD: keystore password was incorrect.");
            System.err.println("::error::Please update RELEASE_STORE_PASSWORD in GitHub Secrets with the correct store password for rivava-upload-new.jks.");
            System.exit(1);
        }

        if (!workingStorePass.equals(rawStorePass)) {
            System.out.println("Note: RELEASE_STORE_PASSWORD succeeded after stripping leading/trailing whitespace or newline.");
        }

        String alias = rawAlias.trim();
        try {
            if (!ks.containsAlias(alias) && ks.containsAlias(rawAlias)) {
                alias = rawAlias;
            }

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

            String[] keyPassCandidates = rawKeyPass.equals(rawKeyPass.trim())
                    ? new String[]{rawKeyPass}
                    : new String[]{rawKeyPass, rawKeyPass.trim()};

            Key key = null;
            for (String kPass : keyPassCandidates) {
                try {
                    key = ks.getKey(alias, kPass.toCharArray());
                    if (key != null) break;
                } catch (Exception ignored) {
                }
            }

            if (key == null) {
                System.err.println("::error::Private key for alias '" + alias + "' could not be recovered using RELEASE_KEY_PASSWORD. Password is incorrect.");
                System.err.println("::error::Please update RELEASE_KEY_PASSWORD in GitHub Secrets.");
                System.exit(1);
            }

            System.out.println("Confirmed: Keystore (" + loadedType + ") is readable, private key for alias '" + alias + "' is accessible, and upload key SHA-1 matches " + EXPECTED_SHA1);

        } catch (Exception e) {
            System.err.println("::error::Keystore verification failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
