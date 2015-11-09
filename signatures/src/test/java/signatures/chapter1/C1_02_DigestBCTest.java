package signatures.chapter1;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.Test;

import signatures.SignatureTest;

import static org.junit.Assert.fail;

public class C1_02_DigestBCTest extends SignatureTest {

    public static final  String expectedOutput = "Digest using MD5: 16\n" +
            "Digest: 5f4dcc3b5aa765d61d8327deb882cf99\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n" +
            "Digest using SHA-1: 20\n" +
            "Digest: 5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n" +
            "Digest using SHA-224: 28\n" +
            "Digest: d63dc919e201d7bc4c825630d2cf25fdc93d4b2f0d46706d29038d01\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n" +
            "Digest using SHA-256: 32\n" +
            "Digest: 5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n" +
            "Digest using SHA-384: 48\n" +
            "Digest: a8b64babd0aca91a59bdbb7761b421d4f2bb38280d3a75ba0f21f2bebc45583d446c598660c94ce680c47d19c30783a7\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n" +
            "Digest using SHA-512: 64\n" +
            "Digest: b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n" +
            "Digest using RIPEMD128: 16\n" +
            "Digest: c9c6d316d6dc4d952a789fd4b8858ed7\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n" +
            "Digest using RIPEMD160: 20\n" +
            "Digest: 2c08e8f5884750a7b99f6f2f342fc638db25ff31\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n" +
            "Digest using RIPEMD256: 32\n" +
            "Digest: f94cf96c79103c3ccad10d308c02a1db73b986e2c48962e96ecd305e0b80ef1b\n" +
            "Is the password 'password'? true\n" +
            "Is the password 'secret'? false\n";

    @Test
    public void runTest() throws DocumentException, GeneralSecurityException, IOException, InterruptedException {

        setupSystemOutput();
        C1_02_DigestBC.main(null);
        String sysOut = getSystemOutput();

        String[] outputLines = sysOut.split("\n");
        String[] expectedLines = expectedOutput.split("\n");

        for (int i = 0; i < outputLines.length; ++i) {
            String line = outputLines[i];
            if (!line.startsWith("Digest: ") && !line.trim().equals(expectedLines[i].trim())) {
                String error = "Unexpected output at line %d.\nExpected: %s\ngot: %s";
                fail(String.format(error, i + 1, expectedLines[i], outputLines[i]));
            }
        }
    }
}
