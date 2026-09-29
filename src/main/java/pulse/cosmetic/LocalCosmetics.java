package pulse.cosmetic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;
import ru.pulse.cosmetic.loader.CosmeticLoader;
import ru.pulse.cosmetic.model.CosmeticModel;

public final class LocalCosmetics {
    private static final int MAX_SCAN_INDEX = 256;
    private static final List<Entry> ENTRIES = loadEntries();
    private static final LinkedHashMap<String, Integer> SELECTED = new LinkedHashMap<>();
    private static final Map<Integer, CosmeticModel> MODELS = new HashMap<>();

    private LocalCosmetics() {}

    public static int size() { return ENTRIES.size(); }

    public static String name(int i) { return entry(i).name; }

    public static String type(int i) { return entry(i).type; }

    public static Identifier texture(int i) {
        return Identifier.of("pulse", "textures/cosmetics/cosmetic_" + entry(i).index + ".png");
    }

    public static List<Integer> selectedIndices() {
        return List.copyOf(SELECTED.values());
    }

    public static void toggle(int i) {
        if (i < 0 || i >= ENTRIES.size()) return;
        String type = type(i);
        Integer old = SELECTED.get(type);
        if (old != null && old == i) {
            SELECTED.remove(type);
        } else {
            SELECTED.put(type, i);
            if (!"cape".equals(type)) {
                model(i);
            }
        }
    }

    /** Select first non-cape cosmetic for quick testing */
    public static void selectDefault() {
        for (int i = 0; i < ENTRIES.size(); i++) {
            if (!"cape".equals(type(i))) {
                SELECTED.put(type(i), i);
                model(i);
                break;
            }
        }
    }

    public static CosmeticModel modelFor(int i) {
        return model(i);
    }

    private static CosmeticModel model(int i) {
        int resourceIndex = entry(i).index;
        return MODELS.computeIfAbsent(resourceIndex, k -> {
            try (InputStream in = LocalCosmetics.class.getResourceAsStream(
                    "/assets/pulse/cosmetics/models/cosmetic_" + k + ".json")) {
                if (in == null) return null;
                String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                Identifier tex = Identifier.of("pulse", "textures/cosmetics/cosmetic_" + k + ".png");
                return CosmeticLoader.getInstance().loadFromJson(json, tex, k);
            } catch (Exception e) {
                return null;
            }
        });
    }

    private static Entry entry(int i) {
        if (i < 0 || i >= ENTRIES.size()) {
            throw new IndexOutOfBoundsException("Cosmetic index " + i);
        }
        return ENTRIES.get(i);
    }

    private static List<Entry> loadEntries() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < MAX_SCAN_INDEX; i++) {
            String path = "/assets/pulse/cosmetics/models/cosmetic_" + i + ".json";
            try (InputStream in = LocalCosmetics.class.getResourceAsStream(path)) {
                if (in != null) {
                    String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject object = JsonParser.parseString(json).getAsJsonObject();
                    String rawName = object.has("name") ? object.get("name").getAsString() : "Cosmetic " + (i + 1);
                    String rawType = object.has("type") ? object.get("type").getAsString() : "";
                    int pos = object.has("pos") ? object.get("pos").getAsInt() : -1;
                    entries.add(new Entry(i, displayName(rawName, i), classifyType(i, rawName, rawType, pos)));
                }
            } catch (Exception ignored) {}
        }
        entries.sort(Comparator.comparingInt(e -> e.index));
        return List.copyOf(entries);
    }

    private static String displayName(String rawName, int index) {
        String name = rawName == null || rawName.isBlank() ? "Cosmetic " + (index + 1) : rawName;
        if (name.startsWith("pulse_")) name = name.substring(6);
        return name.replace('_', ' ').trim();
    }

    private static String classifyType(int index, String name, String rawType, int pos) {
        if (rawType != null && !rawType.isBlank()) return rawType.trim().toLowerCase();
        String lower = name == null ? "" : name.toLowerCase();
        if (lower.contains("cape")) return "cape";
        if (lower.contains("wing")) return "wings";
        if (lower.contains("pet") || lower.contains("bee") || lower.contains("radish")) return "pet";
        if (lower.contains("hat") || lower.contains("nimb") || pos == 2) return "hat";
        if (index <= 13) return "cape";
        if (index <= 26) return "wings";
        if (index <= 37) return "bodywear";
        if (index <= 49) return "pet";
        return "bodywear";
    }

    private static final class Entry {
        final int index;
        final String name;
        final String type;
        Entry(int index, String name, String type) {
            this.index = index;
            this.name = name;
            this.type = type;
        }
    }
}
