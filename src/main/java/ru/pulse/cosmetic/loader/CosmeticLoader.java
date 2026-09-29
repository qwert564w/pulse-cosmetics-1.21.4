package ru.pulse.cosmetic.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Identifier;
import ru.pulse.Pulse;
import ru.pulse.cosmetic.model.CosmeticModel;
import ru.pulse.cosmetic.model.ModelPosition;

public class CosmeticLoader {
    private static CosmeticLoader instance;
    private final Map<Integer, CosmeticModel> loaded = new ConcurrentHashMap<>();

    public static CosmeticLoader getInstance() {
        if (instance == null) instance = new CosmeticLoader();
        return instance;
    }

    public CosmeticModel loadFromJson(String json) {
        return loadFromJson(json, null, -1);
    }

    public CosmeticModel loadFromJson(String json, Identifier forcedTexture, int forcedId) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return loadFromJson(root, forcedTexture, forcedId);
        } catch (Exception e) {
            Pulse.getLOGGER().error("Error loading cosmetic from JSON", e);
            return null;
        }
    }

    public CosmeticModel loadFromJson(JsonObject root, Identifier forcedTexture, int forcedId) {
        try {
            if (!root.has("name") || !root.has("model")) {
                Pulse.getLOGGER().error("Invalid cosmetic JSON: missing name or model");
                return null;
            }
            String name = root.get("name").getAsString();
            int id = forcedId > 0 ? forcedId : (root.has("id") ? root.get("id").getAsInt() : name.hashCode());
            int category = root.has("category") ? root.get("category").getAsInt() : 1;
            CosmeticModel model = new CosmeticModel(name, id, category);
            model.setRawModelJson(root.getAsJsonObject("model").toString());

            if (forcedTexture != null) {
                model.setTextureId(forcedTexture);
            } else {
                // Prefer packaged PNG assets (1.21.4 friendly)
                Identifier assetTex = Identifier.of("pulse", "textures/cosmetics/cosmetic_" + extractIndex(name, id) + ".png");
                model.setTextureId(assetTex);
            }

            if (root.has("pos")) {
                model.setPosition(ModelPosition.getById(root.get("pos").getAsInt()));
            }
            if (root.has("scale")) model.setScale(root.get("scale").getAsFloat());
            if (root.has("x")) model.setX(root.get("x").getAsFloat());
            if (root.has("y")) model.setY(root.get("y").getAsFloat());
            if (root.has("z")) model.setZ(root.get("z").getAsFloat());
            if (root.has("yaw")) model.setYaw(root.get("yaw").getAsFloat());
            if (root.has("pitch")) model.setPitch(root.get("pitch").getAsFloat());
            if (root.has("roll")) model.setRoll(root.get("roll").getAsFloat());
            if (root.has("height")) model.setHeight(root.get("height").getAsFloat());
            if (root.has("previewScale")) model.setPreviewScale(root.get("previewScale").getAsFloat());
            if (root.has("previewY")) model.setPreviewY(root.get("previewY").getAsFloat());
            if (root.has("animation")) {
                model.setAnimationJson(root.getAsJsonObject("animation"));
            } else if (root.has("animations")) {
                model.setAnimationJson(root.getAsJsonObject("animations"));
            }

            loaded.put(id, model);
            return model;
        } catch (Exception e) {
            Pulse.getLOGGER().error("Error loading cosmetic", e);
            return null;
        }
    }

    private static int extractIndex(String name, int id) {
        // cosmetic files are cosmetic_N.json — try parse trailing digits from common patterns
        return Math.abs(id) % 256;
    }

    public CosmeticModel loadFromJson(JsonObject root) {
        return loadFromJson(root, null, -1);
    }
}
