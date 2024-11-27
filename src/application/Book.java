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
    }
    
    public Book(Book other) {
        this(other.getIsbn(), other.getTitle(), other.getPubdate(), other.getPubid(),
             other.getCost(), other.getRetail(), other.getDiscount(), other.getCategory());
    }
    
    public String getIsbn() { return isbn.get(); }
    public String getTitle() { return title.get(); }
    public LocalDate getPubdate() { return pubdate.get(); }
    public String getPubid() { return pubid.get(); }
    public double getCost() { return cost.get(); }
    public double getRetail() { return retail.get(); }
    public double getDiscount() { return discount.get(); }
    public String getCategory() { return category.get(); }
    
    public void setCost(double cost) { this.cost.set(cost); }
    public void setRetail(double retail) { this.retail.set(retail); }
    public void setCategory(String category) { this.category.set(category); }

    public StringProperty isbnProperty() { return isbn; }
    public StringProperty titleProperty() { return title; }
    public ObjectProperty<LocalDate> pubdateProperty() { return pubdate; }
    public StringProperty pubidProperty() { return pubid; }
    public DoubleProperty costProperty() { return cost; }
    public DoubleProperty retailProperty() { return retail; }
    public DoubleProperty discountProperty() { return discount; }
    public StringProperty categoryProperty() { return category; }
    
    public Book copy() {
        return new Book(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return getPubid() == book.getPubid() &&
               Double.compare(book.getCost(), getCost()) == 0 &&
               Double.compare(book.getRetail(), getRetail()) == 0 &&
               Double.compare(book.getDiscount(), getDiscount()) == 0 &&
               Objects.equals(getIsbn(), book.getIsbn()) &&
               Objects.equals(getTitle(), book.getTitle()) &&
               Objects.equals(getPubdate(), book.getPubdate()) &&
               Objects.equals(getCategory(), book.getCategory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIsbn(), getTitle(), getPubdate(), getPubid(), getCost(), getRetail(), getDiscount(), getCategory());
    }
}
