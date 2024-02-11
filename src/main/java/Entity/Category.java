package Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String category_name;

    @Column(name = "left_key")
    private Long leftKey;

    @Column(name = "right_key")
    private Long rightKey;

    @Column(name = "hierarchy_level")
    private Long hierarchyLevel;

    public Long getId() {
        return id;
    }

    public String getName() {
        return category_name;
    }

    public void setName(String category_name) {
        this.category_name = category_name;
    }

    public Long getLeftKey() {
        return leftKey;
    }

    public void setLeftKey(Long leftKey) {
        this.leftKey = leftKey;
    }

    public Long getRightKey() {
        return rightKey;
    }

    public void setRightKey(Long rightKey) {
        this.rightKey = rightKey;
    }

    public Long getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(Long hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    @Override
    public String toString() {
        return String.format("%s \t (%d, %d, %d)", getName(), getLeftKey(), getRightKey(), getHierarchyLevel());
    }

    @Override
    public int hashCode() {
        return 31 * (getName().hashCode() + getLeftKey().hashCode() + getRightKey().hashCode() + getHierarchyLevel().hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || obj.getClass() != this.getClass()) return false;
        Category category = (Category) obj;
        return this.getName().equals(category.getName())
                && this.getRightKey().equals(category.getRightKey())
                && this.getLeftKey().equals(category.getLeftKey())
                && this.getHierarchyLevel().equals(category.getHierarchyLevel());
    }
}
