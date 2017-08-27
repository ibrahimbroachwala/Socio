package myapp.chatapp.ib.socio;

/**
 * Created by ibrah on 8/12/2017.
 */

public class Comments {


    public String by;

    public Comments(){}


    public Comments(String by, String text, long timestamp) {
        this.by = by;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String text;

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long timestamp;



}
