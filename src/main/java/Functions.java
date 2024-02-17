import Entity.Category;
import jakarta.persistence.*;

import java.util.List;
import java.util.Scanner;

public class Functions {
    private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
    private static final EntityManager manager = factory.createEntityManager();
    private static final Scanner scanner = new Scanner(System.in);

    public static boolean createCategory(){
        // Введите id родительской категории: 2
        // Введите название новой категории: МЦСТ

        // Процессоры
        // - Intel
        // - AMD
        // - МЦСТ (new)

        System.out.print("Введите ID родительской категории: ");
        int parentId = Integer.parseInt(scanner.nextLine());

        try {
            manager.getTransaction().begin();
            Category parentCategory = null;
            String newName;
            Category newCategory;
            long maxRightKey = 0L;
            if(parentId != 0){
                TypedQuery<Category> selectParentCategoryQuery = manager.createQuery("select c from Category c " +
                        "where c.id = ?1", Category.class);
                selectParentCategoryQuery.setParameter(1, parentId);
                parentCategory = selectParentCategoryQuery.getSingleResult();
            }
            else{
                TypedQuery<Long> selectMaxRightKeyQuery = manager.createQuery("select max(c.rightKey) from Category c ",
                        Long.class);
                maxRightKey = selectMaxRightKeyQuery.getSingleResult();
            }

            System.out.print("Введите название новой категории: ");
            newName = scanner.nextLine();
            newCategory = new Category();
            newCategory.setName(newName);
            if(parentId != 0){
                newCategory.setHierarchyLevel(parentCategory.getHierarchyLevel() + 1);
                newCategory.setLeftKey(parentCategory.getRightKey());
                newCategory.setRightKey(parentCategory.getRightKey() + 1);
                Query updateCategories = manager.createQuery("update Category c set c.leftKey = c.leftKey + 2, c.rightKey = c.rightKey + 2" +
                        "where c.leftKey > ?1 and c.rightKey > ?1");
                updateCategories.setParameter(1, parentCategory.getRightKey());
                Query updateParent = manager.createQuery("update Category c set c.rightKey = c.rightKey + 2" +
                        "where c.id = ?1 or (c.leftKey < ?2 and c.rightKey > ?2)");
                updateParent.setParameter(1, parentCategory.getId());
                updateParent.setParameter(2, parentCategory.getRightKey());
                updateCategories.executeUpdate();
                updateParent.executeUpdate();
            }
            else{
                newCategory.setHierarchyLevel(0L);
                newCategory.setLeftKey(maxRightKey + 1);
                newCategory.setRightKey(maxRightKey + 2);
            }
            manager.persist(newCategory);

            manager.getTransaction().commit();
        } catch (NoResultException e) {
            System.out.println("Введен неверный ID категории!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            manager.getTransaction().rollback();
            return false;
        }

        manager.close();
        factory.close();
        return true;
    }
    public static boolean deleteCategory(){
        System.out.print("Введите id категории: ");
        int id = Integer.parseInt(scanner.nextLine());

        try{
            manager.getTransaction().begin();
            TypedQuery<Category> getParentCategory = manager.createQuery("select c from Category c " +
                    "where c.id = ?1", Category.class);
            getParentCategory.setParameter(1, id);
            Category parentCategory = getParentCategory.getSingleResult();
            Query deleteCategories = manager.createQuery("delete from Category c " +
                    "where c.rightKey < ?1 and c.leftKey > ?2");
            deleteCategories.setParameter(1, parentCategory.getRightKey());
            deleteCategories.setParameter(2, parentCategory.getLeftKey());
            deleteCategories.executeUpdate();
            manager.remove(parentCategory);

            // Уменьшить правый ключ на разницу ключей удаляемой категории при условии что правый ключ > правого ключа
            // удаляемой категории.

            // Уменьшить левый ключ на разницу ключей удаляемой категории при условии что левый ключ > правого ключа
            // удаляемой категории.

            // - правый ключ родительского элемента
            Query decreaseKeysRight = manager.createQuery("update Category c set " +
                    "c.rightKey = c.rightKey- (?1 - ?2 + 1)" +
                    "where c.rightKey > ?1 ");

            decreaseKeysRight.setParameter(1, parentCategory.getRightKey());
            decreaseKeysRight.setParameter(2, parentCategory.getLeftKey());
            decreaseKeysRight.executeUpdate();

            Query decreaseKeysLeft = manager.createQuery("update Category c set " +
                    "c.leftKey = c.leftKey - (?1 - ?2 + 1) " +
                    "where c.leftKey > ?1");

            decreaseKeysLeft.setParameter(1, parentCategory.getRightKey());
            decreaseKeysLeft.setParameter(2, parentCategory.getLeftKey());
            decreaseKeysLeft.executeUpdate();

            manager.getTransaction().commit();
        }catch (Exception e){
            System.out.println(e.getMessage());
            manager.getTransaction().rollback();
            return false;
        }

        manager.close();
        factory.close();
        return true;
    }

    public static boolean relocateCategory(){
        System.out.print("Введите название категории для перемещения: ");
        String categoryName = scanner.nextLine();

        try {
            TypedQuery<Category> ifCategoryExistsQuery = manager.createQuery("select c from Category c " +
                    "where c.category_name = ?1", Category.class);
            ifCategoryExistsQuery.setParameter(1, categoryName);
            Category selectedCategory = ifCategoryExistsQuery.getSingleResult();

            manager.getTransaction().begin();
            Query turnKeysIntoNegativeValues = manager.createQuery("update Category c set " +
                    "c.leftKey = c.leftKey - c.leftKey * 2, c.rightKey = c.rightKey - c.rightKey * 2" +
                    "where c.rightKey <= ?1 and c.leftKey >= ?2");
            turnKeysIntoNegativeValues.setParameter(1, selectedCategory.getRightKey());
            turnKeysIntoNegativeValues.setParameter(2, selectedCategory.getLeftKey());
            turnKeysIntoNegativeValues.executeUpdate();
            manager.getTransaction().commit();
        } catch (NoResultException e){
            System.out.println("Категории с таким названием не существует!");
            return false;
        } catch (Exception e){
            manager.getTransaction().rollback();
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean findTree(){
        System.out.print("Введите название категории: ");
        String categoryName = scanner.nextLine();

        try{
            TypedQuery<Category> ifCategoryExistsQuery = manager.createQuery("select c from Category c " +
                    "where c.category_name = ?1", Category.class);
            ifCategoryExistsQuery.setParameter(1, categoryName);
            Category selectedCategory = ifCategoryExistsQuery.getSingleResult();

            TypedQuery<Category> innerCategoriesQuery = manager.createQuery("select c from Category c " +
                    "where c.leftKey > ?1 and c.rightKey < ?2", Category.class);
            innerCategoriesQuery.setParameter(1, selectedCategory.getLeftKey());
            innerCategoriesQuery.setParameter(2, selectedCategory.getRightKey());
            List<Category> innerCategories = innerCategoriesQuery.getResultList();

            System.out.println(categoryName);
            for (Category innerCategory : innerCategories) {
                System.out.println("- " + innerCategory);
            }

        }catch (NoResultException e){
            System.out.println("Неправильно введено название категории!");
            return false;
        }

        manager.close();
        factory.close();
        return true;
    }
    public static void show(){
        TypedQuery<Category> categoryTypedQuery = manager.createQuery("select c from Category c", Category.class);
        List<Category> categoryList = categoryTypedQuery.getResultList();

        for (Category category : categoryList) {
            for(int i = 0; i < category.getHierarchyLevel().intValue(); i++){
                System.out.print("- ");
            }
            System.out.println(category);
        }

        manager.close();
        factory.close();
    }
}
