package laundry;

/** Small holder used by combo boxes (id + display label + optional price). */
public class Item {

    public final int id;
    public final String label;
    public final double price;

    public Item(int id, String label, double price) {
        this.id = id;
        this.label = label;
        this.price = price;
    }

    @Override
    public String toString() {
        return label;
    }
}
