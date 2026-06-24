package rsis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import rsis.dto.BusiestDoctorDTO;
import rsis.dto.VisitStatistics;
import rsis.model.Dokter;
import rsis.repository.AppointmentRepository;
import rsis.repository.DokterRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminRSService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DokterRepository dokterRepository;

    @Autowired
    private PoliService poliService;

    @Autowired
    private JadwalPraktikService jadwalPraktikService;

    // ====================
    // Statistik Methods (as per class diagram AdminRS)
    // ====================
    public Long getTotalPasienHariIni() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.countConfirmedAppointmentsByDate(today);
    }

    public Long getTotalPasienBulanIni() {
        LocalDate today = LocalDate.now();
        return getTotalPasienBulanIni(today.getMonthValue(), today.getYear());
    }

    public Long getTotalPasienBulanIni(int bulan, int tahun) {
        YearMonth month = YearMonth.of(tahun, bulan);
        return appointmentRepository.countConfirmedAppointmentsByMonth(month.atDay(1), month.plusMonths(1).atDay(1));
    }

    public String getDokterTersibuk() {
        LocalDate today = LocalDate.now();
        return getDokterTersibuk(today.getMonthValue(), today.getYear());
    }

    public String getDokterTersibuk(int bulan, int tahun) {
        YearMonth month = YearMonth.of(tahun, bulan);
        List<Object[]> results = appointmentRepository.findBusiestDoktersByStatusSelesaiAndMonth(month.atDay(1),
                month.atEndOfMonth(), PageRequest.of(0, 5));
        if (results.isEmpty()) {
            return "N/A";
        }
        Object[] result = results.get(0);
        return (String) result[1];
    }

    public List<BusiestDoctorDTO> getBusiestDoctorsOfMonth(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Object[]> results = appointmentRepository.findBusiestDoktersByStatusSelesaiAndMonth(
                startDate, endDate, PageRequest.of(0, 5));

        List<BusiestDoctorDTO> list = new java.util.ArrayList<>();
        if (results.isEmpty()) {
            return list;
        }

        long maxCount = 0;
        for (Object[] result : results) {
            long count = ((Number) result[2]).longValue();
            if (count > maxCount) {
                maxCount = count;
            }
        }

        for (Object[] result : results) {
            String dokterId = (String) result[0];
            String dokterNama = (String) result[1];
            long count = ((Number) result[2]).longValue();
            int percentage = maxCount > 0 ? (int) ((count * 100) / maxCount) : 0;

            Optional<Dokter> dokterOpt = dokterRepository.findById(dokterId);
            if (dokterOpt.isPresent()) {
                Dokter d = dokterOpt.get();
                d.setNama(dokterNama);
                list.add(new BusiestDoctorDTO(d, count, percentage));
            }
        }

        return list;
    }

    public Map<String, Long> getPasienPerHari() {
        LocalDate today = LocalDate.now();
        return getPasienPerHari(today.getMonthValue(), today.getYear());
    }

    public Map<String, Long> getPasienPerHari(int bulan, int tahun) {
        YearMonth month = YearMonth.of(tahun, bulan);
        List<Object[]> results = appointmentRepository.findPatientsPerDayByMonth(month.atDay(1),
                month.plusMonths(1).atDay(1));
        Map<String, Long> pasienPerHari = new HashMap<>();
        for (Object[] result : results) {
            String date = result[0].toString();
            Long count = ((Number) result[1]).longValue();
            pasienPerHari.put(date, count);
        }
        return pasienPerHari;
    }

    public Long getTotalDokter() {
        return dokterRepository.count();
    }

    public Long getTotalPoli() {
        return poliService.getTotalPoli();
    }

    public Map<String, Long> getJadwalStatistics() {
        List<rsis.model.JadwalPraktik> allJadwal = jadwalPraktikService.getAllJadwal();
        Map<String, Long> stats = new HashMap<>();
        stats.put("tersedia", allJadwal.stream()
                .filter(j -> "TERSEDIA".equals(j.getStatusKetersediaan())).count());
        stats.put("penuh", allJadwal.stream()
                .filter(j -> "PENUH".equals(j.getStatusKetersediaan())).count());
        stats.put("libur", allJadwal.stream()
                .filter(j -> "LIBUR".equals(j.getStatusKetersediaan())).count());
        return stats;
    }

    private String translateDayToIndonesian(String englishDay) {
        switch (englishDay.toUpperCase()) {
            case "MONDAY":
                return "Senin";
            case "TUESDAY":
                return "Selasa";
            case "WEDNESDAY":
                return "Rabu";
            case "THURSDAY":
                return "Kamis";
            case "FRIDAY":
                return "Jumat";
            case "SATURDAY":
                return "Sabtu";
            case "SUNDAY":
                return "Minggu";
            default:
                return englishDay;
        }
    }

    public List<VisitStatistics> getStatsByMonthAndYear(int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Object[]> results = appointmentRepository.countByTanggalBookingBetween(startDate, endDate);

        Map<LocalDate, Long> dateCountMap = new HashMap<>();
        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            Long count = ((Number) result[1]).longValue();
            dateCountMap.put(date, count);
        }

        long maxCount = 0;
        for (Long count : dateCountMap.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
        }

        List<VisitStatistics> stats = new java.util.ArrayList<>();
        int daysInMonth = startDate.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            long count = dateCountMap.getOrDefault(date, 0L);
            int percentage = maxCount > 0 ? (int) ((count * 100) / maxCount) : 0;
            stats.add(new VisitStatistics(String.valueOf(day), count, percentage));
        }

        return stats;
    }

    public List<VisitStatistics> getWeeklyStats() {
        // Get appointments for the last 7 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<Object[]> results = appointmentRepository.countByTanggalBookingBetween(startDate, endDate);
        List<VisitStatistics> stats = new java.util.ArrayList<>();

        // Find maximum count for percentage calculation
        long maxCount = 0;
        for (Object[] result : results) {
            Long count = ((Number) result[1]).longValue();
            if (count > maxCount) {
                maxCount = count;
            }
        }

        // Create statistics for each day
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            String dayName = date.getDayOfWeek().toString();
            String label = translateDayToIndonesian(dayName);

            long count = 0;
            for (Object[] result : results) {
                LocalDate resultDate = (LocalDate) result[0];
                if (resultDate.equals(date)) {
                    count = ((Number) result[1]).longValue();
                    break;
                }
            }

            int percentage = maxCount > 0 ? (int) ((count * 100) / maxCount) : 0;
            stats.add(new VisitStatistics(label, count, percentage));
        }

        return stats;
    }

    public Long getTotalAppointmentHariIni() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.countTotalAppointmentsByDate(today);
    }

    public Long getAppointmentPending() {
        return appointmentRepository.countByStatus("MENUNGGU");
    }

    public List<VisitStatistics> getMonthlyStats() {
        // Get appointments for the last 4 weeks
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(3).with(java.time.DayOfWeek.MONDAY);

        List<Object[]> results = appointmentRepository.countByTanggalBookingBetween(startDate, endDate);
        List<VisitStatistics> stats = new java.util.ArrayList<>();

        // Group by week
        Map<Integer, Long> weeklyCounts = new java.util.HashMap<>();
        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            Long count = ((Number) result[1]).longValue();
            int weekNumber = date.get(java.time.temporal.WeekFields.ISO.weekOfYear());
            weeklyCounts.put(weekNumber, weeklyCounts.getOrDefault(weekNumber, 0L) + count);
        }

        // Find maximum count for percentage calculation
        long maxCount = weeklyCounts.values().stream().max(Long::compareTo).orElse(0L);

        // Create statistics for each week
        for (int i = 0; i < 4; i++) {
            LocalDate weekStart = startDate.plusWeeks(i);
            int weekNumber = weekStart.get(java.time.temporal.WeekFields.ISO.weekOfYear());
            String label = "Minggu " + (i + 1);

            long count = weeklyCounts.getOrDefault(weekNumber, 0L);
            int percentage = maxCount > 0 ? (int) ((count * 100) / maxCount) : 0;
            stats.add(new VisitStatistics(label, count, percentage));
        }

        return stats;
    }

    public Long getScheduledDoctorsCountByDate(LocalDate date) {
        return appointmentRepository.countDistinctDoctorsByTanggalBooking(date);
    }

}