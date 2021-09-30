package com.sscp.pechostelmanagement;

public class StudentClass {
    StudentClass(){}
    String branch;
    String email;
    String imageurl;
    String mobile;
    String password;
    String roll_no;
    String room_no;
    String semester;
    String studentname;
    String userid;
    String year;

    public StudentClass(String branch, String email, String imageurl, String mobile, String password, String roll_no, String room_no, String semester, String studentname, String userid, String year) {
        this.branch = branch;
        this.email = email;
        this.imageurl = imageurl;
        this.mobile = mobile;
        this.password = password;
        this.roll_no = roll_no;
        this.room_no = room_no;
        this.semester = semester;
        this.studentname = studentname;
        this.userid = userid;
        this.year = year;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoll_no() {
        return roll_no;
    }

    public void setRoll_no(String roll_no) {
        this.roll_no = roll_no;
    }

    public String getRoom_no() {
        return room_no;
    }

    public void setRoom_no(String room_no) {
        this.room_no = room_no;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStudentname() {
        return studentname;
    }

    public void setStudentname(String studentname) {
        this.studentname = studentname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
