package com.ams.app.model;

public class Student {
    private final String name;
    private final String regNumber;
    private final String officialEmail;
    private final String mobileNumber;

    public Student(String name, String regNumber, String officialEmail, String mobileNumber) {
        this.name = name;
        this.regNumber = regNumber;
        this.officialEmail = officialEmail;
        this.mobileNumber = mobileNumber;
    }

    public String getName() {
        return name;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public String getOfficialEmail() {
        return officialEmail;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", regNumber='" + regNumber + '\'' +
                ", officialEmail='" + officialEmail + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                '}';
    }
}
