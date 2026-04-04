package com.jef.justenoughfakepixel.features.storage;

import com.jef.justenoughfakepixel.JefMod;
import com.jef.justenoughfakepixel.core.JefConfig;
import com.jef.justenoughfakepixel.utils.render.ResolutionUtils;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;


public class StorageOverlay extends GuiScreen {

    public boolean isHorizontal = true;

    private static final int BASE_BOX_WIDTH = 1536;
    private static final int BASE_BOX_HEIGHT = BASE_BOX_WIDTH * 9 / 16;
    private static final int BASE_CONTAINER_WIDTH = 450;
    private static final int BASE_CONTAINER_HEIGHT = BASE_CONTAINER_WIDTH * 9 / 16;

    private static final int BASE_PADDING = 30;

    private int boxX, boxY, boxWidth, boxHeight;
    private int cWidth, cHeight, pX, pY;
    private int startX, startY;

    private double SCROLL_SPEED = 100;
    public double scrollOffset = 0;
    public double offset = 0;

    private double maxScroll = 0;
    private double totalElements = 0;
    private double visibleElements = 0;

    public HashMap<String,StorageContainer> containers = new HashMap<>();
    public String activeContainer = "";

    private boolean isDraggingScreen = false;
    private boolean isDraggingScrollbar = false;
    private int lastMouseX,lastMouseY;
    private int draggedDistance = 0;
    private int scrollbarDragOffset = 0;

    private int trackX, trackY, trackW, trackH;
    private int thumbX, thumbY, thumbW, thumbH;

    @Override
    public void initGui() {
        SCROLL_SPEED = 100 * (JefConfig.feature.storage.scrollSpeed);
        isHorizontal = JefConfig.feature.storage.horizontalScroll;

        int cols = isHorizontal ? 8 : 3;
        int rows = isHorizontal ? 3 : 6;

        int pageCounter = 1;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                containers.put("echest-" + pageCounter, new StorageContainer(
                        new HashMap<>(), ContainerType.ECHEST, pageCounter, x, y
                ));
                pageCounter++;
            }
        }

        //TODO: Scrap Containers from Storage GUI
        super.initGui();
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        boxWidth = (int) ResolutionUtils.getXStatic(BASE_BOX_WIDTH);
        boxHeight = (int) ResolutionUtils.getYStatic(BASE_BOX_HEIGHT);
        boxX = (ResolutionUtils.getWidth() - boxWidth) / 2;
        boxY = (ResolutionUtils.getHeight() - boxHeight) / 2;

        cWidth = (int) ResolutionUtils.getXStatic(BASE_CONTAINER_WIDTH);
        cHeight = (int) ResolutionUtils.getYStatic(BASE_CONTAINER_HEIGHT);
        pX = (int) ResolutionUtils.getXStatic(BASE_PADDING);
        pY = (int) ResolutionUtils.getYStatic(BASE_PADDING);


        int maxXGrid = 0;
        int maxYGrid = 0;
        for (StorageContainer container : containers.values()) {
            if (container.xGrid > maxXGrid) maxXGrid = container.xGrid;
            if (container.yGrid > maxYGrid) maxYGrid = container.yGrid;
        }
        int columns = maxXGrid + 1;
        int rows = maxYGrid + 1;

        int totalGridWidth = (columns * cWidth) + ((columns - 1) * pX);
        int totalGridHeight = (rows * cHeight) + ((rows - 1) * pY);

        startX = isHorizontal ? boxX + pX : boxX + ((boxWidth - totalGridWidth) / 2);
        startY = isHorizontal ? boxY + ((boxHeight - totalGridHeight) / 2) : boxY + pY;

        double visibleCols = (double) boxWidth / (cWidth + pX);
        double visibleRows = (double) boxHeight / (cHeight + pY);

        visibleElements = isHorizontal ? visibleCols : visibleRows;
        totalElements = isHorizontal ? columns : rows;
        maxScroll = Math.max(0, (isHorizontal ? columns : rows) - visibleElements);
        clampScroll();

        offset = this.scrollOffset;
        if(!JefConfig.feature.storage.smoothScroll){
            offset = Math.round(this.scrollOffset);
        }

        this.drawCenteredString(mc.fontRendererObj, "Offset: " + String.format("%.2f", offset),
                boxX + (boxWidth / 2), boxY - 15, new Color(255, 255, 255).getRGB());

        drawRect(boxX - 2, boxY - 2, boxX + boxWidth + 4, boxY + boxHeight + 4, new Color(0, 0, 0, 150).getRGB());

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        doGlScissor(boxX, boxY, boxWidth, boxHeight);
        for (StorageContainer container : containers.values()) {
            if (canDraw(isHorizontal ? container.xGrid : container.yGrid)) {
                drawContainer(container);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (maxScroll > 0 && JefConfig.feature.storage.barScroll) {
            int SCROLLBAR_SIZE = 10;
            int SCROLLBAR_MARGIN = 5;

            if (isHorizontal) {
                trackX = boxX; trackY = boxY + boxHeight + SCROLLBAR_MARGIN;
                trackW = boxWidth; trackH = SCROLLBAR_SIZE;
            } else {
                trackX = boxX + boxWidth + SCROLLBAR_MARGIN; trackY = boxY;
                trackW = SCROLLBAR_SIZE; trackH = boxHeight;
            }

            double trackLen = isHorizontal ? trackW : trackH;
            double thumbLen = Math.max(20, trackLen * (visibleElements / totalElements));
            double progress = scrollOffset / maxScroll;
            double thumbStart = (isHorizontal ? trackX : trackY) + progress * (trackLen - thumbLen);

            if (isHorizontal) {
                thumbX = (int) thumbStart; thumbY = trackY; thumbW = (int) thumbLen; thumbH = trackH;
            } else {
                thumbX = trackX; thumbY = (int) thumbStart; thumbW = trackW; thumbH = (int) thumbLen;
            }

            drawRect(trackX, trackY, trackX + trackW, trackY + trackH, new Color(30, 30, 30, 200).getRGB());

            drawRect(thumbX, thumbY, thumbX + thumbW, thumbY + thumbH, new Color(150, 150, 150, 255).getRGB());
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void doGlScissor(int x, int y, int width, int height) {
        int scaleFactor = ResolutionUtils.getFactor();
        int displayHeight = mc.displayHeight;

        GL11.glScissor(
                x * scaleFactor,
                displayHeight - ((y + height) * scaleFactor),
                width * scaleFactor,
                height * scaleFactor
        );
    }

    public boolean canDraw(int gridPos) {
        return gridPos >= Math.floor(offset) - 1 && gridPos <= Math.ceil(offset + visibleElements) + 1;
    }

    public boolean isHovering(int mouseX, int mouseY, StorageContainer container) {
        if(container == null) return false;
        if (mouseX < boxX || mouseX > boxX + boxWidth || mouseY < boxY || mouseY > boxY + boxHeight) {
            return false;
        }

        double renderX = startX + ((container.xGrid - (isHorizontal ? offset : 0)) * (cWidth + pX));
        double renderY = startY + ((container.yGrid - (!isHorizontal ? offset : 0)) * (cHeight + pY));

        return mouseX >= renderX && mouseX <= renderX + cWidth &&
                mouseY >= renderY && mouseY <= renderY + cHeight;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        draggedDistance = 0;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        isDraggingScreen = false;
        isDraggingScrollbar = false;
        if (maxScroll > 0) {
            if (mouseX >= thumbX && mouseX <= thumbX + thumbW && mouseY >= thumbY && mouseY <= thumbY + thumbH) {
                if(JefConfig.feature.storage.barScroll) {
                    isDraggingScrollbar = true;
                    scrollbarDragOffset = isHorizontal ? mouseX - thumbX : mouseY - thumbY;
                    return;
                }
            }
            if (mouseX >= trackX && mouseX <= trackX + trackW && mouseY >= trackY && mouseY <= trackY + trackH) {
                double trackLen = isHorizontal ? trackW : trackH;
                double thumbLen = Math.max(20, trackLen * (visibleElements / totalElements));
                double clickPos = isHorizontal ? mouseX - trackX : mouseY - trackY;

                double newThumbStart = clickPos - (thumbLen / 2);
                double progress = newThumbStart / (trackLen - thumbLen);

                scrollOffset = progress * maxScroll;
                clampScroll();
                if(JefConfig.feature.storage.barScroll) {
                    isDraggingScrollbar = true;
                }
                scrollbarDragOffset = (int)(thumbLen / 2);
                return;
            }


            if(!JefConfig.feature.storage.dragScroll){
                if (activeContainer != null && !activeContainer.isEmpty() && containers.containsKey(activeContainer)) {
                    if (isHovering(mouseX, mouseY, containers.get(activeContainer))) {
                        containers.get(activeContainer).mouseClicked(mouseX, mouseY, mouseButton);
                    }
                }
                for (StorageContainer container : containers.values()) {
                    if (isHovering(mouseX, mouseY, container)) {
                        this.activeContainer = container.id;
                        JefMod.logger.info("Active: " + this.activeContainer);
                    }
                }
            }

            if (mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + boxHeight) {
                if(JefConfig.feature.storage.dragScroll) {
                    isDraggingScreen = true;
                }
            }

        }

        super.mouseClicked(mouseX, mouseY, mouseButton);

    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isDraggingScrollbar) {
            double trackLen = isHorizontal ? trackW : trackH;
            double thumbLen = Math.max(20, trackLen * (visibleElements / totalElements));
            double mousePos = isHorizontal ? mouseX : mouseY;
            double tStart = isHorizontal ? trackX : trackY;

            double newThumbStart = mousePos - scrollbarDragOffset;
            double progress = (newThumbStart - tStart) / (trackLen - thumbLen);

            scrollOffset = progress * maxScroll;
            clampScroll();

        } else if (isDraggingScreen) {
            int deltaX = mouseX - lastMouseX;
            int deltaY = mouseY - lastMouseY;

            draggedDistance += Math.abs(deltaX) + Math.abs(deltaY);

            if (isHorizontal) {
                scrollOffset -= (double) deltaX / (cWidth + pX);
            } else {
                scrollOffset -= (double) deltaY / (cHeight + pY);
            }

            clampScroll();
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (isDraggingScreen && draggedDistance < 5) {
            if (activeContainer != null && !activeContainer.isEmpty() && containers.containsKey(activeContainer)) {
                if (isHovering(mouseX, mouseY, containers.get(activeContainer))) {
                    containers.get(activeContainer).mouseClicked(mouseX, mouseY, state);
                }
            }
            for (StorageContainer container : containers.values()) {
                if (isHovering(mouseX, mouseY, container)) {
                    this.activeContainer = container.id;
                    JefMod.logger.info("Active: " + this.activeContainer);
                }
            }
        }

        isDraggingScreen = false;
        isDraggingScrollbar = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    public void drawContainer(StorageContainer container) {
        double renderX = startX + ((container.xGrid - (isHorizontal ? offset : 0)) * (cWidth + pX));
        double renderY = startY + ((container.yGrid - (!isHorizontal ? offset : 0)) * (cHeight + pY));

        drawRect(
                (int) renderX,
                (int) renderY,
                (int) renderX + cWidth,
                (int) renderY + cHeight,
                new Color(150, 150, 150, 200).getRGB()
        );

        this.drawCenteredString(
                mc.fontRendererObj,
                String.valueOf(container.page),
                (int) renderX + (cWidth / 2),
                (int) renderY + (cHeight / 2) - 4,
                new Color(255, 255, 255).getRGB()
        );
    }

    private void clampScroll() {
        if (this.scrollOffset > maxScroll) this.scrollOffset = maxScroll;
        if (this.scrollOffset < 0) this.scrollOffset = 0;
    }

    @Override
    public void handleMouseInput() throws IOException {
        int scroll = Mouse.getDWheel();
        if(scroll != 0){
            int direction = Integer.signum(scroll);
            this.scrollOffset -= direction * (SCROLL_SPEED / 100.0);
            clampScroll();
        }
        super.handleMouseInput();
    }
}
