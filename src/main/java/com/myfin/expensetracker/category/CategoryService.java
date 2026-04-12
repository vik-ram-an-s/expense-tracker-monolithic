package com.myfin.expensetracker.category;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto) {
        if(categoryRepository.existsByNameAndIsDeletedFalse(categoryRequestDto.getName())){
            throw new RuntimeException("Category already exists");
        }
        Category category = new Category();
        category.setName(categoryRequestDto.getName());

        Category savedCategory =  categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    public List<CategoryResponseDto> getActiveCategories() {
        return categoryRepository.findByIsDeletedFalse().stream().map(this::mapToResponse).toList();
    }

    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryRequestDto) {
        Category category = getCategoryEntity(id);
        category.setName(categoryRequestDto.getName());
        Category savedCategory =  categoryRepository.save(category);
        return mapToResponse(savedCategory);

    }

    public Category getCategoryEntity(Long id){
        return categoryRepository.findByIdAndIsDeletedFalse(id).orElseThrow(()->new RuntimeException("Category not found"));
    }

    public CategoryResponseDto getCategory(Long id) {
        Category category = getCategoryEntity(id);
        return mapToResponse(category);
    }


    public List<CategoryResponseDto> getAllCategory() {
       return categoryRepository.findAll().stream().map(this::mapToResponse).toList();

    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryEntity(id);
        category.setIsDeleted(true);

//        categoryRepository.setIsDeletedFlagTrue(id);

    }

    public CategoryResponseDto mapToResponse(Category category){
        CategoryResponseDto response = new CategoryResponseDto();
        response.setName(category.getName());
        response.setId(category.getId());
        response.setCreatedDateTime(category.getCreatedDateTime());
        response.setUpdatedDateTime(category.getUpdatedDateTime());

        return response;
    }

    @Transactional
    public CategoryResponseDto restoreCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(()->new RuntimeException("Category not found"));
        category.setIsDeleted(false);
        return mapToResponse(category);



    }




    public Category getByCategoryType(String categoryName) {
        return categoryRepository.findByNameAndIsDeletedFalse(categoryName).orElseThrow(()->new RuntimeException("Category Type Not found"));
    }

    @Transactional
    public void deleteSoftDeletedCategories() {
        categoryRepository.deleteByIsDeletedTrue();
    }


    public Map<Long, Category> getCategoryMapByIds(Set<Long> ids) {
        List<Category> categories = categoryRepository.findAllById(ids);

        return categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
    }
}
