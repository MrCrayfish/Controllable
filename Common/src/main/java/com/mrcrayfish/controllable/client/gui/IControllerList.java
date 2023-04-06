package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;

/**
 * Author: MrCrayfish
 */
public interface IControllerList<T extends IControllerEntry> extends GuiEventListener, NarratableEntry
{
    void reload();

    void updateSelected();

    T getSelected();

    void setSelected(T entry);

    void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks);
}
