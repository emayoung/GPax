package android.ememobong.com.gpax;

/**
 * Created by ememobong on 05/08/2016.
 */
public class User  {


    private String userUniqueId;
    private String email;
    private String password;
    private String metadata;

    public User() {
    }

    public User(String userUniqueId, String email) {
        this.userUniqueId = userUniqueId;
        this.email = email;
        this.password = " ";
        this.metadata = " ";
    }

    public String getUserUniqueId() {
        return userUniqueId;
    }

    public void setUserUniqueId(String userUniqueId) {
        this.userUniqueId = userUniqueId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMetadata(){
        return metadata;
    }

    public void setMetadata(String metadata){
        this.metadata = metadata;
    }
}
