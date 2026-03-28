package com.jef.justenoughfakepixel.features.dungeons.rooms;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.features.dungeons.DungeonStats;
import com.jef.justenoughfakepixel.init.RegisterEvents;
import com.jef.justenoughfakepixel.utils.data.SkyblockData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RegisterEvents
public class DungeonRoomDetector {

    private static JsonObject roomsJson = null;
    private static int tickCount = 0;
    private static String lastRoomHash = null;
    private static JsonObject lastRoomJson = null;
    private final Executor executor = Executors.newFixedThreadPool(2);

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (JefConfig.feature == null || !JefConfig.feature.dungeons.dungeonRoomOverlay) {
            DungeonRoomOverlay.currentRoomName = null;
            DungeonRoomOverlay.currentRoomNotes = null;
            lastRoomHash = null;
            lastRoomJson = null;
            return;
        }
        if (SkyblockData.getCurrentLocation() != SkyblockData.Location.DUNGEON) return;
        if (DungeonStats.isInBossFight()) return;
        if (++tickCount % 30 != 0) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (roomsJson == null) loadRoomsJson();
        if (roomsJson == null) return;

        executor.execute(() -> {
            try {
                int x = (int) Math.floor(mc.thePlayer.posX);
                int y = (int) Math.floor(mc.thePlayer.posY);
                int z = (int) Math.floor(mc.thePlayer.posZ);

                int top = dungeonTop(x, y, z);
                String blockFreq = blockFrequency(x, top, z);
                if (blockFreq == null) return;

                String md5 = getMD5(blockFreq);
                String floorFreq = floorFrequency(x, top, z);
                String floorHash = getMD5(floorFreq);

                // Box room exception (same as FDR)
                if ("16370f79b2cad049096f881d5294aee6".equals(md5)
                        && !"94fb12c91c4b46bd0c254edadaa49a3d".equals(floorHash)) {
                    floorHash = "e617eff1d7b77faf0f8dd53ec93a220f";
                }

                // Same room, same floor — no change
                if (md5.equals(lastRoomHash) && lastRoomJson != null) {
                    JsonElement jfh = lastRoomJson.get("floorhash");
                    if (jfh == null || (floorHash != null && floorHash.equals(jfh.getAsString()))) return;
                }

                lastRoomHash = md5;

                if (!roomsJson.has(md5)) {
                    if (JefConfig.feature.debug.dungeonRoomDebug) {
                        DungeonRoomOverlay.currentRoomName =
                                "§cUnknown Room §7(" + md5.substring(0, 32) + ")";
                        DungeonRoomOverlay.currentRoomNotes =
                                "§8Hash not in JSON";
                    } else {
                        DungeonRoomOverlay.currentRoomName = null;
                        DungeonRoomOverlay.currentRoomNotes = null;
                    }

                    lastRoomJson = null;
                    return;
                }

                JsonArray arr = roomsJson.get(md5).getAsJsonArray();

                if (arr.size() >= 2) {
                    // Multiple rooms share this hash — use floorHash to disambiguate
                    JsonObject matched = null;
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject obj = arr.get(i).getAsJsonObject();
                        JsonElement jfh = obj.get("floorhash");
                        if (floorHash != null && jfh != null && floorHash.equals(jfh.getAsString())) {
                            matched = obj;
                            break;
                        }
                    }
                    if (matched != null) {
                        lastRoomJson = matched;
                        setOverlay(matched);
                    } else {
                        // Can't disambiguate — show first as fallback
                        lastRoomJson = arr.get(0).getAsJsonObject();
                        setOverlay(lastRoomJson);
                    }
                } else {
                    lastRoomJson = arr.get(0).getAsJsonObject();
                    setOverlay(lastRoomJson);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setOverlay(JsonObject room) {
        String name = room.get("name").getAsString();
        String category = room.get("category").getAsString();
        int secrets = room.get("secrets").getAsInt();

        StringBuilder sb = new StringBuilder();
        sb.append(category).append(" - ").append(name);
        sb.append(" §f[§e").append(secrets).append("§f secrets]");

        JsonElement fairysoul = room.get("fairysoul");
        if (fairysoul != null) sb.append(" §d✿");

        JsonElement notes = room.get("notes");
        if (notes != null) DungeonRoomOverlay.currentRoomNotes = notes.getAsString();
        else DungeonRoomOverlay.currentRoomNotes = null;

        DungeonRoomOverlay.currentRoomName = sb.toString();
    }

    private void loadRoomsJson() {
        try {
            ResourceLocation loc = new ResourceLocation("justenoughfakepixel", "dungeonrooms/dungeonrooms.json");
            InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
            roomsJson = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- Ported directly from FDR Utils ----

    private int dungeonTop(int x, int y, int z) {
        World world = Minecraft.getMinecraft().theWorld;
        for (int i = 255; i >= 78; i--) {
            Block b = world.getBlockState(new BlockPos(x, i, z)).getBlock();
            if (b != Blocks.air && checkPlatform(x, i, z)) return i;
        }
        return -1;
    }

    private int dungeonBottom(int x, int z) {
        World world = Minecraft.getMinecraft().theWorld;
        for (int i = 0; i <= 68; i++) {
            Block b = world.getBlockState(new BlockPos(x, i, z)).getBlock();
            if (b == Blocks.bedrock || b == Blocks.stone) return i;
        }
        return -1;
    }

    private int dungeonHeight(int x, int z) {
        return dungeonTop(x, 68, z) - dungeonBottom(x, z);
    }

    private boolean checkPlatform(int x, int y, int z) {
        World world = Minecraft.getMinecraft().theWorld;
        int n=0, s=0, e=0, w=0;
        for (int j = 0; j < 10; j++) {
            if (world.getBlockState(new BlockPos(x,y,z-j)).getBlock() != Blocks.air) n++;
            if (world.getBlockState(new BlockPos(x,y,z+j)).getBlock() != Blocks.air) s++;
            if (world.getBlockState(new BlockPos(x+j,y,z)).getBlock() != Blocks.air) e++;
            if (world.getBlockState(new BlockPos(x-j,y,z)).getBlock() != Blocks.air) w++;
        }
        return (n==10 || s==10 || e==10 || w==10);
    }

    private int endOfRoom(int x, int y, int z, String dir) {
        World world = Minecraft.getMinecraft().theWorld;
        for (int i = 1; i <= 200; i++) {
            BlockPos bp;
            int coord;
            switch (dir) {
                case "n": bp = new BlockPos(x,y,z-i); coord = z-i+1;
                    if (world.getBlockState(bp).getBlock()==Blocks.air || checkPlatform(x,y+1,z-i)
                            || Math.abs(dungeonHeight(x,z-i)-dungeonHeight(x,z-i+1))>3) return coord; break;
                case "s": bp = new BlockPos(x,y,z+i); coord = z+i-1;
                    if (world.getBlockState(bp).getBlock()==Blocks.air || checkPlatform(x,y+1,z+i)
                            || Math.abs(dungeonHeight(x,z+i)-dungeonHeight(x,z+i-1))>3) return coord; break;
                case "e": bp = new BlockPos(x+i,y,z); coord = x+i-1;
                    if (world.getBlockState(bp).getBlock()==Blocks.air || checkPlatform(x+i,y+1,z)
                            || Math.abs(dungeonHeight(x+i,z)-dungeonHeight(x+i-1,z))>3) return coord; break;
                case "w": bp = new BlockPos(x-i,y,z); coord = x-i+1;
                    if (world.getBlockState(bp).getBlock()==Blocks.air || checkPlatform(x-i,y+1,z)
                            || Math.abs(dungeonHeight(x-i,z)-dungeonHeight(x-i+1,z))>3) return coord; break;
            }
        }
        return -1;
    }

    private int northWidth(int x, int y, int z) { int nz=endOfRoom(x,y,z,"n"); return endOfRoom(x,y,nz,"e")-endOfRoom(x,y,nz,"w"); }
    private int southWidth(int x, int y, int z) { int sz=endOfRoom(x,y,z,"s"); return endOfRoom(x,y,sz,"e")-endOfRoom(x,y,sz,"w"); }
    private int eastWidth(int x, int y, int z)  { int ex=endOfRoom(x,y,z,"e"); return endOfRoom(ex,y,z,"s")-endOfRoom(ex,y,z,"n"); }
    private int westWidth(int x, int y, int z)  { int wx=endOfRoom(x,y,z,"w"); return endOfRoom(wx,y,z,"s")-endOfRoom(wx,y,z,"n"); }

    private String getSize(int x, int y, int z) {
        int n=northWidth(x,y,z), s=southWidth(x,y,z), e=eastWidth(x,y,z), w=westWidth(x,y,z);
        if (n==s && s==e && e==w) { if(n==30) return "1x1"; if(n==62) return "2x2"; }
        else if (n==s && e==w) {
            if((n==62&&e==30)||(n==30&&e==62)) return "1x2";
            if((n==94&&e==30)||(n==30&&e==94)) return "1x3";
            if((n==126&&e==30)||(n==30&&e==126)) return "1x4";
        } else {
            int l62=(n==62?1:0)+(s==62?1:0)+(e==62?1:0)+(w==62?1:0);
            int l30=(n==30?1:0)+(s==30?1:0)+(e==30?1:0)+(w==30?1:0);
            if(l62>=2 && l30==4-l62) return "L-shape";
        }
        return "error";
    }

    private String blockFrequency(int x, int y, int z) {
        if (y == -1) return null;
        World world = Minecraft.getMinecraft().theWorld;
        List<String> blockList = new ArrayList<>();

        int nw = northWidth(x,y,z), sw = southWidth(x,y,z), ew = eastWidth(x,y,z), ww = westWidth(x,y,z);

        if (nw == sw && ew == ww) {
            int nz = endOfRoom(x,y,z,"n"), nwx = endOfRoom(x,y,nz,"w");
            int sz = endOfRoom(x,y,z,"s"), sex = endOfRoom(x,y,sz,"e");
            for (BlockPos bp : BlockPos.getAllInBox(new BlockPos(nwx,y,nz), new BlockPos(sex,y,sz)))
                blockList.add(world.getBlockState(bp).toString());
        } else if (getSize(x,y,z).equals("L-shape")) {
            if (nw == sw) { // E/W unequal
                int startX = ew > ww ? endOfRoom(x,y,z,"e") : endOfRoom(x,y,z,"w");
                int nz = endOfRoom(startX,y,z,"n");
                int dx = ew > ww ? -1 : 1;
                outer: for (int i = 0; i < 200; i++) {
                    int cz = nz + i;
                    if (world.getBlockState(new BlockPos(startX,y,cz)).getBlock()==Blocks.air
                            || checkPlatform(startX,y+1,cz)
                            || (i>0 && Math.abs(dungeonHeight(startX,cz)-dungeonHeight(startX,cz-1))>3)) break;
                    for (int j = 0; j < 200; j++) {
                        BlockPos bp = new BlockPos(startX+dx*j,y,cz);
                        Block b = world.getBlockState(bp).getBlock();
                        if (b==Blocks.air || checkPlatform(startX+dx*j,y+1,cz)
                                || (j>0 && Math.abs(dungeonHeight(startX+dx*j,cz)-dungeonHeight(startX+dx*(j-1),cz))>3)) break;
                        blockList.add(b.toString());
                    }
                }
            } else { // N/S unequal
                int startZ = nw > sw ? endOfRoom(x,y,z,"n") : endOfRoom(x,y,z,"s");
                int wx = endOfRoom(x,y,startZ,"w");
                int dz = nw > sw ? 1 : -1;
                for (int i = 0; i < 200; i++) {
                    int cx = wx + i;
                    if (world.getBlockState(new BlockPos(cx,y,startZ)).getBlock()==Blocks.air
                            || checkPlatform(cx,y+1,startZ)
                            || (i>0 && Math.abs(dungeonHeight(cx,startZ)-dungeonHeight(cx-1,startZ))>3)) break;
                    for (int j = 0; j < 200; j++) {
                        BlockPos bp = new BlockPos(cx,y,startZ+dz*j);
                        Block b = world.getBlockState(bp).getBlock();
                        if (b==Blocks.air || checkPlatform(cx,y+1,startZ+dz*j)
                                || (j>0 && Math.abs(dungeonHeight(cx,startZ+dz*j)-dungeonHeight(cx,startZ+dz*(j-1)))>3)) break;
                        blockList.add(b.toString());
                    }
                }
            }
        }

        if (blockList.isEmpty()) return null;
        Set<String> distinct = new HashSet<>(blockList);
        List<String> freqs = new ArrayList<>();
        for (String s : distinct) freqs.add(s + ":" + Collections.frequency(blockList, s));
        Collections.sort(freqs);
        return String.join(",", freqs);
    }

    private String floorFrequency(int x, int y, int z) {
        if (y == -1) return null;
        World world = Minecraft.getMinecraft().theWorld;
        List<String> blockList = new ArrayList<>();

        if (northWidth(x,y,z) == southWidth(x,y,z) && eastWidth(x,y,z) == westWidth(x,y,z)) {
            int nz=endOfRoom(x,y,z,"n"), nwx=endOfRoom(x,y,nz,"w");
            int sz=endOfRoom(x,y,z,"s"), sex=endOfRoom(x,y,sz,"e");
            for (BlockPos bp : BlockPos.getAllInBox(new BlockPos(nwx+10,68,nz+10), new BlockPos(sex-10,68,sz-10)))
                blockList.add(world.getBlockState(bp).getBlock().toString());
        }
        if (getSize(x,y,z).equals("L-shape")) blockList.add(String.valueOf(dungeonTop(x,68,z)));

        if (blockList.isEmpty()) return null;
        Set<String> distinct = new HashSet<>(blockList);
        List<String> freqs = new ArrayList<>();
        for (String s : distinct) freqs.add(s + ":" + Collections.frequency(blockList, s));
        Collections.sort(freqs);
        return String.join(",", freqs);
    }

    private String getMD5(String input) {
        try {
            if (input == null) return null;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, digest);
            String hash = no.toString(16);
            while (hash.length() < 32) hash = "0" + hash;
            return hash;
        } catch (Exception e) { return null; }
    }
}