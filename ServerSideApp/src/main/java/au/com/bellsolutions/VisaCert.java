package au.com.bellsolutions;

public class VisaCert {
	private static VisaCert cert = null;
	private static String exponent09 = "03";
	private static String sha109 = "1FF80A40173F52D7D27E0F26A146A1C8CCB29046";
	private static String rid09 = "A000000003";
	private static String mod09 = "9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41";

	private static String exponent08 = "03";
	private static String sha108 = "20D213126955DE205ADC2FD2822BD22DE21CF9A8";
	private static String rid08 = "A000000003";
	private static String mod08 = "D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0B";
	
	public String exponent;
	public String sha1;
	public String rid;
	public String mod;
	public String index;
    
    
    private VisaCert() {
    	
    }
    
    public static boolean supported(String index) {
    	return (index.equals("09")||index.equals("08"));
    }
    
    public static VisaCert getInstance(String index) {
    	if (cert == null) {
    		cert = new VisaCert();
    	}
    	if (index.equals("09")) {
    		cert.index = index;
    		cert.setExponent(exponent09);
    		cert.setMod(mod09);
    		cert.setRid(rid09);
    		cert.setSha1(sha109);
    	} else if (index.equals("08")) { 
    		cert.index = index;
    		cert.setExponent(exponent08);
    		cert.setMod(mod08);
    		cert.setRid(rid08);
    		cert.setSha1(sha108);
    	}
    	return cert;
    }
    
    private void setExponent(String exp) {
    	this.exponent = exp;
    }
    private void setSha1(String sha1) {
    	this.sha1 = sha1;
    }
    private void setRid(String rid) {
    	this.rid = rid;
    }
    private void setMod(String mod) {
    	this.mod = mod;
    }

}
