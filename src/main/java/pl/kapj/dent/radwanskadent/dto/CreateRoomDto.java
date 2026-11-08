package pl.kapj.dent.radwanskadent.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import pl.kapj.dent.radwanskadent.constants.ValidationConstants;

import java.time.LocalDateTime;

public class CreateRoomDto {

    @NotBlank(message = "{registerForm.field.required}")
    @Size(min = ValidationConstants.ROOM_NAME_MIN, max = ValidationConstants.ROOM_NAME_MAX, message = "{admin.room.validation.name.size}")
    private String name;

    @NotBlank(message = "{registerForm.field.required}")
    @Size(min = ValidationConstants.ROOM_DESC_MIN, max = ValidationConstants.ROOM_DESC_MAX, message = "{admin.room.validation.desc.size}")
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime renovationTimeStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime renovationTimeEnd;

    @AssertTrue(message = "{admin.room.validation.renovation.bothRequired}")
    public boolean isRenovationBothRequired() {
        return (renovationTimeStart == null && renovationTimeEnd == null)
                || (renovationTimeStart != null && renovationTimeEnd != null);
    }

    @AssertTrue(message = "{admin.room.validation.renovation.endAfterStart}")
    public boolean isRenovationEndAfterStart() {
        if (renovationTimeStart == null || renovationTimeEnd == null) {
            return true;
        }
        return renovationTimeEnd.isAfter(renovationTimeStart);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getRenovationTimeStart() {
        return renovationTimeStart;
    }

    public void setRenovationTimeStart(LocalDateTime renovationTimeStart) {
        this.renovationTimeStart = renovationTimeStart;
    }

    public LocalDateTime getRenovationTimeEnd() {
        return renovationTimeEnd;
    }

    public void setRenovationTimeEnd(LocalDateTime renovationTimeEnd) {
        this.renovationTimeEnd = renovationTimeEnd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
