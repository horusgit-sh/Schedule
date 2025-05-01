package app;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class Model extends AbstractTableModel {
    private List<Item> data;
    private final String[] columns = {"Predmet", "Nazev", "Den", "Zacatek", "Konec", "Ucitel"};

    public Model(List<Item> data) {
        this.data = data;
    }

    public void setData(List<Item> data) {
        this.data = data;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Item item = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> item.predmet();
            case 1 -> item.nazev();
            case 2 -> item.den();
            case 3 -> item.casOd();
            case 4 -> item.casDo();
            case 5 -> item.ucitel();
            default -> null;
        };
    }
}