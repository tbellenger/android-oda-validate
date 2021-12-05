package au.com.bellsolutions.android.emv;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.bellsolutions.android.card.ISO7816Card;
import au.com.bellsolutions.android.card.ISO7816Card.StatusWordException;
import au.com.bellsolutions.android.emv.util.HexString;
import au.com.bellsolutions.android.emv.util.Tlv;
import au.com.bellsolutions.android.emv.util.TlvException;


/**
 * A class acting as an EMV terminal in order to simulate the start 
 * of a transaction and read the card contents.
 * @author tbelleng
 *
 */
public class Terminal {
	public static final String TAG = "Terminal";
	
	private Hashtable<String, String> terminalSettings = new Hashtable<String, String>();
	private Hashtable<String, Tlv> cardContents = new Hashtable<String, Tlv>();
	private Hashtable<String, Tlv> cardSignedContents = new Hashtable<String, Tlv>();
	private static Hashtable<String, String> tagNameMap = new Hashtable<String, String>();
	private JSONObject jsonCardData = new JSONObject();
	private JSONObject jsonSignedCardData = new JSONObject();
	private JSONObject jsonPan = new JSONObject();
	private JSONObject jsonExp = new JSONObject();
	private JSONObject jsonTermSetting = new JSONObject();
	private JSONArray jsonTermSettings = new JSONArray();

	
	private byte[] selectPpse;
	private byte[] paymentAid;
	private byte[] selectPaymentApp;
	private byte[] gpoResponse;
	private Hashtable<AflItem, byte[]> readRecordResponses = new Hashtable<AflItem, byte[]>();
	private byte[] pdolData = new byte[0];
	private ArrayList<AflItem> aflItems = new ArrayList<AflItem>();
	private EmvCard card;
	
	private static Random rand = new Random();
	
	static {
		tagNameMap.put("6F", "Template");
		tagNameMap.put("84", "DF");
		tagNameMap.put("A5", "FCI Proprietary Template");
		tagNameMap.put("BF0C", "FCI Issuer Discretionary Data");
		tagNameMap.put("61", "Application Template");
		tagNameMap.put("4F", "Application Identifier - card");
		tagNameMap.put("50", "Application Label");
		tagNameMap.put("87", "Application Priority Indicator");
		tagNameMap.put("9F12", "Application Preferred Name");
		tagNameMap.put("9F11", "Issuer Code Table Index");
		tagNameMap.put("5F2D", "Language Preference");
		tagNameMap.put("9F38", "PDOL");
		tagNameMap.put("77", "Response Message Template 2");
		tagNameMap.put("9F10", "Issuer Application Data");
		tagNameMap.put("5F20", "Cardholder Name");
		tagNameMap.put("57", "Track 2 Equivalent Data");
		tagNameMap.put("5F34", "PAN Sequence Number");
		tagNameMap.put("82", "Application Interchange Profile");
		tagNameMap.put("9F36", "Application Transaction Counter");
		tagNameMap.put("9F26", "Application Cryptogram");
		tagNameMap.put("70", "Read Record Template");
		tagNameMap.put("9F1F", "Track 1 Discretionary Data");
		tagNameMap.put("94", "Application File Locator");
		tagNameMap.put("9F27","Cryptogram Information Data");
		tagNameMap.put("92","Issuer Public Key Remainder");
		tagNameMap.put("9F07","Application Usage Control");
		tagNameMap.put("90","Issuer Public Key Certificate");
		tagNameMap.put("5F28","Issuer Country Code");
		tagNameMap.put("5F25","Application Effective Date");
		tagNameMap.put("9F32","Issuer Public Key Exponent");
		tagNameMap.put("9F48","ICC Public Key Remainder");
		tagNameMap.put("9F4A","SDA Tag List");
		tagNameMap.put("5F24","Application Expiration Date");
		tagNameMap.put("8F","Certification Authority Public Key Index");
		tagNameMap.put("5A","PAN");
		tagNameMap.put("9F47","ICC Public Key Exponent");
		tagNameMap.put("9F46","ICC Public Key Certificate");
		tagNameMap.put("5F27","Cryptogram Information Data");
		tagNameMap.put("9F4B","Signed Dynamic Application Data");
		tagNameMap.put("9F37","Unpredictable Number");
		tagNameMap.put("9F66","Visa Terminal Transaction Qualifiers");
		tagNameMap.put("9F02","Amount Authorized");
		tagNameMap.put("9F03","Amount Other");
		tagNameMap.put("5F2A","Country Code");
		tagNameMap.put("9A","Transaction Date");
		tagNameMap.put("9F1A","Terminal Country Code");
		tagNameMap.put("95","Terminal Verification Results");
		tagNameMap.put("9C","Transaction Type");
		tagNameMap.put("9F7A","VLP Accepted");
		tagNameMap.put("9F4E", "Merchant Name and Location");
		tagNameMap.put("9F5C","Unknown (MC)");
		tagNameMap.put("9F35", "Terminal Type");
		tagNameMap.put("9F33", "Terminal Capabilities");
		tagNameMap.put("9F15", "Merchant Category Code");
		tagNameMap.put("9F40","Unknown (MC)");
		tagNameMap.put("9F6B","Track 2 equiv (MC)");

	}

	/**
	 * Terminal class to simulate EMV transactions to read the card contents.
	 * The default terminal settings contain settings for 
	 * - Terminal Transaction Qualifiers
	 * - Amt, Authorised
	 * - Amt, Other
	 * - Country Code
	 * - Transaction Date (set to current date)
	 * - Terminal Country Code
	 * - TVR
	 * - Transaction Type
	 * - VLP (set to not accepted)
	 * If different values are required than the default values then the constructor
	 * taking a hashtable argument should be used.
	 * @param card
	 */
	public Terminal(ISO7816Card card) {
		byte[] r = new byte[4];
		rand.nextBytes(r);
		Date today = new Date();
		Format f = new SimpleDateFormat("yyMMdd");
		
		this.card = new EmvCard(card);
		
		Random rand = new Random();
		byte[] randBuff = new byte[4];
		rand.nextBytes(randBuff);
		terminalSettings.put("9F37", HexString.hexify(randBuff));
		//terminalSettings.put("9F66", "86004000");			// Visa Terminal Transaction Qualifiers - MSD only
		terminalSettings.put("9F66", "37004000");			// Visa Terminal Transaction Qualifiers with ODA for Online Auth
		terminalSettings.put("9F02", "000000000000");		// Amt Authorised
		terminalSettings.put("9F03", "000000000000");		// Amt Other
		terminalSettings.put("5F2A", "0036");				// Country Code AU
		terminalSettings.put("9A", f.format(today));		// Transaction Date YYMMDD
		terminalSettings.put("9F1A", "0036");				// Terminal Country Code AU
		terminalSettings.put("95", "0000000000");			// TVR
		terminalSettings.put("9C", "00");					// Transaction Type
		terminalSettings.put("9F7A", "00");					// VLP not accepted by terminal
		terminalSettings.put("9F5C", "0000000000000000");	// Don't know MC
		terminalSettings.put("9F40", "0000000000");			// Don't know MC
		terminalSettings.put("9F35", "00");					// Terminal Type
		terminalSettings.put("9F33", "000000");				// Terminal capabilities
		terminalSettings.put("9F15", "0000");				// Merchant Category Code

		try {
			terminalSettings.put("9F4E", HexString.hexify("Android Mobile POSv1".getBytes("US-ASCII")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}// Merchant Name, Location
	}
	
	/**
	 * Terminal class to simulate EMV transactions to read the card contents.
	 * @param card
	 * @param terminalSettings a hashtable with tag as key and hex string as value
	 */
	public Terminal(ISO7816Card card, Hashtable<String, String> terminalSettings) {
		this(card);
		this.terminalSettings.putAll(terminalSettings);
	}
	
	/**
	 * A mapping of EMV tags and their english names
	 * @return
	 */
	public static Hashtable<String, String> getTagNameMap() {
		return tagNameMap;
	}

	public Hashtable<String, String> getTerminalSettings() {
		return terminalSettings;
	}

	/**
	 * Retrieve the PPSE select response.
	 * @return the response from the selection of the PPSE
	 */
	public byte[] getSelectPpseResponse() {
		return selectPpse;
	}
	
	public byte[] getSelectPaymentAppResponse() {
		return selectPaymentApp;
	}
	
	public byte[] getGpoResponse() {
		return gpoResponse;
	}
	
	public Hashtable<AflItem, byte[]> getReadRecordResponses() {
		return readRecordResponses;
	}
	
	public void clearReadRecordResponses() {
		readRecordResponses.clear();
	}
	
	public String getPAN() {
		Tlv panTlv = null;
		String key = "";
		if (HexString.hexify(paymentAid).startsWith("A0000000031010")) {
			// Visa
			key = "57";
		} else {
			// MasterCard
			key = "9F6B";
		}
		
		// prefer signed data
		if (cardSignedContents.containsKey(key)) {
			panTlv = cardSignedContents.get(key);
		} else if (cardContents.containsKey(key)) {
			panTlv = cardContents.get(key);
		}

		if (panTlv != null) {
			String pan = HexString.hexify(panTlv.getValue());
			if (pan.indexOf("D") != -1) {
				pan = pan.substring(0,pan.indexOf("D"));
			}
			return pan;
		} else {
			return "";
		}
	}
	
	public String getExpiry() {
		Tlv panTlv = null;
		String key = "";
		if (HexString.hexify(paymentAid).startsWith("A0000000031010")) {
			// Visa
			key = "57";
		} else {
			// MasterCard
			key = "9F6B";
		}
		
		// prefer signed data
		if (cardSignedContents.containsKey(key)) {
			panTlv = cardSignedContents.get(key);
		} else if (cardContents.containsKey(key)) {
			panTlv = cardContents.get(key);
		}

		if (panTlv != null) {
			String expiry = HexString.hexify(panTlv.getValue());
			if (expiry.indexOf("D") != -1) {
				expiry = expiry.substring(expiry.indexOf("D") + 1, expiry.indexOf("D") + 5);
			}
			return expiry;
		} else {
			return "";
		}
	}
	
	public Hashtable<String, Tlv> getCardContents() {
		return cardContents;
	}
	
	public Hashtable<String, Tlv> getCardSignedContents() {
		return cardSignedContents;
	}
	
	public ISO7816Card getCard() {
		return card;
	}
	
	public void setCard(ISO7816Card card) {
		this.card = new EmvCard(card);
	}
	
	public boolean connect() {
		return card.connect();
	}
	
	public boolean disconnect() {
		return card.close();
	}

	public void performTxn() throws TlvException, StatusWordException, IOException {
		this.selectPpse();
		this.selectPaymentApp();
		this.getProcessingOptions();
		this.readRecords();
	}
	
	public void selectPpse() throws TlvException, StatusWordException, IOException {
		selectPpse = card.selectPpse();
		String search = "6F|A5|BF0C|61|4F";
		
		Tlv tlv = Tlv.searchTlv(selectPpse, search);
		if (tlv != null) {
			paymentAid = tlv.getValue();
			cardContents.put(HexString.hexify(tlv.getTag()), tlv);
		} else { paymentAid = null; }
	}
	
	public void selectPaymentApp() throws StatusWordException, IOException, TlvException {
		selectPaymentApp = card.select(paymentAid);
		
		// prepare PDOL data
		String search = "6F|A5|9F38";
		String pdolString = "";
		
		Tlv tlv = Tlv.searchTlv(selectPaymentApp, search);
		if (tlv != null) {
			cardContents.put(HexString.hexify(tlv.getTag()), tlv);
			ArrayList<String> list = Tlv.getTagLengthMap(tlv.getValue());
			Log.d(TAG, "List size is: " + list.size());
			for (int i = 0; i < list.size(); i++) {
				Log.d(TAG, list.get(i));
				if (terminalSettings.containsKey(list.get(i))) {
					pdolString += terminalSettings.get(list.get(i));
				} else {
					Log.d(TAG, "Terminal data does not contain " + list.get(i));
				}
			}
			Log.d(TAG, "Terminal data is: " + pdolString);
			pdolData = HexString.parseHexString(pdolString);
		}
	}
	
	public void getProcessingOptions() throws TlvException, StatusWordException, IOException {
		gpoResponse = card.getProcessingOptions(pdolData);
		
		if (gpoResponse[0] == (byte)0x80) {
			// old template
			byte[] afl = new byte[gpoResponse[1] - 2];
			System.arraycopy(gpoResponse, 4, afl, 0, afl.length);
			aflItems = parseAfl(afl);
			return;
		}
		
		// prepare PDOL data
		String search = "77|94";
		byte[] afl;
		Tlv tlv = Tlv.searchTlv(gpoResponse, "77");
		if (tlv != null) {
		//	afl = tlv.getValue();
		//	aflItems = parseAfl(afl);
		//} else {
			// try parsing GPO response for tag 57
			//tlv = Tlv.searchTlv(gpoResponse, "77");
			//if (tlv != null) {
				Log.d(TAG, "Loading GPO contents");
				int offset = 0;
				while (offset < tlv.getValue().length) {
					Tlv c = Tlv.parseTlv(tlv.getValue(),offset);
					Log.d(TAG, "Adding " + HexString.hexify(c.getTag()) + " to unsigned card contents");
					cardContents.put(HexString.hexify(c.getTag()), c);
					if (HexString.hexify(c.getTag()).equals("94")) {
						afl = c.getValue();
						aflItems = parseAfl(afl);
					}
					offset = offset + c.getBuffer().length;
				}
			//}
		}
	}
	
	public void readRecords() throws IOException, TlvException {
		readRecords(aflItems);
	}
	
	public void readRecords(ArrayList<AflItem> aflItems) throws IOException, TlvException {
		for (AflItem i : aflItems) {
			Log.d(TAG, "Reading SFI " + i.sfi + " Record " + i.rec);
			try {
				byte[] cardData = card.readRecord(i.rec, i.sfi);
				readRecordResponses.put(i, cardData);
				Tlv tlv = Tlv.searchTlv(cardData, "70");
				if (tlv != null) {
					Log.d(TAG, "Found record contents");
					int offset = 0;
					while (offset < tlv.getValue().length) {
						Tlv c = Tlv.parseTlv(tlv.getValue(),offset);
						Log.d(TAG, "Adding " + HexString.hexify(c.getTag()) + " to " + (i.signed ? "signed ":"unsigned ") + "card contents");
						if (i.signed) {
							cardSignedContents.put(HexString.hexify(c.getTag()), c);
						} else {
							cardContents.put(HexString.hexify(c.getTag()), c);
						}
						offset = offset + c.getBuffer().length;
					}
				}
			} catch (StatusWordException swe) {
				Log.d(TAG, swe.getMessage());
			}
		}
	}
	
	public void readLog() throws StatusWordException, IOException {
		card.readRecord((byte)1, (byte)20);
		// TODO: Read all log records and parse the results in a nice format
	}

	public static ArrayList<AflItem> parseAfl(byte[] afl) {
		Log.d(TAG,"AFL:" + HexString.hexify(afl));
		ArrayList<AflItem> items = new ArrayList<AflItem>();
		for (int i = 0; i < afl.length; i=i+4) {
			for (byte j=afl[i+1]; j < afl[i+2]+1; j++) {
				AflItem a = new AflItem();
				a.sfi = (byte) (afl[i]>>>3);
				a.rec = j;
				a.signed = (afl[i+3]-j>=0);
				items.add(a);
				Log.d(TAG, "AFL item" + a.toString());
			}
		}
		return items;
	}
	
	public void writeCardContentsToBundle(Bundle bundle) {
		bundle.putSerializable("cardcontents", cardContents);
		bundle.putSerializable("cardsignedcontents", cardSignedContents);
		bundle.putByteArray("gporesponse", getGpoResponse());
		bundle.putSerializable("records", getReadRecordResponses());
	}
	
	public static class AflItem implements Parcelable {
		public byte sfi;
		public byte rec;
		public boolean signed;
		
		public AflItem() {};
		
		private AflItem(Parcel in) {
			boolean[] b = new boolean[1];
			sfi = in.readByte();
			rec = in.readByte();
			in.readBooleanArray(b);
			signed = b[0];
		}
		
		public String toString() {
			return "[SFI:" + sfi + ",REC:" + rec + ",SIGNED:" + signed + "]";
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel dest, int flags) {
			dest.writeByte(sfi);
			dest.writeByte(rec);
			dest.writeBooleanArray(new boolean[] {signed});
		}
		
		public static final Parcelable.Creator<AflItem> CREATOR = new Parcelable.Creator<AflItem>() {
	        public AflItem createFromParcel(Parcel in) {
	            return new AflItem(in);
	        }
	 
	        public AflItem[] newArray(int size) {
	            return new AflItem[size];
	        }
	    };
	}
}
