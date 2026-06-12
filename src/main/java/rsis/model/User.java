package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.time.Instant;

import rsis.model.interfaces.INotifiable;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User implements INotifiable {

    @Id
    @Column(name = "id_user", nullable = false, length = 32)
    private String idUser;

    @Column(name = "nama", nullable = false)
    private String nama;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nomor_hp")
    private String nomorHp;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public User() {
        this.createdAt = Instant.now();
    }

    public User(String idUser, String nama, String email, String password, String role) {
        this();
        this.idUser = idUser;
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNomorHp() {
        return nomorHp;
    }

    public void setNomorHp(String nomorHp) {
        this.nomorHp = nomorHp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public void terimaNotifikasi(Notifikasi notif) {
        // Default implementation - to be overridden if needed
    }

    public void login() {
        // Placeholder - auth handled by Spring Security
    }

    public void logout() {
        // Placeholder - auth handled by Spring Security
    }

    public String getId() {
        return this.idUser;
    }
}
