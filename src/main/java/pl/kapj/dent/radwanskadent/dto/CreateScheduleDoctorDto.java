package pl.kapj.dent.radwanskadent.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class CreateScheduleDoctorDto {

    @NotNull(message = "{registerForm.field.required}")
    private Long doctorId;

    @NotNull(message = "{registerForm.field.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @NotNull(message = "{registerForm.field.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @AssertTrue(message = "{schedule.error.endAfterStart}")
    public boolean isEndAfterStart() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "{schedule.error.sameDay}")
    public boolean isSameDay() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return startDate.toLocalDate().equals(endDate.toLocalDate());
    }

    @AssertTrue(message = "{schedule.error.datePast}")
    public boolean isStartNotInPast() {
        if (startDate == null) {
            return true;
        }
        return !startDate.isBefore(LocalDateTime.now());
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
