package signatures.chapter3;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C3_05_SignWithCRLOfflineTest extends SignatureTest {

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C3_05_SignWithCRLOffline.main(null);

        String[] resultFiles =
                new String[]{ "hello_cacert_crl_offline.pdf" };

        String destPath = String.format(outPath, "chapter3");
        String comparePath = String.format(cmpPath, "chapter3");

        String[] errors = new String[resultFiles.length];
        boolean error = false;

        //TODO probably output file contains visible date of signing, which should be ignored
//        HashMap<Integer, List<Rectangle>> ignoredAreas = new HashMap<Integer, List<Rectangle>>() { {
//            put(1, Arrays.asList(new Rectangle(38f, 758f, 110f, 763f)));
//        }};

        for (int i = 0; i < resultFiles.length; i++) {
            String resultFile = resultFiles[i];
            String fileErrors = checkForErrors(destPath + resultFile, comparePath + "cmp_" + resultFile, destPath, /*ignoredAreas*/ null);
            if (fileErrors != null) {
                errors[i] = fileErrors;
                error = true;
            }
        }

        if (error) {
            fail(accumulateErrors(errors));
        }
    }
}
