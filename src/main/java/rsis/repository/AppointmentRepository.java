package rsis.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rsis.model.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        List<Appointment> findByBulanDanTahun(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'DIKONFIRMASI' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        Long countConfirmedAppointmentsByDate(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        Long countAllByMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'DIKONFIRMASI' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        Long countConfirmedAppointmentsByMonth(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'DIBATALKAN' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        Long countCanceledAppointmentsByMonth(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT a.jadwal.dokter.idUser, COUNT(a) FROM Appointment a WHERE a.status = 'DIKONFIRMASI' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate AND a.jadwal IS NOT NULL AND a.jadwal.dokter IS NOT NULL GROUP BY a.jadwal.dokter.idUser ORDER BY COUNT(a) DESC")
        List<Object[]> findBusiestDokterByMonth(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT a.jadwal.dokter.idUser, u.nama, COUNT(a) "
                        + "FROM Appointment a "
                        + "JOIN a.jadwal.dokter u "
                        + "WHERE a.status = 'SELESAI' "
                        + "AND a.jadwal IS NOT NULL "
                        + "AND a.jadwal.tanggal >= :startDate AND a.jadwal.tanggal <= :endDate "
                        + "GROUP BY a.jadwal.dokter.idUser, u.nama "
                        + "ORDER BY COUNT(a) DESC")
        List<Object[]> findBusiestDoktersByStatusSelesaiAndMonth(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        @Query("SELECT a.tanggalBooking, COUNT(a) FROM Appointment a WHERE a.status = 'DIKONFIRMASI' AND a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate GROUP BY a.tanggalBooking ORDER BY a.tanggalBooking")
        List<Object[]> findPatientsPerDayByMonth(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT a.tanggalBooking, COUNT(a) FROM Appointment a WHERE a.tanggalBooking >= :startDate AND a.tanggalBooking <= :endDate GROUP BY a.tanggalBooking ORDER BY a.tanggalBooking")
        List<Object[]> countByTanggalBookingBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        Long countByStatus(String status);

        @Query("SELECT COUNT(a) FROM Appointment a WHERE a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate")
        Long countTotalAppointmentsByDate(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(DISTINCT a.jadwal.dokter.idUser) FROM Appointment a WHERE a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate AND a.jadwal IS NOT NULL AND a.jadwal.dokter IS NOT NULL")
        Long countDistinctDoctorsByTanggalBooking(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT a FROM Appointment a WHERE a.tanggalBooking >= :startDate AND a.tanggalBooking < :endDate ORDER BY a.tanggalBooking")
        List<Appointment> findByTanggalBookingBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}
