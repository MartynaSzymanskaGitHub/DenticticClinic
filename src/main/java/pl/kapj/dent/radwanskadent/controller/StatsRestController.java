package pl.kapj.dent.radwanskadent.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kapj.dent.radwanskadent.dto.ClinicStatsDto;
import pl.kapj.dent.radwanskadent.service.IAppointmentService;
import pl.kapj.dent.radwanskadent.service.IPaymentService;
import pl.kapj.dent.radwanskadent.service.IRoomService;
import pl.kapj.dent.radwanskadent.service.IUserService;

@RestController
@RequestMapping("/api/stats")
@PreAuthorize("hasRole('ADMIN')")
public class StatsRestController {

    private final IPaymentService paymentService;
    private final IRoomService roomService;
    private final IAppointmentService appointmentService;
    private final IUserService userService;

    public StatsRestController(IPaymentService paymentService,
                               IRoomService roomService,
                               IAppointmentService appointmentService,
                               IUserService userService) {
        this.paymentService = paymentService;
        this.roomService = roomService;
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ClinicStatsDto getStats() {
        return new ClinicStatsDto(
                paymentService.getTotalRevenue(),
                paymentService.getPaidRevenue(),
                appointmentService.countAppointments(),
                (int) userService.countUsers(),
                roomService.getAllRoomsUsageStats()
        );
    }
}
