package service;

import model.Category;
import persistence.CategoryFileManager;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {

    private final CategoryFileManager fileManager;
    private final List<Category> categories;

    public CategoryService() {
        this.fileManager = new CategoryFileManager();
        this.categories = new ArrayList<>();
        loadCategoriesFromFile();
    }

    /**
     * Load categories from file on initialization
     * Seeds with default categories if file doesn't exist
     */
    private void loadCategoriesFromFile() {
        List<Category> loaded = fileManager.loadCategories();
        categories.addAll(loaded);
    }

    /**
     * Add a new category
     * @param username the username of the category owner
     */
    public synchronized void addCategory(String username, String type, String name) {
        Category category = new Category(username, type, name);
        categories.add(category);
        fileManager.saveCategories(categories);
    }

    /**
     * Get all categories
     * @return list of all categories
     */
    public synchronized List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }

    /**
     * Get categories for a specific user
     * Includes both user-specific categories and system default categories (empty username)
     * @param username the username to filter by
     * @return list of categories belonging to the user
     */
    public synchronized List<Category> getCategoriesByUsername(String username) {
        List<Category> userCategories = new ArrayList<>();
        for (Category category : categories) {
            // Include categories that belong to the user OR are system defaults (empty username)
            if (category.getUsername().equals(username) || category.getUsername().isEmpty()) {
                userCategories.add(category);
            }
        }
        return userCategories;
    }

    /**
     * Delete a category by username, type and name
     * @return true if category was found and deleted, false otherwise
     */
    public synchronized boolean deleteCategory(String username, String type, String name) {
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            if (cat.getUsername().equals(username) && 
                cat.getType().equals(type) && 
                cat.getName().equals(name)) {
                categories.remove(i);
                fileManager.saveCategories(categories);
                return true;
            }
        }
        return false;
    }

    /**
     * Get categories filtered by type
     * @param type the category type (TYPE_INCOME or TYPE_EXPENSE)
     * @return list of categories matching the type
     */
    public synchronized List<Category> getCategoriesByType(String type) {
        List<Category> filtered = new ArrayList<>();
        for (Category category : categories) {
            if (category.getType().equals(type)) {
                filtered.add(category);
            }
        }
        return filtered;
    }
}
