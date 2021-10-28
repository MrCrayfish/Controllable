package com.mrcrayfish.controllable.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.gui.ControllerSelectionScreen;
import com.mrcrayfish.controllable.client.gui.widget.ControllerButton;
import com.mrcrayfish.controllable.mixin.client.ForgeIngameGuiMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class GuiEvents
{
    private static final List<IIngameOverlay> INCLUDED_OVERLAYS;

    static
    {
        ImmutableList.Builder<IIngameOverlay> builder = ImmutableList.builder();
        builder.add(ForgeIngameGui.HOTBAR_ELEMENT);
        builder.add(ForgeIngameGui.PLAYER_HEALTH_ELEMENT);
        builder.add(ForgeIngameGui.MOUNT_HEALTH_ELEMENT);
        builder.add(ForgeIngameGui.ARMOR_LEVEL_ELEMENT);
        builder.add(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT);
        builder.add(ForgeIngameGui.FOOD_LEVEL_ELEMENT);
        builder.add(ForgeIngameGui.AIR_LEVEL_ELEMENT);
        builder.add(ForgeIngameGui.CHAT_PANEL_ELEMENT);
        builder.add(ForgeIngameGui.HUD_TEXT_ELEMENT);
        builder.add(ForgeIngameGui.ITEM_NAME_ELEMENT);
        builder.add(ForgeIngameGui.JUMP_BAR_ELEMENT);
        builder.add(ForgeIngameGui.RECORD_OVERLAY_ELEMENT);
        INCLUDED_OVERLAYS = builder.build();
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
            event.addWidget(new ControllerButton((event.getGui().width / 2) + 5 + 150 + 4, y, button -> Minecraft.getInstance().setScreen(new ControllerSelectionScreen(manager, event.getGui()))));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderOverlay(RenderGameOverlayEvent.PreLayer event)
    {
        if(Config.CLIENT.options.consoleHotbar.get() && INCLUDED_OVERLAYS.contains(event.getOverlay()))
        {
            event.getMatrixStack().translate(0, -20, 0);
            if(event.getOverlay() == ForgeIngameGui.HOTBAR_ELEMENT)
            {
                RenderSystem.getModelViewStack().translate(0, -20, 0);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.PostLayer event)
    {
        if(Config.CLIENT.options.consoleHotbar.get() && INCLUDED_OVERLAYS.contains(event.getOverlay()))
        {
            event.getMatrixStack().translate(0, 20, 0);
            if(event.getOverlay() == ForgeIngameGui.HOTBAR_ELEMENT)
            {
                RenderSystem.getModelViewStack().translate(0, 20, 0);
                RenderSystem.applyModelViewMatrix();
            }
        }
    }
}
