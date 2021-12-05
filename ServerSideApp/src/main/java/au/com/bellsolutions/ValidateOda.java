package au.com.bellsolutions;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

@WebServlet(
    name = "Validate ODA",
    urlPatterns = {"/validate"}
)
public class ValidateOda extends HttpServlet {

  /**
	 * 
	 */
	private static final long serialVersionUID = 7743866266258030153L;

@Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
	  
    StringBuilder data = new StringBuilder();
    Boolean cardValid = true;
    
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(400);
    startJsonLogEntry(response.getWriter());
    
    try {
        String s;
        while ((s = request.getReader().readLine()) != null) {
            data.append(s);
        } 
    } catch (Exception e) { 
      writeJsonLogEntry(response.getWriter(),e.getLocalizedMessage(),"Error");
      endJsonLogEntry(response.getWriter(), false);
      return;
    }
    
    try {
	    JsonObject txn = (JsonObject) Jsoner.deserialize(data.toString(), new JsonObject());
	    JsonArray termSettings = (JsonArray)txn.get("terminalSettings");
	    Settings terminal = new Settings(termSettings);
	    JsonArray cardData = (JsonArray)txn.get("cardData");
	    Settings card = new Settings(cardData);
	    String pan = (String)txn.get("pan");
	    String exp = (String)txn.get("expiry");
	    
	    writeJsonLogEntry(response.getWriter(), "Retrieved JSON transaction", "OK");
	
	    // Check CA Key Index
	    String caKeyIndex = card.findTag("8F").getVal();
	    if (!VisaCert.supported(caKeyIndex)) {
	    	writeJsonLogEntry(response.getWriter(), "Cert Authority Index not found [" + caKeyIndex + "]", "Error");
	    	endJsonLogEntry(response.getWriter(), false);
	    	return;
	    } else {
	    	writeJsonLogEntry(response.getWriter(), "Cert Authority Index found [" + caKeyIndex + "]", "OK");
	    }
	    
	    VisaCert visaCert = VisaCert.getInstance(caKeyIndex);
	
	    // SHA-1 on CA key
	    if (Util.toSHA1(Util.hexStringToByteArray(visaCert.rid + visaCert.index + visaCert.mod + visaCert.exponent)).equals(visaCert.sha1)) {
	    	writeJsonLogEntry(response.getWriter(), "CA SHA-1 check success", "OK");
	    } else {
	    	writeJsonLogEntry(response.getWriter(), "CA SHA-1 check failed", "Error");
	    	cardValid = false;
	    }
	
	    // Decrypt Issuer Cert
	    String encIssuerCert = card.findTag("90").getVal();
	    byte[] decIssCert = Util.rsaDecrypt(Util.getPublicKey(visaCert.mod, visaCert.exponent), Util.hexStringToByteArray(encIssuerCert));
	
	    IssuerCert issuerCert = new IssuerCert(decIssCert);
	    issuerCert.setExponent(card.findTag("9F32").getVal());
	
	    // Get remainder
	    issuerCert.concatIssuerCertRemainder(card.findTag("92").getVal());
	
	    issuerCert.validate(pan);
	    if (issuerCert.checkHash()) { 
	    	writeJsonLogEntry(response.getWriter(),"Issuer Cert SHA-1 success", "OK");
	    } else {
	    	writeJsonLogEntry(response.getWriter(),"Issuer Cert SHA-1 failed", "Error");
	    	cardValid = false;
	    }
	
	    // Get encrypted ICC Cert
	    String encIccCert = card.findTag("9F46").getVal();
	    byte[] decIccCert = Util.rsaDecrypt(Util.getPublicKey(issuerCert.issuerPubKey, issuerCert.exponent), Util.hexStringToByteArray(encIccCert));
	
	    IccCert iccCert = new IccCert(decIccCert);
	    iccCert.setExponent(card.findTag("9F47").getVal());
	
	    iccCert.validate(pan);
	    if (iccCert.checkHash()) { 
	    	writeJsonLogEntry(response.getWriter(),"ICC Cert SHA-1 success", "OK");
	    } else {
	    	writeJsonLogEntry(response.getWriter(),"ICC Cert SHA-1 failed", "Error");
	    	cardValid = false;
	    }
	
	    // Decrypt SDAD
	    String encSDAD = card.findTag("9F4B").getVal();
	    byte[] decSDAD = Util.rsaDecrypt(Util.getPublicKey(iccCert.iccPubKey, iccCert.exponent), Util.hexStringToByteArray(encSDAD));
	
	    SignedDynamicAppData oda = new SignedDynamicAppData(decSDAD);
	    
	    String un = terminal.findTag("9F37").getVal();
	    String amount = terminal.findTag("9F02").getVal();
	    String currCode = terminal.findTag("5F2A").getVal();
	    String cardAuthRelatedData = card.findTag("9F69").getVal();
	
	    oda.validate();
	    if (oda.checkHash(un, amount, currCode, cardAuthRelatedData)) { 
	    	writeJsonLogEntry(response.getWriter(),"SDAD SHA-1 success", "OK");
	    } else {
	    	writeJsonLogEntry(response.getWriter(),"SDAD SHA-1 failed", "Error");
	    	cardValid = false;
	    }
	
	} catch (Exception e) {
		writeJsonLogEntry(response.getWriter(),e.getLocalizedMessage(),"Error");
	    endJsonLogEntry(response.getWriter(), false);
	}

    endJsonLogEntry(response.getWriter(), cardValid);
    response.setStatus(200);
  }

public void startJsonLogEntry(PrintWriter w) {
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	LocalDateTime now = LocalDateTime.now();

	w.println("{ \"log\": [");
	writeJsonLogEntry(w,"Start Log - " + dtf.format(now), "OK");
}

	public void writeJsonLogEntry(PrintWriter w, String log, String status) {
		w.println("{ \"entry\":\"" + log + "\", \"status\":\"" + status + "\" },");
	}
	
	public void endJsonLogEntry(PrintWriter w, boolean cardValid) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		
		w.println("{ \"entry\":\"End Log - " + dtf.format(now) + "\", \"status\":\"OK\" }");
		w.println("], \"cardvalid\":" + (cardValid?"true":"false") + "}");
	}
}
