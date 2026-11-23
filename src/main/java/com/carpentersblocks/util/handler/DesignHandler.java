package com.carpentersblocks.util.handler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.carpentersblocks.CarpentersBlocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.Identifier;

/**
 * Chisel, bed, flower pot and tile designs: the textures in textures/block/designs.
 * <p>
 * 1.12.2 listed them by reading the mod jar. The build writes the list to designs.json instead, since the server has
 * no client resources to look them up in, and both sides need the same list to cycle designs.
 */
public final class DesignHandler {

    public enum DesignType {
        CHISEL("chisel"),
        BED("bed"),
        FLOWERPOT("flowerpot"),
        TILE("tile");

        private final String folder;

        DesignType(String folder) {
            this.folder = folder;
        }
    }

    private static final String LIST_RESOURCE = "/assets/" + CarpentersBlocks.MOD_ID + "/designs.json";
    private static final Map<DesignType, List<String>> designs = new EnumMap<>(DesignType.class);

    private DesignHandler() {}

    /**
     * Reads the design list written by the build.
     */
    public static void init() {
        for (DesignType type : DesignType.values()) {
            designs.put(type, new ArrayList<>());
        }
        try (InputStream stream = DesignHandler.class.getResourceAsStream(LIST_RESOURCE)) {
            if (stream == null) {
                CarpentersBlocks.LOGGER.error("Design list {} is missing", LIST_RESOURCE);
                return;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (DesignType type : DesignType.values()) {
                JsonArray names = json.has(type.folder) ? json.getAsJsonArray(type.folder) : new JsonArray();
                for (JsonElement name : names) {
                    designs.get(type).add(name.getAsString());
                }
            }
        } catch (Exception e) {
            CarpentersBlocks.LOGGER.error("Could not read design list {}", LIST_RESOURCE, e);
        }
        CarpentersBlocks.LOGGER.info("Designs found: Bed({}), Chisel({}), FlowerPot({}), Tile({})",
            designs.get(DesignType.BED).size(), designs.get(DesignType.CHISEL).size(),
            designs.get(DesignType.FLOWERPOT).size(), designs.get(DesignType.TILE).size());
    }

    public static List<String> getListForType(DesignType type) {
        return designs.getOrDefault(type, List.of());
    }

    /**
     * Gets a design's texture in the block atlas.
     */
    public static Identifier getTexture(DesignType type, String design) {
        return Identifier.fromNamespaceAndPath(CarpentersBlocks.MOD_ID, "block/designs/" + type.folder + "/" + design.toLowerCase(Locale.ROOT));
    }

    /**
     * Gets a design as a number for render data: its place in the list plus one, or 0 for none.
     */
    public static int getDesignNumber(DesignType type, String design) {
        return design == null ? 0 : getListForType(type).indexOf(design) + 1;
    }

    /**
     * Gets a design from its render data number, or null for none (or one no longer installed).
     */
    public static String getDesign(DesignType type, int number) {
        List<String> list = getListForType(type);
        return number > 0 && number <= list.size() ? list.get(number - 1) : null;
    }

    /**
     * Returns name of next design in list.
     */
    public static String getNext(DesignType type, String design) {
        List<String> list = getListForType(type);
        if (list.isEmpty()) {
            return design;
        }
        int idx = list.indexOf(design) + 1;
        return list.get(idx >= list.size() ? 0 : idx);
    }

    /**
     * Returns name of previous design in list.
     */
    public static String getPrev(DesignType type, String design) {
        List<String> list = getListForType(type);
        if (list.isEmpty()) {
            return design;
        }
        int idx = design == null || design.isEmpty() ? list.size() - 1 : list.indexOf(design) - 1;
        return list.get(idx < 0 ? list.size() - 1 : idx);
    }

}
