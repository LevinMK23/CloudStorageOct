package dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class DbConn {
    private static DbConn instance;
    private Connection conn;


    private DbConn() throws SQLException {
        ResourceBundle rb = ResourceBundle.getBundle("db");
        String host = rb.getString("host");
        String port = rb.getString("port");
        String db = rb.getString("db");
        String user = rb.getString("user");
        String password = rb.getString("password");

        String jdbcURL = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", host, port, db);
        conn = DriverManager.getConnection(jdbcURL, user, password);
    }

    public static DbConn getInstance() {
        if (instance == null) {
            try {
                instance = new DbConn();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Connection connection() {
        return conn;
    }
}
