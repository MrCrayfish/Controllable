package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

/**
 * Author: MrCrayfish
 */
@Mixin(RecipeBookPage.class)
public class RecipeBookPageMixin
{
    @ModifyArgs(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderTooltip(Ljava/util/List;II)V"))
    private void modifyRenderToolTip(Args args)
    {
        if(Controllable.getInput().isControllerInUse() && Config.CLIENT.options.quickCraft.get())
        {
            List<String> components = args.get(0);
            components.add(new TranslationTextComponent("controllable.tooltip.craft").applyTextStyle(TextFormatting.BOLD).applyTextStyle(TextFormatting.BLUE).getFormattedText());
        }
    }
}
