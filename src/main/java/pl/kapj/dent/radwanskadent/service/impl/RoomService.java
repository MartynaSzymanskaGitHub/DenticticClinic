package pl.kapj.dent.radwanskadent.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kapj.dent.radwanskadent.dto.CreateRoomDto;
import pl.kapj.dent.radwanskadent.dto.RoomTableDto;
import pl.kapj.dent.radwanskadent.dto.RoomUsageStatsDto;
import pl.kapj.dent.radwanskadent.mapper.RoomMapper;
import pl.kapj.dent.radwanskadent.model.Appointment;
import pl.kapj.dent.radwanskadent.model.AppointmentStatus;
import pl.kapj.dent.radwanskadent.model.Room;
import pl.kapj.dent.radwanskadent.repository.AppointmentRepository;
import pl.kapj.dent.radwanskadent.repository.RoomRepository;
import pl.kapj.dent.radwanskadent.service.IRoomService;
import pl.kapj.dent.radwanskadent.service.ITreatmentService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RoomService implements IRoomService {

    private static final int ROOM_BREAK_BETWEEN_APPOINTMENTS = 10;

    private final RoomRepository roomRepository;
    private final AppointmentRepository appointmentRepository;
    private final RoomMapper roomMapper;
    private final ITreatmentService treatmentService;

    public RoomService(RoomRepository roomRepository,
                       AppointmentRepository appointmentRepository,
                       RoomMapper roomMapper,
                       ITreatmentService treatmentService) {
        this.roomRepository = roomRepository;
        this.appointmentRepository = appointmentRepository;
        this.roomMapper = roomMapper;
        this.treatmentService = treatmentService;
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public List<RoomTableDto> getAllRoomsTableDto() {
        return getAllRooms().stream()
                .map(roomMapper::toTableDto)
                .toList();
    }

    @Override
    public void createRoom(CreateRoomDto room) {
        if (roomRepository.existsByNameIgnoreCase(room.getName())) {
            throw new EntityExistsException("admin.room.error.exists");
        }
        Room newRoom = roomMapper.createRoomDtoToEntity(room);
        roomRepository.save(newRoom);
    }

    @Override
    public void updateRoom(Long roomId, CreateRoomDto roomDto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("admin.room.error.notFound"));

        if (!room.getName().equalsIgnoreCase(roomDto.getName())
                && roomRepository.existsByNameIgnoreCase(roomDto.getName())) {
            throw new EntityExistsException("admin.room.error.exists");
        }

        if (roomDto.getRenovationTimeStart() != null && roomDto.getRenovationTimeEnd() != null) {
            LocalDateTime renovationStart = roomDto.getRenovationTimeStart();
            LocalDateTime renovationEnd = roomDto.getRenovationTimeEnd();

            List<Appointment> activeRoomAppointments = appointmentRepository.findAllByRoomIdAndStatusNotIn(
                    roomId,
                    List.of(AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED)
            );

            boolean hasConflictingAppointments = activeRoomAppointments.stream()
                    .anyMatch(app -> {
                        int duration = treatmentService.getAccumulatedTreatmentTime(app.getTreatments());
                        LocalDateTime appStart = app.getDateTime();
                        LocalDateTime appEndWithBreak = appStart.plusMinutes(duration + ROOM_BREAK_BETWEEN_APPOINTMENTS);
                        return renovationStart.isBefore(appEndWithBreak) && renovationEnd.isAfter(appStart);
                    });

            if (hasConflictingAppointments) {
                throw new IllegalStateException("room.error.renovationHasAppointments");
            }

            room.setRenovationTimeStart(renovationStart);
            room.setRenovationTimeEnd(renovationEnd);
            room.setUnderRenovation(true);
        } else {
            room.setRenovationTimeStart(null);
            room.setRenovationTimeEnd(null);
            room.setUnderRenovation(false);
        }

        room.setName(roomDto.getName());
        room.setDescription(roomDto.getDescription());
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomUsageStatsDto> getAllRoomsUsageStats() {
        List<Room> allRooms = getAllRooms();

        List<RoomUsageStatsDto> roomUsageStatsList = new ArrayList<>();
        for (Room room : allRooms) {
            long sum = appointmentRepository.countByRoomIdAndStatusNot(room.getId(), AppointmentStatus.CANCELLED);
            roomUsageStatsList.add(new RoomUsageStatsDto(room.getId(), sum));
        }
        return roomUsageStatsList;
    }

    @Override
    public Room getAvailableRoomWithLockBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<Room> allRooms = roomRepository.findAllWithLock();
        LocalDateTime startOfDay = startDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startDate.toLocalDate().atTime(LocalTime.MAX);
        List<Appointment> dayAppointments = appointmentRepository
                .findAppointmentByDateTimeBetweenAndStatusNotOrderByDateTimeAsc(startOfDay, endOfDay, AppointmentStatus.CANCELLED);

        return findAvailableRoomFromList(startDate, endDate, allRooms, dayAppointments);
    }

    @Override
    public Room findAvailableRoomFromList(LocalDateTime startDate, LocalDateTime endDate, List<Room> allRooms, List<Appointment> dayAppointments) {
        LocalDateTime requestedEndWithBreak = endDate.plusMinutes(ROOM_BREAK_BETWEEN_APPOINTMENTS);
        for (Room room : allRooms) {
            if (isRoomUnderRenovationBetween(room, startDate, requestedEndWithBreak)) {
                continue;
            }

            boolean roomBusy = dayAppointments.stream()
                    .filter(app -> app.getRoom().getId().equals(room.getId()))
                    .anyMatch(app -> isAppointmentColliding(app, startDate, requestedEndWithBreak));

            if (!roomBusy) {
                return room;
            }
        }
        return null;
    }

    private boolean isRoomUnderRenovationBetween(Room room, LocalDateTime startDate, LocalDateTime endDate) {
        if (!room.isUnderRenovation()) {
            return false;
        }
        if (room.getRenovationTimeStart() == null || room.getRenovationTimeEnd() == null) {
            return true;
        }
        return startDate.isBefore(room.getRenovationTimeEnd()) && endDate.isAfter(room.getRenovationTimeStart());
    }

    private boolean isAppointmentColliding(Appointment app, LocalDateTime startDate, LocalDateTime requestedEndWithBreak) {
        int duration = treatmentService.getAccumulatedTreatmentTime(app.getTreatments());
        LocalDateTime appEndWithBreak = app.getDateTime().plusMinutes(duration + ROOM_BREAK_BETWEEN_APPOINTMENTS);
        return startDate.isBefore(appEndWithBreak) && requestedEndWithBreak.isAfter(app.getDateTime());
    }

}
