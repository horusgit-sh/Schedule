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
    private static final Set<String> VALID_TYPES = Set.of("Přednáška", "Cvičení");
    private static final String API_URL = "https://stag-demo.uhk.cz/ws/services/rest2/rozvrhy/getRozvrhByMistnost";
    private Model tableModel;

    public Viewer() {
        initializeFrame();
        JPanel topPanel = createTopPanel();
        JTable table = createTable();
        layoutComponents(topPanel, new JScrollPane(table));
    }

    private void initializeFrame() {
        setTitle("Prohlížeč rozvrhu místností");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        JComboBox<String> roomBox = new JComboBox<>(new String[]{"J1", "J2", "J3"});
        JButton loadButton = new JButton("Load");

        panel.add(new JLabel("Mistnost:"));
        panel.add(roomBox);
        panel.add(loadButton);

        loadButton.addActionListener(e -> loadSchedule((String) roomBox.getSelectedItem()));
        return panel;
    }

    private JTable createTable() {
        tableModel = new Model(new ArrayList<>());
        return new JTable(tableModel);
    }

    private void layoutComponents(JPanel topPanel, JScrollPane scrollPane) {
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadSchedule(String room) {
        try {
            String urlStr = String.format("%s?semestr=%%25&budova=J&mistnost=%s&outputFormat=JSON",
                    API_URL, room);
            var reader = new InputStreamReader(URI.create(urlStr).toURL().openStream(),
                    StandardCharsets.UTF_8);

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            List<Item> items = parseScheduleData(root.getAsJsonArray("rozvrhovaAkce"));
            tableModel.setData(items);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error occurred while loading the data: " + ex.getMessage());
        }
    }

    private List<Item> parseScheduleData(JsonArray jsonArray) {
        List<Item> items = new ArrayList<>();

        for (JsonElement el : jsonArray) {
            JsonObject obj = el.getAsJsonObject();
            if (!VALID_TYPES.contains(obj.get("typAkce").getAsString())) {
                continue;
            }

            items.add(new Item(
                    obj.get("predmet").getAsString(),
                    obj.get("nazev").getAsString(),
                    obj.get("den").getAsString(),
                    obj.getAsJsonObject("hodinaSkutOd").get("value").getAsString(),
                    obj.getAsJsonObject("hodinaSkutDo").get("value").getAsString(),
                    obj.get("vsichniUciteleJmenaTituly").getAsString()
            ));
        }
        return items;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Viewer().setVisible(true));
    }
}