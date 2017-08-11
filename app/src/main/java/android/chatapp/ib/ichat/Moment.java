package android.chatapp.ib.ichat;

/**
 * Created by ibrah on 8/10/2017.
 */

public class Moment {


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Moment(long timestamp,String by,String liked) {
        this.timestamp = timestamp;
        this.by = by;
        this.liked = liked;
    }

    public Moment(){

    }
    public long timestamp;


    public String getLiked() {
        return liked;
    }

    public void setLiked(String liked) {
        this.liked = liked;
    }

    public String liked;

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String by;
}
