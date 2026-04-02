package com.team.book_exchange.repository;

import com.team.book_exchange.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("existsByNameIgnoreCase should match regardless of case")
    void existsByNameIgnoreCase_shouldReturnTrue_whenCategoryExists() {
        categoryRepository.save(Category.builder()
            .name("Programming")
            .description("Tech books")
            .build());

        boolean exists = categoryRepository.existsByNameIgnoreCase("programming");

        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByNameIgnoreCaseAndIdNot should ignore the same row and catch duplicates from other rows")
    void existsByNameIgnoreCaseAndIdNot_shouldWorkCorrectly() {
        Category first = categoryRepository.save(Category.builder()
            .name("Programming")
            .description("Tech books")
            .build());

        Category second = categoryRepository.save(Category.builder()
            .name("Science")
            .description("Science books")
            .build());

        boolean sameRowIgnored = categoryRepository.existsByNameIgnoreCaseAndIdNot("Programming", first.getId());
        boolean duplicateDetectedFromOtherRow = categoryRepository.existsByNameIgnoreCaseAndIdNot("Programming", second.getId());

        assertFalse(sameRowIgnored);
        assertTrue(duplicateDetectedFromOtherRow);
    }

    @Test
    @DisplayName("findAllByOrderByNameAsc should return categories sorted by name")
    void findAllByOrderByNameAsc_shouldReturnSortedCategories() {
        categoryRepository.save(Category.builder().name("Science").description("Science books").build());
        categoryRepository.save(Category.builder().name("Academic").description("Academic books").build());
        categoryRepository.save(Category.builder().name("Programming").description("Programming books").build());

        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();

        assertEquals(3, categories.size());
        assertEquals("Academic", categories.get(0).getName());
        assertEquals("Programming", categories.get(1).getName());
        assertEquals("Science", categories.get(2).getName());
    }
}
