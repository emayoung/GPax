package android.ememobong.com.gpax;

/**
 * Created by ememobong on 29/09/2016.
 */
public class CustomOtherResource {

    public String userUID;
    public String fileUrl;
    public CustomHashMap likes;
    public CustomHashMap comments;
    public String dateOfPost;
    public long timeOfPost;
    public String fileName;

    public CustomOtherResource(){

    }

    public CustomHashMap getLikes() {
        return likes;
    }

    public void setLikes(CustomHashMap likes) {
        this.likes = likes;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public long getTimeOfPost() {
        return timeOfPost;
    }

    public void setTimeOfPost(long timeOfPost) {
        this.timeOfPost = timeOfPost;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDateOfPost() {
        return dateOfPost;
    }

    public void setDateOfPost(String dateOfPost) {
        this.dateOfPost = dateOfPost;
    }

    public CustomHashMap getComments() {
        return comments;
    }

    public void setComments(CustomHashMap comments) {
        this.comments = comments;
    }

}
