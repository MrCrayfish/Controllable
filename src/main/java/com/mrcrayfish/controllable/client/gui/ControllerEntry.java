package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.ResourceLocation;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;

import java.awt.*;

/**
 * Author: MrCrayfish
 */
public final class ControllerEntry extends ExtendedList.AbstractListEntry<ControllerEntry>
{
    private ControllerList controllerList;
    private Controller controller;

    public ControllerEntry(ControllerList controllerList, SDL2Controller sdl2Controller)
    {
        this.controllerList = controllerList;
        this.controller = new Controller(sdl2Controller);
    }

    SDL2Controller getSdl2Controller()
    {
        return this.controller.getSDL2Controller();
    }

    @Override
    public void render(MatrixStack matrixStack, int slotIndex, int top, int left, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
    {
        if(!this.controller.getSDL2Controller().isConnected())
            return;

        Minecraft.getInstance().fontRenderer.drawStringWithShadow(matrixStack, this.controller.getName(), left + 20, top + 4, Color.WHITE.getRGB());
        if(this.controllerList.getSelected() == this)
        {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/beacon.png"));
            Screen.blit(matrixStack, left + 2, top + 2, 91, 224, 14, 12, 256, 256); //TODO test
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.controllerList.getSelected() != this)
        {
            this.controllerList.setSelected(this);
            Controllable.setController(this.controller.getSDL2Controller());
        }
        else
        {
            this.controllerList.setSelected(null);
            Controllable.setController(null);
        }
        return true;
    }
}
