package com.myfin.expensetracker.expense;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {

    List<Expense> findByIsDeletedFalse();

    Optional<Expense> findByIdAndIsDeletedFalse(Long id);

    @Modifying
    @Transactional
    @Query("update Expense e set e.isDeleted=true where e.isDeleted=false")
    int setIsDeletedFlagAsTrue();



//    List<Expense> findByCategory_NameAndCategory_IsDeletedFalseAndIsDeletedFalse(String categoryType);

    List<Expense> findByCategory_IdAndCategory_IsDeletedFalseAndIsDeletedFalse(Long categoryId);

    @Query("Select e from Expense e where e.expenseDate between :startDate and :endDate and e.isDeleted=false")
    List<Expense> getExpenseByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("Select e.category.name, sum(e.amount) from Expense e where e.isDeleted=false and e.category.isDeleted=false and function('MONTH',e.expenseDate)=:month and function('YEAR',e.expenseDate)=:year group by e.category.id, e.category.name ")
    List<Object []> getExpenseSummaryByMonthAndYear(Integer month, Integer year);

    @Query("select e.category.name, sum(e.amount) from Expense e where e.isDeleted=false and e.category.isDeleted=false and function('MONTH',e.expenseDate)=:month group by e.category.id, e.category.name")
    List<Object[]> getExpenseSummaryByMonth(Integer month);

    @Query("select e.category.name, sum(e.amount) from Expense e where e.isDeleted=false and e.category.isDeleted=false and function('YEAR',e.expenseDate)=:year group by e.category.id, e.category.name")
    List<Object[]> getExpenseSummaryByYear(Integer year);

    @Query("select e.category.name, sum(e.amount) from Expense e where e.isDeleted=false and e.category.isDeleted=false and  e.expenseDate>=:startDate group by e.category.id, e.category.name")
    List<Object[]> getExpenseSummaryByLastMonth(LocalDateTime startDate);

    @Query("select e.category.name, sum(e.amount) from Expense e where e.isDeleted=false and e.category.isDeleted=false group by e.category.id, e.category.name")
    List<Object[]> getExpenseSummary();

    @Query("select FUNCTION('MONTH',e.expenseDate), sum(e.amount) from Expense e where e.isDeleted=false and e.category.isDeleted=false and function('YEAR',e.expenseDate)=:year group by function('MONTH',e.expenseDate) order by function('MONTH',e.expenseDate) ")
    List<Object[]> getExpenseSummaryByMonthWise(Integer year);

    void deleteByIsDeletedTrue();

    @Query("select sum(e.amount) from Expense e where MONTH(e.expenseDate)=:month and YEAR(e.expenseDate)=:year and e.isDeleted=false and e.category.isDeleted=false")
    BigDecimal getTotalExpense(Integer month,Integer year);

    @Query("select count(e)>0 from Expense e where MONTH(e.expenseDate)=:month and YEAR(e.expenseDate)=:year and e.isDeleted=false and e.category.isDeleted=false")
    boolean existsByMonthAndYear(Integer month, Integer year);

    @Query("select sum(e.amount) from Expense e where e.isDeleted=false and e.category.isDeleted=false and e.category.id=:id and MONTH(e.expenseDate)=:month and YEAR(e.expenseDate)=:year")
    BigDecimal getTotalExpenseByCategoryType(Long id,Integer month,Integer year);

    @Query("select e.category.id, sum(e.amount) from Expense e where e.isDeleted=false and e.category.isDeleted=false and MONTH(e.expenseDate)=:month and YEAR(e.expenseDate)=:year group by e.category.id")
    List<Object[]> getCategoryUsage(Integer month, Integer year);
}
