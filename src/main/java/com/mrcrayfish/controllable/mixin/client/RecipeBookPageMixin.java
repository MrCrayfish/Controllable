package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
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
    @ModifyArgs(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawHoveringText(Ljava/util/List;II)V"))
    private void modifyRenderToolTip(Args args)
    {
        if(Controllable.getInput().isControllerInUse() && Controllable.getOptions().isQuickCraft())
        {
            List<String> components = args.get(0);
            ITextComponent tooltip = new TextComponentTranslation("controllable.tooltip.craft");
            tooltip.getStyle().setColor(TextFormatting.BLUE).setBold(true);
            components.add(tooltip.getFormattedText());
        }
    }
}
