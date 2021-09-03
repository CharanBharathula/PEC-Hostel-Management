package com.sscp.pechostelmanagement;

public class Admin {
    String Name, Email, Mobile, Pwd, occupation, description;

    public Admin(){}
    public Admin(String name, String email, String mobile, String pwd, String occupation, String description) {
        Name = name;
        Email = email;
        Mobile = mobile;
        Pwd = pwd;
        this.occupation = occupation;
        this.description = description;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getPwd() {
        return Pwd;
    }

    public void setPwd(String pwd) {
        Pwd = pwd;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
