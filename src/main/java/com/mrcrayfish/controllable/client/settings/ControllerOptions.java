package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.*;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

import java.text.DecimalFormat;

/**
 * Author: MrCrayfish
 */
public class ControllerOptions
{
    private static final DecimalFormat FORMAT = new DecimalFormat("0.0#");

    public static final BooleanOption FORCE_FEEDBACK = new ControllableBooleanOption("controllable.options.forceFeedback", gameSettings -> {
        return Config.CLIENT.options.forceFeedback.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.forceFeedback.set(value);
        Config.save();
    });

    public static final BooleanOption AUTO_SELECT = new ControllableBooleanOption("controllable.options.autoSelect", gameSettings -> {
        return Config.CLIENT.options.autoSelect.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.autoSelect.set(value);
        Config.save();
    });

    public static final BooleanOption RENDER_MINI_PLAYER = new ControllableBooleanOption("controllable.options.renderMiniPlayer", gameSettings -> {
        return Config.CLIENT.options.renderMiniPlayer.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.renderMiniPlayer.set(value);
        Config.save();
    });

    public static final BooleanOption VIRTUAL_MOUSE = new ControllableBooleanOption("controllable.options.virtualMouse", gameSettings -> {
        return Config.CLIENT.options.virtualMouse.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.virtualMouse.set(value);
        Config.save();
    });

    public static final BooleanOption CONSOLE_HOTBAR = new ControllableBooleanOption("controllable.options.consoleHotbar", gameSettings -> {
        return Config.CLIENT.options.consoleHotbar.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.consoleHotbar.set(value);
        Config.save();
    });

    public static final ControllableEnumOption<CursorType> CURSOR_TYPE = new ControllableEnumOption<>("controllable.options.cursorType", CursorType.class, gameSettings -> {
        return Config.CLIENT.options.cursorType.get();
    }, (gameSettings, cursorType) -> {
        Config.CLIENT.options.cursorType.set(cursorType);
        Config.save();
    }, (gameSettings, controllableEnumOption) -> {
        CursorType cursorType = controllableEnumOption.get(gameSettings);
        return new TranslationTextComponent("controllable.options.cursorType.format", new TranslationTextComponent("controllable.cursor." + cursorType.getString()));
    });

    public static final ControllableEnumOption<ControllerIcons> CONTROLLER_ICONS = new ControllableEnumOption<>("controllable.options.controllerIcons", ControllerIcons.class, gameSettings -> {
        return Config.CLIENT.options.controllerIcons.get();
    }, (gameSettings, controllerIcons) -> {
        Config.CLIENT.options.controllerIcons.set(controllerIcons);
        Config.save();
    }, (gameSettings, controllableEnumOption) -> {
        ControllerIcons controllerIcons = controllableEnumOption.get(gameSettings);
        return new TranslationTextComponent("controllable.options.controllerIcons.format", new TranslationTextComponent("controllable.controller." + controllerIcons.getString()));
    });

    public static final BooleanOption INVERT_LOOK = new ControllableBooleanOption("controllable.options.invertLook", gameSettings -> {
        return Config.CLIENT.options.invertLook.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.invertLook.set(value);
        Config.save();
    });

    public static final SliderPercentageOption DEAD_ZONE = new ControllableSliderPercentageOption("controllable.options.deadZone", 0.0, 1.0, 0.01F, gameSettings -> {
        return Config.CLIENT.options.deadZone.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.deadZone.set(MathHelper.clamp(value, 0.0, 1.0));
        Config.save();
    }, (gameSettings, option) -> {
        double deadZone = Config.CLIENT.options.deadZone.get();
        return new TranslationTextComponent("controllable.options.deadZone.format", FORMAT.format(deadZone));
    });

    public static final SliderPercentageOption ROTATION_SPEED = new ControllableSliderPercentageOption("controllable.options.rotationSpeed", 1.0, 50.0, 1.0F, gameSettings -> {
        return Config.CLIENT.options.rotationSpeed.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.rotationSpeed.set(MathHelper.clamp(value, 0.0, 100.0));
        Config.save();
    }, (gameSettings, option) -> {
        double rotationSpeed = Config.CLIENT.options.rotationSpeed.get();
        return new TranslationTextComponent("controllable.options.rotationSpeed.format", FORMAT.format(rotationSpeed));
    });

    public static final SliderPercentageOption MOUSE_SPEED = new ControllableSliderPercentageOption("controllable.options.mouseSpeed", 1.0, 50.0, 1.0F, gameSettings -> {
        return Config.CLIENT.options.mouseSpeed.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.mouseSpeed.set(MathHelper.clamp(value, 0.0, 50.0));
        Config.save();
    }, (gameSettings, option) -> {
        double mouseSpeed = Config.CLIENT.options.mouseSpeed.get();
        return new TranslationTextComponent("controllable.options.mouseSpeed.format", FORMAT.format(mouseSpeed));
    });

    public static final ControllableEnumOption<ActionVisibility> SHOW_ACTIONS = new ControllableEnumOption<>("controllable.options.showActions", ActionVisibility.class, gameSettings -> {
        return Config.CLIENT.options.showActions.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.showActions.set(value);
        Config.save();
    }, (gameSettings, option) -> {
        ActionVisibility visibility = option.get(gameSettings);
        return new TranslationTextComponent("controllable.options.showActions.format", new TranslationTextComponent("controllable.actionVisibility." + visibility.getString()));
    });

    public static final BooleanOption QUICK_CRAFT = new ControllableBooleanOption("controllable.options.quickCraft", gameSettings -> {
        return Config.CLIENT.options.quickCraft.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.quickCraft.set(value);
        Config.save();
    });

    public static final BooleanOption UI_SOUNDS = new ControllableBooleanOption("controllable.options.uiSounds", gameSettings -> {
        return Config.CLIENT.options.uiSounds.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.uiSounds.set(value);
        Config.save();
    });

    public static final ControllableEnumOption<Thumbstick> RADIAL_THUMBSTICK = new ControllableEnumOption<>("controllable.options.radialThumbstick", Thumbstick.class, gameSettings -> {
        return Config.CLIENT.options.radialThumbstick.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.radialThumbstick.set(value);
        Config.save();
    }, (gameSettings, option) -> {
        Thumbstick thumbstick = option.get(gameSettings);
        return new TranslationTextComponent("controllable.options.radialThumbstick.format", new TranslationTextComponent(thumbstick.getKey()));
    });

    public static final ControllableEnumOption<SneakMode> SNEAK_MODE = new ControllableEnumOption<>("controllable.options.sneakMode", SneakMode.class, gameSettings -> {
        return Config.CLIENT.options.sneakMode.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.sneakMode.set(value);
        Config.save();
    }, (gameSettings, option) -> {
        SneakMode sneakMode = option.get(gameSettings);
        return new TranslationTextComponent("controllable.options.sneakMode.format", new TranslationTextComponent("controllable.sneakMode." + sneakMode.getString()));
    });

    public static final ControllableEnumOption<Thumbstick> CURSOR_THUMBSTICK = new ControllableEnumOption<>("controllable.options.cursorThumbstick", Thumbstick.class, gameSettings -> {
        return Config.CLIENT.options.cursorThumbstick.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.cursorThumbstick.set(value);
        Config.save();
    }, (gameSettings, option) -> {
        Thumbstick cursorThumbstick = option.get(gameSettings);
        return new TranslationTextComponent("controllable.options.cursorThumbstick.format", new TranslationTextComponent("controllable.cursorThumbstick." + cursorThumbstick.getString()));
    });

    public static final SliderPercentageOption HOVER_MODIFIER = new ControllableSliderPercentageOption("controllable.options.hoverModifier", 0.05, 1.0, 0.05F, gameSettings -> {
        return Config.CLIENT.options.hoverModifier.get();
    }, (gameSettings, value) -> {
        Config.CLIENT.options.hoverModifier.set(MathHelper.clamp(value, 0.05, 1.0));
        Config.save();
    }, (gameSettings, option) -> {
        double mouseSpeed = Config.CLIENT.options.hoverModifier.get();
        return new TranslationTextComponent("controllable.options.hoverModifier.format", FORMAT.format(mouseSpeed));
    });
}
