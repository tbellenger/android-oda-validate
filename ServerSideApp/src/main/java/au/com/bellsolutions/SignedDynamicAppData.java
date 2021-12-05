package au.com.bellsolutions;

public class SignedDynamicAppData {
    String header;
    String format;
    String hashAlgo;
    String dynDataLen;
    int dynDataLenInt;
    String dynData;
    String pad;
    String hash;
    String trailer;

    public SignedDynamicAppData(byte[] data) {
        header = Util.byteArrayToHexString(data[0]);
        format = Util.byteArrayToHexString(data[1]);
        hashAlgo = Util.byteArrayToHexString(data, 2, 1);
        dynDataLen = Util.byteArrayToHexString(data, 3, 1);
        dynDataLenInt = (int)(0x00ff & data[3]);
        dynData = Util.byteArrayToHexString(data, 4, dynDataLenInt);
        pad = Util.byteArrayToHexString(data, 4 + dynDataLenInt, data.length - dynDataLenInt - 25);
        hash = Util.byteArrayToHexString(data, data.length - 21, 20);
        trailer = Util.byteArrayToHexString(data[data.length - 1]);
    }

    public boolean checkHash(String un, String amount, String currCode, String cardAuthRelatedData) {
        String result = Util.toSHA1(Util.hexStringToByteArray(format + hashAlgo + dynDataLen + 
        		dynData + pad + un + amount + currCode + cardAuthRelatedData));
        return hash.equals(result);
    }

    public void validate() {
        assert(header.equals("6A"));
        assert(trailer.equals("BC"));
        assert(format.equals("95"));
    }
}

