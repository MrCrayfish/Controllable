package com.mrcrayfish.controllable.client.gui;

import com.badlogic.gdx.utils.Array;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
@SideOnly(Side.CLIENT)
public class GuiListControllers extends GuiListExtended
{
    private SDL2ControllerManager manager;
    private List<ControllerEntry> controllers = new ArrayList<>();

    public GuiListControllers(SDL2ControllerManager manager, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.manager = manager;
        this.reload();
    }

    public void reload()
    {
        this.controllers.clear();
        Array<com.badlogic.gdx.controllers.Controller> controllers = manager.getControllers();
        for(int i = 0; i < controllers.size; i++)
        {
            this.controllers.add(new ControllerEntry((SDL2Controller) controllers.get(i)));
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
        return Controllable.getController() != null && controllers.get(slotIndex).getSDL2Controller() == Controllable.getController().getSDL2Controller();
    }

    public int getSelectedIndex()
    {
        return selectedElement;
    }

    public class ControllerEntry implements IGuiListEntry
    {
        private Controller controller;

        public ControllerEntry(SDL2Controller sdl2Controller)
        {
            this.controller = new Controller(sdl2Controller);
        }

        SDL2Controller getSDL2Controller()
        {
            return controller.getSDL2Controller();
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            if(!controller.getSDL2Controller().isConnected())
                return;

            Minecraft.getMinecraft().fontRenderer.drawString(controller.getName(), x + 20, y + 4, Color.WHITE.getRGB());
            if(isSelected(slotIndex))
            {
                Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/beacon.png"));
                GuiScreen.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 91, 224, 14, 12, 256, 256);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            if(Controllable.getController() == null || Controllable.getController().getSDL2Controller() != this.controller.getSDL2Controller())
            {
                Mappings.updateControllerMappings(controller);
                Controllable.setController(controller.getSDL2Controller());
            }
            else
            {
                Controllable.setController(null);
            }
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {

        }
    }
}
