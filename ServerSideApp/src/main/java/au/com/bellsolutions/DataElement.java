package au.com.bellsolutions;


public class DataElement {
    String desc;
    String tag;
    String val;

    public DataElement(String desc, String tag, String val) {
        this.desc = desc;
        this.tag = tag;
        this.val = val;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getDesc() {
        return desc;
    }

    public String getTag() {
        return tag;
    }

    public String getVal() { return val; }

    @Override
    public boolean equals(Object a) {
        return ((String)a).toLowerCase().equals(tag.toLowerCase());
    }
}
