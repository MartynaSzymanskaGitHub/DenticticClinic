package pl.kapj.dent.radwanskadent.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kapj.dent.radwanskadent.dto.CreateScheduleDoctorDto;
import pl.kapj.dent.radwanskadent.dto.DoctorScheduleTableDto;
import pl.kapj.dent.radwanskadent.mapper.ScheduleDoctorMapper;
import pl.kapj.dent.radwanskadent.model.*;
import pl.kapj.dent.radwanskadent.repository.AppointmentRepository;
import pl.kapj.dent.radwanskadent.repository.DoctorScheduleRepository;
import pl.kapj.dent.radwanskadent.service.IDoctorScheduleService;
import pl.kapj.dent.radwanskadent.service.IRoomService;
import pl.kapj.dent.radwanskadent.service.ITreatmentService;
import pl.kapj.dent.radwanskadent.service.IUserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service("doctorScheduleService")
@Transactional
public class DoctorScheduleService implements IDoctorScheduleService {

    private static final int DOCTOR_BREAK_BETWEEN_APPOINTMENT = 10;
    private static final int TIME_SLOT_INTERVAL_MINUTES = 15;

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final IUserService userService;
    private final ScheduleDoctorMapper scheduleDoctorMapper;
    private final ITreatmentService treatmentService;
    private final IRoomService roomService;

    public DoctorScheduleService(DoctorScheduleRepository doctorScheduleRepository,
                                 AppointmentRepository appointmentRepository,
                                 IUserService userService,
                                 ScheduleDoctorMapper scheduleDoctorMapper,
                                 ITreatmentService treatmentService,
                                 IRoomService roomService) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.scheduleDoctorMapper = scheduleDoctorMapper;
        this.treatmentService = treatmentService;
        this.roomService = roomService;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<LocalDateTime>> getAvailableSlotsForDate(LocalDateTime date, List<Long> treatmentsList, Long patientId) {
        List<Long> uniqueIds = treatmentsList == null ? List.of() : treatmentsList.stream().distinct().toList();
        List<Treatment> treatmentList = treatmentService.findAllById(uniqueIds);
        if (uniqueIds.isEmpty() || uniqueIds.size() != treatmentList.size()) {
            throw new EntityNotFoundException("appointment.book.error.treatments");
        }

        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(LocalTime.MAX);
        List<DoctorSchedule> doctorScheduleList = doctorScheduleRepository.findByStartWorkDayHourBetween(startOfDay, endOfDay);
        if (doctorScheduleList.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Appointment> dayAppointments = appointmentRepository
                .findAppointmentByDateTimeBetweenAndStatusNotOrderByDateTimeAsc(startOfDay, endOfDay, AppointmentStatus.CANCELLED);
        List<Appointment> patientAppointments = patientId == null ? List.of() : dayAppointments.stream()
                .filter(a -> a.getPatient() != null && a.getPatient().getId().equals(patientId))
                .toList();
        List<Room> allRooms = roomService.getAllRooms();

        Map<Long, List<LocalDateTime>> availableSlots = new HashMap<>();
        int estimatedTreatmentTime = treatmentService.getAccumulatedTreatmentTime(treatmentList);
        LocalDateTime now = LocalDateTime.now();

        for (DoctorSchedule doctorSchedule : doctorScheduleList) {
            User doctor = doctorSchedule.getUser();
            if (doctor == null || !doctor.isActive() || doctor.getRole() != Role.DENTIST) {
                continue;
            }
            Long doctorId = doctor.getId();
            List<Appointment> doctorAppointments = dayAppointments.stream()
                    .filter(a -> a.getDentist().getId().equals(doctorId))
                    .toList();

            List<LocalDateTime> doctorFreeSlots = new ArrayList<>();
            LocalDateTime currentTime = doctorSchedule.getStartWorkDayHour();
            LocalDateTime endWorkDay = doctorSchedule.getEndWorkDayHour();

            while (!currentTime.plusMinutes(estimatedTreatmentTime).isAfter(endWorkDay)) {
                LocalDateTime slotEnd = currentTime.plusMinutes(estimatedTreatmentTime);
                if (currentTime.isAfter(now)
                        && isSlotFreeForPatient(currentTime, slotEnd, patientAppointments)
                        && isSlotFreeForDoctor(currentTime, slotEnd, doctorSchedule, doctorAppointments)
                        && roomService.findAvailableRoomFromList(currentTime, slotEnd, allRooms, dayAppointments) != null) {
                    doctorFreeSlots.add(currentTime);
                }
                currentTime = currentTime.plusMinutes(TIME_SLOT_INTERVAL_MINUTES);
            }

            if (!doctorFreeSlots.isEmpty()) {
                availableSlots.put(doctorId, doctorFreeSlots);
            }
        }
        return availableSlots;
    }

    private boolean isSlotFreeForPatient(LocalDateTime slotStart, LocalDateTime slotEnd, List<Appointment> patientAppointments) {
        for (Appointment existing : patientAppointments) {
            int existingMinutes = treatmentService.getAccumulatedTreatmentTime(existing.getTreatments());
            LocalDateTime existingStart = existing.getDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existingMinutes);
            if (slotStart.isBefore(existingEnd) && slotEnd.isAfter(existingStart)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSlotFreeForDoctor(LocalDateTime date, LocalDateTime endDateTime, DoctorSchedule doctorSchedule, List<Appointment> appointmentDoctor) {
        if (date.isBefore(doctorSchedule.getStartWorkDayHour()) || endDateTime.isAfter(doctorSchedule.getEndWorkDayHour())) {
            return false;
        }
        LocalDateTime newAppointmentEndWithBreak = endDateTime.plusMinutes(DOCTOR_BREAK_BETWEEN_APPOINTMENT);
        for (Appointment existing : appointmentDoctor) {
            int existingMinutes = treatmentService.getAccumulatedTreatmentTime(existing.getTreatments());
            LocalDateTime existingStart = existing.getDateTime();
            LocalDateTime existingEndWithBreak = existingStart.plusMinutes(existingMinutes + DOCTOR_BREAK_BETWEEN_APPOINTMENT);
            if (date.isBefore(existingEndWithBreak) && newAppointmentEndWithBreak.isAfter(existingStart)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addSchedule(CreateScheduleDoctorDto dto) {
        if (dto.getStartDate() == null || dto.getEndDate() == null || !dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new IllegalArgumentException("schedule.error.endAfterStart");
        }
        if (dto.getStartDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("schedule.error.datePast");
        }
        User doctor = userService.getUserById(dto.getDoctorId());
        if (doctor.getRole() != Role.DENTIST || !doctor.isActive()) {
            throw new EntityNotFoundException("user.error.doctorNotFound");
        }
        LocalDateTime startOfDay = dto.getStartDate().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = dto.getStartDate().toLocalDate().atTime(LocalTime.MAX);
        Optional<DoctorSchedule> doctorSchedule = doctorScheduleRepository
                .findFirstByUserIdAndStartWorkDayHourBetweenOrderByStartWorkDayHourAsc(doctor.getId(), startOfDay, endOfDay);
        if (doctorSchedule.isPresent()) {
            throw new IllegalStateException("schedule.error.alreadyExists");
        }
        doctorScheduleRepository.save(scheduleDoctorMapper.toDoctorScheduleEntity(dto, doctor));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorScheduleTableDto> getSchedulesTableDtoPageForDoctor(Long doctorId, int page, int size) {
        int safePage = Math.max(page, 0);
        return doctorScheduleRepository.findAllByUserId(
                doctorId,
                PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "startWorkDayHour", "id"))
        ).map(scheduleDoctorMapper::toTableDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorScheduleTableDto> getSchedulesTableDtoPage(int page, int size) {
        int safePage = Math.max(page, 0);
        return doctorScheduleRepository.findAllWithUser(
                PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "startWorkDayHour", "id"))
        ).map(scheduleDoctorMapper::toTableDto);
    }

    @Override
    public void deleteSchedule(Long scheduleId, User requester) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("schedule.error.notFound"));

        validateScheduleAccess(requester, schedule);
        validateCanModify(schedule);

        doctorScheduleRepository.delete(schedule);
    }

    @Override
    public void updateSchedule(Long scheduleId, LocalDateTime newStart, LocalDateTime newEnd, User requester) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("schedule.error.notFound"));

        validateScheduleAccess(requester, schedule);
        validateCanModify(schedule);
        validateNewHours(schedule, newStart, newEnd);

        schedule.setStartWorkDayHour(newStart);
        schedule.setEndWorkDayHour(newEnd);
        doctorScheduleRepository.save(schedule);
    }

    @Override
    public boolean isDoctorAvailable(Long doctorId, LocalDateTime date, LocalDateTime endDateTime) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(LocalTime.MAX);

        DoctorSchedule doctorSchedule = doctorScheduleRepository
                .findFirstByUserIdAndStartWorkDayHourBetweenOrderByStartWorkDayHourAsc(doctorId, startOfDay, endOfDay)
                .orElse(null);
        if (doctorSchedule == null) {
            return false;
        }

        List<Appointment> appointmentDoctor = appointmentRepository
                .findAppointmentByDentistIdAndDateTimeBetweenAndStatusNotOrderByDateTimeAsc(
                        doctorId, startOfDay, endOfDay, AppointmentStatus.CANCELLED
                );
        return isSlotFreeForDoctor(date, endDateTime, doctorSchedule, appointmentDoctor);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPatientAvailable(Long patientId, LocalDateTime date, LocalDateTime endDateTime) {
        if (patientId == null) {
            return true;
        }
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(LocalTime.MAX);
        List<Appointment> patientAppointments = appointmentRepository
                .findAppointmentByDateTimeBetweenAndStatusNotOrderByDateTimeAsc(startOfDay, endOfDay, AppointmentStatus.CANCELLED)
                .stream()
                .filter(a -> a.getPatient() != null && a.getPatient().getId().equals(patientId))
                .toList();
        return isSlotFreeForPatient(date, endDateTime, patientAppointments);
    }

    private void validateScheduleAccess(User requester, DoctorSchedule schedule) {
        if (requester == null || requester.getRole() == Role.CLIENT
                || (requester.getRole() == Role.DENTIST && !schedule.getUser().getId().equals(requester.getId()))) {
            throw new IllegalStateException("label.accessDenied");
        }
    }

    private void validateCanModify(DoctorSchedule schedule) {
        LocalDate scheduleDate = schedule.getStartWorkDayHour().toLocalDate();
        if (!LocalDate.now().isBefore(scheduleDate)) {
            throw new IllegalStateException("schedule.error.editLimit");
        }
        if (hasActiveAppointments(schedule)) {
            throw new IllegalStateException("schedule.error.hasAppointments");
        }
    }

    private void validateNewHours(DoctorSchedule schedule, LocalDateTime newStart, LocalDateTime newEnd) {
        if (newStart == null || newEnd == null || !newEnd.isAfter(newStart)) {
            throw new IllegalArgumentException("schedule.error.endAfterStart");
        }

        LocalDate scheduleDate = schedule.getStartWorkDayHour().toLocalDate();
        if (!newStart.toLocalDate().equals(scheduleDate) || !newEnd.toLocalDate().equals(scheduleDate)) {
            throw new IllegalArgumentException("schedule.error.sameDay");
        }
    }

    private boolean hasActiveAppointments(DoctorSchedule schedule) {
        LocalDateTime startOfDay = schedule.getStartWorkDayHour().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = schedule.getStartWorkDayHour().toLocalDate().atTime(LocalTime.MAX);
        return !appointmentRepository
                .findAppointmentByDentistIdAndDateTimeBetweenAndStatusNotOrderByDateTimeAsc(
                        schedule.getUser().getId(), startOfDay, endOfDay, AppointmentStatus.CANCELLED
                ).isEmpty();
    }

}
