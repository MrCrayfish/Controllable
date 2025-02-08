package com.mrcrayfish.controllable.client.binding.handlers.action.context;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.MutableClientInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MrCrayfish
 */
public class MovementInputContext extends Context
{
    private final ClientInput input;
    private final MutableClientInput mutableInput;

    public MovementInputContext(ButtonBinding binding, Controller controller, Minecraft minecraft, @Nullable LocalPlayer player, @Nullable Level level, @Nullable Screen screen, boolean simulated, ClientInput input)
    {
        super(binding, controller, minecraft, player, level, screen, simulated);
        this.input = input;
        this.mutableInput = new MutableClientInput(input);
    }

    public ClientInput input()
    {
        return this.input;
    }

    public MutableClientInput mutableInput()
    {
        return this.mutableInput;
    }
}
