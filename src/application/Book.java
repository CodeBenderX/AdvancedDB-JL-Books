package application;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.Objects;

public class Book {
    private final StringProperty isbn;
    private final StringProperty title;
    private final ObjectProperty<LocalDate> pubdate;
    private final StringProperty pubid;
    private final DoubleProperty cost;
    private final DoubleProperty retail;
    private final DoubleProperty discount;
    private final StringProperty category;
    private final BooleanProperty changed;
    private final BooleanProperty costChanged;
    private final BooleanProperty retailChanged;
    private final BooleanProperty categoryChanged;

    public Book(String isbn, String title, LocalDate pubdate, String pubid, 
                double cost, double retail, double discount, String category) {
        this.isbn = new SimpleStringProperty(isbn);
        this.title = new SimpleStringProperty(title);
        this.pubdate = new SimpleObjectProperty<>(pubdate);
        this.pubid = new SimpleStringProperty(pubid);
        this.cost = new SimpleDoubleProperty(cost);
        this.retail = new SimpleDoubleProperty(retail);
        this.discount = new SimpleDoubleProperty(discount);
        this.category = new SimpleStringProperty(category);
        this.changed = new SimpleBooleanProperty(false);
        this.costChanged = new SimpleBooleanProperty(false);
        this.retailChanged = new SimpleBooleanProperty(false);
        this.categoryChanged = new SimpleBooleanProperty(false);
    }
    
    public Book(Book other) {
        this(other.getIsbn(), other.getTitle(), other.getPubdate(), other.getPubid(),
             other.getCost(), other.getRetail(), other.getDiscount(), other.getCategory());
        this.changed.set(other.isChanged());
        this.costChanged.set(other.isCostChanged());
        this.retailChanged.set(other.isRetailChanged());
        this.categoryChanged.set(other.isCategoryChanged());
    }
    
    public String getIsbn() { return isbn.get(); }
    public String getTitle() { return title.get(); }
    public LocalDate getPubdate() { return pubdate.get(); }
    public String getPubid() { return pubid.get(); }
    public double getCost() { return cost.get(); }
    public double getRetail() { return retail.get(); }
    public double getDiscount() { return discount.get(); }
    public String getCategory() { return category.get(); }
    public boolean isChanged() { return changed.get(); }
    public boolean isCostChanged() { return costChanged.get(); }
    public boolean isRetailChanged() { return retailChanged.get(); }
    public boolean isCategoryChanged() { return categoryChanged.get(); }
    
    public void setCost(double cost) { 
        if (this.cost.get() != cost) {
            this.cost.set(cost); 
            this.costChanged.set(true);
            this.changed.set(true);
        }
    }
    public void setRetail(double retail) { 
        if (this.retail.get() != retail) {
            this.retail.set(retail); 
            this.retailChanged.set(true);
            this.changed.set(true);
        }
    }
    public void setCategory(String category) { 
        if (!Objects.equals(this.category.get(), category)) {
            this.category.set(category); 
            this.categoryChanged.set(true);
            this.changed.set(true);
        }
    }
    public void setChanged(boolean changed) { 
        this.changed.set(changed); 
    }
    public void setCostChanged(boolean changed) {
        this.costChanged.set(changed);
    }
    public void setRetailChanged(boolean changed) {
        this.retailChanged.set(changed);
    }
    public void setCategoryChanged(boolean changed) {
        this.categoryChanged.set(changed);
    }

    public StringProperty isbnProperty() { return isbn; }
    public StringProperty titleProperty() { return title; }
    public ObjectProperty<LocalDate> pubdateProperty() { return pubdate; }
    public StringProperty pubidProperty() { return pubid; }
    public DoubleProperty costProperty() { return cost; }
    public DoubleProperty retailProperty() { return retail; }
    public DoubleProperty discountProperty() { return discount; }
    public StringProperty categoryProperty() { return category; }
    public BooleanProperty changedProperty() { return changed; }
    public BooleanProperty costChangedProperty() { return costChanged; }
    public BooleanProperty retailChangedProperty() { return retailChanged; }
    public BooleanProperty categoryChangedProperty() { return categoryChanged; }
    
    public Book copy() {
        return new Book(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(getIsbn(), book.getIsbn()) &&
               Objects.equals(getTitle(), book.getTitle()) &&
               Objects.equals(getPubdate(), book.getPubdate()) &&
               Objects.equals(getPubid(), book.getPubid()) &&
               Double.compare(book.getCost(), getCost()) == 0 &&
               Double.compare(book.getRetail(), getRetail()) == 0 &&
               Double.compare(book.getDiscount(), getDiscount()) == 0 &&
               Objects.equals(getCategory(), book.getCategory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIsbn(), getTitle(), getPubdate(), getPubid(), getCost(), getRetail(), getDiscount(), getCategory());
    }
}
