package com.mrcrayfish.controllable.client.gui;

import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.utils.Array;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class ControllerList extends ExtendedList<ControllerEntry>
{
    private SDL2ControllerManager manager;

    public ControllerList(SDL2ControllerManager manager, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.manager = manager;
        this.reload();
    }

    public void reload()
    {
        this.clearEntries();
        Array<com.badlogic.gdx.controllers.Controller> controllers = manager.getControllers();
        for(int i = 0; i < controllers.size; i++)
        {
            this.addEntry(new ControllerEntry(this, (SDL2Controller) controllers.get(i)));
        }
        this.updateSelected();
    }

    private void updateSelected()
    {
        Controller controller = Controllable.getController();
        if(controller == null)
        {
            this.setSelected(null);
            return;
        }

        List<ControllerEntry> entries = this.children();
        for(ControllerEntry entry : entries)
        {
            if(entry.getSdl2Controller() == controller.getSDL2Controller())
            {
                this.setSelected(entry);
                break;
            }
        }
    }
}
