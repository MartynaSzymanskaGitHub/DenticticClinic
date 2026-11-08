package pl.kapj.dent.radwanskadent.service;

import pl.kapj.dent.radwanskadent.dto.TreatmentRequestDto;
import pl.kapj.dent.radwanskadent.model.Treatment;

import java.util.List;

public interface ITreatmentService {

    List<Treatment> getAllTreatments();

    void createTreatment(TreatmentRequestDto treatment);

    void updateTreatment(Long id, TreatmentRequestDto treatment);

    void deleteTreatment(Long id);

    List<Treatment> findAllById(List<Long> treatmentIds);

    int getAccumulatedTreatmentTime(List<Treatment> treatmentIds);
}
