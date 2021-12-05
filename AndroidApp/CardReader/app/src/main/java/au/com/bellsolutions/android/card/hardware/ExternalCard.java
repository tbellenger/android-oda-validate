package au.com.bellsolutions.android.card.hardware;

import java.io.IOException;


import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import au.com.bellsolutions.android.card.ISO7816Card.CardSpec;

public class ExternalCard implements CardSpec {
	private IsoDep mTag;
	
	public ExternalCard(Tag tag) {
		mTag = IsoDep.get(tag);
	}

	public void connect() throws IOException {
		mTag.connect();
	}

	public void close() throws IOException {
		mTag.close();
	}

	public byte[] send(byte[] apdu) throws IOException {
		return mTag.transceive(apdu);
	}

}
