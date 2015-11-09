package signatures.chapter5;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.smartcardio.CardException;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C5_06_ValidateLTVTest extends SignatureTest {

    public static final  String expectedOutput = ""; //TODO

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException, CardException {

        setupSystemOutput();
        C5_06_ValidateLTV.main(null);
        String sysOut = getSystemOutput();

        if (!sysOut.equals(expectedOutput)) {
            fail("Unexpected output.");
        }
    }
}
