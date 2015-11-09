package signatures.chapter3;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C3_02_GetCrlUrlTest extends SignatureTest {

    public static final  String expectedOutput = ""; //TODO

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {

        setupSystemOutput();
        C3_02_GetCrlUrl.main(null);
        String sysOut = getSystemOutput();

        if (!sysOut.equals(expectedOutput)) {
            fail("Unexpected output.");
        }
    }
}
