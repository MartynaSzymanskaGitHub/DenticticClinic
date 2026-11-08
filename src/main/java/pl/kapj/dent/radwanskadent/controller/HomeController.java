package pl.kapj.dent.radwanskadent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.kapj.dent.radwanskadent.service.ITreatmentService;
import pl.kapj.dent.radwanskadent.service.IUserService;

@Controller
public class HomeController {
    private final ITreatmentService treatmentService;
    private final IUserService userService;
    private final static int photoCount = 6;

    public HomeController(ITreatmentService treatmentService, IUserService userService) {
        this.treatmentService = treatmentService;
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("treatments", treatmentService.getAllTreatments());
        model.addAttribute("dentists", userService.getDentists());
        model.addAttribute("photoCount", photoCount);
        return "homePage";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}
