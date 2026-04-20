package org.kumaran.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * One-time utility to grant schema permissions to leavepaldb user.
 * Run with: java -cp "target/classes;<classpath>"
 * org.kumaran.util.GrantDbPermissions
 * Requires Application Default Credentials (ADC) to be configured.
 */
public class GrantDbPermissions {
    public static void main(String[] args) throws Exception {
        String instanceConnectionName = "project-0168023a-2379-46dc-989:asia-south1:leavepal-db";
        String dbName = "leavepal";
        String jdbcUrl = "jdbc:postgresql:///" + dbName
                + "?cloudSqlInstance=" + instanceConnectionName
                + "&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
                + "&ipTypes=PUBLIC";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "leavepal@123");
        System.out.println("Connecting via Cloud SQL socket factory...");
        try (Connection conn = DriverManager.getConnection(jdbcUrl, props);
                Statement stmt = conn.createStatement()) {
            System.out.println("Connected. Granting permissions...");
            stmt.execute("GRANT ALL ON SCHEMA public TO leavepaldb");
            System.out.println("GRANT schema - OK");
            stmt.execute("GRANT ALL PRIVILEGES ON DATABASE leavepal TO leavepaldb");
            System.out.println("GRANT database - OK");
            stmt.execute("ALTER DATABASE leavepal OWNER TO leavepaldb");
            System.out.println("ALTER owner - OK");
            System.out.println("All permissions granted!");
        }
    }
}
