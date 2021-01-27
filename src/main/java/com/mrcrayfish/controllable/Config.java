package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ControllerIcons;
import com.mrcrayfish.controllable.client.CursorType;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Author: MrCrayfish
 */
public class Config
{
    static final ForgeConfigSpec clientSpec;
    public static final Config.Client CLIENT;

    public static class Client
    {
        public final ForgeConfigSpec.LongValue controllerPollInterval;
        public final Options options;

        Client(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Client configuration settings").push("client");
            this.controllerPollInterval = builder
                    .comment("The time in milliseconds to wait before polling the controller. The lower the value the better the input latency but drains the controller battery faster.")
                    .translation("controllable.configgui.controllerPollInterval")
                    .defineInRange("controllerPollInterval", 8L, 1L, 128L);
            this.options = new Options(builder);
            builder.pop();
        }

        public static class Options
        {
            public final ForgeConfigSpec.BooleanValue forceFeedback;
            public final ForgeConfigSpec.BooleanValue autoSelect;
            public final ForgeConfigSpec.BooleanValue renderMiniPlayer;
            public final ForgeConfigSpec.BooleanValue virtualMouse;
            public final ForgeConfigSpec.BooleanValue consoleHotbar;
            public final ForgeConfigSpec.EnumValue<CursorType> cursorType;
            public final ForgeConfigSpec.EnumValue<ControllerIcons> controllerIcons;
            public final ForgeConfigSpec.BooleanValue invertLook;
            public final ForgeConfigSpec.DoubleValue deadZone;
            public final ForgeConfigSpec.DoubleValue rotationSpeed;
            public final ForgeConfigSpec.DoubleValue mouseSpeed;
            public final ForgeConfigSpec.BooleanValue verboseActions;

            public Options(ForgeConfigSpec.Builder builder)
            {
                builder.comment("In-game related options. These can be changed in game instead of config!").push("options");
                {
                    this.forceFeedback = builder.comment("If enabled, some actions will cause the controller to vibrate").define("forceFeedback", true);
                    this.autoSelect = builder.comment("If enabled, controller will be automatically selected on start up or when plugged in").define("autoSelect", true);
                    this.renderMiniPlayer = builder.comment("If enabled, the player will render in the top left corner likes Bedrock Edition").define("renderMiniPlayer", true);
                    this.virtualMouse = builder.comment("If enabled, the game will use a virtual cursor instead of the real cursor. This must be turned on to be able to run multiple instances!").define("virtualMouse", true);
                    this.consoleHotbar = builder.comment("If enabled, hotbar will render closer to the center of the screen like on console.").define("consoleHotbar", false);
                    this.cursorType = builder.comment("The image to use for the cursor. This only applies if virtual mouse is enabled!").defineEnum("cursorType", CursorType.LIGHT);
                    this.controllerIcons = builder.comment("The controller icons to use in game to display actions").defineEnum("controllerIcons", ControllerIcons.DEFAULT);
                    this.invertLook = builder.comment("If enabled, inverts the controls on the Y axis for the camera").define("invertLook", false);
                    this.deadZone = builder.comment("The distance you have to move the thumbstick before it's input is registered. This fixes drifting as some thumbsticks don't center to zero.").defineInRange("deadZone", 0.15, 0.0, 1.0);
                    this.rotationSpeed = builder.comment("The speed which the camera turns in game").defineInRange("rotationSpeed", 25.0, 0.0, 50.0);
                    this.mouseSpeed = builder.comment("The speed which the cursor or virtual mouse moves around the screen").defineInRange("mouseSpeed", 15.0, 0.0, 50.0);
                    this.verboseActions = builder.comment("If true, shows common actions when displaying available on the screen").define("verboseActions", false);
                }
                builder.pop();
            }
        }
    }

    static
    {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config.Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static void save()
    {
        clientSpec.save();
    }
}
