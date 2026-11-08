package pl.kapj.dent.radwanskadent.dto;

import java.math.BigDecimal;
import java.util.List;

public class ClinicStatsDto {

    private BigDecimal totalRevenue;
    private BigDecimal paidRevenue;
    private long totalAppointments;
    private int totalUsers;
    private List<RoomUsageStatsDto> roomUsageStats;

    public ClinicStatsDto() {
    }

    public ClinicStatsDto(BigDecimal totalRevenue, BigDecimal paidRevenue, long totalAppointments, int totalUsers, List<RoomUsageStatsDto> roomUsageStats) {
        this.totalRevenue = totalRevenue;
        this.paidRevenue = paidRevenue;
        this.totalAppointments = totalAppointments;
        this.totalUsers = totalUsers;
        this.roomUsageStats = roomUsageStats;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getPaidRevenue() {
        return paidRevenue;
    }

    public void setPaidRevenue(BigDecimal paidRevenue) {
        this.paidRevenue = paidRevenue;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public List<RoomUsageStatsDto> getRoomUsageStats() {
        return roomUsageStats;
    }

    public void setRoomUsageStats(List<RoomUsageStatsDto> roomUsageStats) {
        this.roomUsageStats = roomUsageStats;
    }
}
