package com.example.csc325_firebase_webview_auth.model;



/**
 * A class representing a User.
 * This can be used as a data model in a JavaFX application or for Firebase.
 */
public class User {

    private String id;
    private String name;
    private String email;

    /**
     * Default constructor for creating a User object.
     * Required for Firebase deserialization.
     */
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * Constructor to create a User object with all properties.
     *
     * @param id The unique ID of the user.
     * @param name The name of the user.
     * @param email The email address of the user.
     */
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // --- Getters and Setters ---

    /**
     * Gets the user's ID.
     * @return The user's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the user's ID.
     * @param id The ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the user's name.
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email.
     * @return The user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns a string representation of the User object.
     * @return A formatted string with the user's details.
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}