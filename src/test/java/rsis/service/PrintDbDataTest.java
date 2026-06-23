package rsis.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rsis.model.JadwalPraktik;
import rsis.model.Appointment;
import rsis.repository.JadwalPraktikRepository;
import rsis.repository.AppointmentRepository;

import java.util.List;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:postgresql://aws-1-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0",
    "spring.datasource.username=postgres.ndfzwfdofcnxkkibgare",
    "spring.datasource.password=Rumahsakitintelligentsystem",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=none"
})
class PrintDbDataTest {

    @Autowired
    private JadwalPraktikRepository jadwalPraktikRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private rsis.repository.UserRepository userRepository;

    @Test
    void testOutofSyncFullSchedules() {
        System.out.println("=== VERIFYING ALL SCHEDULE QUOTAS ===");
        try {
            List<JadwalPraktik> list = jadwalPraktikRepository.findAll();
            int totalChecked = 0;
            int outOfSync = 0;
            int fullyBooked = 0;
            for (JadwalPraktik jp : list) {
                if (Boolean.FALSE.equals(jp.getIsActive())) {
                    continue;
                }
                List<Appointment> apps = appointmentRepository.findByJadwal_IdJadwal(jp.getIdJadwal());
                long activeApps = apps.stream()
                    .filter(a -> "MENUNGGU".equals(a.getStatus()) || "DIKONFIRMASI".equals(a.getStatus()) || "SELESAI".equals(a.getStatus()))
                    .count();
                
                int expectedSisa = jp.getKuota() - (int) activeApps;
                if (expectedSisa < 0) expectedSisa = 0;
                
                if (jp.getSisaKuota() != expectedSisa) {
                    System.out.println("OUT OF SYNC! Jadwal ID: " + jp.getIdJadwal()
                        + " | Doc: " + (jp.getDokter() != null ? jp.getDokter().getNama() : "No Doc")
                        + " | Hari: " + jp.getHari()
                        + " | Kuota: " + jp.getKuota()
                        + " | Actual Sisa in DB: " + jp.getSisaKuota()
                        + " | Expected Sisa (Kuota - Active Apps): " + expectedSisa
                        + " | Active Apps: " + activeApps
                    );
                    outOfSync++;
                }
                
                if (activeApps >= jp.getKuota()) {
                    System.out.println("FULLY BOOKED SCHEDULE: ID: " + jp.getIdJadwal()
                        + " | Doc: " + (jp.getDokter() != null ? jp.getDokter().getNama() : "No Doc")
                        + " | Hari: " + jp.getHari()
                        + " | Kuota: " + jp.getKuota()
                        + " | Sisa: " + jp.getSisaKuota()
                        + " | Status: " + jp.getStatusKetersediaan()
                        + " | Active Apps: " + activeApps
                    );
                    fullyBooked++;
                }
                totalChecked++;
            }
            System.out.println("Total checked: " + totalChecked);
            System.out.println("Total out of sync: " + outOfSync);
            System.out.println("Total fully booked: " + fullyBooked);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("======================================");
    }

    @Test
    void printUsers() {
        System.out.println("=== USERS IN DB ===");
        try {
            userRepository.findAll().forEach(u -> {
                System.out.println("Email: " + u.getEmail() + ", Role: " + u.getRole());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("==========================");
    }
}

