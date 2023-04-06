package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ControllerIcons;
import com.mrcrayfish.controllable.client.CursorType;
import com.mrcrayfish.controllable.client.Thumbstick;
import com.mrcrayfish.framework.api.config.BoolProperty;
import com.mrcrayfish.framework.api.config.DoubleProperty;
import com.mrcrayfish.framework.api.config.EnumProperty;
import com.mrcrayfish.framework.api.config.validate.NumberRange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ControllerOptions
{
    //public static final ControllerSetting<Boolean> FORCE_FEEDBACK = createBoolean("controllable.options.forceFeedback", Config.CLIENT.client.options.forceFeedback);

    /*public static final CycleOption<Boolean> FORCE_FEEDBACK = createOnOff(, options -> {
        return Config.CLIENT.client.options.forceFeedback.get();
    }, (options, option, value) -> {
        Config.CLIENT.client.options.forceFeedback.set(value);
        Config.save();
    });*/

    public static final ControllerSetting<Boolean> AUTO_SELECT = createToggleSetting("controllable.options.autoSelect", Config.CLIENT.client.options.autoSelect);
    public static final ControllerSetting<Boolean> RENDER_MINI_PLAYER = createToggleSetting("controllable.options.renderMiniPlayer", Config.CLIENT.client.options.renderMiniPlayer);
    public static final ControllerSetting<Boolean> VIRTUAL_MOUSE = createToggleSetting("controllable.options.virtualMouse", Config.CLIENT.client.options.virtualCursor);
    public static final ControllerSetting<Boolean> CONSOLE_HOTBAR = createToggleSetting("controllable.options.consoleHotbar", Config.CLIENT.client.options.consoleHotbar);
    public static final ControllerSetting<CursorType> CURSOR_TYPE = createValuesSetting("controllable.options.cursorType", CursorType.class, Config.CLIENT.client.options.cursorType);
    public static final ControllerSetting<ControllerIcons> CONTROLLER_ICONS = createValuesSetting("controllable.options.controllerIcons", ControllerIcons.class, Config.CLIENT.client.options.controllerIcons);
    public static final ControllerSetting<Boolean> INVERT_LOOK = createToggleSetting("controllable.options.invertLook", Config.CLIENT.client.options.invertLook);
    public static final ControllerSetting<Boolean> INVERT_ROTATION = createToggleSetting("controllable.options.invertRotation", Config.CLIENT.client.options.invertRotation);
    public static final ControllerSetting<Double> DEAD_ZONE = createSliderSetting("controllable.options.deadZone", Config.CLIENT.client.options.thumbstickDeadZone, 0.01);
    public static final ControllerSetting<Double> ROTATION_SPEED = createSliderSetting("controllable.options.rotationSpeed", Config.CLIENT.client.options.rotationSpeed, 1.0);
    public static final ControllerSetting<Double> PITCH_SENSITIVITY = createSliderSetting("controllable.options.pitchSensitivity", Config.CLIENT.client.options.pitchSensitivity, 0.01);
    public static final ControllerSetting<Double> YAW_SENSITIVITY = createSliderSetting("controllable.options.yawSensitivity", Config.CLIENT.client.options.yawSensitivity, 0.01);
    public static final ControllerSetting<Double> MOUSE_SPEED = createSliderSetting("controllable.options.mouseSpeed", Config.CLIENT.client.options.cursorSpeed, 1.0);
    public static final ControllerSetting<Boolean> QUICK_CRAFT = createToggleSetting("controllable.options.quickCraft", Config.CLIENT.client.options.quickCraft);
    public static final ControllerSetting<Boolean> UI_SOUNDS = createToggleSetting("controllable.options.uiSounds", Config.CLIENT.client.options.uiSounds);
    public static final ControllerSetting<Double> HOVER_MODIFIER = createSliderSetting("controllable.options.hoverModifier", Config.CLIENT.client.options.hoverModifier, 0.05);
    public static final ControllerSetting<ActionVisibility> SHOW_ACTIONS = createValuesSetting("controllable.options.showActions", ActionVisibility.class, Config.CLIENT.client.options.showButtonHints);
    public static final ControllerSetting<Thumbstick> RADIAL_THUMBSTICK = createValuesSetting("controllable.options.radialThumbstick", Thumbstick.class, Config.CLIENT.client.options.radialThumbstick);
    public static final VanillaSetting<Boolean> SNEAK_MODE = createVanillaSetting(() -> Minecraft.getInstance().options.toggleCrouch());
    public static final VanillaSetting<Boolean> SPRINT_MODE = createVanillaSetting(() -> Minecraft.getInstance().options.toggleSprint());
    public static final ControllerSetting<Thumbstick> CURSOR_THUMBSTICK = createValuesSetting("controllable.options.cursorThumbstick", Thumbstick.class, Config.CLIENT.client.options.cursorThumbstick);
    public static final ControllerSetting<Boolean> FPS_POLLING_FIX = createToggleSetting("controllable.options.fpsPollingFix", Config.CLIENT.client.options.fpsPollingFix);
    public static final ControllerSetting<Boolean> HINT_BACKGROUND = createToggleSetting("controllable.options.hintBackground", Config.CLIENT.client.options.drawHintBackground);
    public static final ControllerSetting<Double> LIST_SCROLL_SPEED = createSliderSetting("controllable.options.listScrollSpeed", Config.CLIENT.client.options.listScrollSpeed, 1.0);

    public static ControllerSetting<Boolean> createToggleSetting(String key, BoolProperty property)
    {
        return new ControllerToggleSetting(key, property);
    }

    public static <T extends Enum<T> & SettingEnum> ControllerSetting<T> createValuesSetting(String key, Class<T> clazz, EnumProperty<T> property)
    {
        return new ControllerEnumSetting<>(key, clazz, property);
    }

    public static ControllerSetting<Double> createSliderSetting(String key, DoubleProperty property, double stepSize)
    {
        if(!(property.getValidator() instanceof NumberRange<Double> range))
            throw new IllegalArgumentException("Double property must have a number range");
        return new ControllerSliderSetting(key, property, range.minValue(), range.maxValue(), stepSize);
    }

    public static <T> VanillaSetting<T> createVanillaSetting(Supplier<OptionInstance<T>> optionSupplier)
    {
        return new VanillaSetting<>(optionSupplier);
    }
}
