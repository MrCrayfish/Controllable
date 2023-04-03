package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeBookPage.class)
public class FabricRecipeBookPageMixin
{
    @ModifyArg(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderComponentTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V"), index = 1)
    private List<Component> controllableModifyRenderToolTip(List<Component> components)
    {
        if(Controllable.getInput().isControllerInUse() && Config.CLIENT.client.options.quickCraft.get())
        {
            if(components.removeIf(c -> c.getContents() instanceof TranslatableContents t && t.getKey().equals("gui.recipebook.moreRecipes")))
            {
                components.add(Component.translatable("controllable.tooltip.more_recipes", ClientHelper.getButtonComponent(ButtonBindings.SPLIT_STACK.getButton())).withStyle(ChatFormatting.YELLOW));
            }
            components.add(Component.translatable("controllable.tooltip.craft", ClientHelper.getButtonComponent(ButtonBindings.PICKUP_ITEM.getButton())).withStyle(ChatFormatting.YELLOW));
        }
        return components;
    }
}
