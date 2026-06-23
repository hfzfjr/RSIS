package rsis.model.enums;

/**
 * Enum untuk status appointment sesuai CHECK constraint di tabel appointment PostgreSQL
 */
public enum AppointmentStatus {
    MENUNGGU,
    DIKONFIRMASI,
    DITOLAK,
    DIBATALKAN,
    SELESAI
}
