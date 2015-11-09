package signatures.chapter4;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.smartcardio.CardException;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C4_04_InspectBEIDTest extends SignatureTest {

    public static final  String expectedOutput = ""; //TODO

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException, CardException {

        setupSystemOutput();
        C4_04_InspectBEID.main(null);
        String sysOut = getSystemOutput();

        if (!sysOut.equals(expectedOutput)) {
            fail("Unexpected output.");
        }
    }
}
