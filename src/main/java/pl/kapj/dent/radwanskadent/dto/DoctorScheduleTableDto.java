package pl.kapj.dent.radwanskadent.dto;

import java.time.LocalDateTime;

public class DoctorScheduleTableDto {

    private Long id;
    private String doctorFullName;
    private LocalDateTime startWorkDayHour;
    private LocalDateTime endWorkDayHour;

    public DoctorScheduleTableDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDoctorFullName() {
        return doctorFullName;
    }

    public void setDoctorFullName(String doctorFullName) {
        this.doctorFullName = doctorFullName;
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
}
