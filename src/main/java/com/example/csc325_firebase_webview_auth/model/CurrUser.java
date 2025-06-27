package com.example.csc325_firebase_webview_auth.model;



/**
 * A simple utility class to hold the UID of the currently authenticated user.
 * This class uses a static variable to make the user's UID accessible globally
 * after a successful login or registration.
 *
 * This pattern is useful for tracking the logged-in user across different
 * views in a JavaFX application without passing data between controllers.
 */
public class CurrUser {

    // A static variable to store the UID of the active user.
    // Static fields are shared across all instances of the class,
    // making this a simple singleton-like data holder.
    private static String uid;

    /**
     * Sets the UID of the currently authenticated user.
     * This method should be called immediately after a user successfully
     * signs in or registers with Firebase Authentication.
     *
     * @param uid The unique user ID provided by Firebase Authentication.
     */
    public static void setUid(String uid) {
        CurrUser.uid = uid;
    }

    /**
     * Retrieves the UID of the currently authenticated user.
     * This method can be called from any controller to get the ID
     * of the user who is currently logged in.
     *
     * @return The user's UID, or null if no user is currently authenticated.
     */
    public static String getUid() {
        return uid;
    }
}