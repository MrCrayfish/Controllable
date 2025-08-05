package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mrcrayfish.controllable.Config;
import net.minecraft.client.ClientRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin
{
    /*
     * Prevents grouping recipes in the recipe book when quick craft mode is enabled. Interacting
     * with grouped recipes is difficult on controller, so flattening them all to single entries
     * feels a lot better. If quick craft option is toggled on or off during runtime, it will
     * automatically group/ungroup the recipes.
     */
    @ModifyExpressionValue(method = "categorizeAndGroupRecipes", at = @At(value = "INVOKE", target = "Ljava/lang/String;isEmpty()Z"))
    private static boolean test(boolean original)
    {
        return original || Config.CLIENT.options.quickCraft.get();
    }
}
