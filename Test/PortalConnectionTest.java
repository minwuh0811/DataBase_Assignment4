import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PortalConnectionTest {

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
}