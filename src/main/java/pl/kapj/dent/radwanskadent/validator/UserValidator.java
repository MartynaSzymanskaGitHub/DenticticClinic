package pl.kapj.dent.radwanskadent.validator;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.kapj.dent.radwanskadent.dto.UserRegistrationDto;
import pl.kapj.dent.radwanskadent.service.IUserService;

@Component
public class UserValidator implements Validator {

    private final IUserService userService;
    private final EmailValidator emailValidator = EmailValidator.getInstance();

    public UserValidator(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRegistrationDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserRegistrationDto user = (UserRegistrationDto) target;

        if (!errors.hasFieldErrors("email") && user.getEmail() != null && !user.getEmail().isBlank()) {
            if (!emailValidator.isValid(user.getEmail())) {
                errors.rejectValue("email", "registerForm.email.invalid");
            } else if (userService.existsByEmail(user.getEmail())) {
                errors.rejectValue("email", "registerForm.email.exists");
            }
        }

        if (!errors.hasFieldErrors("phone") && user.getPhone() != null && !user.getPhone().isBlank()) {
            if (userService.existsByPhone(user.getPhone())) {
                errors.rejectValue("phone", "registerForm.phone.exists");
            }
        }
    }
}
