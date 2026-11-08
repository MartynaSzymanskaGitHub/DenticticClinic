package pl.kapj.dent.radwanskadent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.kapj.dent.radwanskadent.constants.ValidationConstants;
import pl.kapj.dent.radwanskadent.model.Gender;
import pl.kapj.dent.radwanskadent.model.Role;

public class UserRegistrationDto {

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
    @Size(min = ValidationConstants.PASSWORD_MIN, max = ValidationConstants.PASSWORD_MAX, message = "{registerForm.password.size}")
    private String password;

    @NotBlank(message = "{registerForm.field.required}")
    @Pattern(regexp = "^(\\+48)?[0-9]{9}$", message = "{registerForm.phone.pattern}")
    @Size(max = ValidationConstants.PHONE_MAX, message = "{registerForm.phone.size}")
    private String phone;

    @NotNull
    private Role role;

    @NotNull(message = "{registerForm.field.required}")
    private Gender gender;

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
