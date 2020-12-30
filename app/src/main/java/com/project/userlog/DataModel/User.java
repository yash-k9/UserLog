package com.project.userlog.DataModel;


/*User POJO*/

public class User {
    private String email;
    private String time;
    private String location;

    public User() {
    }

    public User(String email, String time, String location) {
        this.email = email;
        this.time = time;
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
