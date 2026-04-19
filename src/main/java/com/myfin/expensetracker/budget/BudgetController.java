package com.myfin.expensetracker.budget;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponseDto> createBudget(@RequestBody @Valid BudgetRequestDto budgetRequestDto){
        return ResponseEntity.ok(budgetService.createBudget(budgetRequestDto));
    }

    @PostMapping("/categoryBudget/{budgetId}")
    public ResponseEntity<BudgetResponseDto> addCategoryBudget(@PathVariable Long budgetId, @RequestBody @Valid CategoryBudgetDto categoryBudgetDto){
        return ResponseEntity.ok(budgetService.addCategoryBudget(budgetId,categoryBudgetDto));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponseDto>> getBudgets(){
        return ResponseEntity.ok(budgetService.getBudgets());
    }

    @GetMapping("/current")
    public ResponseEntity<BudgetResponseDto> getBudgetByCurrentMonthAndYear(){
        return ResponseEntity.ok(budgetService.getBudgetByMonthAndYear(LocalDateTime.now().getMonthValue(),LocalDateTime.now().getYear()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDto> getBudgetById(@PathVariable Long id){
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<BudgetResponseDto> getBudgetByMonthAndYear(@RequestParam(required = false)  Integer month, @RequestParam(required = false) Integer year){
        return ResponseEntity.ok(budgetService.getBudgetByMonthAndYear(month,year));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDto> updateBudget(@RequestBody @Valid BudgetRequestDto budgetRequestDto,@PathVariable Long id){
        return ResponseEntity.ok(budgetService.updateBudget(budgetRequestDto,id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetById(@PathVariable Long id){
        budgetService.deleteBudgetById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBudgets(){
        budgetService.deleteBudgets();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<BudgetSummaryResponseDto> getSummaryByMonthAndYear(@RequestParam(required = false) Integer month, @RequestParam(required=false) Integer year){
        return ResponseEntity.ok(budgetService.getSummaryByMonthAndYear(month,year));
    }

    @GetMapping("/remaining")
    public ResponseEntity<BudgetRemainingResponse> getRemainingBudget(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year ){
        return ResponseEntity.ok(budgetService.getRemainingBudget(month,year));
    }

    @GetMapping("/category-usage")
    public ResponseEntity<CategoryUsageSummaryResponseDto> getCategoryUsage(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year ){
        return ResponseEntity.ok(budgetService.getCategoryUsage(month,year));
    }

    @DeleteMapping("/{budgetId}/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategoryBudget(
            @PathVariable Long budgetId,
            @PathVariable Long categoryId) {

        budgetService.deleteCategoryBudget(budgetId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<BudgetAlertDto> getAlerts(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year ){
        return ResponseEntity.ok(budgetService.getAlerts(month,year));
    }

}
