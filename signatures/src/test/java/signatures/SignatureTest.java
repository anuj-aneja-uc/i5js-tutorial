package signatures;

import com.itextpdf.testutils.CompareTool;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.CRLVerifier;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.OCSPVerifier;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.SignaturePermissions;
import com.itextpdf.text.pdf.security.VerificationException;
import com.itextpdf.text.pdf.security.VerificationOK;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.junit.Before;

public class SignatureTest {

    public static final String ADOBE = "src/main/resources/adobeRootCA.cer";
    public static final String CACERT = "src/main/resources/CACertSigningAuthority.crt";
    public static final String BRUNO = "src/main/resources/bruno.crt";

    public final static String cmpPath = "cmp/%s/";
    public final static String outPath = "results/%s/";

    private String errorMessage;

    private PrintStream oldSysOut;
    private ByteArrayOutputStream output;

    @Before
    public void setup() {
        new File("results/chapter1/").mkdirs();
        new File("results/chapter2/").mkdirs();
        new File("results/chapter3/").mkdirs();
        new File("results/chapter4/").mkdirs();
    }


    protected void setupSystemOutput() {
        output = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(output);
        oldSysOut = System.out;
        System.setOut(ps);
    }

    protected String getSystemOutput() {
        System.out.flush();
        System.setOut(oldSysOut);
        return output.toString();
    }

    protected String checkForErrors(String outFile, String cmpFile, String destPath, Map<Integer, List<Rectangle>> ignoredAreas)
            throws InterruptedException, DocumentException, IOException, GeneralSecurityException {
        errorMessage = null;

        //compares documents visually
        CompareTool ct = new CompareTool();
        String comparisonResult = ct.compare(outFile, cmpFile, destPath, "diff", ignoredAreas);
        addError(comparisonResult);

        //verifies document signatures
        verifySignaturesForDocument(outFile);

        //compares document signatures with signatures in cmp_file
        compareSignatures(outFile, cmpFile);

        if (errorMessage != null) {
            errorMessage = "\n" + outFile + ":\n" + errorMessage + "\n";
        }

        return errorMessage;
    }

    /**
     * In this method we add trusted certificates.
     * If document signatures certificates doesn't contain certificates that are added in this method, verification will fail.
     * NOTE: Override this method to add additional certificates.
     */
    protected void initKeyStoreForVerification(KeyStore ks) throws IOException, NoSuchAlgorithmException, CertificateException, KeyStoreException {
        ks.load(null, null);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate adobeCert = cf.generateCertificate(new FileInputStream(ADOBE));
        Certificate cacertCert = cf.generateCertificate(new FileInputStream(CACERT));
        Certificate brunoCert = cf.generateCertificate(new FileInputStream(BRUNO));
        ks.setCertificateEntry("adobe", adobeCert);
        ks.setCertificateEntry("cacert", cacertCert);
        ks.setCertificateEntry("bruno", brunoCert);
    }

    private void verifySignaturesForDocument(String documentPath)
            throws IOException, GeneralSecurityException {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        PdfReader reader = new PdfReader(documentPath);
        AcroFields fields = reader.getAcroFields();
        ArrayList<String> names = fields.getSignatureNames();

        verifySignatures(fields, names);
        reader.close();
    }

    public void verifySignatures(AcroFields fields, ArrayList<String> names) throws IOException, GeneralSecurityException {
        for (String name : names) {
            PdfPKCS7 pkcs7 = fields.verifySignature(name);

            // verify signature integrity
            if (!pkcs7.verify()) {
                addError(String.format("\"%s\" signature integrity is invalid\n", name));
            }

            verifyCertificates(pkcs7);
        }
    }

    private void verifyCertificates(PdfPKCS7 pkcs7) throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        initKeyStoreForVerification(ks);

        Certificate[] certs = pkcs7.getSignCertificateChain();
        Calendar cal = pkcs7.getSignDate();
        List<VerificationException> errors = CertificateVerification.verifyCertificates(certs, ks, cal);
        if (!errors.isEmpty()) {
            for (VerificationException e : errors) {
                addError(e.getMessage() + "\n");
            }
        }
        for (int i = 0; i < certs.length; i++) {
            X509Certificate cert = (X509Certificate) certs[i];
            checkCertificateInfo(cert, cal.getTime(), pkcs7);
        }

        X509Certificate signCert = (X509Certificate)certs[0];
        X509Certificate issuerCert = (certs.length > 1 ? (X509Certificate)certs[1] : null);
        //Checking validity of the document at the time of signing
        checkRevocation(pkcs7, signCert, issuerCert, cal.getTime());
    }

    private void checkCertificateInfo(X509Certificate cert, Date signDate, PdfPKCS7 pkcs7) throws GeneralSecurityException {
        try {
            cert.checkValidity(signDate);
        } catch (CertificateExpiredException e) {
            addError("The certificate was expired at the time of signing.");
        } catch (CertificateNotYetValidException e) {
            addError("The certificate wasn't valid yet at the time of signing.");
        }

        if (pkcs7.getTimeStampDate() != null) {
            if (!pkcs7.verifyTimestampImprint()) {
                addError("Timestamp is invalid.");
            }
        }
    }

    private void checkRevocation(PdfPKCS7 pkcs7, X509Certificate signCert, X509Certificate issuerCert, Date date) throws GeneralSecurityException, IOException {
        List<BasicOCSPResp> ocsps = new ArrayList<BasicOCSPResp>();
        if (pkcs7.getOcsp() != null)
            ocsps.add(pkcs7.getOcsp());
        OCSPVerifier ocspVerifier = new OCSPVerifier(null, ocsps);
        List<VerificationOK> verification =
                ocspVerifier.verify(signCert, issuerCert, date);
        if (verification.size() == 0) {
            List<X509CRL> crls = new ArrayList<X509CRL>();
            if (pkcs7.getCRLs() != null) {
                for (CRL crl : pkcs7.getCRLs())
                    crls.add((X509CRL)crl);
            }
            CRLVerifier crlVerifier = new CRLVerifier(null, crls);
            verification.addAll(crlVerifier.verify(signCert, issuerCert, date));
        }

        //if exception was not thrown document is not revoked or it couldn't be verified
    }

    private void compareSignatures(String outFile, String cmpFile) throws IOException {
        SignedDocumentInfo outInfo = collectInfo(outFile);
        SignedDocumentInfo cmpInfo = collectInfo(cmpFile);

        compareSignedDocumentsInfo(outInfo, cmpInfo);
    }

    private SignedDocumentInfo collectInfo(String documentPath) throws IOException {
        SignedDocumentInfo docInfo = new SignedDocumentInfo();

        PdfReader reader = new PdfReader(documentPath);
        AcroFields fields = reader.getAcroFields();
        ArrayList<String> names = fields.getSignatureNames();

        docInfo.setNumberOfTotalRevisions(fields.getTotalRevisions());
        SignaturePermissions perms = null;

        List<SignatureInfo> signInfos = new ArrayList<SignatureInfo>();
        for (String name : names) {
            SignatureInfo sigInfo = new SignatureInfo();
            sigInfo.setSignatureName(name);
            sigInfo.setRevisionNumber(fields.getRevision(name));
            sigInfo.setSignatureCoversWholeDocument(fields.signatureCoversWholeDocument(name));

            List<AcroFields.FieldPosition> fps = fields.getFieldPositions(name);
            if (fps != null && fps.size() > 0) {
                sigInfo.setSignaturePosition(fps.get(0));
            }

            PdfPKCS7 pkcs7 = fields.verifySignature(name);
            sigInfo.setDigestAlgorithm(pkcs7.getHashAlgorithm());
            sigInfo.setEncryptionAlgorithm(pkcs7.getEncryptionAlgorithm());
            PdfName filterSubtype = pkcs7.getFilterSubtype();
            if (filterSubtype != null) {
                sigInfo.setFilterSubtype(filterSubtype.toString());
            }

            X509Certificate signCert = pkcs7.getSigningCertificate();
            sigInfo.setSignerName(CertificateInfo.getSubjectFields(signCert).getField("CN"));
            sigInfo.setAlternativeSignerName(pkcs7.getSignName());

            sigInfo.setSignDate(pkcs7.getSignDate().getTime());
            if (pkcs7.getTimeStampDate() != null) {
                sigInfo.setTimeStamp(pkcs7.getTimeStampDate().getTime());
                TimeStampToken ts = pkcs7.getTimeStampToken();
                sigInfo.setTimeStampService(ts.getTimeStampInfo().getTsa().toString());
            }

            sigInfo.setLocation(pkcs7.getLocation());
            sigInfo.setReason(pkcs7.getReason());

            PdfDictionary sigDict = fields.getSignatureDictionary(name);
            PdfString contactInfo = sigDict.getAsString(PdfName.CONTACTINFO);
            if (contactInfo != null) {
                sigInfo.setContactInfo(contactInfo.toString());
            }

            perms = new SignaturePermissions(sigDict, perms);
            sigInfo.setIsCertifiaction(perms.isCertification());
            sigInfo.setIsFieldsFillAllowed(perms.isFillInAllowed());
            sigInfo.setIsAddingAnnotationsAllowed(perms.isAnnotationsAllowed());

            List<String> fieldLocks = new ArrayList<String>();
            for (SignaturePermissions.FieldLock lock : perms.getFieldLocks()) {
                fieldLocks.add(lock.toString());
            }
            sigInfo.setFieldsLocks(fieldLocks);

            Certificate[] certs = pkcs7.getSignCertificateChain();
            List<signatures.CertificateInfo> certInfos = new ArrayList<signatures.CertificateInfo>();
            for (int i = 0; i < certs.length; i++) {
                X509Certificate cert = (X509Certificate) certs[i];
                signatures.CertificateInfo certInfo = new signatures.CertificateInfo();
                certInfo.setIssuer(cert.getIssuerDN());
                certInfo.setSubject(cert.getSubjectDN());
                certInfo.setValidFrom(cert.getNotBefore());
                certInfo.setValidTo(cert.getNotAfter());

                certInfos.add(certInfo);
            }
            sigInfo.setCertificateInfos(certInfos);

            signInfos.add(sigInfo);
        }
        docInfo.setSignatureInfos(signInfos);

        return docInfo;
    }

    private void compareSignedDocumentsInfo(SignedDocumentInfo outInfo, SignedDocumentInfo cmpInfo) {
        if (outInfo.getNumberOfTotalRevisions() != cmpInfo.getNumberOfTotalRevisions()) {
            addComparisonError("Number of total revisions",
                    String.valueOf(outInfo.getNumberOfTotalRevisions()),
                    String.valueOf(cmpInfo.getNumberOfTotalRevisions()));
        }

        if (outInfo.getSignatureInfos().size() != cmpInfo.getSignatureInfos().size()) {
            addComparisonError("Number of signatures in document",
                    String.valueOf(outInfo.getSignatureInfos().size()),
                    String.valueOf(cmpInfo.getSignatureInfos().size()));
        }

        for (int i = 0; i < outInfo.getSignatureInfos().size(); ++i) {
            SignatureInfo outSig = outInfo.getSignatureInfos().get(i);
            SignatureInfo cmpSig = cmpInfo.getSignatureInfos().get(i);

            String outAltName = outSig.getAlternativeSignerName();
            String cmpAltName = cmpSig.getAlternativeSignerName();
            if (checkIfEqual(outAltName, cmpAltName)) {
                addComparisonError("Alternative signer name", outAltName, cmpAltName);
            }

            String outContactInfo = outSig.getContactInfo();
            String cmpContactInfo = cmpSig.getContactInfo();
            if (checkIfEqual(outContactInfo, cmpContactInfo)) {
                addComparisonError("Contact info", outContactInfo, cmpContactInfo);
            }

            String outDigestAlg = outSig.getDigestAlgorithm();
            String cmpDigestAlg = cmpSig.getDigestAlgorithm();
            if (checkIfEqual(outDigestAlg, cmpDigestAlg)) {
                addComparisonError("Digest algorithm", outDigestAlg, cmpDigestAlg);
            }

            String outEncryptAlg = outSig.getEncryptionAlgorithm();
            String cmpEncryptAlg = cmpSig.getEncryptionAlgorithm();
            if (checkIfEqual(outEncryptAlg, cmpEncryptAlg)) {
                addComparisonError("Encryption algorithm", outEncryptAlg, cmpEncryptAlg);
            }

            String outLocation = outSig.getLocation();
            String cmpLocation = cmpSig.getLocation();
            if (checkIfEqual(outLocation, cmpLocation)) {
                addComparisonError("Location", outLocation, cmpLocation);
            }

            String outReason = outSig.getReason();
            String cmpReason = cmpSig.getReason();
            if (checkIfEqual(outReason, cmpReason)) {
                addComparisonError("Reason", outReason, cmpReason);
            }

            String outSigName = outSig.getSignatureName();
            String cmpSigName = cmpSig.getSignatureName();
            if (checkIfEqual(outSigName, cmpSigName)) {
                addComparisonError("Signature name", outSigName, cmpSigName);
            }

            String outSignerName = outSig.getSignerName();
            String cmpSignerName = cmpSig.getSignerName();
            if (checkIfEqual(outSignerName, cmpSignerName)) {
                addComparisonError("Signer name", outSignerName, cmpSignerName);
            }

            String outFilterSubtype = outSig.getFilterSubtype();
            String cmpFilterSubtype = cmpSig.getFilterSubtype();
            if (checkIfEqual(outFilterSubtype, cmpFilterSubtype)) {
                addComparisonError("Filter subtype", outFilterSubtype, cmpFilterSubtype);
            }

            int outSigRevisionNumber = outSig.getRevisionNumber();
            int cmpSigRevisionNumber = cmpSig.getRevisionNumber();
            if (outSigRevisionNumber != cmpSigRevisionNumber) {
                addComparisonError("Signature revision number",
                        String.valueOf(outSigRevisionNumber),
                        String.valueOf(cmpSigRevisionNumber));
            }

            String outTimeStampService = outSig.getTimeStampService();
            String cmpTimeStampService = cmpSig.getTimeStampService();
            if (checkIfEqual(outTimeStampService, cmpTimeStampService)) {
                addComparisonError("TimeStamp service", outTimeStampService, cmpTimeStampService);
            }

            boolean outAnnotsAllowed = outSig.isAddingAnnotationsAllowed();
            boolean cmpAnnotsAllowed = cmpSig.isAddingAnnotationsAllowed();
            if (outAnnotsAllowed != cmpAnnotsAllowed) {
                addComparisonError("Annotations allowance",
                        String.valueOf(outAnnotsAllowed),
                        String.valueOf(cmpAnnotsAllowed));
            }

            boolean outFieldsFillAllowed = outSig.isFieldsFillAllowed();
            boolean cmpFieldsFillAllowed = cmpSig.isFieldsFillAllowed();
            if (outFieldsFillAllowed != cmpFieldsFillAllowed) {
                addComparisonError("Fields filling allowance",
                        String.valueOf(outFieldsFillAllowed),
                        String.valueOf(cmpFieldsFillAllowed));
            }

            boolean outIsCertification = outSig.isCertifiaction();
            boolean cmpIsCertification = cmpSig.isCertifiaction();
            if (outIsCertification != cmpIsCertification) {
                addComparisonError("Comparing signature to certification result",
                        String.valueOf(outIsCertification),
                        String.valueOf(cmpIsCertification));
            }

            boolean outIsWholeDocument = outSig.isSignatureCoversWholeDocument();
            boolean cmpIsWholeDocument = cmpSig.isSignatureCoversWholeDocument();
            if (outIsWholeDocument != cmpIsWholeDocument) {
                addComparisonError("Whole document covering",
                        String.valueOf(outIsWholeDocument),
                        String.valueOf(cmpIsWholeDocument));
            }

            AcroFields.FieldPosition outFp = outSig.getSignaturePosition();
            AcroFields.FieldPosition cmpFp = cmpSig.getSignaturePosition();

            int outFpPage = outFp.page;
            int cmpFpPage = cmpFp.page;
            if (outFpPage != cmpFpPage) {
                addComparisonError("Signature page",
                        String.valueOf(outFpPage),
                        String.valueOf(cmpFpPage));
            }

            String outPositionRect = outFp.position == null ? null : outFp.position.toString();
            String cmpPositionRect = cmpFp.position == null ? null : cmpFp.position.toString();
            if (checkIfEqual(outPositionRect, cmpPositionRect)) {
                addComparisonError("Signature position", outPositionRect, cmpPositionRect);
            }

            List<String> outFieldLocks = outSig.getFieldsLocks();
            List<String> cmpFieldLocks = cmpSig.getFieldsLocks();
            int outLocksNumber = outFieldLocks.size();
            int cmpLocksNumber = cmpFieldLocks.size();
            if (outLocksNumber != cmpLocksNumber) {
                addComparisonError("Field locks number",
                        String.valueOf(outLocksNumber),
                        String.valueOf(cmpLocksNumber));
            }
            for (int j = 0; j < outLocksNumber; ++j) {
                if (!outFieldLocks.get(j).equals(cmpFieldLocks.get(j))) {
                    addComparisonError("Field lock", outFieldLocks.get(j), cmpFieldLocks.get(j));
                }
            }

            if (outSig.getCertificateInfos().size() != cmpSig.getCertificateInfos().size()) {
                addComparisonError("Certificates number",
                        String.valueOf(outSig.getCertificateInfos().size()),
                        String.valueOf(cmpSig.getCertificateInfos().size()));
            }

            for (int j = 0; j < outSig.getCertificateInfos().size(); ++j) {
                signatures.CertificateInfo outCert = outSig.getCertificateInfos().get(j);
                signatures.CertificateInfo cmpCert = cmpSig.getCertificateInfos().get(j);

                if (!outCert.getIssuer().equals(cmpCert.getIssuer())) {
                    addComparisonError("Certificate issuer", outCert.getIssuer().toString(), cmpCert.getIssuer().toString());
                }

                if (!outCert.getSubject().equals(cmpCert.getSubject())) {
                    addComparisonError("Certificate subject", outCert.getSubject().toString(), cmpCert.getSubject().toString());
                }

                if (!outCert.getValidFrom().equals(cmpCert.getValidFrom())) {
                    addComparisonError("Date \"valid from\"", outCert.getValidFrom().toString(), cmpCert.getValidFrom().toString());
                }

                if (!outCert.getValidTo().equals(cmpCert.getValidTo())) {
                    addComparisonError("Date \"valid to\"", outCert.getValidTo().toString(), cmpCert.getValidTo().toString());
                }
            }

        }
    }

    protected String accumulateErrors(String[] errors) {
        String result = "";
        for (String error : errors) {
            if (error != null) {
                result += error + "\n";
            }
        }
        return result;
    }

    private boolean checkIfEqual(Object obj1, Object obj2) {
        return !checkNulls(obj1, obj2) || (obj1 != null && !obj1.equals(obj2));
    }

    private boolean checkNulls(Object obj1, Object obj2) {
        return (obj1 == null && obj2 == null) || (obj1 != null && obj2 != null);

    }

    private void addComparisonError(String comparisonCategory, String newVal, String oldVal) {
        String error = "%s [%s] isn't equal to expected value [%s].";
        addError(String.format(error, comparisonCategory, newVal, oldVal));
    }

    private void addError(String error) {
        if (error != null && error.length() > 0) {
            if (errorMessage == null)
                errorMessage = "";
            else
                errorMessage += "\n";

            errorMessage += error;
        }
    }

}
