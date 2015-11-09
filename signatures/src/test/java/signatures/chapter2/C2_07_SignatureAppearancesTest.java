package signatures.chapter2;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C2_07_SignatureAppearancesTest extends SignatureTest {

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C2_07_SignatureAppearances.main(null);

        String[] resultFiles =
                new String[]{ "signature_appearance_1.pdf", "signature_appearance_2.pdf", "signature_appearance_3.pdf", "signature_appearance_4.pdf" };

        String destPath = String.format(outPath, "chapter2");
        String comparePath = String.format(cmpPath, "chapter2");

        String[] errors = new String[resultFiles.length];
        boolean error = false;
        ArrayList<HashMap<Integer, List<Rectangle>>> ignoredAreasForDocuments = initIgnoredAreas();
        for (int i = 0; i < resultFiles.length; i++) {
            String resultFile = resultFiles[i];
            String fileErrors = checkForErrors(destPath + resultFile, comparePath + "cmp_" + resultFile, destPath, ignoredAreasForDocuments.get(i));
            if (fileErrors != null) {
                errors[i] = fileErrors;
                error = true;
            }
        }

        if (error) {
            fail(accumulateErrors(errors));
        }
    }

    private ArrayList<HashMap<Integer, List<Rectangle>>> initIgnoredAreas() {
        ArrayList<HashMap<Integer, List<Rectangle>>> ignoredAreasForDocuments = new ArrayList<HashMap<Integer, List<Rectangle>>>();

        HashMap<Integer, List<Rectangle>> ignoredAreas1 = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(40f, 723f, 220f, 757f)));
        }};
        ignoredAreasForDocuments.add(ignoredAreas1);

        HashMap<Integer, List<Rectangle>> ignoredAreas2 = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(140f, 735f, 250f, 777f)));
        }};
        ignoredAreasForDocuments.add(ignoredAreas2);

        HashMap<Integer, List<Rectangle>> ignoredAreas3 = new HashMap<Integer, List<Rectangle>>() { {
            put(1, Arrays.asList(new Rectangle(140f, 735f, 250f, 777f)));
        }};
        ignoredAreasForDocuments.add(ignoredAreas3);

        ignoredAreasForDocuments.add(null);

        return ignoredAreasForDocuments;
    }
}
