package com.mrcrayfish.controllable.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.Icons;
import com.mrcrayfish.controllable.client.RenderEvents;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ClientHelper
{
    public static final ResourceLocation ICON_FONT = new ResourceLocation(Constants.MOD_ID, "icons");
    public static final ResourceLocation BUTTON_FONT = new ResourceLocation(Constants.MOD_ID, "buttons");

    public static MutableComponent getIcon(Icons icon)
    {
        MutableComponent component = Component.literal(String.valueOf((char) (33 + icon.ordinal())));
        component.setStyle(component.getStyle().withColor(ChatFormatting.WHITE).withFont(ClientHelper.ICON_FONT));
        return component;
    }

    public static MutableComponent getButtonComponent(int button)
    {
        MutableComponent component = Component.literal(String.valueOf((char) (32 + (Config.CLIENT.client.options.controllerIcons.get().ordinal() * 17 + button))));
        component.setStyle(component.getStyle().withColor(ChatFormatting.WHITE).withFont(ClientHelper.BUTTON_FONT));
        return component;
    }

    public static void drawButton(PoseStack poseStack, int x, int y, int button)
    {
        int texU = button * 13;
        int texV = Config.CLIENT.client.options.controllerIcons.get().ordinal() * 13;
        int size = 13;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, RenderEvents.CONTROLLER_BUTTONS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(poseStack, x, y, texU, texV, size, size, RenderEvents.CONTROLLER_BUTTONS_WIDTH, RenderEvents.CONTROLLER_BUTTONS_HEIGHT);
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

    public static float applyDeadzone(float input, float deadZone)
    {
        return Mth.sign(input) * Math.max(Mth.abs(input) - deadZone, 0) / (1 - deadZone);
    }
}
