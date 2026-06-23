package rsis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service untuk menangani upload dan delete file
 * Cross-cutting concern untuk file storage
 */
@Service
public class FileStorageService {
    
    private final String uploadDir = "uploads/dokter/";

    /**
     * Simpan file ke direktori upload
     * @param file MultipartFile yang akan disimpan
     * @param fileName Nama file yang diinginkan
     * @return Path lengkap file yang disimpan
     * @throws IOException Jika terjadi error saat menyimpan file
     */
    public String saveFile(MultipartFile file, String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath.toString();
    }

    /**
     * Hapus file dari direktori upload
     * @param filePath Path file yang akan dihapus
     * @throws IOException Jika terjadi error saat menghapus file
     */
    public void deleteFile(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
    }
}
