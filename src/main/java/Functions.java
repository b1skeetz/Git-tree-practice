import Entity.Category;
import jakarta.persistence.*;

import java.util.List;
import java.util.Scanner;

public class Functions {
    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("main");
    private static final EntityManager MANAGER = FACTORY.createEntityManager();
    private static final Scanner SCANNER = new Scanner(System.in);

    public static boolean createCategory() {
        // Введите id родительской категории: 2
        // Введите название новой категории: МЦСТ

        // Процессоры
        // - Intel
        // - AMD
        // - МЦСТ (new)

        System.out.print("Введите ID родительской категории: ");
        int parentId = Integer.parseInt(SCANNER.nextLine());

        try {
            MANAGER.getTransaction().begin();
            Category parentCategory = null;
            String newName;
            Category newCategory;
            long maxRightKey = 0L;
            if (parentId != 0) {
                TypedQuery<Category> selectParentCategoryQuery = MANAGER.createQuery("select c from Category c " +
                        "where c.id = ?1", Category.class);
                selectParentCategoryQuery.setParameter(1, parentId);
                parentCategory = selectParentCategoryQuery.getSingleResult();
            } else {
                TypedQuery<Long> selectMaxRightKeyQuery = MANAGER.createQuery("select max(c.rightKey) from Category c ",
                        Long.class);
                maxRightKey = selectMaxRightKeyQuery.getSingleResult();
            }

            System.out.print("Введите название новой категории: ");
            newName = SCANNER.nextLine();
            newCategory = new Category();
            newCategory.setName(newName);
            if (parentId != 0) {
                newCategory.setHierarchyLevel(parentCategory.getHierarchyLevel() + 1);
                newCategory.setLeftKey(parentCategory.getRightKey());
                newCategory.setRightKey(parentCategory.getRightKey() + 1);
                Query updateCategories = MANAGER.createQuery("update Category c set c.leftKey = c.leftKey + 2, c.rightKey = c.rightKey + 2" +
                        "where c.leftKey > ?1 and c.rightKey > ?1");
                updateCategories.setParameter(1, parentCategory.getRightKey());
                Query updateParent = MANAGER.createQuery("update Category c set c.rightKey = c.rightKey + 2" +
                        "where c.id = ?1 or (c.leftKey < ?2 and c.rightKey > ?2)");
                updateParent.setParameter(1, parentCategory.getId());
                updateParent.setParameter(2, parentCategory.getRightKey());
                updateCategories.executeUpdate();
                updateParent.executeUpdate();
            } else {
                newCategory.setHierarchyLevel(0L);
                newCategory.setLeftKey(maxRightKey + 1);
                newCategory.setRightKey(maxRightKey + 2);
            }
            MANAGER.persist(newCategory);

            MANAGER.getTransaction().commit();
        } catch (NoResultException e) {
            System.out.println("Введен неверный ID категории!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            MANAGER.getTransaction().rollback();
            return false;
        } finally {
            MANAGER.close();
            FACTORY.close();
        }

        return true;
    }

    public static boolean deleteCategory() {
        System.out.print("Введите id категории: ");
        int id = Integer.parseInt(SCANNER.nextLine());

        try {
            MANAGER.getTransaction().begin();
            TypedQuery<Category> getParentCategory = MANAGER.createQuery("select c from Category c " +
                    "where c.id = ?1", Category.class);
            getParentCategory.setParameter(1, id);
            Category parentCategory = getParentCategory.getSingleResult();
            Query deleteCategories = MANAGER.createQuery("delete from Category c " +
                    "where c.rightKey < ?1 and c.leftKey > ?2");
            deleteCategories.setParameter(1, parentCategory.getRightKey());
            deleteCategories.setParameter(2, parentCategory.getLeftKey());
            deleteCategories.executeUpdate();
            MANAGER.remove(parentCategory);

            // Уменьшить правый ключ на разницу ключей удаляемой категории при условии что правый ключ > правого ключа
            // удаляемой категории.

            // Уменьшить левый ключ на разницу ключей удаляемой категории при условии что левый ключ > правого ключа
            // удаляемой категории.

            // - правый ключ родительского элемента
            Query decreaseKeysRight = MANAGER.createQuery("update Category c set " +
                    "c.rightKey = c.rightKey- (?1 - ?2 + 1)" +
                    "where c.rightKey > ?1 ");

            decreaseKeysRight.setParameter(1, parentCategory.getRightKey());
            decreaseKeysRight.setParameter(2, parentCategory.getLeftKey());
            decreaseKeysRight.executeUpdate();

            Query decreaseKeysLeft = MANAGER.createQuery("update Category c set " +
                    "c.leftKey = c.leftKey - (?1 - ?2 + 1) " +
                    "where c.leftKey > ?1");

            decreaseKeysLeft.setParameter(1, parentCategory.getRightKey());
            decreaseKeysLeft.setParameter(2, parentCategory.getLeftKey());
            decreaseKeysLeft.executeUpdate();

            MANAGER.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            MANAGER.getTransaction().rollback();
            return false;
        } finally {
            MANAGER.close();
            FACTORY.close();
        }
        return true;
    }

    public static boolean relocateCategory() {
        System.out.print("Введите название категории для перемещения: ");
        String categoryName = SCANNER.nextLine();

        System.out.print("""
                Введите название категории назначения
                 (категория назначения должна быть выше по иерархии,
                 чем выбранная ранее категория для перемещения):\s""");
        String relocateCategoryName = SCANNER.nextLine();

        try {
            // =============================================================================== //
            TypedQuery<Category> ifCategoryExistsQuery = MANAGER.createQuery("select c from Category c " +
                    "where c.category_name = ?1", Category.class);
            ifCategoryExistsQuery.setParameter(1, categoryName);
            Category selectedCategory = ifCategoryExistsQuery.getSingleResult();

            TypedQuery<Category> parentCategoryForRelocation = MANAGER.createQuery("select c from Category c " +
                    "where c.category_name = ?1", Category.class);
            parentCategoryForRelocation.setParameter(1, relocateCategoryName);
            Category relocateCategory = parentCategoryForRelocation.getSingleResult();

            if (!(relocateCategory.getHierarchyLevel() < selectedCategory.getHierarchyLevel())) {
                System.out.println("Перемещение в данную категорию не имеет смысла! " +
                        "Выберите другую категорию назначения.");
                return false;
            }
            // =============================================================================== //

            MANAGER.getTransaction().begin();
            // Перевод ключей в отрицательные значения
            Query turnKeysIntoNegativeValues = MANAGER.createQuery("update Category c set " +
                    "c.leftKey = c.leftKey - c.leftKey * 2, c.rightKey = c.rightKey - c.rightKey * 2" +
                    "where c.rightKey <= ?1 and c.leftKey >= ?2");
            turnKeysIntoNegativeValues.setParameter(1, selectedCategory.getRightKey());
            turnKeysIntoNegativeValues.setParameter(2, selectedCategory.getLeftKey());
            turnKeysIntoNegativeValues.executeUpdate();

            // =============================================================================== //

            // Ликвидация образовавшегося промежутка
            Query decreaseKeysRight = MANAGER.createQuery("update Category c set " +
                    "c.rightKey = c.rightKey - (?1 - ?2 + 1)" +
                    "where c.rightKey > ?1 ");

            decreaseKeysRight.setParameter(1, selectedCategory.getRightKey());
            decreaseKeysRight.setParameter(2, selectedCategory.getLeftKey());
            decreaseKeysRight.executeUpdate();

            Query decreaseKeysLeft = MANAGER.createQuery("update Category c set " +
                    "c.leftKey = c.leftKey - (?1 - ?2 + 1) " +
                    "where c.leftKey > ?2");

            decreaseKeysLeft.setParameter(1, selectedCategory.getRightKey());
            decreaseKeysLeft.setParameter(2, selectedCategory.getLeftKey());
            decreaseKeysLeft.executeUpdate();

            // =============================================================================== //



            MANAGER.getTransaction().commit();
            System.out.println("Категория успешно перемещена!");
            return true;
        } catch (NoResultException e) {
            System.out.println("Категории с таким названием не существует!");
            return false;
        } catch (Exception e) {
            MANAGER.getTransaction().rollback();
            System.out.println(e.getMessage());
            return false;
        } finally {
            MANAGER.close();
            FACTORY.close();
        }
    }

    public static boolean findTree() {
        System.out.print("Введите название категории: ");
        String categoryName = SCANNER.nextLine();

        try {
            TypedQuery<Category> ifCategoryExistsQuery = MANAGER.createQuery("select c from Category c " +
                    "where c.category_name = ?1", Category.class);
            ifCategoryExistsQuery.setParameter(1, categoryName);
            Category selectedCategory = ifCategoryExistsQuery.getSingleResult();

            TypedQuery<Category> innerCategoriesQuery = MANAGER.createQuery("select c from Category c " +
                    "where c.leftKey > ?1 and c.rightKey < ?2", Category.class);
            innerCategoriesQuery.setParameter(1, selectedCategory.getLeftKey());
            innerCategoriesQuery.setParameter(2, selectedCategory.getRightKey());
            List<Category> innerCategories = innerCategoriesQuery.getResultList();

            System.out.println(categoryName);
            for (Category innerCategory : innerCategories) {
                System.out.println("- " + innerCategory);
            }

        } catch (NoResultException e) {
            System.out.println("Неправильно введено название категории!");
            return false;
        } finally {
            MANAGER.close();
            FACTORY.close();
        }

        return true;
    }

    public static void show() {
        TypedQuery<Category> categoryTypedQuery = MANAGER.createQuery("select c from Category c", Category.class);
        List<Category> categoryList = categoryTypedQuery.getResultList();

        for (Category category : categoryList) {
            for (int i = 0; i < category.getHierarchyLevel().intValue(); i++) {
                System.out.print("- ");
            }
            System.out.println(category);
        }

        MANAGER.close();
        FACTORY.close();
    }
}
