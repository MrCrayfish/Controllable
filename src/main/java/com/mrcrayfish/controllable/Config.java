package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ControllerIcons;
import com.mrcrayfish.controllable.client.CursorType;
import com.mrcrayfish.controllable.client.Thumbstick;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class Config
{
    static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;

    static final ForgeConfigSpec serverSpec;
    public static final Server SERVER;

    public static class Client
    {
        public final Options options;

        Client(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Client configuration settings").push("client");
            this.options = new Options(builder);
            builder.pop();
        }

        public static class Options
        {
            public final ForgeConfigSpec.BooleanValue rumble;
            public final ForgeConfigSpec.BooleanValue autoSelect;
            public final ForgeConfigSpec.BooleanValue renderMiniPlayer;
            public final ForgeConfigSpec.BooleanValue virtualCursor;
            public final ForgeConfigSpec.BooleanValue consoleHotbar;
            public final ForgeConfigSpec.EnumValue<CursorType> cursorType;
            public final ForgeConfigSpec.EnumValue<ControllerIcons> controllerIcons;
            public final ForgeConfigSpec.BooleanValue invertLook;
            public final ForgeConfigSpec.BooleanValue invertRotation;
            public final ForgeConfigSpec.DoubleValue thumbstickDeadZone;
            public final ForgeConfigSpec.DoubleValue triggerDeadZone;
            public final ForgeConfigSpec.DoubleValue rotationSpeed;
            public final ForgeConfigSpec.DoubleValue pitchSensitivity;
            public final ForgeConfigSpec.DoubleValue yawSensitivity;
            public final ForgeConfigSpec.DoubleValue cursorSpeed;
            public final ForgeConfigSpec.EnumValue<ActionVisibility> showButtonHints;
            public final ForgeConfigSpec.BooleanValue drawHintBackground;
            public final ForgeConfigSpec.BooleanValue quickCraft;
            public final ForgeConfigSpec.BooleanValue uiSounds;
            public final ForgeConfigSpec.EnumValue<Thumbstick> radialThumbstick;
            public final ForgeConfigSpec.EnumValue<Thumbstick> cursorThumbstick;
            public final ForgeConfigSpec.DoubleValue hoverModifier;
            public final ForgeConfigSpec.BooleanValue fpsPollingFix;
            public final ForgeConfigSpec.BooleanValue hintBackground;
            public final ForgeConfigSpec.DoubleValue listScrollSpeed;
            public final ForgeConfigSpec.DoubleValue spyglassSensitivity;

            public Options(ForgeConfigSpec.Builder builder)
            {
                builder.comment("In-game related options. These can be changed in game instead of config!").push("options");
                {
                    this.rumble = builder.comment("If enabled, some actions will cause the controller to vibrate").define("forceFeedback", true);
                    this.autoSelect = builder.comment("If enabled, controller will be automatically selected on start up or when plugged in").define("autoSelect", true);
                    this.renderMiniPlayer = builder.comment("If enabled, the player will render in the top left corner likes Bedrock Edition").define("renderMiniPlayer", true);
                    this.virtualCursor = builder.comment("If enabled, the game will use a virtual cursor instead of the real cursor. This must be turned on to be able to run multiple instances!").define("virtualMouse", true);
                    this.consoleHotbar = builder.comment("If enabled, hotbar will render closer to the center of the screen like on console.").define("consoleHotbar", false);
                    this.cursorType = builder.comment("The image to use for the cursor. This only applies if virtual mouse is enabled!").defineEnum("cursorType", CursorType.CONSOLE);
                    this.controllerIcons = builder.comment("The controller icons to use in game to display actions").defineEnum("controllerIcons", ControllerIcons.DEFAULT);
                    this.invertLook = builder.comment("If enabled, inverts the controls on the Y axis for the camera").define("invertLook", false);
                    this.invertRotation = builder.comment("If enabled, inverts the controls on the X axis for the camera").define("invertRotation", false);
                    this.thumbstickDeadZone = builder.comment("The distance you have to move the thumbstick before it's input is registered. This fixes drifting as some thumbsticks don't center to zero.").defineInRange("deadZone", 0.1, 0.0, 1.0);
                    this.triggerDeadZone = builder.comment("How much the trigger has to be pressed before it's input is registered. This fixes issues with triggers not being completely released to zero").defineInRange("triggerDeadZone", 0.05, 0.0, 1.0);
                    this.rotationSpeed = builder.comment("The speed which the camera turns in game").defineInRange("rotationSpeed", 25.0, 0.0, 100.0);
                    this.pitchSensitivity = builder.comment("The sensitivity of the camera's pitch rotation when applying the rotation speed. Setting to 1.0 would mean applying 100% of the rotation speed.").defineInRange("pitchSensitivity", 0.75, 0.0, 1.0);
                    this.yawSensitivity = builder.comment("The sensitivity of the camera's yaw rotation when applying the rotation speed. Setting to 1.0 would mean applying 100% of the rotation speed.").defineInRange("yawSensitivity", 1.0, 0.0, 1.0);
                    this.cursorSpeed = builder.comment("The speed which the cursor or virtual mouse moves around the screen").defineInRange("mouseSpeed", 15.0, 0.0, 50.0);
                    this.showButtonHints = builder.comment("If enabled, shows common actions when displaying available on the screen").defineEnum("showActions", ActionVisibility.MINIMAL);
                    this.drawHintBackground = builder.comment("Draws a transparent background behind the text of the button hint").define("hintBackground", true);
                    this.quickCraft = builder.comment("If enabled, allows you to craft quickly when clicking an item in the recipe book").define("quickCraft", true);
                    this.uiSounds = builder.comment("If enabled, plays a pop sound when you navigate in inventories, menus or scrolling the radial menu").translation("controllable.config.uiSounds").define("uiSounds", true);
                    this.radialThumbstick = builder.comment("The thumbstick to use when scrolling items in the radial menu").translation("controllable.config.radialThumbstick").defineEnum("radialThumbstick", Thumbstick.RIGHT);
                    this.cursorThumbstick = builder.comment("The thumbstick that controls moving the cursor").translation("controllable.config.cursorThumbstick").defineEnum("cursorThumbstick", Thumbstick.LEFT);
                    this.hoverModifier = builder.comment("The scale of the mouse speed when hovering a widget or item slot").defineInRange("hoverModifier", 0.6, 0.05, 1.0);
                    this.fpsPollingFix = builder.comment("Enabling this option will improve polling of controllers when your game FPS is capped. This will not have an effect if your game is already running at a low FPS without a cap.").define("fpsPollingFix", true);
                    this.hintBackground = builder.comment("Draws a transparent background behind the text of the button hint").define("hintBackground", true);
                    this.listScrollSpeed = builder.comment("The speed that lists scroll").defineInRange("listScrollSpeed", 10.0, 1.0, 30.0);
                    this.spyglassSensitivity = builder.comment("Adjusts the camera sensitivity when looking through a spyglass").defineInRange("spyglassSensitivity", 0.2, 0.0, 1.0);
                }
                builder.pop();
            }

            public void restoreDefaults()
            {
                this.resetIfChanged(this.rumble);
                this.resetIfChanged(this.autoSelect);
                this.resetIfChanged(this.renderMiniPlayer);
                this.resetIfChanged(this.virtualCursor);
                this.resetIfChanged(this.consoleHotbar);
                this.resetIfChanged(this.cursorType);
                this.resetIfChanged(this.controllerIcons);
                this.resetIfChanged(this.invertLook);
                this.resetIfChanged(this.invertRotation);
                this.resetIfChanged(this.thumbstickDeadZone);
                this.resetIfChanged(this.rotationSpeed);
                this.resetIfChanged(this.pitchSensitivity);
                this.resetIfChanged(this.yawSensitivity);
                this.resetIfChanged(this.cursorSpeed);
                this.resetIfChanged(this.showButtonHints);
                this.resetIfChanged(this.quickCraft);
                this.resetIfChanged(this.uiSounds);
                this.resetIfChanged(this.radialThumbstick);
                this.resetIfChanged(this.cursorThumbstick);
                this.resetIfChanged(this.hoverModifier);
                this.resetIfChanged(this.fpsPollingFix);
                this.resetIfChanged(this.hintBackground);
                this.resetIfChanged(this.listScrollSpeed);
            }
            
            private <T> void resetIfChanged(ForgeConfigSpec.ConfigValue<T> configValue)
            {
                if(!Objects.equals(configValue.get(), configValue.getDefault()))
                {
                    configValue.set(configValue.getDefault());
                }
            }
        }
    }

    public static class Server
    {
        public final ForgeConfigSpec.BooleanValue restrictToController;

        public Server(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Server configuration settings").push("server");
            {
                this.restrictToController = builder.comment("Restricts players to use only a controller when playing on the server. Be warned that this is not guaranteed and players may still be able to use keyboard and mouse input.").define("restrictToController", false);
            }
            builder.pop();
        }
    }

    static
    {
        final Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = clientPair.getRight();
        CLIENT = clientPair.getLeft();

        final Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = serverPair.getRight();
        SERVER = serverPair.getLeft();
    }

    public static void save()
    {
        clientSpec.save();
    }
}
