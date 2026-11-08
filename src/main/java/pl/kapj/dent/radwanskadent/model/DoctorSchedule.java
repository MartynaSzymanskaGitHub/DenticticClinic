package pl.kapj.dent.radwanskadent.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table
public class DoctorSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDateTime startWorkDayHour;

    @NotNull
    private LocalDateTime endWorkDayHour;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartWorkDayHour() {
        return startWorkDayHour;
    }

    public void setStartWorkDayHour(LocalDateTime startWorkDayHour) {
        this.startWorkDayHour = startWorkDayHour;
    }

    public LocalDateTime getEndWorkDayHour() {
        return endWorkDayHour;
    }

    public void setEndWorkDayHour(LocalDateTime endWorkDayHour) {
        this.endWorkDayHour = endWorkDayHour;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
