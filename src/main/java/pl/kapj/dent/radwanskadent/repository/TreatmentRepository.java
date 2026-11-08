package pl.kapj.dent.radwanskadent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.kapj.dent.radwanskadent.model.Treatment;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    boolean existsByNameIgnoreCase(String name);

}
