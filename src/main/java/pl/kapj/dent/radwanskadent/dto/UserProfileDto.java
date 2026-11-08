package pl.kapj.dent.radwanskadent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.kapj.dent.radwanskadent.constants.ValidationConstants;
import pl.kapj.dent.radwanskadent.model.Gender;
import pl.kapj.dent.radwanskadent.model.User;

public class UserProfileDto {

    @NotBlank(message = "{registerForm.field.required}")
    @Size(min = ValidationConstants.FIRST_NAME_MIN, message = "{registerForm.firstName.size}")
    private String firstName;

    @NotBlank(message = "{registerForm.field.required}")
    @Size(min = ValidationConstants.LAST_NAME_MIN, message = "{registerForm.lastName.size}")
    private String lastName;

    @NotBlank(message = "{registerForm.field.required}")
    @Email(message = "{registerForm.email.invalid}")
    @Size(min = ValidationConstants.EMAIL_MIN, message = "{registerForm.email.size}")
    private String email;

    @NotBlank(message = "{registerForm.field.required}")
    @Pattern(regexp = "^(\\+48)?[0-9]{9}$", message = "{registerForm.phone.pattern}")
    @Size(max = ValidationConstants.PHONE_MAX, message = "{registerForm.phone.size}")
    private String phone;

    @NotNull(message = "{registerForm.field.required}")
    private Gender gender;

    @Size(min = ValidationConstants.PASSWORD_MIN, max = ValidationConstants.PASSWORD_MAX, message = "{registerForm.password.size}")
    private String newPassword;

    public UserProfileDto() {
    }

    public UserProfileDto(User user) {
        if (user != null) {
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
            this.phone = user.getPhone();
            this.gender = user.getGender();
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = (newPassword != null && newPassword.trim().isEmpty()) ? null : newPassword;
    }
}
