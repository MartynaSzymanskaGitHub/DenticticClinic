package pl.kapj.dent.radwanskadent.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.kapj.dent.radwanskadent.dto.AppointmentTableDto;
import pl.kapj.dent.radwanskadent.dto.CreateRoomDto;
import pl.kapj.dent.radwanskadent.dto.CreateScheduleDoctorDto;
import pl.kapj.dent.radwanskadent.dto.DoctorScheduleTableDto;
import pl.kapj.dent.radwanskadent.dto.RoomUsageStatsDto;
import pl.kapj.dent.radwanskadent.dto.TreatmentRequestDto;
import pl.kapj.dent.radwanskadent.dto.UserTableDto;
import pl.kapj.dent.radwanskadent.model.Treatment;
import pl.kapj.dent.radwanskadent.model.User;
import pl.kapj.dent.radwanskadent.service.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController extends BaseController {

    private static final String DASHBOARD = "/admin/dashboard";
    private static final int PAGE_SIZE = 10;

    private final IRoomService roomService;
    private final ITreatmentService treatmentService;
    private final IDoctorScheduleService doctorScheduleService;
    private final IAppointmentService appointmentService;
    private final IPaymentService paymentService;
    private final IUserService userService;

    public AdminController(IRoomService roomService,
                           ITreatmentService treatmentService,
                           IDoctorScheduleService doctorScheduleService,
                           IAppointmentService appointmentService,
                           IPaymentService paymentService,
                           IUserService userService) {
        this.roomService = roomService;
        this.treatmentService = treatmentService;
        this.doctorScheduleService = doctorScheduleService;
        this.appointmentService = appointmentService;
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(value = "schedulePage", defaultValue = "0") int schedulePage,
            @RequestParam(value = "appointmentPage", defaultValue = "0") int appointmentPage,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
        Page<DoctorScheduleTableDto> schedulesPage = doctorScheduleService.getSchedulesTableDtoPage(schedulePage, PAGE_SIZE);
        Page<AppointmentTableDto> appointmentsPage = appointmentService.getAppointmentsTableDtoPage(date, appointmentPage, PAGE_SIZE);
        List<UserTableDto> dentistList = userService.getDentists();
        List<RoomUsageStatsDto> roomUsageStatsList = roomService.getAllRoomsUsageStats();
        List<Treatment> treatmentList = treatmentService.getAllTreatments();
        BigDecimal totalRevenue = paymentService.getTotalRevenue();
        BigDecimal paidRevenue = paymentService.getPaidRevenue();

        model.addAttribute("appointments", appointmentsPage.getContent());
        model.addAttribute("appointmentCurrentPage", appointmentsPage.getNumber());
        model.addAttribute("appointmentTotalPages", appointmentsPage.getTotalPages());
        model.addAttribute("selectedDate", date);

        model.addAttribute("schedules", schedulesPage.getContent());
        model.addAttribute("scheduleCurrentPage", schedulesPage.getNumber());
        model.addAttribute("scheduleTotalPages", schedulesPage.getTotalPages());

        model.addAttribute("totalAppointments", appointmentService.countAppointments());
        model.addAttribute("dentists", dentistList);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("rooms", roomService.getAllRoomsTableDto());
        model.addAttribute("roomUsageStatsList", roomUsageStatsList);
        model.addAttribute("treatments", treatmentList);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("paidRevenue", paidRevenue);
        model.addAttribute("roomDto", new CreateRoomDto());
        model.addAttribute("treatmentDto", new TreatmentRequestDto());
        model.addAttribute("scheduleDto", new CreateScheduleDoctorDto());
        return "admin/dashboard";
    }

    @PostMapping("/appointment/pay/{appointmentId}")
    public String payForAppointment(@PathVariable("appointmentId") Long appointmentId,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User admin = userService.getCurrentUser(principal);
        appointmentService.payForAppointment(appointmentId, admin);
        redirectAttributes.addFlashAttribute("success", msg("appointment.pay.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/appointment/complete/{appointmentId}")
    public String completeAppointment(@PathVariable("appointmentId") Long appointmentId,
              Principal principal,
              RedirectAttributes redirectAttributes) {
        User admin = userService.getCurrentUser(principal);
        appointmentService.completeAppointment(appointmentId, admin);
        redirectAttributes.addFlashAttribute("success", msg("appointment.complete.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/appointment/cancel/{appointmentId}")
    public String cancelAppointment(@PathVariable("appointmentId") Long appointmentId,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User admin = userService.getCurrentUser(principal);
        appointmentService.cancelAppointment(appointmentId, admin);
        redirectAttributes.addFlashAttribute("success", msg("appointment.cancel.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/user/status/{userId}")
    public String changeUserStatus(@PathVariable("userId") Long userId, Principal principal, RedirectAttributes redirectAttributes) {
        userService.changeUserActiveStatus(userId, principal);
        redirectAttributes.addFlashAttribute("success", msg("admin.user.status.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/user/delete/{userId}")
    public String deleteUser(@PathVariable("userId") Long userId, Principal principal, RedirectAttributes redirectAttributes) {
        userService.deleteUser(userId, principal);
        redirectAttributes.addFlashAttribute("success", msg("admin.user.delete.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/room/add")
    public String createRoom(@Valid @ModelAttribute("roomDto") CreateRoomDto roomDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", extractErrors(bindingResult));
            return "redirect:" + DASHBOARD;
        }
        roomService.createRoom(roomDto);
        redirectAttributes.addFlashAttribute("success", msg("admin.room.add.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/room/update/{id}")
    public String updateRoom(@PathVariable("id") Long id,
             @Valid @ModelAttribute("roomDto") CreateRoomDto roomDto,
             BindingResult bindingResult,
             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", extractErrors(bindingResult));
            return "redirect:" + DASHBOARD;
        }
        roomService.updateRoom(id, roomDto);
        redirectAttributes.addFlashAttribute("success", msg("admin.room.update.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/treatment/add")
    public String createTreatment(@Valid @ModelAttribute("treatmentDto") TreatmentRequestDto treatmentDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", extractErrors(bindingResult));
            return "redirect:" + DASHBOARD;
        }
        treatmentService.createTreatment(treatmentDto);
        redirectAttributes.addFlashAttribute("success", msg("admin.treatment.add.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/treatment/update/{id}")
    public String updateTreatment(@PathVariable("id") Long id,
                                  @Valid @ModelAttribute("treatmentDto") TreatmentRequestDto treatmentDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", extractErrors(bindingResult));
            return "redirect:" + DASHBOARD;
        }
        treatmentService.updateTreatment(id, treatmentDto);
        redirectAttributes.addFlashAttribute("success", msg("admin.treatment.update.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/treatment/delete/{id}")
    public String deleteTreatment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        treatmentService.deleteTreatment(id);
        redirectAttributes.addFlashAttribute("success", msg("admin.treatment.delete.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/schedule/add")
    public String addDoctorSchedule(@Valid @ModelAttribute("scheduleDto") CreateScheduleDoctorDto scheduleDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", extractErrors(bindingResult));
            return "redirect:" + DASHBOARD;
        }
        doctorScheduleService.addSchedule(scheduleDto);
        redirectAttributes.addFlashAttribute("success", msg("schedule.add.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/schedule/update/{scheduleId}")
    public String updateDoctorSchedule(@PathVariable("scheduleId") Long scheduleId,
               @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
               @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
               Principal principal,
               RedirectAttributes redirectAttributes) {
        User admin = userService.getCurrentUser(principal);
        doctorScheduleService.updateSchedule(scheduleId, startDate, endDate, admin);
        redirectAttributes.addFlashAttribute("success", msg("schedule.update.success"));
        return "redirect:" + DASHBOARD;
    }

    @PostMapping("/schedule/delete/{scheduleId}")
    public String deleteDoctorSchedule(@PathVariable("scheduleId") Long scheduleId, Principal principal, RedirectAttributes redirectAttributes) {
        User admin = userService.getCurrentUser(principal);
        doctorScheduleService.deleteSchedule(scheduleId, admin);
        redirectAttributes.addFlashAttribute("success", msg("schedule.delete.success"));
        return "redirect:" + DASHBOARD;
    }
}
