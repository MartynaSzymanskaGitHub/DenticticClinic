package pl.kapj.dent.radwanskadent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.kapj.dent.radwanskadent.constants.ValidationConstants;

public class UserLoginDto {

    @NotNull
    @Size(min = ValidationConstants.EMAIL_MIN)
    private String email;

    @NotNull
    @Size(min = ValidationConstants.PASSWORD_MIN)
    private String password;

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
}

