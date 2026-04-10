package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.controllable.client.binding.handlers.impl.AttackHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Shadow
    @Nullable
    public LocalPlayer player;

    /*
     * Modifies the return value of keyAttack.isDown() when calling Minecraft#continueAttack()
     */
    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
    private boolean modifyAttackKeyDown(boolean original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed() && ButtonBindings.ATTACK.isButtonDown())
        {
            controller.updateInputTime();
            return !AttackHandler.shouldPreventContinue();
        }
        return original;
    }

    /*
     * Modifies the return value of keyAttack.isDown() when calling Minecraft#continueAttack()
     */
    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 3))
    private boolean modifyUseKeyDown(boolean original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed() && ButtonBindings.USE_ITEM.isButtonDown())
        {
            controller.updateInputTime();
            return true;
        }
        return original;
    }

    /*
     * Modifies the return value of keyUse.isDown() when checking before releasing using item
     */
    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
    private boolean modifyReleaseUseKeyDown(boolean original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed() && ButtonBindings.USE_ITEM.isButtonDown())
        {
            controller.updateInputTime();
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = "shouldEntityAppearGlowing", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean controllableIsEntityGlowing(boolean original)
    {
        return original || ButtonBindings.HIGHLIGHT_PLAYERS.isButtonDown();
    }

    // Prevents the game from pausing (when losing focus) when a controller is plugged in.
    @Inject(method = "isWindowActive", at = @At(value = "HEAD"), cancellable = true)
    private void controllableIsWindowActiveHead(CallbackInfoReturnable<Boolean> cir)
    {
        // Only apply when in game
        if(this.player != null && Controllable.getController() != null && Config.CLIENT.options.backgroundInput.get())
        {
            cir.setReturnValue(true);
        }
    }

    // Note: Minecraft Development plugin is failing to process this correctly.
    @ModifyVariable(method = "runTick", at = @At(value = "STORE", target = "Lnet/minecraft/client/Minecraft;getFramerateLimit()I"), index = 7)
    private int controllableModifyFramerate(int originalFps)
    {
        Minecraft mc = (Minecraft) (Object) this;
        if(mc.getOverlay() == null)
        {
            if(Config.CLIENT.options.fpsPollingFix.get() && ClientServices.CLIENT.getMinecraftFramerateLimit() < 40)
            {
                return 260; // To bypass "fps < 260" condition
            }
        }
        return originalFps;
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getFramerateLimit()I"))
    private void controllableWaitEvents(boolean outOfMemory, CallbackInfo ci)
    {
        Minecraft mc = (Minecraft) (Object) this;
        if(mc.getOverlay() == null)
        {
            if(Config.CLIENT.options.fpsPollingFix.get() && ClientServices.CLIENT.getMinecraftFramerateLimit() < 40)
            {
                Controllable.getInputProcessor().queueInputsWait();
            }
        }
    }
}
