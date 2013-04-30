package basics.chapter01;

import com.itextpdf.text.pdf.PdfName;

public class C0104_NameObject {

	public static void main(String[] args) {
		showObject(PdfName.CONTENTS);
		showObject(new PdfName("CustomName"));
		showObject(new PdfName("Test #1 100%"));
	}
	
	public static void showObject(PdfName obj) {
		System.out.println(obj.getClass().getName() + ":");
		System.out.println("-> name? " + obj.isName());
		System.out.println("-> type: " + obj.type());
		System.out.println("-> bytes: " + new String(obj.getBytes()));
		System.out.println("-> toString: " + obj.toString());
	}
}
