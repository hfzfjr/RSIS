package rsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Appointment;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

        List<Appointment> findByUser_IdUserOrderByTanggalBookingDesc(String idUser);

        List<Appointment> findByJadwal_Dokter_IdUser(String idUser);

        List<Appointment> findByJadwal_IdJadwal(String idJadwal);

        List<Appointment> findByStatus(String status);

        List<Appointment> findByUser_IdUserAndStatus(String idUser, String status);

        List<Appointment> findByJadwal_Dokter_IdUserAndStatus(String idUser, String status);

        List<Appointment> findByJadwal_IdJadwalAndStatusIn(String idJadwal, List<String> statuses);

        @Query("SELECT a FROM Appointment a WHERE a.jadwal.dokter.idUser = :dokterId AND a.status = 'MENUNGGU' ORDER BY a.tanggalBooking ASC")
        List<Appointment> findPendingAppointmentsByDokterId(@Param("dokterId") String dokterId);

        @Query("SELECT a FROM Appointment a WHERE a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        List<Appointment> findByBulanDanTahun(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'DIKONFIRMASI' AND a.tanggalBooking = :tanggal")
        Long countConfirmedAppointmentsByDate(@Param("tanggal") LocalDate tanggal);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        Long countAllByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'DIKONFIRMASI' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        Long countConfirmedAppointmentsByMonth(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'DIBATALKAN' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        Long countCanceledAppointmentsByMonth(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT a.jadwal.dokter.idUser, COUNT(a) FROM Appointment a WHERE a.status = 'DIKONFIRMASI' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate AND a.jadwal IS NOT NULL AND a.jadwal.dokter IS NOT NULL GROUP BY a.jadwal.dokter.idUser ORDER BY COUNT(a) DESC")
        List<Object[]> findBusiestDokterByMonth(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT a.tanggalBooking, COUNT(a) FROM Appointment a WHERE a.status = 'DIKONFIRMASI' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate GROUP BY a.tanggalBooking ORDER BY a.tanggalBooking")
        List<Object[]> findPatientsPerDayByMonth(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT a.tanggalBooking, COUNT(a) FROM Appointment a WHERE a.tanggalBooking >= :startDate AND a.tanggalBooking <= :endDate GROUP BY a.tanggalBooking ORDER BY a.tanggalBooking")
        List<Object[]> countByTanggalBookingBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        Long countByStatus(String status);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.tanggalBooking = :tanggal")
        Long countTotalAppointmentsByDate(@Param("tanggal") LocalDate tanggal);
}
