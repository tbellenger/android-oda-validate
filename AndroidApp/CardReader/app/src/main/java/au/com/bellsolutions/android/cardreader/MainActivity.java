package au.com.bellsolutions.android.cardreader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Set;

import au.com.bellsolutions.android.card.ISO7816Card;
import au.com.bellsolutions.android.card.hardware.ExternalCard;
import au.com.bellsolutions.android.emv.Terminal;
import au.com.bellsolutions.android.emv.util.HexString;
import au.com.bellsolutions.android.nfc.activity.BaseNfcActivity;
import au.com.bellsolutions.android.emv.util.Tlv;

public class MainActivity extends BaseNfcActivity {
    TextView dataText;
    Bundle cardData = new Bundle();
    JSONArray data = new JSONArray();
    JSONArray sigData = new JSONArray();
    JSONArray termData = new JSONArray();
    JSONObject transaction = new JSONObject();
    String pan = "";
    String exp = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataText = (TextView)findViewById(R.id.data);
    }

    @Override
    public void onNfcDiscovered(Tag tag) {

        // create card and terminal and do transaction
        ISO7816Card card = new ISO7816Card(new ExternalCard(tag));
        Terminal term = new Terminal(card);

        try {
            term.connect();
            term.performTxn();
            term.disconnect();
        } catch (Exception e) {
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // clean out data storage
        pan = term.getPAN();
        exp = term.getExpiry();
        data = new JSONArray();
        sigData = new JSONArray();
        termData = new JSONArray();

        term.writeCardContentsToBundle(cardData);
        Hashtable<String, Tlv> cardContents = term.getCardContents();
        Hashtable<String, Tlv> cardSignedContents = term.getCardSignedContents();
        Tlv tlv;

        // clean out the data text on screen
        dataText.setText("");

        // iterate through the different data from card and terminal and create JSON
        Set<String> keys = cardContents.keySet();
        for(String key: keys){
            tlv = cardContents.get(key);
            addData(data, HexString.hexify(tlv.getTag()), HexString.hexify(tlv.getValue()));
        }
        keys = cardSignedContents.keySet();
        for(String key: keys){
            tlv = cardSignedContents.get(key);
            addData(sigData, HexString.hexify(tlv.getTag()), HexString.hexify(tlv.getValue()));
        }
        keys = term.getTerminalSettings().keySet();
        for(String key: keys){
            String setting = term.getTerminalSettings().get(key);
            addData(termData, key, setting);
        }

        try {
            // Update the screen and copy to clipboard
            String json = this.getJsonData().toString(2);
            dataText.append(json);

            ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Transaction Data", json));

            Toast.makeText(this, "JSON copied to clipboard", Toast.LENGTH_SHORT).show();

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://validate-oda.appspot.com/validate";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, this.getJsonData(), new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            dataText.append("\r\n");
                            try {
                                dataText.append(response.toString(2));
                            } catch (JSONException e) {
                                dataText.append(response.toString());
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dataText.append("\r\n");
                            dataText.append(parseVolleyError(error));

                        }
                    });

            queue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String parseVolleyError(VolleyError error) {
        String message = "Error";
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            //JSONArray errors = data.getJSONArray("errors");
            //JSONObject jsonMessage = errors.getJSONObject(0);
            message = data.toString(2);
        } catch (JSONException e) {
        } catch (UnsupportedEncodingException errorr) {
        }
        return message;
    }

    private void addData(JSONArray array, String tag, String value) {
        JSONObject element = new JSONObject();
        String description = "";
        try {
            element.put("tag", tag);
            if (Terminal.getTagNameMap().containsKey(tag)) {
                description = Terminal.getTagNameMap().get(tag);
            }
            element.put("description", description);
            element.put("value", value);
            array.put(element);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJsonData() {
        transaction = new JSONObject();
        try {
            transaction.put("terminalSettings", termData);
            transaction.put("cardData", data);
            transaction.put("cardSignedData" , sigData);
            transaction.put("pan", pan);
            transaction.put("expiry", exp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return transaction;
    }

}
