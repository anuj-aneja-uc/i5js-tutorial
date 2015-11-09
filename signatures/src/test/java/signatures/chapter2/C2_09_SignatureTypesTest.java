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

public class C2_09_SignatureTypesTest extends SignatureTest {
    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C2_09_SignatureTypes.main(null);

        String[] resultFiles =
                new String[]{ "hello_level_1.pdf", "hello_level_2.pdf", "hello_level_3.pdf", "hello_level_4.pdf",
                        "hello_level_1_annotated_wrong.pdf", "hello_level_2_annotated.pdf", "hello_level_3_annotated.pdf", "hello_level_4_annotated.pdf",
                        "hello_level_1_text.pdf", // this document's signature is not broken, that's why verifier doesn't show any errors;
                                                  // this document is invalid from certificate point of view, which is not checked by itext
                        "hello_level_1_double.pdf", "hello_level_2_double.pdf", "hello_level_3_double.pdf", "hello_level_4_double.pdf" };

        String destPath = String.format(outPath, "chapter2");
        String comparePath = String.format(cmpPath, "chapter2");

        String[] errors = new String[resultFiles.length];
        boolean error = false;
        int indexOfInvalidFile = 4;

        HashMap<Integer, List<Rectangle>> ignoredAreas = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(38f, 758f, 110f, 763f), new Rectangle(38f, 710f, 110f, 715f)));
        }};

        for (int i = 0; i < resultFiles.length; i++) {
            String resultFile = resultFiles[i];
            String fileErrors = checkForErrors(destPath + resultFile, comparePath + "cmp_" + resultFile, destPath, ignoredAreas);

            if (i == indexOfInvalidFile) {
                if (fileErrors == null) {
                    errors[i] = "Document signature was expected to be invalid.";
                    error = true;
                }
            } else if (fileErrors != null) {
                errors[i] = fileErrors;
                error = true;
            }
        }

        if (error) {
            fail(accumulateErrors(errors));
        }
    }
}
