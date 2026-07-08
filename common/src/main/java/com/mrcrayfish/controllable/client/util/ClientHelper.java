package com.mrcrayfish.controllable.client.util;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.settings.ButtonIcons;
import com.mrcrayfish.controllable.mixin.client.OverlayRecipeComponentAccessor;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.controllable.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ClientHelper
{
    public static final FontDescription.Resource ICON_FONT = new FontDescription.Resource(Utils.resource("icons"));
    public static final FontDescription.Resource BUTTON_FONT = new FontDescription.Resource(Utils.resource("buttons"));

    public static MutableComponent getIconComponent(Icons icon)
    {
        MutableComponent component = Component.literal(String.valueOf((char) (33 + icon.ordinal())));
        component.setStyle(component.getStyle().withColor(ChatFormatting.WHITE).withFont(ClientHelper.ICON_FONT));
        return component;
    }

    public static MutableComponent getButtonComponent(int button)
    {
        MutableComponent component = Component.literal(String.valueOf((char) (33 + (Config.CLIENT.options.controllerIcons.get().ordinal() * Buttons.LENGTH + button))));
        component.setStyle(component.getStyle().withColor(ChatFormatting.WHITE).withFont(ClientHelper.BUTTON_FONT));
        return component;
    }

    public static MutableComponent join(int button, Component label)
    {
        return Component.empty().append(getButtonComponent(button)).append(" ").append(label);
    }

    public static MutableComponent join(Icons icon, Component label)
    {
        return Component.empty().append(getIconComponent(icon)).append(" ").append(label);
    }

    public static void drawButton(GuiGraphicsExtractor extractor, int x, int y, int button)
    {
        int texU = button * 13;
        int texV = Config.CLIENT.options.controllerIcons.get().ordinal() * 13;
        int size = 13;
        extractor.blit(RenderPipelines.GUI_TEXTURED, ButtonIcons.TEXTURE, x, y, texU, texV, size, size, ButtonIcons.TEXTURE_WIDTH, ButtonIcons.TEXTURE_HEIGHT);
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
        ClientServices.CLIENT.pushLinesToTooltip(blank, charSequenceList);
        return blank;
    }

    public static Component getOptionName(OptionInstance<Boolean> option)
    {
        return ClientServices.CLIENT.getOptionInstanceName(option);
    }

    public static Tooltip getOptionTooltip(OptionInstance<Boolean> option)
    {
        return ClientServices.CLIENT.getOptionInstanceTooltip(option);
    }

    public static boolean isChatVisible()
    {
        Minecraft mc = Minecraft.getInstance();
        List<GuiMessage.Line> messages = ClientServices.CLIENT.getChatTrimmedMessages(mc.gui.hud.getChat());
        return mc.gui.screen() == null && messages.stream().anyMatch(chatLine -> mc.gui.hud.getGuiTicks() - chatLine.addedTime() < 200);
    }

    public static boolean isSubtitleShowing()
    {
        Minecraft mc = Minecraft.getInstance();
        return mc.options.showSubtitles().get() && mc.gui.screen() == null;
    }

    public static List<AbstractWidget> mixinGetRecipeButtons(OverlayRecipeComponent overlay)
    {
        return ((OverlayRecipeComponentAccessor) overlay).controllableGetRecipeButtons();
    }
}
