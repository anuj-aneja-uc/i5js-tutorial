package signatures.chapter4;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.smartcardio.CardException;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C4_06_SignWithBEIDTest extends SignatureTest {

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException, CardException {
        C4_06_SignWithBEID.main(null);

        String[] resultFiles =
                new String[]{ "hello_beid.pdf" };

        String destPath = String.format(outPath, "chapter4");
        String comparePath = String.format(cmpPath, "chapter4");

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
