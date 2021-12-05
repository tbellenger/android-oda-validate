package au.com.bellsolutions.android.emv;

import java.io.IOException;

import au.com.bellsolutions.android.card.ISO7816Card;
import au.com.bellsolutions.android.emv.util.HexString;


public class VMPACard extends EmvCard {
	public static final String GPD = "80D8000000";
	public static final String PASSCODE_CHANGE = "80D4000010";
	public static final String VERIFY = "0020008008";
	public static final String READ_TRANS_LOG = "80B2000000";
	public static final String SET_STATUS = "80F0000000";
	public static final String GET_STATUS = "80F24000024F0000";
	
	public static final byte STATUS_AVAILABILITY = 0x01;
	public static final byte STATUS_ACTIVATE_AND_PRIORITY = 0x02;
	
	public static final byte STATUS_AVAIL_DEACTIVATED = 0x00;
	public static final byte STATUS_AVAIL_ACTIVATED = 0x01;
	
	public static final byte STATUS_PRI_HIGHEST = 0x01;
	public static final byte STATUS_PRI_LOWEST = (byte)0x81;
	public static final byte STATUS_PRI_OVERRIDE = 0x02;
	public static final byte STATUS_PRI_RESET = (byte)0x82;
	
	public static final byte GPD_PROTECTED = 0;
	public static final byte GPD_UNPROTECTED = 1;
	
	public static final byte VERIFY_VALID_GPO = (byte)0x80;
	public static final byte VERIFY_VALID_MSU = 0x40;
	public static final byte VERIFY_VALID_GDN = 0x20;
	public static final byte VERIFY_VALID_NONE = 0x00;
	
	public VMPACard(CardSpec card) {
		super(card);
	}
	
	public VMPACard(ISO7816Card card) {
		super(card);
	}
	
	public byte[] getProtectedData(boolean accessProtected) throws StatusWordException, IOException {
		byte[] apdu = HexString.parseHexString(GPD);
		apdu[P1_OFFSET] = accessProtected ? GPD_PROTECTED : GPD_UNPROTECTED;
		return send(apdu);
	}
	
	public byte[] verify(byte[] pinBlock, byte validity) throws StatusWordException, IOException {
		byte[] apdu = HexString.parseHexString(VERIFY + HexString.hexify(pinBlock)) ;
		apdu[P1_OFFSET] = validity;
		return send(apdu);
	}
	
	public byte[] passcodeChange(byte[] currentPinBlock, byte[] newPinBlock) throws StatusWordException, IOException {
		byte[] apdu = HexString.parseHexString(PASSCODE_CHANGE + HexString.hexify(currentPinBlock) + HexString.hexify(newPinBlock));
		return send(apdu);
	}
	
	/**
	 * Set the contactless status of the VMPA applet
	 * @param status {@link #STATUS_ACTIVATE_AND_PRIORITY} or {@link #STATUS_AVAILABILITY}
	 * @param statusDef {@link #STATUS_AVAIL_ACTIVATED}, {@link #STATUS_AVAIL_DEACTIVATED}
	 * @param commandDataTemplate Set to BF6200 or BF620BDF0008 + PIN Block
	 * @return card response and status word
	 * @throws StatusWordException  if status words are being checked {@link #Card.setCheckSw()}
	 * @throws IOException if there is a problem communicating with the card
	 */
	public byte[] setStatus(byte status, byte statusDef, byte[] commandDataTemplate) throws StatusWordException, IOException {
		byte[] apdu = HexString.parseHexString(SET_STATUS + HexString.hexify(commandDataTemplate) + "00");
		apdu[P1_OFFSET] = status;
		apdu[P2_OFFSET] = statusDef;
		apdu[LC_OFFSET] = (byte)commandDataTemplate.length;
		return send(apdu);
	}
	
	public byte[] getStatus() throws StatusWordException, IOException {
		return send(GET_STATUS);
	}
	
	public byte[] readTransactionLog(byte rec, byte sfi) throws StatusWordException, IOException {
		byte p2 = (byte) ((sfi<<3)|0x4);
		byte[] read_rec = HexString.parseHexString(READ_TRANS_LOG);
		read_rec[P1_OFFSET] = rec;
		read_rec[P2_OFFSET] = p2;
		return send(read_rec);
	}

}
