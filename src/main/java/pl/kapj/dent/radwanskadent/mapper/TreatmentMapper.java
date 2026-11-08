package pl.kapj.dent.radwanskadent.mapper;

import org.springframework.stereotype.Component;
import pl.kapj.dent.radwanskadent.dto.TreatmentRequestDto;
import pl.kapj.dent.radwanskadent.model.Treatment;

@Component
public class TreatmentMapper {

    public Treatment toEntity(TreatmentRequestDto treatmentRequest) {
        Treatment treatment = new Treatment();
        treatment.setName(treatmentRequest.getName());
        treatment.setDescription(treatmentRequest.getDescription());
        treatment.setPrice(treatmentRequest.getPrice());
        treatment.setEstimatedMinutes(treatmentRequest.getEstimatedMinutes());
        return treatment;
    }

}
