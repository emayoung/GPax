package android.ememobong.com.gpax;

/**
 * Created by ememobong on 29/08/2016.
 */
public class PastQuestionResource {

    public String userUID;
    public String imageUrls;
    public String likes;
    public String comments;
    public String dateOfPost;
    public long timeOfPost;
    public String imageNamesOfFiles;

    public PastQuestionResource(){

    }

    public String getImageNamesOfFiles() {
        return imageNamesOfFiles;
    }

    public void setImageNamesOfFiles(String imageNamesOfFiles) {
        this.imageNamesOfFiles = imageNamesOfFiles;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDateOfPost() {
        return dateOfPost;
    }

    public void setDateOfPost(String dateOfPost) {
        this.dateOfPost = dateOfPost;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
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
