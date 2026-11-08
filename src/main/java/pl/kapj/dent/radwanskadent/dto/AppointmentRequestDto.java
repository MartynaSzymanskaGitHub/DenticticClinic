package pl.kapj.dent.radwanskadent.dto;

import pl.kapj.dent.radwanskadent.model.Treatment;

import java.util.List;

public class AppointmentRequestDto {

    private List<Treatment> treatmentList;

    public List<Treatment> getTreatmentList() {
        return treatmentList;
    }

    public void setTreatmentList(List<Treatment> treatmentList) {
        this.treatmentList = treatmentList;
    }
}
