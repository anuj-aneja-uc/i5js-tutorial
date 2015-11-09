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

public class C3_08_GetTsaUrlTest extends SignatureTest {

    public static final String expectedOutput = ""; //TODO

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {

        setupSystemOutput();
        C3_08_GetTsaUrl.main(null);
        String sysOut = getSystemOutput();

        if (!sysOut.equals(expectedOutput)) {
            fail("Unexpected output.");
        }
    }
}
