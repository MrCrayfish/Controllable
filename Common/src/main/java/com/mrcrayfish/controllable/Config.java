package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ControllerIcons;
import com.mrcrayfish.controllable.client.CursorType;
import com.mrcrayfish.controllable.client.Thumbstick;
import com.mrcrayfish.framework.api.config.BoolProperty;
import com.mrcrayfish.framework.api.config.ConfigProperty;
import com.mrcrayfish.framework.api.config.DoubleProperty;
import com.mrcrayfish.framework.api.config.EnumProperty;
import com.mrcrayfish.framework.api.config.FrameworkConfig;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class Config
{
    public static final ResourceLocation CLIENT_CONFIG_ID = new ResourceLocation(Constants.MOD_ID, "client");

    @FrameworkConfig(id = Constants.MOD_ID, name = "client", separator = '-')
    public static final ClientWrapper CLIENT = new ClientWrapper();

    public static class ClientWrapper
    {
        @ConfigProperty(name = "client", comment = "Client configuration settings")
        public final Client client = new Client();
    }

    public static class Client
    {
        @ConfigProperty(name = "options", comment = "In-game related options. These can be changed in game instead of config!")
        public final Options options = new Options();

        public static class Options
        {
            @ConfigProperty(name = "forceFeedback", comment = "If enabled, some actions will cause the controller to vibrate")
            public final BoolProperty forceFeedback = BoolProperty.create(true);

            @ConfigProperty(name = "autoSelect", comment = "If enabled, controller will be automatically selected on start up or when plugged in")
            public final BoolProperty autoSelect = BoolProperty.create(true);

            @ConfigProperty(name = "renderMiniPlayer", comment = "If enabled, the player will render in the top left corner likes Bedrock Edition")
            public final BoolProperty renderMiniPlayer = BoolProperty.create(true);

            @ConfigProperty(name = "virtualMouse", comment = "If enabled, the game will use a virtual cursor instead of the real cursor. This must be turned on to be able to run multiple instances!")
            public final BoolProperty virtualCursor = BoolProperty.create(true);

            @ConfigProperty(name = "consoleHotbar", comment = "If enabled, hotbar will render closer to the center of the screen like on console.")
            public final BoolProperty consoleHotbar = BoolProperty.create(false);

            @ConfigProperty(name = "cursorType", comment = "The image to use for the cursor. This only applies if virtual mouse is enabled!")
            public final EnumProperty<CursorType> cursorType = EnumProperty.create(CursorType.CONSOLE);

            @ConfigProperty(name = "controllerIcons", comment = "The controller icons to use in game to display actions")
            public final EnumProperty<ControllerIcons> controllerIcons = EnumProperty.create(ControllerIcons.DEFAULT);

            @ConfigProperty(name = "invertLook", comment = "If enabled, inverts the controls on the Y axis for the camera")
            public final BoolProperty invertLook = BoolProperty.create(false); //TODO rename

            @ConfigProperty(name = "invertRotation", comment = "If enabled, inverts the controls on the X axis for the camera")
            public final BoolProperty invertRotation = BoolProperty.create(false); //TODO rename

            @ConfigProperty(name = "deadZone", comment = "The distance you have to move the thumbstick before it's input is registered. This fixes drifting as some thumbsticks don't center to zero.")
            public final DoubleProperty thumbstickDeadZone = DoubleProperty.create(0.15, 0.0, 1.0);

            @ConfigProperty(name = "rotationSpeed", comment = "The speed which the camera turns in game")
            public final DoubleProperty rotationSpeed = DoubleProperty.create(25.0, 0.0, 100.0);

            @ConfigProperty(name = "pitchSensitivity", comment = "The sensitivity of the camera's pitch rotation when applying the rotation speed. Setting to 1.0 would mean applying 100% of the rotation speed.")
            public final DoubleProperty pitchSensitivity = DoubleProperty.create(0.75, 0.0, 1.0);

            @ConfigProperty(name = "yawSensitivity", comment = "The sensitivity of the camera's yaw rotation when applying the rotation speed. Setting to 1.0 would mean applying 100% of the rotation speed.")
            public final DoubleProperty yawSensitivity = DoubleProperty.create(1.0, 0.0, 1.0);

            @ConfigProperty(name = "mouseSpeed", comment = "The speed which the cursor or virtual mouse moves around the screen")
            public final DoubleProperty cursorSpeed = DoubleProperty.create(15.0, 0.0, 50.0);

            @ConfigProperty(name = "showActions", comment = "If enabled, shows common actions when displaying available on the screen")
            public final EnumProperty<ActionVisibility> showButtonHints = EnumProperty.create(ActionVisibility.MINIMAL);

            @ConfigProperty(name = "quickCraft", comment = "If enabled, allows you to craft quickly when clicking an item in the recipe book")
            public final BoolProperty quickCraft = BoolProperty.create(true);

            @ConfigProperty(name = "uiSounds", comment = "If enabled, plays a pop sound when you navigate in inventories, menus or scrolling the radial menu")
            public final BoolProperty uiSounds = BoolProperty.create(true);

            @ConfigProperty(name = "radialThumbstick", comment = "The thumbstick to use when scrolling items in the radial menu")
            public final EnumProperty<Thumbstick> radialThumbstick = EnumProperty.create(Thumbstick.RIGHT);

            @ConfigProperty(name = "cursorThumbstick", comment = "The thumbstick that controls moving the cursor")
            public final EnumProperty<Thumbstick> cursorThumbstick = EnumProperty.create(Thumbstick.LEFT);

            @ConfigProperty(name = "hoverModifier", comment = "The scale of the mouse speed when hovering a widget or item slot")
            public final DoubleProperty hoverModifier = DoubleProperty.create(0.6, 0.05, 1.0);

            @ConfigProperty(name = "fpsPollingFix", comment = "Enabling this option will improve polling of controllers when your game FPS is capped. This will not have an effect if your game is already running at a low FPS without a cap.")
            public final BoolProperty fpsPollingFix = BoolProperty.create(false);

            @ConfigProperty(name = "hintBackground", comment = "Draws a transparent background behind the text of the button hint")
            public final BoolProperty drawHintBackground = BoolProperty.create(true);

            @ConfigProperty(name = "listScrollSpeed", comment = "The speed that lists scroll")
            public final DoubleProperty listScrollSpeed = DoubleProperty.create(10.0, 1.0, 30.0);
        }
    }
}
