package pl.kapj.dent.radwanskadent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.kapj.dent.radwanskadent.model.DoctorSchedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    @Query(value = "SELECT ds FROM DoctorSchedule ds JOIN FETCH ds.user",
           countQuery = "SELECT COUNT(ds) FROM DoctorSchedule ds")
    Page<DoctorSchedule> findAllWithUser(Pageable pageable);

    Page<DoctorSchedule> findAllByUserId(Long userId, Pageable pageable);

    Optional<DoctorSchedule> findFirstByUserIdAndStartWorkDayHourBetweenOrderByStartWorkDayHourAsc(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<DoctorSchedule> findByStartWorkDayHourBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    boolean existsByUserId(Long userId);

}
