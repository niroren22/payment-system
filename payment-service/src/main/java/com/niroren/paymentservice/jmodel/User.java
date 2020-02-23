package com.niroren.paymentservice.jmodel;

import java.util.Objects;

public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private Gender gender;

    enum Gender {
        Male, Female,
    }

    public User(String userId, String firstName, String lastName, String email, Gender gender) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
    }

    // For serialization
    public User() {}

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
