package pl.kapj.dent.radwanskadent.service;

import org.springframework.data.domain.Page;
import pl.kapj.dent.radwanskadent.dto.AppointmentTableDto;
import pl.kapj.dent.radwanskadent.model.Appointment;
import pl.kapj.dent.radwanskadent.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IAppointmentService {

    Appointment getAppointment(Long appointmentId);

    void cancelUnpaidAppointments();

    void completePastAppointments();

    Page<AppointmentTableDto> getAppointmentsTableDtoPage(LocalDate date, int page, int size);

    void bookAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, List<Long> treatmentIds);

    Page<Appointment> getAppointmentsForPatient(Long patientId, int page, int size);

    Page<Appointment> getAppointmentsForDentist(Long dentistId, int page, int size);

    void payForAppointment(Long appointmentId, User requester);

    void cancelAppointment(Long appointmentId, User requester);

    void completeAppointment(Long appointmentId, User requester);

    long countAppointments();
}
