package pl.kapj.dent.radwanskadent.model;

public enum AppointmentStatus {
    PENDING_PAYMENT, // po utworzeniu wizyty
    CONFIRMED, // po oplaceniu
    CANCELLED,
    COMPLETED // automatycznie po zakonczeniu
}
