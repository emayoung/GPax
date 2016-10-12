package android.ememobong.com.gpax;

/**
 * Created by ememobong on 27/09/2016.
 */
public class CourseNewsResource {

    public String userUID;
    public String heading;
    public String likes;
    public String comments;
    public String dateOfPost;
    public long timeOfPost;
    public String newsBody;
    public String attachmentLinks;

    public String getAttachmentLinks() {
        return attachmentLinks;
    }

    public void setAttachmentLinks(String attachmentLinks) {
        this.attachmentLinks = attachmentLinks;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
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

    public String getNewsBody() {
        return newsBody;
    }

    public void setNewsBody(String newsBody) {
        this.newsBody = newsBody;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getDateOfPost() {
        return dateOfPost;
    }

    public void setDateOfPost(String dateOfPost) {
        this.dateOfPost = dateOfPost;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }


    public CourseNewsResource(){

    }
}
