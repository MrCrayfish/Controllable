package com.mrcrayfish.controllable.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Author: MrCrayfish
 */
@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Shadow
    public LocalPlayer player;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void controllableOnFinishedLoading(CallbackInfo ci)
    {
        BindingRegistry.getInstance().load();
        Controllable.getControllerManager().onClientFinishedLoading();
    }

    /*
     * Modifies the return value of keyAttack.isDown() when calling Minecraft#continueAttack()
     */
    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
    private boolean modifyAttackKeyDown(boolean original)
    {
        Controller controller = Controllable.getController();
        if(controller != null && controller.isBeingUsed() && ButtonBindings.ATTACK.isButtonDown())
        {
            return true;
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
            return true;
        }
        return original;
    }

    @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "HEAD"), cancellable = true)
    private void controllableIsEntityGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if(this.player != null && this.player.isSpectator() && ButtonBindings.HIGHLIGHT_PLAYERS.isButtonDown() && entity.getType() == EntityType.PLAYER)
        {
            cir.setReturnValue(true);
        }
    }

    // Prevents the game from pausing (when losing focus) when a controller is plugged in.
    @Inject(method = "isWindowActive", at = @At(value = "HEAD"), cancellable = true)
    private void controllableIsWindowActiveHead(CallbackInfoReturnable<Boolean> cir)
    {
        // Only apply when in game
        if(this.player != null && Controllable.getController() != null)
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
