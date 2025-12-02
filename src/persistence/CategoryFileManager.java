package persistence;

import model.Category;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryFileManager {

    private static final String FILE_NAME = "data/categories.txt";

    /**
     * Load all categories from file
     * Seeds with default categories if file doesn't exist
     */
    public synchronized List<Category> loadCategories() {
        List<Category> list = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            // Seed with default categories on first load
            list = getDefaultCategories();
            saveCategories(list);
            return list;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Category category = Category.fromCSV(line);
                    list.add(category);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Save all categories to file
     * Overwrites existing file
     */
    public synchronized void saveCategories(List<Category> categories) {
        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME, false), "UTF-8"))) {

            for (Category category : categories) {
                writer.write(category.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get default categories for initial seeding
     */
    private List<Category> getDefaultCategories() {
        List<Category> defaults = new ArrayList<>();
        
        // Default categories with empty username (system defaults)
        // These will be available to all users
        String systemUser = "";
        
        // Default expense categories
        defaults.add(new Category(systemUser, Category.TYPE_EXPENSE, "Makanan & Minuman"));
        defaults.add(new Category(systemUser, Category.TYPE_EXPENSE, "Transportasi"));
        defaults.add(new Category(systemUser, Category.TYPE_EXPENSE, "Belanja"));
        defaults.add(new Category(systemUser, Category.TYPE_EXPENSE, "Hiburan"));
        defaults.add(new Category(systemUser, Category.TYPE_EXPENSE, "Tagihan"));
        defaults.add(new Category(systemUser, Category.TYPE_EXPENSE, "Kesehatan"));
        defaults.add(new Category(systemUser, Category.TYPE_EXPENSE, "Pendidikan"));
        defaults.add(new Category(systemUser, Category.TYPE_EXPENSE, "Lainnya"));
        
        // Default income categories
        defaults.add(new Category(systemUser, Category.TYPE_INCOME, "Gaji"));
        defaults.add(new Category(systemUser, Category.TYPE_INCOME, "Freelance"));
        defaults.add(new Category(systemUser, Category.TYPE_INCOME, "Investasi"));
        defaults.add(new Category(systemUser, Category.TYPE_INCOME, "Bonus"));
        defaults.add(new Category(systemUser, Category.TYPE_INCOME, "Lainnya"));
        
        return defaults;
    }
}
