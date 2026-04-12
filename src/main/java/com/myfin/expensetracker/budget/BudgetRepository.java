package com.myfin.expensetracker.budget;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Long> {

    Optional<Budget> findByMonthAndYear(@NotNull @Min(1) @Max(12) Integer month, @NotNull Integer year);

    Optional<Budget> findByYear(Integer year);

    boolean existsByMonthAndYearAndIdNot(@NotNull @Min(1) @Max(12) Integer month, @NotNull Integer year, Long id);
}
