package pl.kapj.dent.radwanskadent.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.kapj.dent.radwanskadent.constants.ValidationConstants;

import java.math.BigDecimal;

public class TreatmentRequestDto {

    @NotBlank(message = "{registerForm.field.required}")
    @Size(min = ValidationConstants.TREATMENT_NAME_MIN, max = ValidationConstants.TREATMENT_NAME_MAX, message = "{admin.treatment.validation.name.size}")
    private String name;

    @NotNull(message = "{registerForm.field.required}")
    @DecimalMin(value = "1.00", message = "{admin.treatment.validation.price.min}")
    private BigDecimal price;

    @NotBlank(message = "{registerForm.field.required}")
    @Size(min = ValidationConstants.TREATMENT_DESC_MIN, max = ValidationConstants.TREATMENT_DESC_MAX, message = "{admin.treatment.validation.desc.size}")
    private String description;

    @NotNull(message = "{registerForm.field.required}")
    @Min(value = ValidationConstants.TREATMENT_DURATION_MIN, message = "{admin.treatment.validation.duration.min}")
    private Integer estimatedMinutes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }
}
