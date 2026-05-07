package rsis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import rsis.model.interfaces.INotifiable;

@Entity
@Table(name = "pasien")
public class Pasien extends User implements INotifiable {
    // Inherits id_user as @Id from User MappedSuperclass
}
