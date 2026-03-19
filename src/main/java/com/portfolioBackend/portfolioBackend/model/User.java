package com.portfolioBackend.portfolioBackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;

@Document(collection = "users")
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
    private Set<Role> roles;

    private boolean emailVerified;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
        this.roles = Set.of(Role.USER);
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

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    public String getGoogleSubjectId() { return googleSubjectId; }
    public void setGoogleSubjectId(String googleSubjectId) { this.googleSubjectId = googleSubjectId; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
