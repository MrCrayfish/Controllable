package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.studiohartman.jamepad.ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class ControllerList extends ExtendedList<ControllerEntry>
{
    private ControllerManager manager;

    public ControllerList(ControllerManager manager, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.manager = manager;
        this.reload();
    }

    public void reload()
    {
        this.clearEntries();
        for(int i = 0; i < manager.getNumControllers(); i++)
        {
            this.addEntry(new ControllerEntry(this, manager.getControllerIndex(i)));
        }
        this.updateSelected();
    }

    private void updateSelected()
    {
        List<ControllerEntry> entries = this.children();
        for(ControllerEntry entry : entries)
        {
            if(entry.getController().getIndex() == Controllable.getSelectedControllerIndex())
            {
                this.setSelected(entry);
                break;
            }
        }
    }
}
