package com.sscp.pechostelmanagement;

public class Warden {
    String warden_name, warden_mobile, warden_password, warden_email;

    Warden(){}

    public Warden(String warden_name, String warden_mobile, String warden_password, String warden_email) {
        this.warden_name = warden_name;
        this.warden_mobile = warden_mobile;
        this.warden_password = warden_password;
        this.warden_email = warden_email;
    }

    public String getWarden_name() {
        return warden_name;
    }

    public void setWarden_name(String warden_name) {
        this.warden_name = warden_name;
    }

    public String getWarden_mobile() {
        return warden_mobile;
    }

    public void setWarden_mobile(String warden_mobile) {
        this.warden_mobile = warden_mobile;
    }

    public String getWarden_password() {
        return warden_password;
    }

    public void setWarden_password(String warden_password) {
        this.warden_password = warden_password;
    }

    public String getWarden_email() {
        return warden_email;
    }

    public void setWarden_email(String warden_email) {
        this.warden_email = warden_email;
    }
}
