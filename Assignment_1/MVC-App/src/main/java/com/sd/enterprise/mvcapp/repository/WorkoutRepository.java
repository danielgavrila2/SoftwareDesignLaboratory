package com.sd.enterprise.mvcapp.repository;

import com.sd.enterprise.mvcapp.entity.Difficulty;
import com.sd.enterprise.mvcapp.entity.MuscleGroup;
import com.sd.enterprise.mvcapp.entity.Workout;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout,Long>, JpaSpecificationExecutor<Workout> {
    Page<Workout> findByMuscleGroup(MuscleGroup muscleGroup, Pageable pageable);

    Page<Workout> findByDifficulty(Difficulty difficulty, Pageable pageable);

    Page<Workout> findByMuscleGroupAndDifficulty(MuscleGroup muscleGroup,
                                                 Difficulty difficulty,
                                                 Pageable pageable);

    // --- Search by name ---
    @Query("SELECT w FROM Workout w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Workout> searchByName(@Param("name") String name, Pageable pageable);

    // --- Combined filter + search ---
    @Query("""
        SELECT w FROM Workout w
        WHERE (:name IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:muscleGroup IS NULL OR w.muscleGroup = :muscleGroup)
        AND (:difficulty IS NULL OR w.difficulty = :difficulty)
        AND (:minDuration IS NULL OR w.durationMinutes >= :minDuration)
        AND (:maxDuration IS NULL OR w.durationMinutes <= :maxDuration)
        """)
    Page<Workout> findWithFilters(
            @Param("name") String name,
            @Param("muscleGroup") MuscleGroup muscleGroup,
            @Param("difficulty") Difficulty difficulty,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration,
            Pageable pageable);

    // --- Stats ---
    @Query("SELECT w.muscleGroup, COUNT(w) FROM Workout w GROUP BY w.muscleGroup")
    List<Object[]> countByMuscleGroup();

    @Query("SELECT w.difficulty, COUNT(w) FROM Workout w GROUP BY w.difficulty")
    List<Object[]> countByDifficulty();

    long countByDifficulty(Difficulty difficulty);

    long countByMuscleGroup(MuscleGroup muscleGroup);
}
