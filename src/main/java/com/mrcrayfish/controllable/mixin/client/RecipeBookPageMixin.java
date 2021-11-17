package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeBookPage.class)
public class RecipeBookPageMixin
{
    @ModifyArg(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderComponentTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;IILnet/minecraft/world/item/ItemStack;)V"), index = 1)
    private List<Component> modifyRenderToolTip(List<Component> components)
    {
        if(Controllable.getInput().isControllerInUse() && Config.CLIENT.options.quickCraft.get())
        {
            components.add(new TranslatableComponent("controllable.tooltip.craft").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.BLUE));
        }
        return components;
    }
}
