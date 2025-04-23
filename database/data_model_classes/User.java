
import java.util.Objects;

/**
 * Model class representing a user in the database
 */
public class User {
    private Long userId;
    private String email;
    private String passwordHash;
    private String userName;

    /**
     * Full constructor for User
     * 
     * @param userId the unique identifier for the user
     * @param email the user's email address
     * @param passwordHash the hashed password
     * @param userName the display name of the user
     */
    public User(Long userId, String email, String passwordHash, String userName) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userName = userName;
    }

    // Getters and Setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Equals and HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && 
               Objects.equals(email, user.email) && 
               Objects.equals(passwordHash, user.passwordHash) && 
               Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, passwordHash, userName);
    }

    // toString

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", passwordHash='" + "[PROTECTED]" + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}