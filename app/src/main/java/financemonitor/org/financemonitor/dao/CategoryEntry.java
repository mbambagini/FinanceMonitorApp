package financemonitor.org.financemonitor.dao;

public class CategoryEntry {

    public long getIdEntry() {
        return idEntry;
    }

    public void setIdEntry(long idEntry) {
        this.idEntry = idEntry;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private long idEntry = 0;
    private String entry;
    private String category;

}
