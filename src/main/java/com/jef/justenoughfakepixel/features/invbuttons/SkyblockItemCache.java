package com.jef.justenoughfakepixel.features.invbuttons;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SkyblockItemCache {

    private static final SkyblockItemCache INSTANCE = new SkyblockItemCache();
    public static SkyblockItemCache getInstance() { return INSTANCE; }

    private static final String REPO_ZIP_URL =
            "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/master.zip";

    // internalname -> full item JsonObject
    private final TreeMap<String, JsonObject> itemMap = new TreeMap<>();
    // internalname -> texture hash (only skull items)
    private final LinkedHashMap<String, String> skullMap = new LinkedHashMap<>();

    private volatile boolean loaded = false;

    private final ExecutorService loader = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "JEF-ItemCache"); t.setDaemon(true); return t;
    });

    private SkyblockItemCache() {}

    public void loadAsync() {
        if (loaded) return;
        loader.submit(this::loadSync);
    }

    private void loadSync() {
        try {
            System.out.println("[JEF] Downloading NEU item repo...");
            HttpURLConnection conn = (HttpURLConnection) new URL(REPO_ZIP_URL).openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("User-Agent", "JEF/1.0");

            int items = 0, skulls = 0;
            JsonParser parser = new JsonParser();

            try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(conn.getInputStream()))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (!name.endsWith(".json") || !name.contains("/items/")) { zip.closeEntry(); continue; }

                    String filename = name.substring(name.lastIndexOf('/') + 1);
                    String internalName = filename.substring(0, filename.length() - 5);

                    byte[] bytes = readEntry(zip);
                    try {
                        JsonObject json = parser.parse(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
                        if (!json.has("itemid")) { zip.closeEntry(); continue; }

                        synchronized (itemMap) { itemMap.put(internalName, json); }
                        items++;

                        // Detect skull items: itemid ends with "skull" and nbttag has texture
                        String itemid = json.get("itemid").getAsString().toLowerCase();
                        if ((itemid.endsWith("skull") || itemid.endsWith("skull_item")) && json.has("nbttag")) {
                            String hash = extractSkullHash(json.get("nbttag").getAsString());
                            if (hash != null) {
                                synchronized (skullMap) { skullMap.put(internalName, hash); }
                                skulls++;
                            }
                        }
                    } catch (Exception ignored) {}
                    zip.closeEntry();
                }
            }

            loaded = true;
            System.out.println("[JEF] Loaded " + items + " items, " + skulls + " skulls from NEU repo.");
        } catch (Exception e) {
            System.err.println("[JEF] Item cache failed: " + e.getMessage());
            loaded = true;
        }
    }

    /**
     * Extracts the texture hash from a raw NBT string like:
     * {SkullOwner:{Properties:{textures:[{Value:"...base64..."}]}}}
     * Decodes the base64, then finds /texture/HASH
     */
    private String extractSkullHash(String nbtString) {
        try {
            // Find Value:" in the nbt string
            int vi = nbtString.indexOf("Value:\"");
            if (vi == -1) vi = nbtString.indexOf("Value:'");
            if (vi == -1) return null;
            vi += 7;
            char closeChar = nbtString.charAt(vi - 1);
            int end = nbtString.indexOf(closeChar, vi);
            if (end == -1) return null;
            String b64 = nbtString.substring(vi, end);
            String decoded = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
            int ti = decoded.indexOf("/texture/");
            if (ti == -1) return null;
            ti += "/texture/".length();
            int hashEnd = decoded.indexOf("\"", ti);
            if (hashEnd == -1) hashEnd = decoded.length();
            String hash = decoded.substring(ti, hashEnd).trim();
            return hash.isEmpty() ? null : hash;
        } catch (Exception e) { return null; }
    }

    private byte[] readEntry(ZipInputStream zip) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = zip.read(buf)) != -1) baos.write(buf, 0, n);
        return baos.toByteArray();
    }

    public boolean isLoaded() { return loaded; }

    public JsonObject getItemJson(String internalName) {
        synchronized (itemMap) { return itemMap.get(internalName); }
    }

    /** All item internal names, sorted */
    public Set<String> getAllItemIds() {
        synchronized (itemMap) { return new LinkedHashSet<>(itemMap.keySet()); }
    }

    /** Skull items: internalName -> texture hash */
    public Map<String, String> getSkullItems() {
        synchronized (skullMap) { return new LinkedHashMap<>(skullMap); }
    }
}