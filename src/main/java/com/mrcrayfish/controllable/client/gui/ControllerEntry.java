package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * Author: MrCrayfish
 */
public final class ControllerEntry implements GuiListExtended.IGuiListEntry
{
    private ControllerList controllerList;
    private int jid;

    public ControllerEntry(ControllerList controllerList, int jid)
    {
        this.controllerList = controllerList;
        this.jid = jid;
    }

    public int getJid()
    {
        return this.jid;
    }

    @Override
    public void updatePosition(int i, int i1, int i2, float v) {}

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
        String controllerName = Controllable.getManager().getName(this.jid);
        if(controllerName == null)
            return;
        controllerName = controllerName.replace("SDL GameController ", "").replace("SDL Joystick ", "");
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(controllerName, x + 20, y + 4, Color.WHITE.getRGB());
        if(this.controllerList.getSelected() == this)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/beacon.png"));
            GuiScreen.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 91, 224, 14, 12, 256, 256);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        controllerList.updateSelected();
        if(this.controllerList.getSelected() != this)
        {
            this.controllerList.setSelected(this);
            Controllable.setController(new Controller(this.jid));
        }
        else
        {
            this.controllerList.setSelected(null);
            Controllable.setController(null);
        }
        return true;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relative) {}
}
