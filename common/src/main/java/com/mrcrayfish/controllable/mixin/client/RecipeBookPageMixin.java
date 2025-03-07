package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeBookPage.class)
public class RecipeBookPageMixin
{
    @Shadow
    @Final
    private OverlayRecipeComponent overlay;

    @Shadow
    private Minecraft minecraft;

    @Inject(method = "renderTooltip", at = @At(value = "TAIL"))
    private void controllableRenderTooltipTail(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci)
    {
        Controller controller = Controllable.getController();
        if(controller == null || !controller.isBeingUsed() || !Config.CLIENT.options.quickCraft.get())
            return;

        if(this.minecraft.screen == null || !this.overlay.isVisible())
            return;

        List<AbstractWidget> recipeButtons = ClientHelper.mixinGetRecipeButtons(this.overlay);
        recipeButtons.stream().filter(AbstractWidget::isHoveredOrFocused).findFirst().ifPresent(btn -> {
            if(ClientHelper.mixinIsCraftable(btn)) {
                Component craftText = Component.translatable("controllable.tooltip.craft", ClientHelper.getButtonComponent(ButtonBindings.PICKUP_ITEM.getButton())).withStyle(ChatFormatting.YELLOW);
                graphics.renderTooltip(this.minecraft.font, craftText, mouseX, mouseY);
            }
        });
    }
}
