package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.Buttons;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class ClientHelper
{
    public static final ResourceLocation BUTTON_FONT = new ResourceLocation(Reference.MOD_ID, "buttons");

    public static MutableComponent getButtonComponent(int button)
    {
        MutableComponent component = Component.literal(String.valueOf((char) (32 + (Config.CLIENT.options.controllerIcons.get().ordinal() * 17 + button))));
        component.setStyle(component.getStyle().withColor(ChatFormatting.WHITE).withFont(ClientHelper.BUTTON_FONT));
        return component;
    }

    public static boolean isPlayingGame()
    {
        Minecraft mc = Minecraft.getInstance();
        return mc.getConnection() != null && mc.getConnection().getConnection().isConnected();
    }
}
