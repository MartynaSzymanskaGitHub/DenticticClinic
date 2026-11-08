package pl.kapj.dent.radwanskadent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kapj.dent.radwanskadent.model.Appointment;
import pl.kapj.dent.radwanskadent.model.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByStatusAndCreatedAtBefore(AppointmentStatus status, LocalDateTime dateTime);

    List<Appointment> findAllByStatusAndDateTimeBefore(AppointmentStatus status, LocalDateTime dateTime);

    Page<Appointment> findAllByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findAllByDentistId(Long dentistId, Pageable pageable);

    Page<Appointment> findAllByDateTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay, Pageable pageable);

    List<Appointment> findAppointmentByDateTimeBetweenAndStatusNotOrderByDateTimeAsc(LocalDateTime startOfDay, LocalDateTime endOfDay, AppointmentStatus status);

    List<Appointment> findAppointmentByDentistIdAndDateTimeBetweenAndStatusNotOrderByDateTimeAsc(Long dentistId, LocalDateTime startOfDay, LocalDateTime endOfDay, AppointmentStatus status);

    List<Appointment> findAllByRoomIdAndStatusNotIn(Long roomId, List<AppointmentStatus> statuses);

    long countByRoomIdAndStatusNot(Long roomId, AppointmentStatus status);

    boolean existsByPatientIdOrDentistId(Long patientId, Long dentistId);
}
