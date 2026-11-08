package pl.kapj.dent.radwanskadent.dto;

import pl.kapj.dent.radwanskadent.model.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AppointmentTableDto {

    private Long id;
    private LocalDateTime dateTime;
    private Long patientId;
    private String patientFirstName;
    private String patientLastName;
    private String dentistFirstName;
    private String dentistLastName;
    private String roomName;
    private AppointmentStatus status;
    private boolean paid;
    private BigDecimal amount;
    private List<Long> recipeIds;

    public AppointmentTableDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientFirstName() {
        return patientFirstName;
    }

    public void setPatientFirstName(String patientFirstName) {
        this.patientFirstName = patientFirstName;
    }

    public String getPatientLastName() {
        return patientLastName;
    }

    public void setPatientLastName(String patientLastName) {
        this.patientLastName = patientLastName;
    }

    public String getDentistFirstName() {
        return dentistFirstName;
    }

    public void setDentistFirstName(String dentistFirstName) {
        this.dentistFirstName = dentistFirstName;
    }

    public String getDentistLastName() {
        return dentistLastName;
    }

    public void setDentistLastName(String dentistLastName) {
        this.dentistLastName = dentistLastName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Long> getRecipeIds() {
        return recipeIds;
    }

    public void setRecipeIds(List<Long> recipeIds) {
        this.recipeIds = recipeIds;
    }
}
