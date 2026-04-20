import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class RunMigration {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5434/leavepal?sslmode=disable";
        String user = "leavepaldb";
        String password = "leavepal@123";

        String[] sqls = {
                "ALTER TABLE leave_application ALTER COLUMN duration TYPE NUMERIC(4,1) USING duration::NUMERIC(4,1)",
                "ALTER TABLE leave_tracker ALTER COLUMN sick_leave_available TYPE NUMERIC(4,1) USING sick_leave_available::NUMERIC(4,1)",
                "ALTER TABLE leave_tracker ALTER COLUMN casual_leave_available TYPE NUMERIC(4,1) USING casual_leave_available::NUMERIC(4,1)",
                "ALTER TABLE leave_tracker ALTER COLUMN loss_of_pay_available TYPE NUMERIC(4,1) USING loss_of_pay_available::NUMERIC(4,1)",
                "ALTER TABLE leave_tracker ALTER COLUMN sick_leave_booked TYPE NUMERIC(4,1) USING sick_leave_booked::NUMERIC(4,1)",
                "ALTER TABLE leave_tracker ALTER COLUMN casual_leave_booked TYPE NUMERIC(4,1) USING casual_leave_booked::NUMERIC(4,1)",
                "ALTER TABLE leave_tracker ALTER COLUMN loss_of_pay_booked TYPE NUMERIC(4,1) USING loss_of_pay_booked::NUMERIC(4,1)"
        };

        try (Connection conn = DriverManager.getConnection(url, user, password);
                Statement st = conn.createStatement()) {
            System.out.println("Connected to leavepal database.");
            for (String sql : sqls) {
                try {
                    st.execute(sql);
                    System.out.println("OK: " + sql.substring(0, 60) + "...");
                } catch (Exception e) {
                    System.out.println("SKIP/ERR: " + e.getMessage());
                }
            }
            System.out.println("Migration complete.");
        }
    }
}
