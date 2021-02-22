package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ControllerList extends GuiListExtended
{
    private ControllerManager manager;
    private List<ControllerEntry> controllers = new ArrayList<>();

    public ControllerList(ControllerManager manager, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.manager = manager;
        this.reload();
    }

    public void reload()
    {
        this.controllers.clear();
        Map<Integer, String> controllers = this.manager.getControllers();
        controllers.forEach((jid, name) -> {
            this.controllers.add(new ControllerEntry(this, jid));
        });
        this.updateSelected();
    }

    public void updateSelected()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
        {
            this.selectedElement = -1;
            return;
        }

        for(ControllerEntry entry : this.controllers)
        {
            if(entry.getJid() == controller.getJid())
            {
                this.setSelected(entry);
                break;
            }
        }
    }

    public void setSelected(IGuiListEntry entry)
    {
        int index = this.controllers.indexOf(entry);
        if(index != -1)
        {
            this.selectedElement = index;
        }
    }

    public IGuiListEntry getSelected()
    {
        return this.getListEntry(this.selectedElement);
    }

    @Override
    @Nullable
    public IGuiListEntry getListEntry(int index)
    {
        if(index >= 0 && index < this.getSize())
        {
            return this.controllers.get(index);
        }
        return null;
    }

    @Override
    protected int getSize()
    {
        return this.controllers.size();
    }
}
