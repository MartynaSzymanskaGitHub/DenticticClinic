package pl.kapj.dent.radwanskadent.service.impl;

import jakarta.persistence.PersistenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kapj.dent.radwanskadent.dto.TreatmentRequestDto;
import pl.kapj.dent.radwanskadent.mapper.TreatmentMapper;
import pl.kapj.dent.radwanskadent.model.Treatment;
import pl.kapj.dent.radwanskadent.repository.TreatmentRepository;
import pl.kapj.dent.radwanskadent.service.ITreatmentService;

import java.util.List;

@Service
@Transactional
public class TreatmentService implements ITreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final TreatmentMapper treatmentMapper;

    public TreatmentService(TreatmentRepository treatmentRepository, TreatmentMapper treatmentMapper) {
        this.treatmentRepository = treatmentRepository;
        this.treatmentMapper = treatmentMapper;
    }

    @Override
    public List<Treatment> getAllTreatments() {
        return treatmentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public void createTreatment(TreatmentRequestDto treatment) {
        if (treatmentRepository.existsByNameIgnoreCase(treatment.getName())) {
            throw new IllegalArgumentException("admin.treatment.error.exists");
        }
        treatmentRepository.save(treatmentMapper.toEntity(treatment));
    }

    @Override
    public void updateTreatment(Long id, TreatmentRequestDto treatment) {
        Treatment oldTreatment = existById(id);
        // Nazwa i opis zabiegu celowo nie są edytowalne - formularz wysyła je tylko do odczytu.
        oldTreatment.setPrice(treatment.getPrice());
        oldTreatment.setEstimatedMinutes(treatment.getEstimatedMinutes());
        treatmentRepository.save(oldTreatment);
    }

    @Override
    public void deleteTreatment(Long id) {
        Treatment treatment = existById(id);
        try {
            treatmentRepository.delete(treatment);
            treatmentRepository.flush();
        } catch (DataAccessException | PersistenceException e) {
            throw new IllegalStateException("admin.treatment.error.assigned", e);
        }
    }

    @Override
    public List<Treatment> findAllById(List<Long> treatmentIds) {
        return treatmentRepository.findAllById(treatmentIds);
    }

    public Treatment existById(Long id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("admin.treatment.error.notFound"));
    }

    @Override
    public int getAccumulatedTreatmentTime(List<Treatment> treatmentIds) {
        return treatmentIds.stream().mapToInt(Treatment::getEstimatedMinutes).sum();
    }

}
