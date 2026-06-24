package rsis.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rsis.dto.BusiestDoctorDTO;
import rsis.dto.VisitStatistics;
import rsis.model.Appointment;
import rsis.repository.AppointmentRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LaporanBulananService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AdminRSService adminRSService;

    public Map<String, Object> getLaporanBulanan(int bulan, int tahun) {
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        String[] monthNames = { "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember" };
        String labelPeriode = monthNames[bulan - 1] + " " + tahun;

        // Get total appointments with DIKONFIRMASI and SELESAI status
        Long totalAppointment = appointmentRepository.countAppointmentsConfirmedAndCompletedByMonth(
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.plusMonths(1).atDay(1).atStartOfDay());

        // Get total unique patients with DIKONFIRMASI and SELESAI status
        Long totalPasien = appointmentRepository.countConfirmedAndCompletedByMonth(
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.plusMonths(1).atDay(1).atStartOfDay());

        // Get visit stats (patients per day)
        List<VisitStatistics> visitStats = adminRSService.getStatsByMonthAndYear(bulan, tahun);

        // Get busiest doctors
        List<BusiestDoctorDTO> busiestDoctors = adminRSService.getBusiestDoctorsOfMonth(bulan, tahun);

        Map<String, Object> result = new HashMap<>();
        result.put("totalPasien", totalPasien);
        result.put("totalAppointment", totalAppointment);
        result.put("labelPeriode", labelPeriode);
        result.put("visitStats", visitStats);
        result.put("busiestDoctors", busiestDoctors);

        return result;
    }

    public byte[] generatePDF(int bulan, int tahun) throws IOException {
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Appointment> appointments = appointmentRepository.findConfirmedAndCompletedByDateRange(startDate, endDate);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Title
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Laporan Bulanan - " + yearMonth.getMonth() + " " + tahun, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        // Summary
        Map<String, Object> laporanData = getLaporanBulanan(bulan, tahun);
        Paragraph summary = new Paragraph();
        summary.add(new Phrase("Total Pasien: " + laporanData.get("totalPasien") + "\n"));
        summary.add(new Phrase("Total Appointment: " + laporanData.get("totalAppointment") + "\n"));
        document.add(summary);
        document.add(new Paragraph(" "));

        // Table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.addCell("Tanggal");
        table.addCell("Nama Pasien");
        table.addCell("Nama Dokter");
        table.addCell("Spesialisasi");
        table.addCell("Poli");
        table.addCell("Status");

        for (Appointment apt : appointments) {
            table.addCell(apt.getTanggalBooking() != null ? apt.getTanggalBooking().toLocalDate().toString() : "-");
            table.addCell(apt.getUser() != null ? apt.getUser().getNama() : "-");
            table.addCell(apt.getJadwal() != null && apt.getJadwal().getDokter() != null
                    ? apt.getJadwal().getDokter().getNama()
                    : "-");
            table.addCell(apt.getJadwal() != null && apt.getJadwal().getDokter() != null
                    && apt.getJadwal().getDokter().getSpesialisasi() != null
                            ? apt.getJadwal().getDokter().getSpesialisasi().getNama()
                            : "-");
            table.addCell(apt.getJadwal() != null && apt.getJadwal().getDokter() != null
                    && apt.getJadwal().getDokter().getPoli() != null
                            ? apt.getJadwal().getDokter().getPoli().getNamaPoli()
                            : "-");
            table.addCell(apt.getStatus() != null ? apt.getStatus() : "-");
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generateCSV(int bulan, int tahun) throws IOException {
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Appointment> appointments = appointmentRepository.findConfirmedAndCompletedByDateRange(startDate, endDate);

        StringWriter writer = new StringWriter();
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .builder()
                .setHeader("Tanggal", "Nama Pasien", "Nama Dokter", "Spesialisasi", "Poli", "Status")
                .build());

        for (Appointment apt : appointments) {
            String tanggal = apt.getTanggalBooking() != null ? apt.getTanggalBooking().toLocalDate().toString() : "-";
            String namaPasien = apt.getUser() != null ? apt.getUser().getNama() : "-";
            String namaDokter = apt.getJadwal() != null && apt.getJadwal().getDokter() != null
                    ? apt.getJadwal().getDokter().getNama()
                    : "-";
            String spesialisasi = apt.getJadwal() != null && apt.getJadwal().getDokter() != null
                    && apt.getJadwal().getDokter().getSpesialisasi() != null
                            ? apt.getJadwal().getDokter().getSpesialisasi().getNama()
                            : "-";
            String poli = apt.getJadwal() != null && apt.getJadwal().getDokter() != null
                    && apt.getJadwal().getDokter().getPoli() != null
                            ? apt.getJadwal().getDokter().getPoli().getNamaPoli()
                            : "-";
            String status = apt.getStatus() != null ? apt.getStatus() : "-";

            csvPrinter.printRecord(tanggal, namaPasien, namaDokter, spesialisasi, poli, status);
        }

        csvPrinter.flush();
        csvPrinter.close();

        return writer.toString().getBytes();
    }
}
