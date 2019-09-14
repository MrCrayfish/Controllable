package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * Author: MrCrayfish
 */
public final class ControllerEntry extends ExtendedList.AbstractListEntry<ControllerEntry>
{
    private ControllerList controllerList;
    private ControllerIndex controller;

    public ControllerEntry(ControllerList controllerList, ControllerIndex controller)
    {
        this.controllerList = controllerList;
        this.controller = controller;
    }

    public ControllerIndex getController()
    {
        return controller;
    }

    @Override
    public void render(int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
        try
        {
            if(!controller.isConnected())
            {
                return;
            }

            Minecraft.getInstance().fontRenderer.drawString(controller.getName(), left + 20, top + 4, Color.WHITE.getRGB());
            if(controllerList.getSelected() == this)
            {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/beacon.png"));
                Screen.blit(left + 2, top + 2, 91, 224, 14, 12, 256, 256);
            }
        }
        catch(ControllerUnpluggedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(controllerList.getSelected() != this)
        {
            controllerList.setSelected(this);
            ControllerIndex index = controllerList.getManager().getControllerIndex(controller.getIndex());
            Controller controller = new Controller(index);
            Mappings.updateControllerMappings(controller);
            Controllable.setController(controller);
        }
        else
        {
            controllerList.setSelected(null);
            Controllable.setController(null);
        }
        return true;
    }
}
