package com.mrcrayfish.controllable.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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
    private void controllableRenderTooltipTail(PoseStack stack, int mouseX, int mouseY, CallbackInfo ci)
    {
        if(Controllable.getInput().isControllerInUse() && Config.CLIENT.client.options.quickCraft.get())
        {
            if(this.minecraft.screen != null && this.overlay.isVisible())
            {
                List<AbstractWidget> recipeButtons = ((OverlayRecipeComponentAccessor) this.overlay).getRecipeButtons();
                recipeButtons.stream().filter(AbstractWidget::isHoveredOrFocused).findFirst().ifPresent(btn ->
                {
                    if(((OverlayRecipeButtonAccessor) btn).isCraftable())
                    {
                        Component craftText = Component.translatable("controllable.tooltip.craft", ClientHelper.getButtonComponent(ButtonBindings.PICKUP_ITEM.getButton())).withStyle(ChatFormatting.YELLOW);
                        this.minecraft.screen.renderTooltip(stack, craftText, mouseX, mouseY);
                    }
                });
            }
        }
    }
}
