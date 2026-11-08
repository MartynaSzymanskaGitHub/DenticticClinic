package pl.kapj.dent.radwanskadent.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kapj.dent.radwanskadent.constants.DateTimeFormats;
import pl.kapj.dent.radwanskadent.dto.AppointmentTableDto;
import pl.kapj.dent.radwanskadent.mapper.AppointmentMapper;
import pl.kapj.dent.radwanskadent.model.*;
import pl.kapj.dent.radwanskadent.repository.AppointmentRepository;
import pl.kapj.dent.radwanskadent.repository.UserRepository;
import pl.kapj.dent.radwanskadent.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service("appointmentService")
@Transactional
public class AppointmentService implements IAppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final IEmailService emailService;
    private final IDoctorScheduleService doctorScheduleService;
    private final IUserService userService;
    private final ITreatmentService treatmentService;
    private final IRoomService roomService;
    private final AppointmentMapper appointmentMapper;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              IEmailService emailService,
                              IDoctorScheduleService doctorScheduleService,
                              IUserService userService,
                              ITreatmentService treatmentService,
                              IRoomService roomService,
                              AppointmentMapper appointmentMapper,
                              UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
        this.doctorScheduleService = doctorScheduleService;
        this.userService = userService;
        this.treatmentService = treatmentService;
        this.roomService = roomService;
        this.appointmentMapper = appointmentMapper;
        this.userRepository = userRepository;
    }

    @Override
    public Appointment getAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId).orElse(null);
    }

    @Scheduled(fixedRate = 60 * 1000)
    @Override
    public void cancelUnpaidAppointments() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(2);
        List<Appointment> unpaidAppointments = appointmentRepository.findAllByStatusAndCreatedAtBefore(
                AppointmentStatus.PENDING_PAYMENT, expirationThreshold
        );

        for (Appointment appointment : unpaidAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setCancelationTime(LocalDateTime.now());
            appointmentRepository.save(appointment);
            log.info("[SCHEDULER] Automatycznie anulowano nieoplacona wizyte ID: {}", appointment.getId());
        }
    }

    @Override
    @Scheduled(fixedRate = 60 * 1000)
    public void completePastAppointments() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> confirmedAppointments = appointmentRepository.findAllByStatusAndDateTimeBefore(
                AppointmentStatus.CONFIRMED, now
        );
        for (Appointment appointment : confirmedAppointments) {
            int durationMinutes = treatmentService.getAccumulatedTreatmentTime(appointment.getTreatments());
            LocalDateTime estimatedEndTime = appointment.getDateTime().plusMinutes(durationMinutes);
            if (now.isAfter(estimatedEndTime)) {
                appointment.setStatus(AppointmentStatus.COMPLETED);
                appointmentRepository.save(appointment);
                log.info("[SCHEDULER] Automatycznie oznaczono wizyte ID: {} jako zrealizowana.", appointment.getId());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentTableDto> getAppointmentsTableDtoPage(LocalDate date, int page, int size) {
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "dateTime", "id"));
        Page<Appointment> appointmentPage;
        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            appointmentPage = appointmentRepository.findAllByDateTimeBetween(startOfDay, endOfDay, pageable);
        } else {
            appointmentPage = appointmentRepository.findAll(pageable);
        }
        return appointmentPage.map(appointmentMapper::toTableDto);
    }

    @Override
    public void bookAppointment(Long patientId, Long doctorId, LocalDateTime dateTime, List<Long> treatmentIds) {
        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("appointment.error.datePast");
        }

        User patient = userService.getUserById(patientId);
        User doctor = userRepository.findByIdWithLock(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("user.error.doctorNotFound"));
        if (doctor.getRole() != Role.DENTIST || !doctor.isActive()) {
            throw new IllegalArgumentException("user.error.doctorNotFound");
        }

        List<Long> uniqueTreatmentIds = treatmentIds.stream().distinct().toList();
        List<Treatment> treatmentList = treatmentService.findAllById(uniqueTreatmentIds);
        if (uniqueTreatmentIds.isEmpty() || !Objects.equals(treatmentList.size(), uniqueTreatmentIds.size())) {
            throw new IllegalArgumentException("appointment.book.error.treatments");
        }
        int treatmentMinutes = treatmentService.getAccumulatedTreatmentTime(treatmentList);

        if (!doctorScheduleService.isPatientAvailable(patientId, dateTime, dateTime.plusMinutes(treatmentMinutes))) {
            throw new IllegalStateException("appointment.book.error.patientBusy");
        }

        if (!doctorScheduleService.isDoctorAvailable(doctorId, dateTime, dateTime.plusMinutes(treatmentMinutes))) {
            throw new IllegalArgumentException("appointment.book.error.doctorUnavailable");
        }

        Room lockedRoom = roomService.getAvailableRoomWithLockBetweenDates(dateTime, dateTime.plusMinutes(treatmentMinutes));
        if (lockedRoom == null) {
            throw new IllegalArgumentException("appointment.book.error.noRoom");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDentist(doctor);
        appointment.setRoom(lockedRoom);
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
        appointment.setTreatments(treatmentList);
        appointment.setDateTime(dateTime);

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setPaid(false);
        payment.setAmount(treatmentList.stream().map(Treatment::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        appointment.setPayment(payment);

        appointmentRepository.save(appointment);

        String subjectMail = "Potwierdzenie rezerwacji wizyty";
        String contentMail = "Pomyślnie zarezerwowano wizytę na dzień: " + DateTimeFormats.DATE_TIME.format(appointment.getDateTime());

        emailService.sendMail(patient.getEmail(), subjectMail, contentMail);
    }


    @Override
    public Page<Appointment> getAppointmentsForPatient(Long patientId, int page, int size) {
        int safePage = Math.max(page, 0);
        return appointmentRepository.findAllByPatientId(
                patientId,
                PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "dateTime", "id"))
        );
    }

    @Override
    public Page<Appointment> getAppointmentsForDentist(Long dentistId, int page, int size) {
        int safePage = Math.max(page, 0);
        return appointmentRepository.findAllByDentistId(
                dentistId,
                PageRequest.of(safePage, size, Sort.by(Sort.Direction.DESC, "dateTime", "id"))
        );
    }

    @Override
    public void payForAppointment(Long appointmentId, User requester) {
        if (requester == null) {
            throw new IllegalStateException("label.accessDenied");
        }
        Appointment appointment = existById(appointmentId);
        if (requester.getRole() == Role.CLIENT
                && !appointment.getPatient().getId().equals(requester.getId())) {
            throw new IllegalStateException("label.accessDenied");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("appointment.pay.error.invalidStatus");
        }

        Payment payment = appointment.getPayment();
        if (payment == null) {
            payment = new Payment();
            payment.setAppointment(appointment);
        }

        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal amount = appointment.getTreatments().stream()
                            .map(Treatment::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            payment.setAmount(amount);
        }

        String invoiceNumber = appointment.getId() + "-" + DateTimeFormats.INVOICE_DATE.format(LocalDateTime.now());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaid(true);
        payment.setInvoiceNumber(invoiceNumber);
        appointment.setPayment(payment);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        String mailSubject = "Wizyta opłacona";
        String mailBody = "Wizyta na dzień: " + DateTimeFormats.DATE_TIME.format(appointment.getDateTime()) + " została pomyślnie opłacona";

        emailService.sendMail(appointment.getPatient().getEmail(), mailSubject, mailBody);
    }

    @Override
    public void cancelAppointment(Long appointmentId, User requester) {
        if (requester == null) {
            throw new IllegalStateException("label.accessDenied");
        }
        Appointment appointment = existById(appointmentId);
        if (requester.getRole() == Role.CLIENT && !appointment.getPatient().getId().equals(requester.getId())) {
            throw new IllegalStateException("label.accessDenied");
        }
        if (requester.getRole() == Role.DENTIST && !appointment.getDentist().getId().equals(requester.getId())) {
            throw new IllegalStateException("label.accessDenied");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("appointment.cancel.error.alreadyCancelled");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("appointment.cancel.error.alreadyCompleted");
        }
        if (!LocalDateTime.now().isBefore(appointment.getDateTime().minusMinutes(30))) {
            throw new IllegalStateException("appointment.cancel.error.timeLimit");
        }

        Payment payment = appointment.getPayment();
        boolean wasPaid = payment != null && payment.isPaid();

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (payment != null) {
            payment.setPaid(false);
        }
        appointment.setCancelationTime(LocalDateTime.now());
        appointmentRepository.save(appointment);

        String mailSubject = "Wizyta odwołana";
        String mailBody = "Wizyta zaplanowana na dzień: " + DateTimeFormats.DATE_TIME.format(appointment.getDateTime()) + " została pomyślnie odwołana."
                + (wasPaid ? " Wpłacone środki zostaną zwrócone w ciągu 3 dni roboczych." : "");
        String mailBodyDentist = "Wizyta zaplanowana na dzień: " + DateTimeFormats.DATE_TIME.format(appointment.getDateTime()) + " została odwołana";

        emailService.sendMail(appointment.getPatient().getEmail(), mailSubject, mailBody);
        emailService.sendMail(appointment.getDentist().getEmail(), mailSubject, mailBodyDentist);
    }

    @Override
    public void completeAppointment(Long appointmentId, User requester) {
        if (requester == null || (requester.getRole() != Role.DENTIST && requester.getRole() != Role.ADMIN)) {
            throw new IllegalStateException("label.accessDenied");
        }
        Appointment appointment = existById(appointmentId);
        if (requester.getRole() == Role.DENTIST
                && !appointment.getDentist().getId().equals(requester.getId())) {
            throw new IllegalStateException("label.accessDenied");
        }
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("appointment.complete.error.invalidStatus");
        }
        if (appointment.getDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("appointment.complete.error.futureDate");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    public Appointment existById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("appointment.error.notFound"));
    }

    @Override
    public long countAppointments() {
        return appointmentRepository.count();
    }

}
