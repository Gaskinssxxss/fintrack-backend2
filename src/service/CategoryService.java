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
     */
    public synchronized void addCategory(String type, String name) {
        Category category = new Category(type, name);
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
     * Delete a category by type and name
     * @return true if category was found and deleted, false otherwise
     */
    public synchronized boolean deleteCategory(String type, String name) {
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            if (cat.getType().equals(type) && cat.getName().equals(name)) {
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
