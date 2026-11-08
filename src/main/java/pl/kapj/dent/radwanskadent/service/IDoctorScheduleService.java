package pl.kapj.dent.radwanskadent.service;

import org.springframework.data.domain.Page;
import pl.kapj.dent.radwanskadent.dto.CreateScheduleDoctorDto;
import pl.kapj.dent.radwanskadent.dto.DoctorScheduleTableDto;
import pl.kapj.dent.radwanskadent.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IDoctorScheduleService {

    Map<Long, List<LocalDateTime>> getAvailableSlotsForDate(LocalDateTime date, List<Long> treatmentsList, Long patientId);

    void addSchedule(CreateScheduleDoctorDto dto);

    Page<DoctorScheduleTableDto> getSchedulesTableDtoPageForDoctor(Long doctorId, int page, int size);

    Page<DoctorScheduleTableDto> getSchedulesTableDtoPage(int page, int size);

    void deleteSchedule(Long scheduleId, User requester);

    void updateSchedule(Long scheduleId, LocalDateTime startDate, LocalDateTime endDate, User requester);

    boolean isDoctorAvailable(Long doctorId, LocalDateTime date, LocalDateTime endDateTime);

    boolean isPatientAvailable(Long patientId, LocalDateTime date, LocalDateTime endDateTime);

}
