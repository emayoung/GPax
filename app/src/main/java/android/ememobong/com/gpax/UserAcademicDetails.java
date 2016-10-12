package android.ememobong.com.gpax;

/**
 * Created by ememobong on 05/08/2016.
 */
public class UserAcademicDetails {

    private String username;
    private String name;
    private String faculty;
    private String department;
    private String regnumber;
    private char gender;
    private int level;

    public UserAcademicDetails(){

    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegNumber() {
        return regnumber;
    }

    public void setRegNumber(String regNumber) {
        this.regnumber = regNumber;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }


}
