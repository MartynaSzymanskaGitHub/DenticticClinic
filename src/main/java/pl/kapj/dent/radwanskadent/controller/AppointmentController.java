package pl.kapj.dent.radwanskadent.controller;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.kapj.dent.radwanskadent.dto.AppointmentRequestDto;
import pl.kapj.dent.radwanskadent.model.Appointment;
import pl.kapj.dent.radwanskadent.model.User;
import pl.kapj.dent.radwanskadent.service.IAppointmentService;
import pl.kapj.dent.radwanskadent.service.IDoctorScheduleService;
import pl.kapj.dent.radwanskadent.service.ITreatmentService;
import pl.kapj.dent.radwanskadent.service.IUserService;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/appointment")
@PreAuthorize("hasRole('CLIENT')")
public class AppointmentController extends BaseController {

    private static final int PAGE_SIZE = 5;

    private final IAppointmentService appointmentService;
    private final IDoctorScheduleService doctorScheduleService;
    private final ITreatmentService treatmentService;
    private final IUserService userService;

    public AppointmentController(IAppointmentService appointmentService,
                                 IDoctorScheduleService doctorScheduleService,
                                 ITreatmentService treatmentService,
                                 IUserService userService) {
        this.appointmentService = appointmentService;
        this.doctorScheduleService = doctorScheduleService;
        this.treatmentService = treatmentService;
        this.userService = userService;
    }

    @GetMapping("/book")
    public String showBookingForm(@RequestParam(value = "treatmentId", required = false) Long selectedTreatmentId,
                                  Model model) {
        AppointmentRequestDto appointmentRequestDto = new AppointmentRequestDto();
        appointmentRequestDto.setTreatmentList(treatmentService.getAllTreatments());
        model.addAttribute("appointment", appointmentRequestDto);
        model.addAttribute("selectedTreatmentId", selectedTreatmentId);
        model.addAttribute("defaultDate", LocalDate.now().plusDays(1));
        return "appointment/booking";
    }

    @GetMapping("/available-slots")
    public String showAvailableSlots(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate localDate,
            @RequestParam(value = "treatmentIds", required = false) List<Long> treatmentIds,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (localDate == null) {
            redirectAttributes.addFlashAttribute("error", msg("appointment.error.selectDate"));
            return "redirect:/appointment/book";
        }
        if (treatmentIds == null || treatmentIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", msg("appointment.error.selectTreatment"));
            return "redirect:/appointment/book";
        }
        if (localDate.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", msg("appointment.error.datePast"));
            return "redirect:/appointment/book";
        }

        User currentUser = userService.getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        LocalDateTime date = localDate.atStartOfDay();
        Map<Long, List<LocalDateTime>> availableTreatmentSlots = doctorScheduleService.getAvailableSlotsForDate(date, treatmentIds, currentUser.getId());
        model.addAttribute("selectedDate", localDate);
        model.addAttribute("selectedTreatments", treatmentService.findAllById(treatmentIds));
        model.addAttribute("treatmentSlots", availableTreatmentSlots);
        model.addAttribute("treatmentIds", treatmentIds);
        model.addAttribute("dentists", userService.getDentists());
        return "appointment/available-slots";
    }

    @PostMapping("/book")
    public String bookAppointment(
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam("treatmentIds") List<Long> treatmentIds,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }
        appointmentService.bookAppointment(user.getId(), doctorId, dateTime, treatmentIds);
        redirectAttributes.addFlashAttribute("success", msg("appointment.book.success"));
        return "redirect:/appointment/my";
    }

    @GetMapping("/my")
    public String showMyAppointments(@RequestParam(value = "page", defaultValue = "0") int page,
                                     Model model,
                                     Principal principal) {
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }
        Page<Appointment> appointmentPage = appointmentService.getAppointmentsForPatient(user.getId(), page, PAGE_SIZE);
        model.addAttribute("currentUser", user);
        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("currentPage", appointmentPage.getNumber());
        model.addAttribute("totalPages", appointmentPage.getTotalPages());
        model.addAttribute("totalItems", appointmentPage.getTotalElements());
        return "appointment/list";
    }

    @PostMapping("/pay/{appointmentId}")
    public String payForAppointment(@PathVariable("appointmentId") Long appointmentId,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(principal);
        appointmentService.payForAppointment(appointmentId, user);
        redirectAttributes.addFlashAttribute("success", msg("appointment.pay.success"));
        return "redirect:/appointment/my?page=" + page;
    }

    @PostMapping("/cancel/{appointmentId}")
    public String cancelAppointment(@PathVariable("appointmentId") Long appointmentId,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(principal);
        appointmentService.cancelAppointment(appointmentId, user);
        redirectAttributes.addFlashAttribute("success", msg("appointment.cancel.success"));
        return "redirect:/appointment/my?page=" + page;
    }
}
