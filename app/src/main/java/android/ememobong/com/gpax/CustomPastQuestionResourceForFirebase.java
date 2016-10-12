package android.ememobong.com.gpax;

/**
 * Created by ememobong on 05/09/2016.
 */
public class CustomPastQuestionResourceForFirebase {

    public String userUID;
    public CustomHashMap imageUrls;
    public CustomHashMap likes;
    public CustomHashMap comments;
    public String dateOfPost;
    public long timeOfPost;
    public CustomHashMap imageNamesOfFiles;

    public CustomPastQuestionResourceForFirebase(){
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

    public CustomHashMap getImageNamesOfFiles() {
        return imageNamesOfFiles;
    }

    public void setImageNamesOfFiles(CustomHashMap imageNamesOfFiles) {
        this.imageNamesOfFiles = imageNamesOfFiles;
    }

    public CustomHashMap getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(CustomHashMap imageUrls) {
        this.imageUrls = imageUrls;
    }

    public CustomHashMap getLikes() {
        return likes;
    }

    public void setLikes(CustomHashMap likes) {
        this.likes = likes;
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
