package app;

import com.google.gson.*;
import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class Viewer extends JFrame {
    private static final Set<String> VALID_TYPES = Set.of("Přednáška", "Cvičení"); //del to view full schedule
    private static final String API_URL = "https://stag-demo.uhk.cz/ws/services/rest2/rozvrhy/getRozvrhByMistnost";
    private final Model tableModel = new Model(new ArrayList<>());

    private final Map<String, String[]> roomMap = Map.of(
            "A", generateRooms("A", 30),
            "J", generateRooms("J", 30)
    );

    public Viewer() {
        setTitle("Rozvrh mistnosti");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout());

        JComboBox<String> buildingBox = new JComboBox<>(roomMap.keySet().toArray(new String[0]));
        JComboBox<String> roomBox = new JComboBox<>();
        updateRoomBox(roomBox, "A");

        buildingBox.addActionListener(e -> updateRoomBox(roomBox, (String) buildingBox.getSelectedItem()));

        JButton loadButton = new JButton("Load");
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Budova:"));
        topPanel.add(buildingBox);
        topPanel.add(new JLabel("Mistnost:"));
        topPanel.add(roomBox);
        topPanel.add(loadButton);
        add(topPanel, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadButton.addActionListener(e -> loadSchedule(
                (String) buildingBox.getSelectedItem(),
                (String) roomBox.getSelectedItem()
        ));
    }

    private void loadSchedule(String building, String room) {
        try (var reader = new InputStreamReader(URI.create(String.format("%s?semestr=%%25&budova=%s&mistnost=%s&outputFormat=JSON", API_URL, building, room)).toURL().openStream(), StandardCharsets.UTF_8)) {

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

    private void updateRoomBox(JComboBox<String> roomBox, String building) {
        roomBox.removeAllItems();
        for (String room : roomMap.getOrDefault(building, new String[0])) {
            roomBox.addItem(room);
        }
    }

    private static String[] generateRooms(String prefix, int count) {
        String[] rooms = new String[count];
        for (int i = 0; i < count; i++) {
            rooms[i] = prefix + (i + 1);
        }
        return rooms;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Viewer().setVisible(true));
    }
}
