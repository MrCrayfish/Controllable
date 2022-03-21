package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.*;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class ControllerOptions
{
    private static final DecimalFormat FORMAT = new DecimalFormat("0.0#");

    public static final CycleOption<Boolean> FORCE_FEEDBACK = createOnOff("controllable.options.forceFeedback", options -> {
        return Config.CLIENT.options.forceFeedback.get();
    }, (options, option, value) -> {
        Config.CLIENT.options.forceFeedback.set(value);
        Config.save();
    });

    public static final CycleOption<Boolean> AUTO_SELECT = createOnOff("controllable.options.autoSelect", options -> {
        return Config.CLIENT.options.autoSelect.get();
    }, (options, option, value) -> {
        Config.CLIENT.options.autoSelect.set(value);
        Config.save();
    });

    public static final CycleOption<Boolean> RENDER_MINI_PLAYER = createOnOff("controllable.options.renderMiniPlayer", options -> {
        return Config.CLIENT.options.renderMiniPlayer.get();
    }, (options, option, value) -> {
        Config.CLIENT.options.renderMiniPlayer.set(value);
        Config.save();
    });

    public static final CycleOption<Boolean> VIRTUAL_MOUSE = createOnOff("controllable.options.virtualMouse", options -> {
        return Config.CLIENT.options.virtualMouse.get();
    }, (options, option, value) -> {
        Config.CLIENT.options.virtualMouse.set(value);
        Config.save();
    });

    public static final CycleOption<Boolean> CONSOLE_HOTBAR = createOnOff("controllable.options.consoleHotbar", options -> {
        return Config.CLIENT.options.consoleHotbar.get();
    }, (options, option, value) -> {
        Config.CLIENT.options.consoleHotbar.set(value);
        Config.save();
    });

    public static final CycleOption<CursorType> CURSOR_TYPE = create("controllable.options.cursorType", CursorType.values(), cursorType -> {
        return new TranslatableComponent("controllable.cursor." + cursorType.getId());
    }, options -> {
        return Config.CLIENT.options.cursorType.get();
    }, (options, option, cursorType) -> {
        Config.CLIENT.options.cursorType.set(cursorType);
        Config.save();
    });

    public static final CycleOption<ControllerIcons> CONTROLLER_ICONS = create("controllable.options.controllerIcons", ControllerIcons.values(), controllerIcon -> {
        return new TranslatableComponent("controllable.controller." + controllerIcon.getId());
    }, options -> {
        return Config.CLIENT.options.controllerIcons.get();
    }, (options, option, controllerIcon) -> {
        Config.CLIENT.options.controllerIcons.set(controllerIcon);
        Config.save();
    });

    public static final CycleOption<Boolean> INVERT_LOOK = createOnOff("controllable.options.invertLook", options -> {
        return Config.CLIENT.options.invertLook.get();
    }, (options, option, value) -> {
        Config.CLIENT.options.invertLook.set(value);
        Config.save();
    });

    public static final ProgressOption DEAD_ZONE = new ControllableProgressOption("controllable.options.deadZone", 0.0, 1.0, 0.01F, options -> {
        return Config.CLIENT.options.deadZone.get();
    }, (options, value) -> {
        Config.CLIENT.options.deadZone.set(Mth.clamp(value, 0.0, 1.0));
        Config.save();
    }, (options, option) -> {
        double deadZone = Config.CLIENT.options.deadZone.get();
        return new TranslatableComponent("controllable.options.deadZone.format", FORMAT.format(deadZone));
    });

    public static final ProgressOption ROTATION_SPEED = new ControllableProgressOption("controllable.options.rotationSpeed", 1.0, 50.0, 1.0F, options -> {
        return Config.CLIENT.options.rotationSpeed.get();
    }, (options, value) -> {
        Config.CLIENT.options.rotationSpeed.set(Mth.clamp(value, 0.0, 100.0));
        Config.save();
    }, (options, option) -> {
        double rotationSpeed = Config.CLIENT.options.rotationSpeed.get();
        return new TranslatableComponent("controllable.options.rotationSpeed.format", FORMAT.format(rotationSpeed));
    });

    public static final ProgressOption MOUSE_SPEED = new ControllableProgressOption("controllable.options.mouseSpeed", 1.0, 50.0, 1.0F, options -> {
        return Config.CLIENT.options.mouseSpeed.get();
    }, (options, value) -> {
        Config.CLIENT.options.mouseSpeed.set(Mth.clamp(value, 0.0, 50.0));
        Config.save();
    }, (options, option) -> {
        double mouseSpeed = Config.CLIENT.options.mouseSpeed.get();
        return new TranslatableComponent("controllable.options.mouseSpeed.format", FORMAT.format(mouseSpeed));
    });

    public static final CycleOption<ActionVisibility> SHOW_ACTIONS = create("controllable.options.showActions", ActionVisibility.values(), actionVisibility -> {
        return new TranslatableComponent("controllable.actionVisibility." + actionVisibility.getId());
    }, options -> {
        return Config.CLIENT.options.showActions.get();
    }, (options, option, actionVisibility) -> {
        Config.CLIENT.options.showActions.set(actionVisibility);
        Config.save();
    });

    public static final CycleOption<Boolean> QUICK_CRAFT = createOnOff("controllable.options.quickCraft", options -> {
        return Config.CLIENT.options.quickCraft.get();
    }, (options, option, value) -> {
        Config.CLIENT.options.quickCraft.set(value);
        Config.save();
    });

    public static final CycleOption<Boolean> UI_SOUNDS = createOnOff("controllable.options.uiSounds", options -> {
        return Config.CLIENT.options.uiSounds.get();
    }, (options, option, value) -> {
        Config.CLIENT.options.uiSounds.set(value);
        Config.save();
    });

    public static final CycleOption<Thumbstick> RADIAL_THUMBSTICK = create("controllable.options.radialThumbstick", Thumbstick.values(), thumbstick -> {
        return new TranslatableComponent("controllable.thumbstick." + thumbstick.getId());
    }, options -> {
        return Config.CLIENT.options.radialThumbstick.get();
    }, (options, option, thumbstick) -> {
        Config.CLIENT.options.radialThumbstick.set(thumbstick);
        Config.save();
    });

    public static final CycleOption<SneakMode> SNEAK_MODE = create("controllable.options.sneakMode", SneakMode.values(), sneakMode -> {
        return new TranslatableComponent("controllable.sneakMode." + sneakMode.getId());
    }, options -> {
        return Config.CLIENT.options.sneakMode.get();
    }, (options, option, sneakMode) -> {
        Config.CLIENT.options.sneakMode.set(sneakMode);
        Config.save();
    });

    public static final CycleOption<Thumbstick> CURSOR_THUMBSTICK = create("controllable.options.cursorThumbstick", Thumbstick.values(), thumbstick -> {
        return new TranslatableComponent("controllable.thumbstick." + thumbstick.getId());
    }, options -> {
        return Config.CLIENT.options.cursorThumbstick.get();
    }, (options, option, thumbstick) -> {
        Config.CLIENT.options.cursorThumbstick.set(thumbstick);
        Config.save();
    });

    public static final ProgressOption HOVER_MODIFIER = new ControllableProgressOption("controllable.options.hoverModifier", 0.05, 1.0, 0.05F, options -> {
        return Config.CLIENT.options.hoverModifier.get();
    }, (options, value) -> {
        Config.CLIENT.options.hoverModifier.set(Mth.clamp(value, 0.05, 1.0));
        Config.save();
    }, (options, option) -> {
        double mouseSpeed = Config.CLIENT.options.hoverModifier.get();
        return new TranslatableComponent("controllable.options.hoverModifier.format", FORMAT.format(mouseSpeed));
    });

    public static CycleOption<Boolean> createOnOff(String key, Function<Options, Boolean> getter, CycleOption.OptionSetter<Boolean> setter)
    {
        return CycleOption.createOnOff(key, new TranslatableComponent(key + ".desc"), getter, setter);
    }

    public static <T> CycleOption<T> create(String key, T[] values, Function<T, Component> display, Function<Options, T> getter, CycleOption.OptionSetter<T> setter)
    {
        List<FormattedCharSequence> tooltip = Minecraft.getInstance().font.split(new TranslatableComponent(key + ".desc"), 200);
        return CycleOption.create(key, values, display, getter, setter).setTooltip(minecraft -> t -> tooltip);
    }
}
