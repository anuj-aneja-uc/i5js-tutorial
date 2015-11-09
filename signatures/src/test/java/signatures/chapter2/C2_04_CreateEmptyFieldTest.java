package signatures.chapter2;

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

public class C2_04_CreateEmptyFieldTest extends SignatureTest{

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C2_04_CreateEmptyField.main(null);

        String[] resultFiles =
                new String[]{ "hello_empty.pdf", "hello_empty2.pdf", "field_signed.pdf" };

        String destPath = String.format(outPath, "chapter2");
        String comparePath = String.format(cmpPath, "chapter2");

        String[] errors = new String[resultFiles.length];
        boolean error = false;

        HashMap<Integer, List<Rectangle>> ignoredAreas = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(70f, 748f, 145f, 753f)));
        }};

        for (int i = 0; i < resultFiles.length; i++) {
            String resultFile = resultFiles[i];
            String fileErrors;
            if (i < 2) {
                fileErrors = checkForErrors(destPath + resultFile, comparePath + "cmp_" + resultFile, destPath, null);
            } else {
                fileErrors = checkForErrors(destPath + resultFile, comparePath + "cmp_" + resultFile, destPath, ignoredAreas);
            }
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
