package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Controllers;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
@SideOnly(Side.CLIENT)
public class GuiListControllers extends GuiListExtended
{
    private List<ControllerEntry> controllers = new ArrayList<>();

    public GuiListControllers(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.init();
    }

    private void init()
    {
        int count = Controllers.getControllerCount();
        for(int i = 0; i < count; i++)
        {
            org.lwjgl.input.Controller controller = Controllers.getController(i);
            controllers.add(new ControllerEntry(controller));
            if(Controllable.getController() != null && Controllable.getController().getRawController() == controller)
            {
                selectedElement = i;
            }
        }
    }

    public void reload()
    {
        controllers.clear();
        selectedElement = -1;
        Controllable.setController(null);
        int count = Controllers.getControllerCount();
        for(int i = 0; i < count; i++)
        {
            org.lwjgl.input.Controller controller = Controllers.getController(i);
            controllers.add(new ControllerEntry(controller));
            if(Controllable.getController() != null && Controllable.getController().getRawController() == controller)
            {
                selectedElement = i;
            }
        }

        if(Controllers.getControllerCount() > 0)
        {
            Controllable.setController(new Controller(Controllers.getController(0)));
        }
    }

    @Override
    public IGuiListEntry getListEntry(int index)
    {
        return controllers.get(index);
    }

    @Override
    protected int getSize()
    {
        return controllers.size();
    }

    @Override
    protected boolean isSelected(int slotIndex)
    {
        return selectedElement == slotIndex;
    }

    @Nullable
    public org.lwjgl.input.Controller getSelectedController()
    {
        if(selectedElement >= 0 && selectedElement < controllers.size())
        {
            return controllers.get(selectedElement).controller;
        }
        return null;
    }

    public class ControllerEntry implements IGuiListEntry
    {
        private org.lwjgl.input.Controller controller;

        public ControllerEntry(org.lwjgl.input.Controller controller)
        {
            this.controller = controller;
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            Minecraft.getMinecraft().fontRenderer.drawString(controller.getName(), x + 20, y + 4, Color.WHITE.getRGB());
            if(selectedElement == slotIndex)
            {
                Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/beacon.png"));
                GuiScreen.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 91, 224, 14, 12, 256, 256);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            return true;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {

        }
    }
}
