package signatures.chapter2;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C2_10_SequentialSignaturesTest extends SignatureTest{
    public static final String ALICE = "src/main/resources/alice";
    public static final String BOB = "src/main/resources/bob";
    public static final String CAROL = "src/main/resources/carol";
    public static final char[] PASSWORD = "password".toCharArray();

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C2_10_SequentialSignatures.main(null);

        String[] resultFiles =
                new String[]{ "signed_by_alice.pdf", "signed_by_bob.pdf", "signed_by_carol.pdf",
                        "signed_by_alice2.pdf", "signed_by_bob2.pdf", "signed_by_carol2.pdf",
                        "signed_by_alice3.pdf", "signed_by_bob3.pdf", "signed_by_carol3.pdf"};

        String destPath = String.format(outPath, "chapter2");
        String comparePath = String.format(cmpPath, "chapter2");

        String[] errors = new String[resultFiles.length];
        boolean error = false;

        HashMap<Integer, List<Rectangle>> ignoredAreas = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(38f, 743f, 215f, 759f), new Rectangle(38f, 676f, 215f, 692f), new Rectangle(38f, 611f, 215f, 627f)));
        }};

        for (int i = 0; i < resultFiles.length; i++) {
            String resultFile = resultFiles[i];
            String fileErrors = checkForErrors(destPath + resultFile, comparePath + "cmp_" + resultFile, destPath, ignoredAreas);
            if (fileErrors != null) {
                errors[i] = fileErrors;
                error = true;
            }
        }

        if (error) {
            fail(accumulateErrors(errors));
        }
    }

    @Override
    protected void initKeyStoreForVerification(KeyStore ks) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        super.initKeyStoreForVerification(ks);
        Certificate aliceCertificate = getCertificateFromKeyStore(ALICE, PASSWORD, "demo");
        Certificate bobCertificate = getCertificateFromKeyStore(BOB, PASSWORD, "demo");
        Certificate carolCertificate = getCertificateFromKeyStore(CAROL, PASSWORD, "demo");

        ks.setCertificateEntry("alice", aliceCertificate);
        ks.setCertificateEntry("bob", bobCertificate);
        ks.setCertificateEntry("carol", carolCertificate);
    }

    protected Certificate getCertificateFromKeyStore(String keyStorePath, char[] password, String alias) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(keyStorePath), password);
        return ks.getCertificate(alias);
    }
}
