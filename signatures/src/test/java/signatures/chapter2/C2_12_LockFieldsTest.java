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

public class C2_12_LockFieldsTest extends C2_11_SignatureWorkflowTest{

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C2_12_LockFields.main(null);

        String[] resultFiles =
                new String[]{ "step_1_signed_by_alice.pdf", "step_2_signed_by_alice_and_bob.pdf", "step_3_signed_by_alice_bob_and_carol.pdf",
                        "step_4_signed_by_alice_bob_carol_and_dave.pdf", "step_5_signed_by_alice_and_bob_broken_by_chuck.pdf",
                        "step_6_signed_by_dave_broken_by_chuck.pdf" };
        String outPath = String.format(SignatureTest.outPath, "chapter2");
        String cmpPath = String.format(SignatureTest.cmpPath, "chapter2");

        String[] errors = new String[resultFiles.length];
        boolean error = false;

        HashMap<Integer, List<Rectangle>> ignoredAreas = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(38f, 743f, 215f, 759f), new Rectangle(38f, 657f, 215f, 673f),
                    new Rectangle(38f, 573f, 215f, 589f), new Rectangle(38f, 484f, 215f, 500f)));
        }};

        for (int i = 0; i < resultFiles.length; i++) {
            String resultFile = resultFiles[i];
            String fileErrors = checkForErrors(outPath + resultFile, cmpPath + "cmp_" + resultFile, outPath, ignoredAreas);
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
