package pl.kapj.dent.radwanskadent.controller;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.kapj.dent.radwanskadent.dto.DoctorScheduleTableDto;
import pl.kapj.dent.radwanskadent.model.Appointment;
import pl.kapj.dent.radwanskadent.model.User;
import pl.kapj.dent.radwanskadent.service.IAppointmentService;
import pl.kapj.dent.radwanskadent.service.IDoctorScheduleService;
import pl.kapj.dent.radwanskadent.service.IRecipeService;
import pl.kapj.dent.radwanskadent.service.IUserService;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/dentist")
@PreAuthorize("hasRole('DENTIST')")
public class DentistController extends BaseController {

    private static final int PAGE_SIZE = 5;
    private static final int SCHEDULE_PAGE_SIZE = 9;
    private static final String DASHBOARD = "/dentist/dashboard";

    private final IAppointmentService appointmentService;
    private final IDoctorScheduleService doctorScheduleService;
    private final IUserService userService;
    private final IRecipeService recipeService;

    public DentistController(IAppointmentService appointmentService,
                             IDoctorScheduleService doctorScheduleService,
                             IUserService userService,
                             IRecipeService recipeService) {
        this.appointmentService = appointmentService;
        this.doctorScheduleService = doctorScheduleService;
        this.userService = userService;
        this.recipeService = recipeService;
    }

    @GetMapping("/dashboard")
    public String showDentistDashboard(@RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "schedulePage", defaultValue = "0") int schedulePage,
                                       Model model,
                                       Principal principal) {
        User dentist = userService.getCurrentUser(principal);
        if (dentist == null) {
            return "redirect:/login";
        }
        Page<DoctorScheduleTableDto> schedulePageData = doctorScheduleService.getSchedulesTableDtoPageForDoctor(dentist.getId(), schedulePage, SCHEDULE_PAGE_SIZE);
        Page<Appointment> appointmentPage = appointmentService.getAppointmentsForDentist(dentist.getId(), page, PAGE_SIZE);
        model.addAttribute("dentist", dentist);
        model.addAttribute("doctorSchedule", schedulePageData.getContent());
        model.addAttribute("scheduleCurrentPage", schedulePageData.getNumber());
        model.addAttribute("scheduleTotalPages", schedulePageData.getTotalPages());
        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("currentPage", appointmentPage.getNumber());
        model.addAttribute("totalPages", appointmentPage.getTotalPages());
        model.addAttribute("totalItems", appointmentPage.getTotalElements());
        return "dentist/dashboard";
    }

    @PostMapping("/appointment/complete/{appointmentId}")
    public String completeAppointment(@PathVariable("appointmentId") Long appointmentId,
                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "schedulePage", defaultValue = "0") int schedulePage,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        User dentist = userService.getCurrentUser(principal);
        appointmentService.completeAppointment(appointmentId, dentist);
        redirectAttributes.addFlashAttribute("success", msg("appointment.complete.success"));
        return "redirect:" + DASHBOARD + "?page=" + page + "&schedulePage=" + schedulePage;
    }

    @PostMapping("/appointment/cancel/{appointmentId}")
    public String cancelAppointment(@PathVariable("appointmentId") Long appointmentId,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "schedulePage", defaultValue = "0") int schedulePage,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        User dentist = userService.getCurrentUser(principal);
        appointmentService.cancelAppointment(appointmentId, dentist);
        redirectAttributes.addFlashAttribute("success", msg("appointment.cancel.success"));
        return "redirect:" + DASHBOARD + "?page=" + page + "&schedulePage=" + schedulePage;
    }

    @PostMapping("/schedule/update/{scheduleId}")
    public String updateSchedule(@PathVariable("scheduleId") Long scheduleId,
                                 @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                 @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "schedulePage", defaultValue = "0") int schedulePage,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User dentist = userService.getCurrentUser(principal);
        if (dentist == null) {
            return "redirect:/login";
        }
        doctorScheduleService.updateSchedule(scheduleId, startDate, endDate, dentist);
        redirectAttributes.addFlashAttribute("success", msg("schedule.update.success"));
        return "redirect:" + DASHBOARD + "?page=" + page + "&schedulePage=" + schedulePage;
    }

    @PostMapping("/schedule/delete/{scheduleId}")
    public String deleteSchedule(@PathVariable("scheduleId") Long scheduleId,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "schedulePage", defaultValue = "0") int schedulePage,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User dentist = userService.getCurrentUser(principal);
        if (dentist == null) {
            return "redirect:/login";
        }
        doctorScheduleService.deleteSchedule(scheduleId, dentist);
        redirectAttributes.addFlashAttribute("success", msg("schedule.delete.success"));
        return "redirect:" + DASHBOARD + "?page=" + page + "&schedulePage=" + schedulePage;
    }

    @PostMapping("/recipe/create/{appointmentId}")
    public String createRecipe(@PathVariable("appointmentId") Long appointmentId,
                               @RequestParam("description") String description,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "schedulePage", defaultValue = "0") int schedulePage,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        User dentist = userService.getCurrentUser(principal);
        if (dentist == null) {
            return "redirect:/login";
        }
        recipeService.createRecipe(appointmentId, description, dentist);
        redirectAttributes.addFlashAttribute("success", msg("recipe.create.success"));
        return "redirect:" + DASHBOARD + "?page=" + page + "&schedulePage=" + schedulePage;
    }
}
