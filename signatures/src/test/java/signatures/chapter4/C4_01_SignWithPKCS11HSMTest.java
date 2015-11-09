package signatures.chapter4;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.Test;

import signatures.SignatureTest;

public class C4_01_SignWithPKCS11HSMTest extends SignatureTest {


    /**
     * In this test we only run the sample. If no exception were thrown - test succeeds.
     * For some reason, the output of this sample is in the "/home/itext/" folder. This is like a private folder,
     * so may be we shouldn't put cmp_file to the public folder either.
     */
    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {
        C4_01_SignWithPKCS11HSM.main(null);
    }
}
