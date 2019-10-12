package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
@SideOnly(Side.CLIENT)
public class GuiListControllers extends GuiListExtended
{
    private ControllerManager manager;
    private List<ControllerEntry> controllers = new ArrayList<>();

    public GuiListControllers(ControllerManager manager, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.manager = manager;
        this.selectedElement = Controllable.getSelectedControllerIndex();
        this.reload();
    }

    public void reload()
    {
        controllers.clear();
        for(int i = 0; i < manager.getNumControllers(); i++)
        {
            controllers.add(new ControllerEntry(manager.getControllerIndex(i)));
        }
    }

    public void setSelectedElement(int index)
    {
        this.selectedElement = index;
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
        return Controllable.getController() != null && controllers.get(slotIndex).index == Controllable.getController().getIndex();
    }

    public int getSelectedIndex()
    {
        return selectedElement;
    }

    public class ControllerEntry implements IGuiListEntry
    {
        private ControllerIndex index;

        public ControllerEntry(ControllerIndex index)
        {
            this.index = index;
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks)
        {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            try
            {
                if(!index.isConnected())
                    return;

                Minecraft.getMinecraft().fontRenderer.drawString(index.getName(), x + 20, y + 4, Color.WHITE.getRGB());
                if(isSelected(slotIndex))
                {
                    Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/beacon.png"));
                    GuiScreen.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 91, 224, 14, 12, 256, 256);
                }
            }
            catch(ControllerUnpluggedException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            if(Controllable.getController() == null || Controllable.getController().getIndex() != this.index)
            {
                ControllerIndex index = manager.getControllerIndex(this.index.getIndex());
                Controller controller = new Controller(index);
                Mappings.updateControllerMappings(controller);
                Controllable.setController(controller);
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
