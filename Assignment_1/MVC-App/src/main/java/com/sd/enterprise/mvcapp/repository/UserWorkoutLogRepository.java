package com.sd.enterprise.mvcapp.repository;

import com.sd.enterprise.mvcapp.entity.UserWorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserWorkoutLogRepository extends JpaRepository<UserWorkoutLog, Long> {

    List<UserWorkoutLog> findByUserIdOrderByLogDateDesc(Long userId);

    List<UserWorkoutLog> findByUserIdAndLogDateBetween(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT SUM(l.workout.caloriesBurned) FROM UserWorkoutLog l WHERE l.user.id = :userId")
    Long sumCaloriesByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);
}
