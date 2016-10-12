package android.ememobong.com.gpax;

/**
 * Created by ememobong on 08/10/2016.
 */
public class AdvertsJOPO {

    public CustomHashMap comments;
    public String dateOfPost;
    public long timeOfPost;
    public String advertLink;
    public double advertID;
    public String userUID;

    public AdvertsJOPO(){

    }

    public double getAdvertID() {
        return advertID;
    }

    public void setAdvertID(double advertID) {
        this.advertID = advertID;
    }

    public String getAdvertLink() {
        return advertLink;
    }

    public void setAdvertLink(String advertLink) {
        this.advertLink = advertLink;
    }

    public CustomHashMap getComments() {
        return comments;
    }

    public void setComments(CustomHashMap comments) {
        this.comments = comments;
    }

    public String getDateOfPost() {
        return dateOfPost;
    }

    public void setDateOfPost(String dateOfPost) {
        this.dateOfPost = dateOfPost;
    }

    public long getTimeOfPost() {
        return timeOfPost;
    }

    public void setTimeOfPost(long timeOfPost) {
        this.timeOfPost = timeOfPost;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }


}
