package com.mrcrayfish.controllable.client.binding.handlers.action.context;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class RenderingContext extends Context
{
    private final DeltaTracker tracker;

    public RenderingContext(ButtonBinding binding, Controller controller, Minecraft minecraft, @Nullable LocalPlayer player, @Nullable Level level, @Nullable Screen screen, boolean simulated, DeltaTracker tracker)
    {
        super(binding, controller, minecraft, player, level, screen, simulated);
        this.tracker = tracker;
    }

    public DeltaTracker tracker()
    {
        return this.tracker;
    }
}
