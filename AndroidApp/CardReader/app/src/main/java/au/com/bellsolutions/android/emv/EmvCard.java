package au.com.bellsolutions.android.emv;

import java.io.IOException;

import au.com.bellsolutions.android.card.ISO7816Card;
import au.com.bellsolutions.android.emv.util.HexString;
import au.com.bellsolutions.android.emv.util.Tlv;
import au.com.bellsolutions.android.emv.util.TlvException;


public class EmvCard extends ISO7816Card {
	public static final String PPSE = "325041592E5359532E4444463031";
	public static final String SELECT = "00A40400";
	public static final String GET_PROCESSING_OPTIONS = "80A80000";
	public static final String READ_RECORD = "00B2000000";
	
	public EmvCard(CardSpec card) {
		super(card);
	}
	
	public EmvCard(ISO7816Card card) {
		super(card);
	}
	
	/**
	 * Select an applet using its AID
	 * @param aid The AID of the applet
	 * @return card response and status word
	 * @throws StatusWordException if status words are being checked {@link #Card.setCheckSw}
	 * @throws IOException if there is a problem communicating with the card
	 */
	public byte[] select(String aid) throws StatusWordException, IOException {
		return select(HexString.parseHexString(aid));
	}
	
	/**
	 * Select an applet using its AID
	 * @param aid The AID of the applet
	 * @return card response and status word
	 * @throws StatusWordException if status words are being checked {@link #Card.setCheckSw}
	 * @throws IOException if there is a problem communicating with the card
	 */
	public byte[] select(byte[] aid) throws StatusWordException, IOException {
		return send(SELECT + HexString.hexify(aid.length) + HexString.hexify(aid) + "00");
	}
	
	/**
	 * Utility method to select the PPSE
	 * @return card response and status word
	 * @throws StatusWordException if status words are being checked {@link #Card.setCheckSw}
	 * @throws IOException if there is a problem communicating with the card
	 */
	public byte[] selectPpse() throws StatusWordException, IOException {
		return select(PPSE);
	}
	
	/**
	 * Send Get Processing Options APDU
	 * @param pdol PDOL data which will be packaged in TLV 83
	 * @return card response and status word
	 * @throws StatusWordException if status words are being checked {@link #Card.setCheckSw}
	 * @throws IOException if there is a problem communicating with the card
	 */
	public byte[] getProcessingOptions(byte[] pdol) throws StatusWordException, IOException {
		Tlv tlv = null;
		try {
			tlv = Tlv.createTlv(new byte[] {(byte)0x83}, pdol);
		} catch (TlvException e) {
			e.printStackTrace();
		}
		return send(GET_PROCESSING_OPTIONS + HexString.hexify(tlv.getBuffer().length) + HexString.hexify(tlv.getBuffer()) + "00");
	}
	
	/**
	 * 
	 * @param rec Record to access
	 * @param sfi SFI to access
	 * @return record contents and status word
	 * @throws StatusWordException if status words are being checked {@link #Card.setCheckSw}
	 * @throws IOException if there is a problem communicating with the card
	 */
	public byte[] readRecord(byte rec, byte sfi) throws StatusWordException, IOException {
		byte p2 = (byte) ((sfi<<3)|0x4);
		byte[] read_rec = HexString.parseHexString(READ_RECORD);
		read_rec[2] = rec;
		read_rec[3] = p2;
		return send(read_rec);
	}
}
