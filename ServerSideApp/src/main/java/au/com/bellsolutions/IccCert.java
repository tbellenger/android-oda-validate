package au.com.bellsolutions;

public class IccCert {
    String header;
    String format;
    String applicationPan;
    String certExpDate;
    String certSerialNum;
    String hashAlgo;
    String iccPubKeyAlgo;
    String iccPubKeyLen;
    int iccPubKeyLenInt;
    String iccPubKeyExpLen;
    String iccPubKey;
    String hash;
    String trailer;
    String exponent;

    public IccCert(byte[] data) {
        header = Util.byteArrayToHexString(data[0]);
        format = Util.byteArrayToHexString(data[1]);
        applicationPan = Util.byteArrayToHexString(data,2,10);
        certExpDate = Util.byteArrayToHexString(data, 12, 2);
        certSerialNum = Util.byteArrayToHexString(data, 14, 3);
        hashAlgo = Util.byteArrayToHexString(data, 17, 1);
        iccPubKeyAlgo = Util.byteArrayToHexString(data, 18, 1);
        iccPubKeyLen = Util.byteArrayToHexString(data, 19, 1);
        iccPubKeyLenInt = (int)(0x00ff & data[19]);
        iccPubKeyExpLen = Util.byteArrayToHexString(data, 20, 1);
        iccPubKey = Util.byteArrayToHexString(data, 21, data.length - 42);
        hash = Util.byteArrayToHexString(data, data.length - 21, 20);
        trailer = Util.byteArrayToHexString(data[data.length - 1]);
        if (iccPubKey.length() > iccPubKeyLenInt * 2) {
            // remove padding
            iccPubKey = iccPubKey.substring(0,iccPubKeyLenInt * 2);
        }
    }

    public void setExponent(String exp) {
        exponent = exp;
    }

    public boolean checkHash() {
        String result = Util.toSHA1(Util.hexStringToByteArray(format + applicationPan + 
        		certExpDate + certSerialNum + hashAlgo + iccPubKeyAlgo + 
        		iccPubKeyLen + iccPubKeyExpLen + iccPubKey + exponent + "2000"));
        return hash.equals(result);
    }

    public void validate(String pan) {
        assert(header.equals("6A"));
        assert(trailer.equals("BC"));
        assert(format.equals("04"));

        // check PAN against issuerId

        //
    }

}
