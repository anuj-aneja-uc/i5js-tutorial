package signatures.chapter4;

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

public class C4_09_DeferredSigningTest extends SignatureTest {

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C4_09_DeferredSigning.main(null);

        String[] resultFiles =
                new String[]{ "hello_sig_ok.pdf" };

        String destPath = String.format(outPath, "chapter4");
        String comparePath = String.format(cmpPath, "chapter4");

        String[] errors = new String[resultFiles.length];
        boolean error = false;

        HashMap<Integer, List<Rectangle>> ignoredAreas = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(38f, 750f, 140f, 760f)));
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
}
