package com.mrcrayfish.controllable;

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

        Client(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Client configuration settings").push("common");
            this.controllerPollInterval = builder
                    .comment("The time in milliseconds to wait before polling the controller. The lower the value the better the input latency but drains the controller battery faster.")
                    .translation("controllable.configgui.controllerPollInterval")
                    .defineInRange("renderOllieStrengthBar", 8L, 1L, 128L);
            builder.pop();
        }
    }

    static
    {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config.Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }
}
