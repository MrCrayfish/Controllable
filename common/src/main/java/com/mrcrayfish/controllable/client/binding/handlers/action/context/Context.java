package com.mrcrayfish.controllable.client.binding.handlers.action.context;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class Context
{
    private final ButtonBinding binding;
    private final Controller controller;
    private final Minecraft minecraft;
    private final @Nullable LocalPlayer player;
    private final @Nullable Level level;
    private final @Nullable Screen screen;
    private final boolean simulated;

    public Context(ButtonBinding binding, Controller controller, Minecraft minecraft, @Nullable LocalPlayer player, @Nullable Level level, @Nullable Screen screen, boolean simulated)
    {
        this.binding = binding;
        this.controller = controller;
        this.minecraft = minecraft;
        this.player = player;
        this.level = level;
        this.screen = screen;
        this.simulated = simulated;
    }

    public ButtonBinding binding()
    {
        return this.binding;
    }

    public Controller controller()
    {
        return this.controller;
    }

    public Minecraft minecraft()
    {
        return this.minecraft;
    }

    public Optional<LocalPlayer> player()
    {
        return Optional.ofNullable(this.player);
    }

    public Optional<Level> level()
    {
        return Optional.ofNullable(this.level);
    }

    public Optional<Screen> screen()
    {
        return Optional.ofNullable(this.screen);
    }

    public boolean simulated()
    {
        return this.simulated;
    }
}
