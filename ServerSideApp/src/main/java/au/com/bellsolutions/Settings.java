package au.com.bellsolutions;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;

import java.util.*;

public class Settings {
    private ArrayList<DataElement> settings = new ArrayList<DataElement>();

    public Settings(JsonArray cont) {
        //Iterate over setting array
        cont.forEach(t -> parseElement((JsonObject)t));
    }

    public DataElement findTag(String tag) {
        Iterator<DataElement> i = settings.iterator();
        DataElement dt = null;
        while (i.hasNext()) {
            dt = i.next();
            if (dt.equals(tag)) {
                return dt;
            }
        }
        return dt;
    }

    public ArrayList<DataElement> getSettings() {
        return settings;
    }

    private void parseElement(JsonObject i) {
    	DataElement triplet = new DataElement((String)i.get("description"), (String)i.get("tag"), (String)i.get("value"));
        settings.add(triplet);
    }
}

