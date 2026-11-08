package pl.kapj.dent.radwanskadent.dto;

public class RoomUsageStatsDto {

    public RoomUsageStatsDto(Long roomNumber, Long number) {
        this.roomNumber = roomNumber;
        this.appointmentNumber = number;
    }

    private Long roomNumber;

    private Long appointmentNumber;

    public Long getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Long roomNumber) {
        this.roomNumber = roomNumber;
    }

    public long getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(Long appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

}
