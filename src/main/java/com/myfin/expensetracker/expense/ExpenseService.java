package com.myfin.expensetracker.expense;

import com.myfin.expensetracker.category.Category;
import com.myfin.expensetracker.category.CategoryResponseDto;
import com.myfin.expensetracker.category.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryService categoryService;

    private Expense getExpenseEntity(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    public ExpenseResponseDto getExpense(Long id){
//        Optional<Expense> expense = expenseRepository.findByIdAndIsDeletedFalse(id);
//        if(expense.isEmpty()){
//            throw new RuntimeException(
//                    "Expense with the given id " + id + " not present");
//        }
//
//        return expense.get();
        Expense savedExpense = getExpenseEntity(id);
        return mapToResponse(savedExpense);
    }

    public BigDecimal getTotalExpense(Integer month, Integer year){
        BigDecimal totalExpense;
        if(!expenseRepository.existsByMonthAndYear(month,year)){
            totalExpense = BigDecimal.ZERO;
        }
        else{
            totalExpense = expenseRepository.getTotalExpense(month,year);
        }



        return totalExpense;

    }


    public ExpenseResponseDto addExpense(ExpenseRequestDto expenseRequestDto) {
        Expense saveExpense = new Expense();
        saveExpense.setAmount(expenseRequestDto.getAmount());
        Category category = categoryService.getCategoryEntity(expenseRequestDto.getCategoryId());
        saveExpense.setCategory(category);
        saveExpense.setPaymentMethod(expenseRequestDto.getPaymentMethod());
        saveExpense.setDescription(expenseRequestDto.getDescription());
        if(expenseRequestDto.getExpenseDate()==null){
            saveExpense.setExpenseDate(LocalDateTime.now());
        }
        else{
            if(expenseRequestDto.getExpenseDate().isEqual(LocalDate.now())){
                saveExpense.setExpenseDate(LocalDateTime.now());
            }
            else{
                saveExpense.setExpenseDate(expenseRequestDto.getExpenseDate().atStartOfDay());
            }
        }


        Expense savedExpense = expenseRepository.save(saveExpense);
        return mapToResponse(savedExpense);
    }

    public ExpenseResponseDto updateExpense(Long id, ExpenseRequestDto expenseRequestDto ) {
        Expense expense = getExpenseEntity(id);
        expense.setAmount(expenseRequestDto.getAmount());

        Category category = categoryService.getCategoryEntity(expenseRequestDto.getCategoryId());
        expense.setCategory(category);

        expense.setDescription(expenseRequestDto.getDescription());
        expense.setPaymentMethod(expenseRequestDto.getPaymentMethod());
        if(expenseRequestDto.getExpenseDate()==null){
            expense.setExpenseDate(LocalDateTime.now());
        }
        else{
            if(expenseRequestDto.getExpenseDate().isEqual(LocalDate.now())){
                expense.setExpenseDate(LocalDateTime.now());
            }
            else{
                expense.setExpenseDate(expenseRequestDto.getExpenseDate().atStartOfDay());
            }
        }


        Expense updatedExpense = expenseRepository.save(expense);
        return mapToResponse(updatedExpense);
    }
    private ExpenseResponseDto mapToResponse(Expense expense) {

        CategoryResponseDto categoryDto = new CategoryResponseDto();
        categoryDto.setId(expense.getCategory().getId());
        categoryDto.setName(expense.getCategory().getName());
        categoryDto.setCreatedDateTime(expense.getCategory().getCreatedDateTime());
        categoryDto.setUpdatedDateTime(expense.getCategory().getUpdatedDateTime());

        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setCategory(categoryDto);
        dto.setPaymentMethod(expense.getPaymentMethod());
        dto.setDescription(expense.getDescription());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setCreatedDateTime(expense.getCreatedDateTime());
        dto.setUpdatedDateTime(expense.getUpdatedDateTime());

        return dto;
    }

    public List<ExpenseResponseDto> getAllExpense() {
        //        return expenseRepository.findAll()
//                .stream()
//                .filter(e -> !e.getIsDeleted())
//                .toList();
//        List<Expense> allExpenses = expenseRepository.findByIsDeletedFalse();
//        List<ExpenseResponseDto> expenses = new ArrayList<>();
//        for(Expense expense:allExpenses){
//            expenses.add(mapToResponse(expense));
//        }

        return expenseRepository.findByIsDeletedFalse()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    public void deleteAllExpense() {
//        List<Expense> expenseList = expenseRepository.findByIsDeletedFalse();
//        expenseList.forEach(x->x.setIsDeleted(true));
        int affected = expenseRepository.setIsDeletedFlagAsTrue();
        if(affected ==0){
            throw new RuntimeException("No active expenses to delete");
        }

    }

    public void deleteExpense(Long id) {
        Expense expense = getExpenseEntity(id);
        expense.setIsDeleted(true);
        expenseRepository.save(expense);
    }

    public List<ExpenseResponseDto> getExpenseByCategoryId(Long categoryId) {
//        Category category  = categoryService.getByCategoryType(categoryType);
        return expenseRepository.findByCategory_IdAndCategory_IsDeletedFalseAndIsDeletedFalse(categoryId).stream().map(this::mapToResponse).toList();

    }

    public List<ExpenseResponseDto> getExpenseByDateRange(LocalDate startDate, LocalDate endDate){

        if (!startDate.isBefore(endDate)) {
            throw new RuntimeException("Start Date must be before EndDate");
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23,59,59);

        return expenseRepository.getExpenseByDateRange(startDateTime,endDateTime).stream().map(this::mapToResponse).toList();
    }

    public Map<String,BigDecimal> getExpenseSummary(Integer month, Integer year, Integer lastMonths) {

        //using object array because db will return as an object[] when we retrieve specific columns
        List<Object[]> expenseSummary ;

        if(lastMonths != null && (month != null || year != null)){
            throw new RuntimeException("lastMonths cannot be combined with month/year");
        }
        else if(month !=null && year!=null){
            expenseSummary =  expenseRepository.getExpenseSummaryByMonthAndYear(month,year);
        }
        else if(month!=null){
            expenseSummary = expenseRepository.getExpenseSummaryByMonth(month);
        }
        else if(lastMonths !=null){
            LocalDateTime startDate = LocalDateTime.now().minusMonths(lastMonths);
            System.out.println(startDate);
            expenseSummary = expenseRepository.getExpenseSummaryByLastMonth(startDate);
        }
        else if(year!=null){
            expenseSummary = expenseRepository.getExpenseSummaryByYear(year);
        }
        else{
            expenseSummary = expenseRepository.getExpenseSummary();
            System.out.println("inside the else block");
        }


        Map<String,BigDecimal> summary = new HashMap<>();

        for(Object[] obj:expenseSummary){
            String category = (String) obj[0];
            BigDecimal expenses = (BigDecimal) obj[1];
            summary.put(category,expenses);
        }

        return summary;
    }

        public Map<String, BigDecimal> getExpenseSummaryByMonth(Integer year) {
        List<Object[]> expenses;
        if(year!=null){
            expenses = expenseRepository.getExpenseSummaryByMonthWise(year);
        }
        else{
            Integer currentYear = LocalDateTime.now().getYear();
            expenses = expenseRepository.getExpenseSummaryByMonthWise(currentYear);
        }
        Map<String,BigDecimal> expenseSummary = new LinkedHashMap<>();

        for(Object[] arr:expenses){
            String month = Month.of((Integer) arr[0]).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            BigDecimal amount = (BigDecimal) arr[1];
            expenseSummary.put(month,amount);
        }

        return expenseSummary;

    }

    public BigDecimal getTotalExpenseByCategoryType(Long id,Integer month, Integer year){
        if(expenseRepository.getTotalExpenseByCategoryType(id,month,year)==null){
            return BigDecimal.ZERO;
        }
        return expenseRepository.getTotalExpenseByCategoryType(id,month,year);
    }



    public List<String> getPaymentMethods() {
        return Arrays.stream(PaymentMethod.values())
                .map(Enum::name)
                .toList();
    }

    @Transactional
    public void deleteSoftDeletedExpense() {
        expenseRepository.deleteByIsDeletedTrue();
    }

    public Map<Long, BigDecimal> getCategoryUsage(Integer month,Integer year) {

        Map<Long,BigDecimal> response = new HashMap<>();
        List<Object[]> categoryUsage =expenseRepository.getCategoryUsage(month,year);
        for(Object[] obj:categoryUsage){
            Long categoryId = (Long) obj[0];
            BigDecimal spent = (BigDecimal) obj[1];
            response.put(categoryId,spent);
        }
        return response;
    }
}
