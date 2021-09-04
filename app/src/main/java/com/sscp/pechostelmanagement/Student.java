package com.sscp.pechostelmanagement;

public class Student {
    String userid, studentname, email, password, mobile, imageurl, branch, year, semester, room_no, roll_no;

    Student(){}

    public Student(String userid, String name, String email, String pass, String mobile,
                   String imageurl, String branch, String year, String sem, String roomNo, String rollNo) {
        this.userid = userid;
        this.studentname = name;
        this.email = email;
        this.password = pass;
        this.mobile = mobile;
        this.imageurl = imageurl;
        this.branch = branch;
        this.year = year;
        this.semester = sem;
        this.room_no = roomNo;
        this.roll_no = rollNo;
    }

    public String getRollNo() {
        return roll_no;
    }

    public void setRollNo(String rollNo) {
        this.roll_no = rollNo;
    }

    public String getRoomNo() {
        return room_no;
    }

    public void setRoomNo(String roomNo) {
        this.room_no = roomNo;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSem() {
        return semester;
    }

    public void setSem(String sem) {
        this.semester = sem;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return studentname;
    }

    public void setName(String name) {
        this.studentname = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return password;
    }

    public void setPass(String pass) {
        this.password = pass;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
