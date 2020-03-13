import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PortalConnectionTest {
    String path = System.getProperty("user.home")
            + java.io.File.separator + "IdeaProjects"
            + java.io.File.separator + "Database"
            + java.io.File.separator + "Test"
            + java.io.File.separator + "Resource";


    @org.junit.jupiter.api.Test
    void register() throws SQLException, ClassNotFoundException {
        PortalConnection portalConnection=new PortalConnection();
        portalConnection.register("1111111111", "CCC111");
        portalConnection.getInfo("1111111111");

    }

    @Test
    void unregister() throws SQLException, ClassNotFoundException{
        PortalConnection portalConnection=new PortalConnection();
        portalConnection.unregister("1111111111", "CCC111");
    }

    @Test
    void writeJSON() throws SQLException, ClassNotFoundException{
        PortalConnection portalConnection=new PortalConnection();
        String path_bin = path + java.io.File.separator + "test.json";
        portalConnection.WriteJSON(path_bin, "2222222222");
    }
}