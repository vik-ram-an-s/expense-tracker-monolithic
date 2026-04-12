package com.myfin.expensetracker.category;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByIdAndIsDeletedFalse(Long id);


    List<Category> findByIsDeletedFalse();

    boolean existsByNameAndIsDeletedFalse(String name);

    @Modifying
    @Query(value = "update Category c set c.isDeleted=true where c.id=:id")
    void setIsDeletedFlagTrue(@Param("id") Long id);

    Optional<Category> findByNameAndIsDeletedFalse(String categoryName);

    @Modifying
    @Query("DELETE FROM Category c WHERE c.isDeleted = true AND c.id NOT IN ( SELECT e.category.id FROM Expense e)")
    void deleteByIsDeletedTrue();

//    @Modifying
//    @Query("update Category c set c.isDeleted=false where c.id=:id")
//    void restoreCategory(Long id);
}
