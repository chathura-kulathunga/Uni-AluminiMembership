package service;

import db.DatabaseConnector;

/**
 *
 * @author Fox C
 */
public class PaymentStatusUpdater {

    public static void runDailyUpdates() {
        deactivateMembersWithExpiredPayments();
        upgradeMembersToPermanent();
    }

    private static void deactivateMembersWithExpiredPayments() {
        try {
            String sql = """
                UPDATE member m
                JOIN payments pay ON pay.member_id = m.id
                JOIN payment_cycle py ON pay.payment_cycle_id = py.id
                JOIN payment_status ps ON py.payment_status_id = ps.id
                SET m.status_id = 2  -- 2 = Deactivated
                WHERE ps.name = 'Unpaid'
                AND pay.due_date < CURDATE()
            """;
            int updated = DatabaseConnector.executeUpdateWithParams(sql);
            System.out.println("Deactivation task done. Updated rows: " + updated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void upgradeMembersToPermanent() {
        try {
            String sql = """
                UPDATE member m
                JOIN payments pay ON pay.member_id = m.id
                SET m.member_type_id = 2  -- 2 = Permanent
                WHERE pay.payment_cycle_id = 5  -- 3rd year paid
                AND pay.paid_date IS NOT NULL
                AND pay.paid_date <= (CURDATE() - INTERVAL 12 MONTH)
                AND m.member_type_id != 2
            """;
            int updated = DatabaseConnector.executeUpdateWithParams(sql);
            System.out.println("Upgrade to permanent task done. Updated rows: " + updated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}