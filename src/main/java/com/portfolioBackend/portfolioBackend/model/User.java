package com.portfolioBackend.portfolioBackend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "users")
@Data
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String name;
    private String pictureUrl; // from Google OAuth

    /** Google subject id when signed up via Google */
    private String googleSubjectId;

    @Indexed
    private UserRole userRoles;

    private boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
        this.userRoles = UserRole.USER;
        this.emailVerified = false;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static User fromEmail(String email) {
        User u = new User();
        u.setEmail(email);
        u.setName(email);
        return u;
    }

    public static User fromGoogle(String email, String name, String pictureUrl, String googleSubjectId) {
        User u = new User();
        u.setEmail(email);
        u.setName(name != null ? name : email);
        u.setPictureUrl(pictureUrl);
        u.setGoogleSubjectId(googleSubjectId);
        u.setEmailVerified(true);
        return u;
    }

}
