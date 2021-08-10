package site.zpweb.barker.model;

public class LoginRegisterData {
    String phoneNumber,email,username,displayName;

    public LoginRegisterData(String phoneNumber, String email, String username, String displayName) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.username = username;
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
