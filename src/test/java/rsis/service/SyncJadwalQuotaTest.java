package rsis.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0",
    "spring.datasource.username=postgres.ndfzwfdofcnxkkibgare",
    "spring.datasource.password=Rumahsakitintelligentsystem",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=none"
})
class SyncJadwalQuotaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void syncQuotas() {
        System.out.println("=== SYNCING JADWAL QUOTAS ===");
        try {
            int rowsUpdated = jdbcTemplate.update(
                "UPDATE jadwal_praktik j " +
                "SET sisa_kuota = j.kuota - COALESCE( " +
                "    (SELECT COUNT(*) " +
                "     FROM appointment a " +
                "     WHERE a.id_jadwal = j.id_jadwal " +
                "       AND a.status IN ('MENUNGGU', 'DIKONFIRMASI', 'SELESAI') " +
                "    ), 0 " +
                ");"
            );
            System.out.println("Successfully updated sisa_kuota for " + rowsUpdated + " schedules.");
            
            // Set status to PENUH for schedules where sisa_kuota <= 0 (unless they are set to LIBUR)
            int statusUpdated = jdbcTemplate.update(
                "UPDATE jadwal_praktik SET status_ketersediaan = 'PENUH' WHERE sisa_kuota <= 0 AND status_ketersediaan != 'LIBUR';"
            );
            System.out.println("Set status to PENUH for " + statusUpdated + " schedules.");
            
            // Restored status to TERSEDIA for schedules where sisa_kuota > 0 but currently set to PENUH
            int statusRestored = jdbcTemplate.update(
                "UPDATE jadwal_praktik SET status_ketersediaan = 'TERSEDIA' WHERE sisa_kuota > 0 AND status_ketersediaan = 'PENUH';"
            );
            System.out.println("Restored status to TERSEDIA for " + statusRestored + " schedules.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=============================");
    }
}
