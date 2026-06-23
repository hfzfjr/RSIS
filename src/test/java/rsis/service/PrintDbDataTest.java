package rsis.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rsis.model.Appointment;
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
    private AppointmentRepository appointmentRepository;

    @Autowired
    private rsis.repository.UserRepository userRepository;

    @Test
    void printAppointments() {
        System.out.println("=== APPOINTMENTS IN DB ===");
        try {
            List<Appointment> list = appointmentRepository.findAll();
            System.out.println("Total appointments count: " + list.size());
            for (Appointment app : list) {
                System.out.println("ID: " + app.getIdAppointment() 
                    + ", Date: " + app.getTanggalBooking() 
                    + ", Status: " + app.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("==========================");
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
