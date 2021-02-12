package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

/**
 * Author: MrCrayfish
 */
public final class ControllerEntry extends ExtendedList.AbstractListEntry<ControllerEntry>
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
    public void render(int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
        String controllerName = GLFW.glfwGetGamepadName(this.jid);
        if(controllerName == null)
            return;
        Minecraft.getInstance().fontRenderer.drawStringWithShadow(controllerName, left + 20, top + 4, Color.WHITE.getRGB());
        if(this.controllerList.getSelected() == this)
        {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/beacon.png"));
            Screen.blit(left + 2, top + 2, 91, 224, 14, 12, 256, 256); //TODO test
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
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
}
