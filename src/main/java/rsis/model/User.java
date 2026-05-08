package rsis.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import rsis.model.interfaces.INotifiable;

@MappedSuperclass
public abstract class User implements INotifiable {
    // Only id_user is common (as FK in subclass tables, PK in users table)
    @Column(name = "id_user", nullable = false, length = 32)
    private String idUser;

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    @Override
    public void terimaNotifikasi(Notifikasi notif) {
        // Default implementation - to be overridden if needed
    }

    // Abstract methods - implemented by subclasses that have these fields
    public abstract String getEmail();

    public abstract String getNama();

    public abstract String getPassword();

    public abstract String getNomorHp();

    public abstract String getRole();

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
