package pl.kapj.dent.radwanskadent.mapper;

import org.springframework.stereotype.Component;
import pl.kapj.dent.radwanskadent.dto.CreateScheduleDoctorDto;
import pl.kapj.dent.radwanskadent.dto.DoctorScheduleTableDto;
import pl.kapj.dent.radwanskadent.model.DoctorSchedule;
import pl.kapj.dent.radwanskadent.model.User;

@Component
public class ScheduleDoctorMapper {

    public DoctorSchedule toDoctorScheduleEntity(CreateScheduleDoctorDto scheduleDoctorDto, User user) {
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        doctorSchedule.setUser(user);
        doctorSchedule.setEndWorkDayHour(scheduleDoctorDto.getEndDate());
        doctorSchedule.setStartWorkDayHour(scheduleDoctorDto.getStartDate());
        return doctorSchedule;
    }

    public DoctorScheduleTableDto toTableDto(DoctorSchedule schedule) {
        if (schedule == null) {
            return null;
        }
        DoctorScheduleTableDto dto = new DoctorScheduleTableDto();
        dto.setId(schedule.getId());
        dto.setStartWorkDayHour(schedule.getStartWorkDayHour());
        dto.setEndWorkDayHour(schedule.getEndWorkDayHour());
        if (schedule.getUser() != null) {
            dto.setDoctorFullName(schedule.getUser().getFirstName() + " " + schedule.getUser().getLastName());
        }
        return dto;
    }
}
