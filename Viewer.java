package app;

import com.google.gson.*;
import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Viewer extends JFrame {
    private static final Set<String> VALID_TYPES = Set.of("Přednáška", "Cvičení"); //del to view full schedule
    private static final String API_URL = "https://stag-demo.uhk.cz/ws/services/rest2/rozvrhy/getRozvrhByMistnost";
    private final Model tableModel = new Model(new ArrayList<>());

    public Viewer() {
        setTitle("Rozvrh místností");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout());

        JComboBox<String> roomBox = new JComboBox<>(new String[]{"J1", "J2", "J3"});
        JButton loadButton = new JButton("Load");
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Místnost:"));
        topPanel.add(roomBox);
        topPanel.add(loadButton);
        add(topPanel, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadButton.addActionListener(e -> loadSchedule((String) roomBox.getSelectedItem()));
    }

    private void loadSchedule(String room) {
        try (var reader = new InputStreamReader(URI.create(String.format("%s?semestr=%%25&budova=J&mistnost=%s&outputFormat=JSON", API_URL, room)).toURL().openStream(), StandardCharsets.UTF_8)) {

            JsonArray array = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("rozvrhovaAkce");

            List<Item> items = new ArrayList<>();
            for (JsonElement el : array) {
                JsonObject o = el.getAsJsonObject();
                if (!VALID_TYPES.contains(o.get("typAkce").getAsString())) continue; //del to view full schedule
                items.add(new Item(
                        o.get("predmet").getAsString(),
                        o.get("nazev").getAsString(),
                        o.get("den").getAsString(),
                        o.getAsJsonObject("hodinaSkutOd").get("value").getAsString(),
                        o.getAsJsonObject("hodinaSkutDo").get("value").getAsString(),
                        o.get("vsichniUciteleJmenaTituly").getAsString()
                ));
            }
            tableModel.setData(items);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Chyba: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Viewer().setVisible(true));
    }
}
