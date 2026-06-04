package rsis.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rsis.model.Appointment;
import rsis.repository.AppointmentRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
public class LaporanBulananService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    // PDF generation temporarily disabled due to dependency issues
    public byte[] generatePDF(int bulan, int tahun) throws IOException {
        throw new UnsupportedOperationException("PDF generation temporarily disabled. Use CSV export instead.");
    }

    public byte[] generateCSV(int bulan, int tahun) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .builder()
                .setHeader("ID Appointment", "Tanggal Booking", "Pasien", "Dokter", "Status")
                .build());

        List<Appointment> appointments = appointmentRepository.findAll();
        for (Appointment apt : appointments) {
            csvPrinter.printRecord(
                    apt.getIdAppointment(),
                    apt.getTanggalBooking(),
                    apt.getPasien() != null ? apt.getPasien().getIdPasien() : "N/A",
                    apt.getDokter() != null ? apt.getDokter().getIdDokter() : "N/A",
                    apt.getStatus());
        }

        csvPrinter.flush();
        csvPrinter.close();
        writer.close();

        return outputStream.toByteArray();
    }
}
