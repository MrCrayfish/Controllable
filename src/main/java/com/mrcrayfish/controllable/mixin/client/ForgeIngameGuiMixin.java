package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Author: MrCrayfish
 */
@Mixin(GuiIngameForge.class)
public class ForgeIngameGuiMixin
{
    /**
     * Fixes record name rendering not being offset by console hotbar
     */
    @Inject(method = "renderRecordOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", shift = At.Shift.AFTER))
    private void renderRecordOverlay(int width, int height, float partialTicks, CallbackInfo ci)
    {
        if(Controllable.getOptions().useConsoleHotbar())
        {
            GlStateManager.translate(0, -20, 0);
        }
    }

    /**
     * Fixes record name rendering not being offset by console hotbar
     */
    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    private boolean renderPlayerList(KeyBinding binding)
    {
        return binding.isKeyDown() || canShowPlayerList();
    }

    /**
     * Checks if a controller is connected and the player list button is down
     */
    private static boolean canShowPlayerList()
    {
        Controller controller = Controllable.getController();
        return controller != null && ButtonBindings.PLAYER_LIST.isButtonDown();
    }
}
