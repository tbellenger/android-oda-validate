package au.com.bellsolutions.android.card;

import java.io.IOException;


import android.util.Log;
import au.com.bellsolutions.android.emv.util.HexString;

/**
 * A generic card that accepts ISO7816 type APDU's
 * @author tbelleng
 *
 */
public class ISO7816Card {
	protected static final String TAG = "Card";
	public static final byte CMD_OFFSET = 0;
	public static final byte INS_OFFSET = 1;
	public static final byte P1_OFFSET = 2;
	public static final byte P2_OFFSET = 3;
	public static final byte LC_OFFSET = 4;
	public static final byte CDATA_OFFSET = 5;
	protected CardSpec mCard;
	protected boolean checkSw = true;
	
	/**
	 * Implementations using {@link com.visa.android.emv.Card} should use this interface
	 * @author tbelleng
	 *
	 */
	public interface CardSpec {
		
		/**
		 * Connect to the card. This must be called before attempting to send APDU's to the card
		 * @throws IOException if there was a problem connecting to the card
		 */
		public void connect() throws IOException;
		
		/**
		 * Close the connection to the card and release associated resources
		 * @throws IOException if there was a problem closing the connection to the card
		 */
		public void close() throws IOException;
		
		/**
		 * Sends the APDU to the card
		 * @param apdu APDU in a byte array
		 * @return card response and status word
		 * @throws IOException if there is a communication problem with the card
		 */
		public byte[] send(byte[] apdu) throws IOException;
	}
	
	
	public ISO7816Card(CardSpec card) {
		mCard = card;
	}
	
	public ISO7816Card(ISO7816Card card) {
		mCard = card.mCard;
	}
	
	/**
	 * Is status word being checked to be 0x9000 or 0x61XX
	 * @return true if checked
	 */
	public boolean isCheckSw() {
		return checkSw;
	}
	
	/**
	 * Should the status word returned by the card be checked to be 0x9000 or 0x61XX
	 * @param checkSw should check take place
	 */
	public void setCheckSw(boolean checkSw) {
		this.checkSw = checkSw;
	}
	
	/**
	 * Connect to card
	 * @return true if connection successful
	 */
	public boolean connect() {
		try {
			Log.d(TAG,"Connect");
			mCard.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Close connection to card and release resources
	 * @return true if successful
	 */
	public boolean close() {
		try {
			Log.d(TAG, "Close");
			mCard.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Status word exception when the status word returned is not 0x9000 or 0x61XX
	 * @author tbelleng
	 *
	 */
	public class StatusWordException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public StatusWordException(String error) {
			super(error);
		}
	}
	
	/**
	 * Send an APDU to the card
	 * @param hexstring APDU coded as a hex string
	 * @return card response and status word
	 * @throws StatusWordException if status words are being checked {@link #setCheckSw}
	 * @throws IOException if there is a problem communicating with the card
	 */
	public byte[] send(String hexstring) throws StatusWordException, IOException {
		return sendAndCheckSw(HexString.parseHexString(hexstring));
	}
	
	/**
	 * Send an APDU to the card
	 * @param apdu APDU in a byte buffer
	 * @return card response and status word
	 * @throws StatusWordException if status words are being checked {@link #setCheckSw}
	 * @throws IOException if there is a problem communicating with the card
	 */
	public byte[] send(byte[] apdu) throws StatusWordException, IOException {
		return sendAndCheckSw(apdu);
	}
	
	/**
	 * Send an APDU to the card and check the status word
	 * @param apdu APDU in a byte buffer
	 * @return card response and status word
	 * @throws StatusWordException if status words are being checked {@link #setCheckSw}
	 * @throws IOException if there is a problem communicating with the card
	 */
	private byte[] sendAndCheckSw(byte[] apdu) throws StatusWordException, IOException {
		Log.d(TAG, "Tx: " + HexString.hexify(apdu));
		byte[] resp = mCard.send(apdu);
		Log.d(TAG, "Rx: " + HexString.hexify(resp));
		if (resp == null) {
			throw new IOException("No response received from card");
		}
		if (checkSw) {
			if (resp[resp.length - 2] == (byte)0x90 || resp[resp.length - 2] == 0x61) {
				return resp;
			} else {
				throw new StatusWordException("Status word error : " + HexString.hexify(resp));
			}
		} else {
			return resp;
		}
	}
}
