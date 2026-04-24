package com.myfin.expensetracker.budget;

import com.myfin.expensetracker.category.Category;
import com.myfin.expensetracker.category.CategoryService;
import com.myfin.expensetracker.expense.ExpenseService;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ExpenseService expenseService;



    public BudgetResponseDto mapToResponse(Budget budget){
        BudgetResponseDto response = new BudgetResponseDto();
        response.setId(budget.getId());
        response.setMonth(budget.getMonth());
        response.setYear(budget.getYear());
        response.setCreatedDateTime(budget.getCreatedDateTime());
        response.setUpdatedDateTime(budget.getUpdatedDateTime());
        if(budget.getCategoryBudgets()!=null){
            response.setCategoryBudgets(mapToCategoryBudgetResponse(budget.getCategoryBudgets()));
        }

        response.setTotalBudget(budget.getTotalBudget());
        return response;
    }

    public CategoryBudgetResponseDto mapToCategoryBudgetResponse(CategoryBudget categoryBudget) {

        CategoryBudgetResponseDto response = new CategoryBudgetResponseDto();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setName(categoryBudget.getCategory().getName());
        categoryResponse.setId(categoryBudget.getCategory().getId());

        response.setCategoryResponse(categoryResponse);
        response.setId(categoryBudget.getId());
        response.setLimitAmount(categoryBudget.getLimitAmount());

        return response;
    }

    public List<CategoryBudgetResponseDto> mapToCategoryBudgetResponse(List<CategoryBudget> categoryBudgets){
        return categoryBudgets.stream().map(this::mapToCategoryBudgetResponse).toList();
    }


    @Transactional
    public BudgetResponseDto createBudget( BudgetRequestDto budgetRequestDto) {
        Budget budget = new Budget();

        if(budgetRepository.findByMonthAndYear(budgetRequestDto.getMonth(),budgetRequestDto.getYear()).isPresent()){
            throw new RuntimeException("Budget Already Exists");
        }
            budget.setMonth(budgetRequestDto.getMonth());
            budget.setYear(budgetRequestDto.getYear());


        // Validating if the sum of category budget is less that total budget
        BigDecimal totalBudget = budgetRequestDto.getTotalBudget();


        // mapping request dto to respective category budget object
        if(budgetRequestDto.getCategoryBudgets()!=null && !budgetRequestDto.getCategoryBudgets().isEmpty()){
            BigDecimal sumOfCategoryBudget = BigDecimal.ZERO;

            Set<Long> categoryIds = new HashSet<>();
            List<CategoryBudget> categoryBudgetList = new ArrayList<>();

            for(CategoryBudgetDto dto:budgetRequestDto.getCategoryBudgets()){

                // Validation for duplicate category
                if(!categoryIds.add(dto.getCategoryId())){
                    throw new RuntimeException("Duplicate category in budget");
                }

                if (dto.getLimitAmount() == null) {
                    throw new RuntimeException("Limit amount cannot be null");
                }


                CategoryBudget categoryBudget = new CategoryBudget();
                Category category = categoryService.getCategoryEntity(dto.getCategoryId());

                categoryBudget.setCategory(category);
                categoryBudget.setBudget(budget);

                sumOfCategoryBudget = sumOfCategoryBudget.add(dto.getLimitAmount());
                categoryBudget.setLimitAmount(dto.getLimitAmount());

                categoryBudgetList.add(categoryBudget);
            }
            budget.setCategoryBudgets(categoryBudgetList);

            // validation for total budget and sum of category budget
            if(sumOfCategoryBudget.compareTo(totalBudget)>0){
                throw new RuntimeException("Category limits exceed total budget");
            }


        }
        budget.setTotalBudget(budgetRequestDto.getTotalBudget());
        Budget savedBudget = budgetRepository.save(budget);
        return mapToResponse(savedBudget);

    }


    public List<BudgetResponseDto> getBudgets() {
        return budgetRepository.findAll().stream().map(this::mapToResponse).toList();

    }

    public Budget getBudgetEntityByMonthAndYear(Integer month,Integer year){
        Budget budget;

        MonthYearPair monthYearPair = resolveMonthYearPair(month,year);
        budget = budgetRepository.findByMonthAndYear(monthYearPair.getMonth(), monthYearPair.getYear())
                    .orElseThrow(() -> new RuntimeException("Budget not found for given month and year"));

        return budget;
    }

    public BudgetResponseDto getBudgetByMonthAndYear(Integer month, Integer year) {

        Budget budget = getBudgetEntityByMonthAndYear(month,year);
        return mapToResponse(budget);
    }

    @Transactional
    public BudgetResponseDto updateBudget(BudgetRequestDto dto, Long id) {

        Budget existingBudget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget with given id not found"));

        // 🔹 1. Validate duplicate (month + year)
        boolean exists = budgetRepository.existsByMonthAndYearAndIdNot(
                dto.getMonth(),
                dto.getYear(),
                id
        );

        if (exists) {
            throw new RuntimeException("Budget already exists for this month and year");
        }

        // 🔹 2. Validate total budget
        BigDecimal totalBudget = dto.getTotalBudget();
        if (totalBudget == null || totalBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Invalid total budget");
        }

        // 🔹 3. Update basic fields
        existingBudget.setMonth(dto.getMonth());
        existingBudget.setYear(dto.getYear());
        existingBudget.setTotalBudget(totalBudget);

        Set<Long> categoryIds = dto.getCategoryBudgets()
                .stream()
                .map(CategoryBudgetDto::getCategoryId)
                .collect(Collectors.toSet());

// 🔹 Single DB call
// 🔹 Map for lookup
        Map<Long, Category> categoryMap = categoryService.getCategoryMapByIds(categoryIds);

        // 🔹 4. Handle Category Budgets

        if (dto.getCategoryBudgets() != null) {

            // 🔹 Clear only if user explicitly sends the field
            if (existingBudget.getCategoryBudgets() != null) {
                existingBudget.getCategoryBudgets().clear();
                budgetRepository.flush();
            }

            // 🔹 If list is not empty → recreate
            if (!dto.getCategoryBudgets().isEmpty()) {

                BigDecimal sum = BigDecimal.ZERO;
                Set<Long> categoryIdsForValidation = new HashSet<>();

                for (CategoryBudgetDto cbDto : dto.getCategoryBudgets()) {

                    if (!categoryIdsForValidation.add(cbDto.getCategoryId())) {
                        throw new RuntimeException("Duplicate category in request");
                    }

                    if (cbDto.getLimitAmount() == null ||
                            cbDto.getLimitAmount().compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("Invalid category limit");
                    }

                    sum = sum.add(cbDto.getLimitAmount());

                    Category category = categoryMap.get(cbDto.getCategoryId());

                    CategoryBudget cb = new CategoryBudget();
                    cb.setBudget(existingBudget);
                    cb.setCategory(category);
                    cb.setLimitAmount(cbDto.getLimitAmount());

                    existingBudget.getCategoryBudgets().add(cb);
                }

                if (sum.compareTo(totalBudget) > 0) {
                    throw new RuntimeException("Category limits exceed total budget");
                }
            }
        }
        // 🔹 5. Save
        Budget savedBudget;
        savedBudget = budgetRepository.save(existingBudget);

        // 🔹 6. Return response
        return mapToResponse(savedBudget);
    }

    public void deleteBudgets() {
        budgetRepository.deleteAll();
    }

    public void deleteBudgetById(Long id) {

        if(budgetRepository.existsById(id)){
            budgetRepository.deleteById(id);
        }
        else{
            throw new RuntimeException("Budget Not Present");
        }
    }

    public Budget getBudgetEntity(Long id){
        return budgetRepository.findById(id).orElseThrow(()->new RuntimeException("Budget with "+id +" not found"));
    }

    public BudgetResponseDto getBudgetById(Long id) {
        Budget budget = getBudgetEntity(id);
        return mapToResponse(budget);
    }

    public BudgetSummaryResponseDto getSummaryByMonthAndYear(Integer month, Integer year) {

        BigDecimal totalBudget,totalExpense;

        totalBudget = getBudgetEntityByMonthAndYear(month,year).getTotalBudget();

        MonthYearPair monthYearPair = resolveMonthYearPair(month,year);

        totalExpense = expenseService.getTotalExpense(monthYearPair.getMonth(),monthYearPair.getYear());

        BudgetComputation budgetDetails = computeBudget(totalBudget,totalExpense);

        BudgetSummaryResponseDto summaryResponse = new BudgetSummaryResponseDto();
        summaryResponse.setTotalBudget(budgetDetails.getLimit());
        summaryResponse.setRemainingBudget(budgetDetails.getRemaining());
        summaryResponse.setTotalExpense(budgetDetails.getSpent());
        summaryResponse.setStatus(budgetDetails.getStatus());
        summaryResponse.setUsedPercentage(budgetDetails.getUsedPercentage());
        summaryResponse.setOverspent(budgetDetails.getOverspent());
        return summaryResponse;

    }

    public BudgetRemainingResponse getRemainingBudget(Integer month, Integer year) {
        BudgetRemainingResponse remainingResponse = new BudgetRemainingResponse();
        BigDecimal remainingBudget = getSummaryByMonthAndYear(month,year).getRemainingBudget();
        remainingResponse.setRemainingBudget(remainingBudget);
        return remainingResponse;
    }


    public CategoryUsageSummaryResponseDto getCategoryUsage(Integer month, Integer year) {
        Budget budget = getBudgetEntityByMonthAndYear(month,year);
        List<CategoryBudget> categoryBudgets = budget.getCategoryBudgets();

        List<CategoryUsageResponseDto> categoryUsageResponseDtoList = new ArrayList<>();

        MonthYearPair monthYearPair = resolveMonthYearPair(month,year);
        Map<Long,BigDecimal> categories = expenseService.getCategoryUsage(monthYearPair.getMonth(),monthYearPair.getYear());


        for(CategoryBudget categoryBudget:categoryBudgets){
            CategoryUsageResponseDto categoryUsageResponseDto = new CategoryUsageResponseDto();
            Category category = categoryBudget.getCategory();

            categoryUsageResponseDto.setCategoryName(category.getName());
            categoryUsageResponseDto.setLimit(categoryBudget.getLimitAmount());


            BigDecimal limit = categoryBudget.getLimitAmount();
            BigDecimal spent = categories.getOrDefault(category.getId(), BigDecimal.ZERO);

            BudgetComputation budgetDetails = computeBudget(limit,spent);

            categoryUsageResponseDto.setOverspent(budgetDetails.getOverspent());

            categoryUsageResponseDto.setRemaining(budgetDetails.getRemaining());

            categoryUsageResponseDto.setSpent(budgetDetails.getSpent());

            categoryUsageResponseDto.setUsedPercentage(budgetDetails.getUsedPercentage());

            categoryUsageResponseDto.setStatus(budgetDetails.getStatus());

            categoryUsageResponseDtoList.add(categoryUsageResponseDto);
        }

        CategoryUsageSummaryResponseDto categoryUsageSummaryResponseDto = new CategoryUsageSummaryResponseDto();
        categoryUsageSummaryResponseDto.setCategoryUsageResponseDtoList(categoryUsageResponseDtoList);

        return categoryUsageSummaryResponseDto;

    }

    public PercentAndStatus getStatus(BigDecimal totalExpense,
                                      BigDecimal totalBudget,
                                      BigDecimal remainingBudget) {

        PercentAndStatus percentAndStatus = new PercentAndStatus();

        // 🔥 1. Handle ZERO budget FIRST (edge case)
        if (totalBudget.compareTo(BigDecimal.ZERO) == 0) {

            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                percentAndStatus.setStatus(BudgetStatus.OVER_BUDGET);
            } else {
                percentAndStatus.setStatus(BudgetStatus.SAFE);
            }

            percentAndStatus.setPercent(BigDecimal.ZERO);
            return percentAndStatus;
        }

        // 🔹 2. Calculate percentage
        BigDecimal percent = totalExpense
                .divide(totalBudget, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        percentAndStatus.setPercent(percent);

        // 🔹 3. Determine status
        BudgetStatus status;

        if (remainingBudget.compareTo(BigDecimal.ZERO) < 0) {
            status = BudgetStatus.OVER_BUDGET;
        } else if (percent.compareTo(BigDecimal.valueOf(80)) >= 0) {
            status = BudgetStatus.WARNING;
        } else {
            status = BudgetStatus.SAFE;
        }

        percentAndStatus.setStatus(status);

        return percentAndStatus;
    }

    public BudgetComputation computeBudget(BigDecimal limit, BigDecimal spent){
        BudgetComputation result = new BudgetComputation();

        // Safety
        if (spent == null) spent = BigDecimal.ZERO;
        if (limit == null) limit = BigDecimal.ZERO;

        // 1. Remaining
        BigDecimal remaining = limit.subtract(spent);

        // 2. Overspent
        BigDecimal overspent = remaining.compareTo(BigDecimal.ZERO) < 0
                ? remaining.abs()
                : BigDecimal.ZERO;

        // 3. Percent + Status
        PercentAndStatus ps = getStatus(spent, limit, remaining);

        // 4. Set values
        result.setSpent(spent);
        result.setLimit(limit);
        result.setRemaining(remaining);
        result.setOverspent(overspent);
        result.setUsedPercentage(ps.getPercent());
        result.setStatus(ps.getStatus());

        return result;
    }

    @Transactional
    public BudgetResponseDto addCategoryBudget(Long budgetId, CategoryBudgetDto categoryBudgetDto) {

        Budget existingBudget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        Category category = categoryService.getCategoryEntity(categoryBudgetDto.getCategoryId());

        // 🔥 Ensure list is initialized
        if (existingBudget.getCategoryBudgets() == null) {
            existingBudget.setCategoryBudgets(new ArrayList<>());
        }

        // 🔥 Check if category already exists
        Optional<CategoryBudget> existingCategoryBudget = existingBudget.getCategoryBudgets()
                .stream()
                .filter(cb -> cb.getCategory().getId().equals(category.getId()))
                .findFirst();



        if (existingCategoryBudget.isPresent()) {

            // 👉 Option 1: Throw error
             throw new RuntimeException("Category already exists in this budget");



        } else {
            BigDecimal budgetLimit = existingBudget.getTotalBudget();
            BigDecimal totalCategoryLimit = existingBudget.getCategoryBudgets()
                    .stream()
                    .map(CategoryBudget::getLimitAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal updatedLimit = totalCategoryLimit.add(categoryBudgetDto.getLimitAmount());

            // 👉 Add new category budget
            if(budgetLimit.compareTo(updatedLimit)>=0){
                CategoryBudget categoryBudget = new CategoryBudget();
                categoryBudget.setLimitAmount(categoryBudgetDto.getLimitAmount());
                categoryBudget.setCategory(category);
                categoryBudget.setBudget(existingBudget);

                existingBudget.getCategoryBudgets().add(categoryBudget);
            }
            else{
                throw new RuntimeException("Budget Limit exceeds");
            }


        }

        Budget saved = budgetRepository.save(existingBudget);
        return mapToResponse(saved);
    }

    public MonthYearPair resolveMonthYearPair(Integer month,Integer year){
        if (month != null && year != null) {
            return new MonthYearPair(month, year);

        } else if (month != null) {
            int currentYear = LocalDateTime.now().getYear();
            return new MonthYearPair(month, currentYear);

        } else if (year != null) {
            throw new RuntimeException("Please provide month along with year");

        } else {
            LocalDateTime now = LocalDateTime.now();
            return new MonthYearPair(now.getMonthValue(), now.getYear());
        }
    }


    public BudgetAlertDto getAlerts(Integer month, Integer year) {

        BudgetAlertDto alertResponse = new BudgetAlertDto();


            BudgetSummaryResponseDto summary = getSummaryByMonthAndYear(month,year);

            CategoryUsageSummaryResponseDto categoryUsageSummaryResponseDto = getCategoryUsage(month,year);

            if(categoryUsageSummaryResponseDto.getCategoryUsageResponseDtoList().isEmpty()){
                alertResponse.setCategoryAlertDtoList(Collections.emptyList());
            }

            alertResponse.setCategoryAlertDtoList(new ArrayList<>());


            for(CategoryUsageResponseDto dto:categoryUsageSummaryResponseDto.getCategoryUsageResponseDtoList()){


//                 No need for alert if budget is safe
                if (dto.getStatus() == BudgetStatus.SAFE) {
                    continue;
                }
                else{
                    CategoryAlertDto categoryAlertDto = getCategoryAlertDto(dto);
                    alertResponse.getCategoryAlertDtoList().add(categoryAlertDto);
                }

            }


            alertResponse.setOverAllStatus(summary.getStatus());
            if (summary.getStatus() == BudgetStatus.WARNING) {
                alertResponse.setMessage("Total budget exceeds 80%");
            } else if (summary.getStatus() == BudgetStatus.OVER_BUDGET) {
                alertResponse.setMessage("Total budget exceeded");
            }

        return alertResponse;
    }

    @Nonnull
    private static CategoryAlertDto getCategoryAlertDto(CategoryUsageResponseDto dto) {
        CategoryAlertDto categoryAlertDto = new CategoryAlertDto();
        categoryAlertDto.setStatus(dto.getStatus());
        categoryAlertDto.setCategoryName(dto.getCategoryName());


        if(dto.getStatus().equals(BudgetStatus.OVER_BUDGET)){
            categoryAlertDto.setMessage(dto.getCategoryName()+" budget exceeded");
        }
        else{
            categoryAlertDto.setMessage(dto.getCategoryName()+" budget above 80%");
        }
        return categoryAlertDto;
    }

    @Transactional
    public void deleteCategoryBudget(Long budgetId, Long categoryId) {

        Budget budget = getBudgetEntity(budgetId);

        List<CategoryBudget> categoryBudgets = budget.getCategoryBudgets();

        boolean removed = categoryBudgets.removeIf(c -> Objects.equals(c.getCategory().getId(), categoryId));

        if(!removed){
            throw new RuntimeException("Category not found in the budget");
        }

        budgetRepository.save(budget);


    }

    @Transactional
    public CategoryBudgetResponseDto updateCategoryBudget(
            Long budgetId,
            Long categoryId,
            UpdateCategoryBudgetDto dto) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        CategoryBudget categoryBudget = budget.getCategoryBudgets()
                .stream()
                .filter(cb -> cb.getCategory().getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found in this budget"));

        BigDecimal newLimit = dto.getLimitAmount();

        if (newLimit == null || newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Invalid limit amount");
        }

        BigDecimal total = budget.getCategoryBudgets()
                .stream()
                .map(CategoryBudget::getLimitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        total = total.subtract(categoryBudget.getLimitAmount())
                .add(newLimit);

        if (total.compareTo(budget.getTotalBudget()) > 0) {
            throw new RuntimeException("Category limits exceed total budget");
        }

        categoryBudget.setLimitAmount(newLimit);

        budgetRepository.save(budget);

        return mapToCategoryBudgetResponse(categoryBudget);
    }
}
