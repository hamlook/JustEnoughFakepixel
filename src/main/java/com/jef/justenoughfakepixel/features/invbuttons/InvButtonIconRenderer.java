package com.jef.justenoughfakepixel.features.invbuttons;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class InvButtonIconRenderer {

    private static final HashMap<String, ItemStack> skullMap = new HashMap<>();
    private static final HashMap<String, ItemStack> stackCache = new HashMap<>();

    private InvButtonIconRenderer() {}

    public static void renderIcon(String icon, int x, int y) {
        if (icon == null || icon.isEmpty()) return;

        if (icon.startsWith("extra:")) {
            String name = icon.substring("extra:".length());
            ResourceLocation loc = new ResourceLocation("justenoughfakepixel",
                    "invbuttons/extraicons/" + name + ".png");
            Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
            GlStateManager.color(1, 1, 1, 1);
            drawTexturedRect(x, y, 16, 16);
        } else {
            ItemStack stack = getStack(icon);
            if (stack == null) return;

            float scale = icon.startsWith("skull:") ? 1.2f : 1f;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 8, y + 8, 0);
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.translate(-8, -8, 0);
            drawItemStack(stack, 0, 0);
            GlStateManager.popMatrix();
        }
    }

    public static ItemStack getStack(String icon) {
        if (icon == null || icon.isEmpty()) return null;
        if (icon.startsWith("extra:")) return null;

        if (icon.startsWith("skull:")) {
            String link = icon.substring("skull:".length());
            if (skullMap.containsKey(link)) return skullMap.get(link);
            ItemStack stack = buildSkullStack(link);
            skullMap.put(link, stack);
            return stack;
        }

        // Check stack cache first
        if (stackCache.containsKey(icon)) return stackCache.get(icon);

        // Build from NEU repo JSON
        JsonObject json = SkyblockItemCache.getInstance().getItemJson(icon);
        if (json != null) {
            ItemStack stack = jsonToStack(json);
            if (stack != null) {
                stackCache.put(icon, stack);
                return stack;
            }
        }

        return null;
    }

    /**
     * Direct port of NEU's NEUManager.jsonToStack — builds an ItemStack from a NEU repo item JSON.
     * JSON fields: itemid, damage, nbttag, displayname, lore
     */
    public static ItemStack jsonToStack(JsonObject json) {
        if (json == null) return null;

        String itemid = json.has("itemid") ? json.get("itemid").getAsString() : null;
        if (itemid == null) return null;

        Item mcItem = Item.getByNameOrId(itemid);
        if (mcItem == null) mcItem = Item.getByNameOrId("minecraft:" + itemid.toLowerCase());
        if (mcItem == null) return null;

        ItemStack stack = new ItemStack(mcItem);

        if (json.has("damage")) {
            stack.setItemDamage(json.get("damage").getAsInt());
        }

        if (json.has("nbttag")) {
            try {
                NBTTagCompound tag = JsonToNBT.getTagFromJson(json.get("nbttag").getAsString());
                stack.setTagCompound(tag);
            } catch (Exception ignored) {}
        }

        if (json.has("lore")) {
            NBTTagCompound display = new NBTTagCompound();
            if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("display")) {
                display = stack.getTagCompound().getCompoundTag("display");
            }
            display.setTag("Lore", processLore(json.get("lore").getAsJsonArray()));
            NBTTagCompound tag = stack.getTagCompound() != null ? stack.getTagCompound() : new NBTTagCompound();
            tag.setTag("display", display);
            stack.setTagCompound(tag);
        }

        return stack;
    }

    private static NBTTagList processLore(JsonArray lore) {
        NBTTagList nbtLore = new NBTTagList();
        for (JsonElement line : lore) {
            String lineStr = line.getAsString();
            // Skip recipe lines (same filter as NEU)
            if (!lineStr.contains("Click to view recipes!") && !lineStr.contains("Click to view recipe!")) {
                nbtLore.appendTag(new NBTTagString(lineStr));
            }
        }
        return nbtLore;
    }

    /** Builds a skull ItemStack from a texture hash — same as NEU's GuiInvButtonEditor.getStack */
    private static ItemStack buildSkullStack(String hash) {
        ItemStack render       = new ItemStack(Items.skull, 1, 3);
        NBTTagCompound nbt     = new NBTTagCompound();
        NBTTagCompound owner   = new NBTTagCompound();
        NBTTagCompound props   = new NBTTagCompound();
        NBTTagList     texs    = new NBTTagList();
        NBTTagCompound tex0    = new NBTTagCompound();

        String uuid = UUID.nameUUIDFromBytes(hash.getBytes()).toString();
        owner.setString("Id",   uuid);
        owner.setString("Name", uuid);

        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + hash + "\"}}}";
        tex0.setString("Value", Base64.getEncoder().encodeToString(json.getBytes()));
        texs.appendTag(tex0);
        props.setTag("textures", texs);
        owner.setTag("Properties", props);
        nbt.setTag("SkullOwner", owner);
        render.setTagCompound(nbt);
        return render;
    }

    /** Exact port of NEU's Utils.drawItemStack */
    public static void drawItemStack(ItemStack stack, int x, int y) {
        if (stack == null) return;
        net.minecraft.client.renderer.entity.RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        RenderHelper.enableGUIStandardItemLighting();
        ri.zLevel = -145;
        ri.renderItemAndEffectIntoGUI(stack, x, y);
        ri.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, stack, x, y, null);
        ri.zLevel = 0;
        RenderHelper.disableStandardItemLighting();
    }

    private static void drawTexturedRect(float x, float y, float w, float h) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
        wr.pos(x,     y + h, 0).tex(0, 1).endVertex();
        wr.pos(x + w, y + h, 0).tex(1, 1).endVertex();
        wr.pos(x + w, y,     0).tex(1, 0).endVertex();
        wr.pos(x,     y,     0).tex(0, 0).endVertex();
        tess.draw();
        GlStateManager.disableBlend();
    }
}
