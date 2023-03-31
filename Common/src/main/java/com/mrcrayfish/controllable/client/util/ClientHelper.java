package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ClientHelper
{
    public static final ResourceLocation BUTTON_FONT = new ResourceLocation(Constants.MOD_ID, "buttons");

    public static MutableComponent getButtonComponent(int button)
    {
        MutableComponent component = Component.literal(String.valueOf((char) (32 + (Config.CLIENT.client.options.controllerIcons.get().ordinal() * 17 + button))));
        component.setStyle(component.getStyle().withColor(ChatFormatting.WHITE).withFont(ClientHelper.BUTTON_FONT));
        return component;
    }

    public static boolean isPlayingGame()
    {
        Minecraft mc = Minecraft.getInstance();
        return mc.getConnection() != null && mc.getConnection().isAcceptingMessages();
    }

    @SuppressWarnings("unchecked")
    public static <T extends FormattedText> Tooltip createListTooltip(List<T> lines)
    {
        List<FormattedCharSequence> charSequenceList = Language.getInstance().getVisualOrder((List<FormattedText>) lines);
        Tooltip blank = Tooltip.create(CommonComponents.EMPTY);
        //TODO finish this
        //ReflectUtil.pushLinesToTooltip(blank, charSequenceList);
        return blank;
    }
}
