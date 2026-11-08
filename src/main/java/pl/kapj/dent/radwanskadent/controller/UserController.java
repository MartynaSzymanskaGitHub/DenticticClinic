package pl.kapj.dent.radwanskadent.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.kapj.dent.radwanskadent.dto.UserProfileDto;
import pl.kapj.dent.radwanskadent.dto.UserRegistrationDto;
import pl.kapj.dent.radwanskadent.model.User;
import pl.kapj.dent.radwanskadent.service.IJwtService;
import pl.kapj.dent.radwanskadent.service.IRecaptchaService;
import pl.kapj.dent.radwanskadent.service.IUserService;
import pl.kapj.dent.radwanskadent.validator.UserValidator;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    private final IUserService userService;
    private final UserValidator userValidator;
    private final IRecaptchaService recaptchaService;
    private final PasswordEncoder passwordEncoder;
    private final IJwtService jwtService;
    private final String recaptchaSiteKey;

    public UserController(IUserService userService,
                          UserValidator userValidator,
                          IRecaptchaService recaptchaService,
                          PasswordEncoder passwordEncoder,
                          IJwtService jwtService,
                          @Value("${recaptcha.site-key}") String recaptchaSiteKey) {
        this.userService = userService;
        this.userValidator = userValidator;
        this.recaptchaService = recaptchaService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.recaptchaSiteKey = recaptchaSiteKey;
    }

    @ModelAttribute("recaptchaSiteKey")
    public String getRecaptchaSiteKey() {
        return recaptchaSiteKey;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("userDto") UserRegistrationDto userDto,
            BindingResult bindingResult,
            HttpServletRequest request) {

        userValidator.validate(userDto, bindingResult);

        String captchaResponse = request.getParameter("g-recaptcha-response");
        if (!recaptchaService.verify(captchaResponse)) {
            bindingResult.reject("recaptcha.invalid");
        }
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        userService.registerUser(userDto);
        return "redirect:/user/emailConfirmation";
    }

    @GetMapping("/emailConfirmation")
    public String showEmailConfirmationForm() {
        return "user/emailConfirmation";
    }

    @GetMapping("/activate")
    public String activateAccount(@RequestParam("token") UUID token, RedirectAttributes redirectAttributes) {
        boolean activated = userService.activateUser(token);
        if (activated) {
            redirectAttributes.addFlashAttribute("message", msg("user.activate.success"));
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", msg("user.activate.error"));
            return "redirect:/login?error";
        }
    }

    @GetMapping("/profile")
    public String showProfileForm(Model model, Principal principal) {
        User user = userService.getCurrentUser(principal);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("userProfileDto", new UserProfileDto(user));
        model.addAttribute("currentUser", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("userProfileDto") UserProfileDto profileDto,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response) {

        User currentUser = userService.getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!bindingResult.hasFieldErrors("newPassword")
                && profileDto.getNewPassword() != null
                && !profileDto.getNewPassword().trim().isEmpty()
                && passwordEncoder.matches(profileDto.getNewPassword(), currentUser.getPassword())) {
            bindingResult.rejectValue("newPassword", "profile.password.duplicated");
        }

        if (!bindingResult.hasFieldErrors("email")
                && !currentUser.getEmail().equalsIgnoreCase(profileDto.getEmail())) {
            Optional<User> existingEmailUser = userService.findUserByEmail(profileDto.getEmail());
            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(currentUser.getId())) {
                bindingResult.rejectValue("email", "registerForm.email.exists");
            }
        }

        if (!bindingResult.hasFieldErrors("phone")) {
            Optional<User> existingPhoneUser = userService.findUserByPhone(profileDto.getPhone());
            if (existingPhoneUser.isPresent() && !existingPhoneUser.get().getId().equals(currentUser.getId())) {
                bindingResult.rejectValue("phone", "registerForm.phone.exists");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", currentUser);
            return "user/profile";
        }

        String newToken = userService.updateUserProfile(currentUser.getId(), profileDto);
        if (newToken != null) {
            response.addCookie(jwtService.createJwtCookie(newToken));
        }

        redirectAttributes.addFlashAttribute("successMessage", msg("profile.success"));
        return "redirect:/user/profile";
    }
}
