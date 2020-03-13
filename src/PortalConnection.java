
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*; // JDBC stuff.
import java.util.Properties;

public class PortalConnection {

    // For connecting to the portal database on your local machine
    static final String DATABASE = "jdbc:postgresql://localhost:5432/postgres";
    static final String USERNAME = "postgres";
    static final String PASSWORD = "w860314m";

    // For connecting to the chalmers database server (from inside chalmers)
    // static final String DATABASE = "jdbc:postgresql://ate.ita.chalmers.se/";
    // static final String USERNAME = "tda357_nnn";
    // static final String PASSWORD = "yourPasswordGoesHere";


    // This is the JDBC connection object you will be using in your methods.
    private Connection conn;
    private long idnr;
    private String code;

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public long getIdnr() {
        return idnr;
    }

    public void setIdnr(long idnr) {
        this.idnr = idnr;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public PortalConnection() throws SQLException, ClassNotFoundException {
        this(DATABASE, USERNAME, PASSWORD);
    }

    // Initializes the connection, no need to change anything here
    public PortalConnection(String db, String user, String pwd) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        conn = DriverManager.getConnection(db, props);
        setConn(conn);
    }


    // Register a student on a course, returns a tiny JSON document (as a String)
    public String register(String student, String courseCode) {
        try {
            String sql="INSERT INTO Registrations VALUES (" + Long.parseLong(student) + ", \'" + courseCode +"\');";
            getConn().createStatement().executeUpdate(sql);
      // placeholder, remove along with this comment. 
            return "{\"success\":true}";
      // Here's a bit of useful code, use it or delete it 
      } catch (SQLException e) {
          return "{\"success\":false, \"error\":\""+getError(e)+"\"}";
       }
    }

    // Unregister a student from a course, returns a tiny JSON document (as a String)
    public String unregister(String student, String courseCode){
        setCode(courseCode);
        setIdnr(Long.parseLong(student));
        String sql1 = "select * from registrations where student= ? and course = ?"; //+ Long.parseLong(student) + " and course=\'" + courseCode +"\';";
        String sql2 = "delete from registrations where student= ? and course= ? "; //+ Long.parseLong(student) + " and course=\'" + courseCode + "\';";
        try(PreparedStatement ps1 = getConn().prepareStatement(sql1);
            PreparedStatement ps2 = getConn().prepareStatement(sql2);
        ) {
            ps1.setLong(1,getIdnr());
            ps1.setString(2, getCode());
            ResultSet resultSet= ps1.executeQuery();
            if (resultSet.next()) {
                ps2.setLong(1,getIdnr());
                ps2.setString(2, getCode());//= "delete from registrations where student=" + Long.parseLong(student) + " and course=\'" + courseCode + "\';";
                ps2.executeUpdate();
                // placeholder, remove along with this comment.
                return "{\"success\":true}";
            } else {
                return "{\"success\":false, \"error\": The student does not exist.}";
            }
            // Here's a bit of useful code, use it or delete it
       } catch (SQLException e) {
            return "{\"success\":false, \"error\":\""+getError(e)+"\"}";
        }


    }

    // Return a JSON document containing lots of information about a student, it should validate against the schema found in information_schema.json
    public String getInfo(String student) throws SQLException{
        try{
            String string=WriteJSON("test.json", student);
            return string;

        }   catch (SQLException e) {
            return "{\"success\":false, \"error\":\""+getError(e)+"\"}";
        }
    }
    // This is a hack to turn an SQLException into a JSON string error message. No need to change.
    public static String getError(SQLException e){
       String message = e.getMessage();
       int ix = message.indexOf('\n');
       if (ix > 0) message = message.substring(0, ix);
       message = message.replace("\"","\\\"");
       return message;
    }
    public String WriteJSON(String filename, String student) throws SQLException{
        JSONObject j = new JSONObject();
        String sql = "Select * from BasicInformation where idnr=" + Long.parseLong(student) + ";";
        Statement statement1=getConn().createStatement();
        Statement statement2=getConn().createStatement();
        Statement statement3=getConn().createStatement();
        Statement statement4=getConn().createStatement();
        ResultSet resultSetFromBasicInformation=statement1.executeQuery(sql);
        sql = "Select C.name, FC.course, FC.credits, FC.grade from FinishedCourses as FC left join Courses as C on C.code= FC.course where FC.student=" + Long.parseLong(student) + ";" ;
        ResultSet resultSetFinishedCourses=statement2.executeQuery(sql);
        sql ="Select T.name, T.course, T.status, W.position from (Select R.student as student, C.name as name, R.course as course, R.status as status from Registrations as R left join Courses as C on R.course=C.code) as T left join WaitingList as W on T.student=W.student and T.course = W.course where T.student=" + Long.parseLong(student) + ";";
        ResultSet resultSetRegistrations=statement3.executeQuery(sql);
        sql ="Select seminarCourses, mathCredits, researchCredits, totalCredits,  qualified from PathToGraduation where student=" + Long.parseLong(student) + ";";
        ResultSet resultSetPass=statement4.executeQuery(sql);
        //System.out.println(sql);
        if(resultSetFromBasicInformation.next()){
            j.put("student", ""+resultSetFromBasicInformation.getLong(1));
            j.put("name", resultSetFromBasicInformation.getString(2));
            j.put("login",resultSetFromBasicInformation.getString(3));
            j.put("program",resultSetFromBasicInformation.getString(4));
            j.put("branch",resultSetFromBasicInformation.getString(5));
        } else{
            return "{\"student\":\"does not exist :(\"}";}

        JSONArray jsonArrayFinishedCourses=new JSONArray();
        while(resultSetFinishedCourses.next()) {
            JSONObject json = new JSONObject();
            json.put("course", resultSetFinishedCourses.getString(1));
            json.put("code", resultSetFinishedCourses.getString(2));
            json.put("credits", resultSetFinishedCourses.getString(3));
            json.put("grade", resultSetFinishedCourses.getString(4));
            jsonArrayFinishedCourses.put(json);
        }
        j.put("finished",jsonArrayFinishedCourses);
        JSONArray jsonArrayRegistrations=new JSONArray();
        while(resultSetRegistrations.next()) {
            JSONObject json = new JSONObject();
            json.put("course", resultSetRegistrations.getString(1));
            json.put("code", resultSetRegistrations.getString(2));
            json.put("status", resultSetRegistrations.getString(3));
            json.put("position", resultSetRegistrations.getString(4)== null ? "null" : resultSetRegistrations.getString(4) );
            jsonArrayRegistrations.put(json);
        }
        j.put("registered",jsonArrayRegistrations);
        if(resultSetPass.next()) {
            j.put("seminarCourses", resultSetPass.getInt(1));
            j.put("mathCredits", resultSetPass.getInt(2));
            j.put("researchCredits", resultSetPass.getInt(3));
            j.put("totalCredits", resultSetPass.getInt(4));
            j.put("canGraduate", resultSetPass.getBoolean(5));
        }

        try(FileWriter fileWriter=new FileWriter(filename)) {
            TestPortal testPortal=new TestPortal();
            testPortal.prettyPrint(j.toString());
            fileWriter.write(j.toString());
            System.out.println();
        } catch (IOException e){
            e.printStackTrace();
        }
        return "{\"success\":true}";
    }


}