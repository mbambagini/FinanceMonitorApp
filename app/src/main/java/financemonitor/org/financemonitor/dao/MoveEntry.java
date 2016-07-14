package financemonitor.org.financemonitor.dao;

import java.util.Date;

public class MoveEntry {

    private double amount = 0.0;
    private int currency = 0;
    private Date when;
    private long id = 0;
    private CategoryEntry category;

    public CategoryEntry getCategory() {
        return category;
    }

    public void setCategory(CategoryEntry category) {
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

}
