package pl.kapj.dent.radwanskadent.service;

import pl.kapj.dent.radwanskadent.dto.CreateRoomDto;
import pl.kapj.dent.radwanskadent.dto.RoomTableDto;
import pl.kapj.dent.radwanskadent.dto.RoomUsageStatsDto;
import pl.kapj.dent.radwanskadent.model.Appointment;
import pl.kapj.dent.radwanskadent.model.Room;

import java.time.LocalDateTime;
import java.util.List;

public interface IRoomService {

    List<Room> getAllRooms();

    List<RoomTableDto> getAllRoomsTableDto();

    void createRoom(CreateRoomDto room);

    void updateRoom(Long roomId, CreateRoomDto roomDto);

    List<RoomUsageStatsDto> getAllRoomsUsageStats();

    Room getAvailableRoomWithLockBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    Room findAvailableRoomFromList(LocalDateTime startDate, LocalDateTime endDate, List<Room> allRooms, List<Appointment> dayAppointments);
}
