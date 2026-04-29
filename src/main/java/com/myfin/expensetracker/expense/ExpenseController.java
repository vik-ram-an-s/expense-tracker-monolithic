package com.myfin.expensetracker.expense;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")

public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponseDto> addExpense(
            @RequestBody @Valid ExpenseRequestDto expense) {
        ExpenseResponseDto saved = expenseService.addExpense(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> updateExpense(
            @PathVariable Long id, @RequestBody @Valid ExpenseRequestDto expense) {

        return ResponseEntity.status(HttpStatus.OK).body(expenseService.updateExpense(id, expense));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDto> getExpense(@PathVariable Long id,@RequestParam Long userId) {
        return ResponseEntity.ok(expenseService.getExpense(id,userId));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDto>> getUserExpenses(@RequestParam Long userId) {
        return ResponseEntity.ok(expenseService.getUserExpenses(userId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExpenseResponseDto>> getExpenseByCategoryType(@PathVariable Long categoryId){
        return ResponseEntity.ok(expenseService.getExpenseByCategoryId(categoryId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ExpenseResponseDto>> getExpenseByDateRange(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate){
        return ResponseEntity.ok(expenseService.getExpenseByDateRange(startDate, endDate));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String,BigDecimal>> getExpenseSummary(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer lastMonths){
        return ResponseEntity.ok( expenseService.getExpenseSummary(month,year,lastMonths));
    }



    @GetMapping("/summary-by-month")
    public ResponseEntity<Map<String, BigDecimal>> getExpenseSummaryByMonth(@RequestParam(required = false) Integer year){
        return ResponseEntity.ok( expenseService.getExpenseSummaryByMonth(year));
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<String>> getPaymentMethods() {
        return ResponseEntity.ok(expenseService.getPaymentMethods());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id,@RequestParam Long userId) {
        expenseService.deleteExpense(id,userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllExpense(@RequestParam Long userId) {
        expenseService.deleteAllExpense(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteAllDeletedExpense(){
        expenseService.deleteSoftDeletedExpense();
        return ResponseEntity.noContent().build();
    }
}