package test;

import service.CategoryService;
import model.Category;
import java.util.List;

public class TestCategoryService {

    public static void main(String[] args) {
        System.out.println("=== Testing CategoryService ===\n");
        
        CategoryService categoryService = new CategoryService();
        
        // Test 1: Add categories
        System.out.println("Test 1: Add categories");
        categoryService.addCategory(Category.TYPE_EXPENSE, "Makanan & Minuman");
        categoryService.addCategory(Category.TYPE_EXPENSE, "Transportasi");
        categoryService.addCategory(Category.TYPE_INCOME, "Gaji");
        categoryService.addCategory(Category.TYPE_INCOME, "Freelance");
        System.out.println("Added 4 categories");
        System.out.println();
        
        // Test 2: Get all categories
        System.out.println("Test 2: Get all categories");
        List<Category> allCategories = categoryService.getAllCategories();
        System.out.println("Total categories: " + allCategories.size());
        for (Category cat : allCategories) {
            System.out.println("  - " + cat.getType() + ": " + cat.getName());
        }
        System.out.println();
        
        // Test 3: Get categories by type (EXPENSE)
        System.out.println("Test 3: Get expense categories");
        List<Category> expenseCategories = categoryService.getCategoriesByType(Category.TYPE_EXPENSE);
        System.out.println("Expense categories: " + expenseCategories.size());
        for (Category cat : expenseCategories) {
            System.out.println("  - " + cat.getName());
        }
        System.out.println();
        
        // Test 4: Get categories by type (INCOME)
        System.out.println("Test 4: Get income categories");
        List<Category> incomeCategories = categoryService.getCategoriesByType(Category.TYPE_INCOME);
        System.out.println("Income categories: " + incomeCategories.size());
        for (Category cat : incomeCategories) {
            System.out.println("  - " + cat.getName());
        }
        System.out.println();
        
        // Test 5: Delete a category
        System.out.println("Test 5: Delete a category");
        boolean deleted = categoryService.deleteCategory(Category.TYPE_EXPENSE, "Transportasi");
        System.out.println("Deleted 'Transportasi': " + deleted);
        System.out.println("Total categories after deletion: " + categoryService.getAllCategories().size());
        System.out.println();
        
        // Test 6: Try to delete non-existent category
        System.out.println("Test 6: Delete non-existent category");
        boolean deletedNonExistent = categoryService.deleteCategory(Category.TYPE_EXPENSE, "NonExistent");
        System.out.println("Deleted 'NonExistent': " + deletedNonExistent);
        System.out.println();
        
        System.out.println("=== All tests completed ===");
    }
}
