package pl.kapj.dent.radwanskadent.mapper;

import org.springframework.stereotype.Component;
import pl.kapj.dent.radwanskadent.dto.CreateRoomDto;
import pl.kapj.dent.radwanskadent.dto.RoomTableDto;
import pl.kapj.dent.radwanskadent.model.Room;

@Component
public class RoomMapper {

    public Room createRoomDtoToEntity(CreateRoomDto createRoomDto) {
        Room room = new Room();
        room.setDescription(createRoomDto.getDescription());
        room.setName(createRoomDto.getName());
        if (createRoomDto.getRenovationTimeStart() != null && createRoomDto.getRenovationTimeEnd() != null) {
            room.setRenovationTimeStart(createRoomDto.getRenovationTimeStart());
            room.setRenovationTimeEnd(createRoomDto.getRenovationTimeEnd());
            room.setUnderRenovation(Boolean.TRUE);
        } else {
            room.setRenovationTimeStart(null);
            room.setRenovationTimeEnd(null);
            room.setUnderRenovation(Boolean.FALSE);
        }
        return room;
    }

    public RoomTableDto toTableDto(Room room) {
        if (room == null) {
            return null;
        }
        RoomTableDto dto = new RoomTableDto();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setDescription(room.getDescription());
        dto.setUnderRenovation(room.isUnderRenovation());
        dto.setCurrentlyUnderRenovation(room.isCurrentlyUnderRenovation());
        dto.setPlannedRenovation(room.isPlannedRenovation());
        dto.setRenovationTimeStart(room.getRenovationTimeStart());
        dto.setRenovationTimeEnd(room.getRenovationTimeEnd());
        return dto;
    }
}
