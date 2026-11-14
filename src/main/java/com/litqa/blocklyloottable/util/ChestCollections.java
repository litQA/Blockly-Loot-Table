package com.litqa.blocklyloottable.util;

import net.mcreator.minecraft.DataListEntry;
import net.mcreator.ui.init.L10N;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Loads the bundled catalogue of vanilla chest loot tables.
 */
public final class ChestCollections {

    private static final String RESOURCE = "/loottable/chest_collections.tsv";

    private ChestCollections() {
    }

    /**
     * Builds searchable selector entries using localized display names.
     *
     * @return chest entries in catalogue order
     */
    public static List<DataListEntry> entries() {
        List<DataListEntry> result = new ArrayList<>();
        boolean chinese = L10N.getLocale().getLanguage().toLowerCase(Locale.ROOT).startsWith("zh");
        try (InputStream stream = ChestCollections.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                return result;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\t", -1);
                    if (parts.length < 3 || parts[0].isBlank()) {
                        continue;
                    }
                    DataListEntry entry = new DataListEntry.Dummy(parts[0].trim());
                    entry.setReadableName((chinese ? parts[2] : parts[1]).trim());
                    entry.setDescription(parts[0].trim());
                    result.add(entry);
                }
            }
        } catch (Exception ignored) {
            // The selector simply stays empty if the bundled catalogue cannot be read.
        }
        return result;
    }
}
