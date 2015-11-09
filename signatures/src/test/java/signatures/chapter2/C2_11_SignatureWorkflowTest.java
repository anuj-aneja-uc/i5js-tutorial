package signatures.chapter2;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
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

import static org.junit.Assert.fail;

public class C2_11_SignatureWorkflowTest extends C2_10_SequentialSignaturesTest{
    public static final String DAVE = "src/main/resources/dave";

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C2_11_SignatureWorkflow.main(null);

        String[] resultFiles =
                new String[]{ "step1_signed_by_alice.pdf", "step2_signed_by_alice_and_filled_out_by_bob.pdf", "step3_signed_by_alice_and_bob.pdf",
                        "step4_signed_by_alice_and_bob_filled_out_by_carol.pdf", "step5_signed_by_alice_bob_and_carol.pdf", "step6_signed_by_alice_bob_carol_and_dave.pdf" };

        String destPath = String.format(outPath, "chapter2");
        String comparePath = String.format(cmpPath, "chapter2");

        String[] errors = new String[resultFiles.length];
        boolean error = false;

        HashMap<Integer, List<Rectangle>> ignoredAreas = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(38f, 743f, 215f, 759f), new Rectangle(38f, 657f, 215f, 673f),
                    new Rectangle(38f, 573f, 215f, 589f), new Rectangle(38f, 484f, 215f, 500f)));
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
        Certificate daveCertificate = getCertificateFromKeyStore(DAVE, PASSWORD, "demo");
        ks.setCertificateEntry("dave", daveCertificate);
    }
}
