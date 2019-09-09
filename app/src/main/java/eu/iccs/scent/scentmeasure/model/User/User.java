package eu.iccs.scent.scentmeasure.model.User;
/**
 * Created by theodoropoulos on 11/2/2018.
 */

public class User {
    public User(String userId, String password, String email){
        setPassword(password);
        setUserId(userId);
        setEmail(email);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String userId;
    private String password;
    private String email;


}
