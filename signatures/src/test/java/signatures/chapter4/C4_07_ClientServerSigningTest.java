package signatures.chapter4;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C4_07_ClientServerSigningTest extends SignatureTest {

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C4_07_ClientServerSigning.main(null);

        String[] resultFiles =
                new String[]{ "hello_server.pdf" };

        String destPath = String.format(outPath, "chapter4");
        String comparePath = String.format(cmpPath, "chapter4");

        String[] errors = new String[resultFiles.length];
        boolean error = false;

        HashMap<Integer, List<Rectangle>> ignoredAreas = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(38f, 758f, 110f, 763f)));
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
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        URL certUrl = new URL(C4_07_ClientServerSigning.CERT);
        Certificate itextCert = cf.generateCertificate(certUrl.openStream());

        ks.setCertificateEntry("itextpdf", itextCert);
    }
}
