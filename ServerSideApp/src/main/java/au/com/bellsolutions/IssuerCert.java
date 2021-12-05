package au.com.bellsolutions;

public class IssuerCert {
    String header;
    String format;
    String issuerId;
    String certExpDate;
    String certSerialNum;
    String hashAlgo;
    String issuerPubKeyAlgo;
    String issuerPubKeyLen;
    String issuerPubKeyExpLen;
    String issuerPubKey;
    String hash;
    String trailer;
    String exponent;

    public IssuerCert(byte[] data) {
        header = Util.byteArrayToHexString(data[0]);
        format = Util.byteArrayToHexString(data[1]);
        issuerId = Util.byteArrayToHexString(data,2,4);
        certExpDate = Util.byteArrayToHexString(data, 6, 2);
        certSerialNum = Util.byteArrayToHexString(data, 8, 3);
        hashAlgo = Util.byteArrayToHexString(data, 11, 1);
        issuerPubKeyAlgo = Util.byteArrayToHexString(data, 12, 1);
        issuerPubKeyLen = Util.byteArrayToHexString(data, 13, 1);
        issuerPubKeyExpLen = Util.byteArrayToHexString(data, 14, 1);
        issuerPubKey = Util.byteArrayToHexString(data, 15, data.length - 36);
        hash = Util.byteArrayToHexString(data, data.length - 21, 20);
        trailer = Util.byteArrayToHexString(data[data.length - 1]);
    }

    public void concatIssuerCertRemainder(String rem) {
        issuerPubKey = issuerPubKey + rem;
    }

    public void setExponent(String exp) {
        exponent = exp;
    }

    public boolean checkHash() {
        String result = Util.toSHA1(Util.hexStringToByteArray(format + issuerId + certExpDate + certSerialNum + 
        		hashAlgo + issuerPubKeyAlgo + issuerPubKeyLen + 
        		issuerPubKeyExpLen + issuerPubKey + exponent));
        return hash.equals(result);
    }

    public void validate(String pan) {
        assert(header.equals("6A"));
        assert(trailer.equals("BC"));
        assert(format.equals("02"));

        // check PAN against issuerId

        //
    }
}
