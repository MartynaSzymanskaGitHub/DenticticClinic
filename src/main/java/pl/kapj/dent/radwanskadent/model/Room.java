package pl.kapj.dent.radwanskadent.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 150)
    private String description;

    @OneToMany(mappedBy = "room")
    private List<Appointment> appointments;

    @Column(name = "isUnderRenovation")
    private boolean underRenovation;

    private LocalDateTime renovationTimeStart;

    private LocalDateTime renovationTimeEnd;

    public boolean isUnderRenovation() {
        return underRenovation;
    }

    public boolean isCurrentlyUnderRenovation() {
        if (!underRenovation) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (renovationTimeEnd != null && now.isAfter(renovationTimeEnd)) {
            return false;
        }
        if (renovationTimeStart != null && now.isBefore(renovationTimeStart)) {
            return false;
        }
        return true;
    }

    public boolean isPlannedRenovation() {
        if (!underRenovation || renovationTimeStart == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(renovationTimeStart)
                && (renovationTimeEnd == null || now.isBefore(renovationTimeEnd));
    }

    public void setUnderRenovation(boolean underRenovation) {
        this.underRenovation = underRenovation;
    }

    public LocalDateTime getRenovationTimeStart() {
        return renovationTimeStart;
    }

    public void setRenovationTimeStart(LocalDateTime renovationTimeStart) {
        this.renovationTimeStart = renovationTimeStart;
    }

    public LocalDateTime getRenovationTimeEnd() {
        return renovationTimeEnd;
    }

    public void setRenovationTimeEnd(LocalDateTime renovationTimeEnd) {
        this.renovationTimeEnd = renovationTimeEnd;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String roomNumber) {
        this.name = roomNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}
