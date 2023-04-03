package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public final class FabricControllerEntry extends AbstractSelectionList.Entry<FabricControllerEntry> implements IControllerEntry
{
    private final FabricControllerList controllerList;
    private final int jid;

    public FabricControllerEntry(IControllerList<?> controllerList, int jid)
    {
        this.controllerList = (FabricControllerList) controllerList;
        this.jid = jid;
    }

    @Override
    public int getJid()
    {
        return this.jid;
    }

    @Override
    public void render(PoseStack poseStack, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
        String controllerName = GLFW.glfwGetGamepadName(this.jid);
        if(controllerName == null)
            return;
        Minecraft.getInstance().font.drawShadow(poseStack, controllerName, left + 22, top + 5, 0xFFFFFFFF);
        if(this.controllerList.getSelected() == this)
        {
            RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/container/beacon.png"));
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Screen.blit(poseStack, left + 3, top + 3, 91, 224, 14, 12, 256, 256);
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
