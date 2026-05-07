package rsis.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import rsis.model.interfaces.INotifiable;
import rsis.model.interfaces.ISchedulable;

@Entity
@Table(name = "dokter")
public class Dokter extends User implements INotifiable, ISchedulable {
}
