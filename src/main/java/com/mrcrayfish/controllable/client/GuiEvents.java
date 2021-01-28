package com.mrcrayfish.controllable.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.gui.ControllerSelectionScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class GuiEvents
{
    private static final List<RenderGameOverlayEvent.ElementType> EXCLUDED_TYPES;

    static
    {
        ImmutableList.Builder<RenderGameOverlayEvent.ElementType> builder = ImmutableList.builder();
        builder.add(RenderGameOverlayEvent.ElementType.ALL);
        builder.add(RenderGameOverlayEvent.ElementType.BOSSHEALTH);
        builder.add(RenderGameOverlayEvent.ElementType.BOSSINFO);
        builder.add(RenderGameOverlayEvent.ElementType.CROSSHAIRS);
        builder.add(RenderGameOverlayEvent.ElementType.DEBUG);
        builder.add(RenderGameOverlayEvent.ElementType.FPS_GRAPH);
        builder.add(RenderGameOverlayEvent.ElementType.HELMET);
        builder.add(RenderGameOverlayEvent.ElementType.PLAYER_LIST);
        builder.add(RenderGameOverlayEvent.ElementType.PORTAL);
        builder.add(RenderGameOverlayEvent.ElementType.POTION_ICONS);
        builder.add(RenderGameOverlayEvent.ElementType.SUBTITLES);
        builder.add(RenderGameOverlayEvent.ElementType.VIGNETTE);
        EXCLUDED_TYPES = builder.build();
    }

    private ControllerManager manager;

    public GuiEvents(ControllerManager manager)
    {
        this.manager = manager;
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onOpenGui(GuiScreenEvent.InitGuiEvent.Post event)
    {
        /* Resets the controller button states */
        ButtonBinding.resetButtonStates();

        if(event.getGui() instanceof OptionsScreen)
        {
            int y = event.getGui().height / 6 + 72 - 6;
            event.addWidget(new ControllerButton((event.getGui().width / 2) + 5 + 150 + 4, y, button -> Minecraft.getInstance().displayGuiScreen(new ControllerSelectionScreen(manager, event.getGui()))));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            if(EXCLUDED_TYPES.contains(event.getType()))
            {
                return;
            }
            RenderSystem.translated(0, -20, 0);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event)
    {
        if(Config.CLIENT.options.consoleHotbar.get())
        {
            if(EXCLUDED_TYPES.contains(event.getType()))
            {
                return;
            }
            RenderSystem.translated(0, 20, 0);
        }
    }
}
