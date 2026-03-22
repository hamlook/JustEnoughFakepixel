package com.jef.justenoughfakepixel.features.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PetCache {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static PetCache INSTANCE;
    public static PetCache getInstance() {
        if (INSTANCE == null) INSTANCE = new PetCache();
        return INSTANCE;
    }


    private PetCache() {}

    public static String normalizePetName(String name) {
        if (name == null) return "";

        return name
                .replace("✦", "")   // remove skinned pet symbol
                .replace("’", "'")  // normalize apostrophe
                .trim();
    }

    private File file;
    private static final int MAX_ENTRIES = 200;

    private final Map<String, CachedPet> pets = new LinkedHashMap<String, CachedPet>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedPet> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public void initFile(File configDir) {
        file = new File(configDir, "pet_cache.json");
    }

    public void load() {
        if (file == null || !file.exists()) return;
        try (Reader r = new FileReader(file)) {
            Type type = new TypeToken<Map<String, CachedPet>>(){}.getType();
            Map<String, CachedPet> loaded = GSON.fromJson(r, type);
            if (loaded != null) {
                // sanitize corrupted § on load
                for (CachedPet pet : loaded.values()) {
                    if (pet.formattedName != null)
                        pet.formattedName = pet.formattedName.replace("Â§", "§");
                }
                pets.putAll(loaded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void warmupTextures() {
        for (CachedPet pet : pets.values()) {
            if (pet.textureValue == null || pet.textureValue.isEmpty()) continue;
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", pet.textureValue));
            Minecraft.getMinecraft().getSkinManager().loadProfileTextures(profile, null, false);
        }
    }

    private void save() {
        if (file == null) return;
        try {
            if (!file.exists()) file.createNewFile();
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
                w.write(GSON.toJson(pets));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(String baseName, String formattedName, String textureValue) {
        baseName = normalizePetName(baseName);

        String starSuffix = "";
        int starIndex = formattedName.indexOf("✦");
        if (starIndex > 0) {
            String before = formattedName.substring(0, starIndex);
            if (before.length() >= 2 && before.charAt(before.length() - 2) == '\u00a7') {
                starSuffix = " " + before.substring(before.length() - 2) + "✦";
            } else {
                starSuffix = " ✦";
            }
        }

        formattedName = formattedName.replace("✦", "").trim();
        if (!starSuffix.isEmpty()) formattedName = formattedName + starSuffix;

        CachedPet existing = pets.get(baseName);
        if (existing != null
                && existing.formattedName.equals(formattedName)
                && existing.textureValue.equals(textureValue)) return;

        CachedPet pet     = new CachedPet();
        pet.baseName      = baseName;
        pet.formattedName = formattedName;
        pet.textureValue  = textureValue;
        pets.put(baseName, pet);
        save();
    }

    public CachedPet get(String baseName) {
        return pets.get(normalizePetName(baseName));
    }

    public boolean hasTexture(String baseName) {
        CachedPet p = pets.get(normalizePetName(baseName));
        return p != null && p.textureValue != null && !p.textureValue.isEmpty();
    }
}