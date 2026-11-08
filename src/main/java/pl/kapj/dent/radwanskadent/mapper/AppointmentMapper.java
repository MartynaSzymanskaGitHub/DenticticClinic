package pl.kapj.dent.radwanskadent.mapper;

import org.springframework.stereotype.Component;
import pl.kapj.dent.radwanskadent.dto.AppointmentTableDto;
import pl.kapj.dent.radwanskadent.model.Appointment;
import pl.kapj.dent.radwanskadent.model.Recipe;

import java.util.List;

@Component
public class AppointmentMapper {

    public AppointmentTableDto toTableDto(Appointment app) {
        if (app == null) {
            return null;
        }
        AppointmentTableDto dto = new AppointmentTableDto();
        dto.setId(app.getId());
        dto.setDateTime(app.getDateTime());
        dto.setPatientId(app.getPatient().getId());
        dto.setPatientFirstName(app.getPatient().getFirstName());
        dto.setPatientLastName(app.getPatient().getLastName());
        dto.setDentistFirstName(app.getDentist().getFirstName());
        dto.setDentistLastName(app.getDentist().getLastName());
        dto.setRoomName(app.getRoom().getName());
        dto.setStatus(app.getStatus());
        dto.setPaid(app.getPayment() != null && app.getPayment().isPaid());
        dto.setAmount(app.getPayment() != null ? app.getPayment().getAmount() : null);
        dto.setRecipeIds(app.getRecipes() == null ? List.of() : app.getRecipes().stream().map(Recipe::getId).toList());
        return dto;
    }
}
