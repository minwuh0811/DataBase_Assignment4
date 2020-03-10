
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
    private Statement stat;

    public Statement getStat() {
        return stat;
    }

    public void setStat(Statement stat) {
        this.stat = stat;
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
        setStat(conn.createStatement());
    }


    // Register a student on a course, returns a tiny JSON document (as a String)
    public String register(String student, String courseCode) {
        try {
            String sql="INSERT INTO Registrations VALUES (" + Long.parseLong(student) + ", \'" + courseCode +"\');";
            getStat().executeUpdate(sql);
      // placeholder, remove along with this comment. 
            return "{\"success\":true}";
      // Here's a bit of useful code, use it or delete it 
      } catch (SQLException e) {
          return "{\"success\":false, \"error\":\""+getError(e)+"\"}";
       }
    }

    // Unregister a student from a course, returns a tiny JSON document (as a String)
    public String unregister(String student, String courseCode){
        try {
            String sql="delete from registrations where student=" + Long.parseLong(student) + " and course=\'" + courseCode +"\';";
            getStat().executeUpdate(sql);
            // placeholder, remove along with this comment.
            return "{\"success\":true}";

            // Here's a bit of useful code, use it or delete it
       } catch (SQLException e) {
            return "{\"success\":false, \"error\":\""+getError(e)+"\"}";
        }


    }

    // Return a JSON document containing lots of information about a student, it should validate against the schema found in information_schema.json
    public String getInfo(String student) throws SQLException{
        
        try(PreparedStatement st = conn.prepareStatement(
            // replace this with something more useful
            "SELECT jsonb_build_object('student',idnr,'name',name) AS jsondata FROM BasicInformation WHERE idnr=?"
            );){
            
            st.setString(1, student);
            
            ResultSet rs = st.executeQuery();
            
            if(rs.next())
              return rs.getString("jsondata");
            else
              return "{\"student\":\"does not exist :(\"}"; 
            
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
}