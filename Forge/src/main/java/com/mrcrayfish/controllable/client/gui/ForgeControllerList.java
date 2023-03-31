package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.ControllerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ForgeControllerList extends AbstractSelectionList<ForgeControllerEntry> implements IControllerList<ForgeControllerEntry>
{
    private final ControllerManager manager;

    public ForgeControllerList(ControllerManager manager, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.manager = manager;
        this.reload();
    }

    @Override
    public void reload()
    {
        this.clearEntries();
        Map<Integer, String> controllers = this.manager.getControllers();
        controllers.forEach((jid, name) -> {
            this.addEntry(new ForgeControllerEntry(this, jid));
        });
        this.updateSelected();
    }

    @Override
    public void updateSelected()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
        {
            this.setSelected(null);
            return;
        }

        List<ForgeControllerEntry> entries = this.children();
        for(ForgeControllerEntry entry : entries)
        {
            if(entry.getJid() == controller.getJid())
            {
                this.setSelected(entry);
                break;
            }
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput output)
    {

    }
}
