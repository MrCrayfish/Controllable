package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Author: MrCrayfish
 */
@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Shadow
    public LocalPlayer player;

    @ModifyArg(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"), index = 0)
    private boolean sendClickBlockToController(boolean original)
    {
        return original || isLeftClicking();
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V")))
    private boolean onKeyDown(KeyMapping mapping)
    {
        return mapping.isDown() || isRightClicking();
    }

    /**
     * Checks if a controller is connected and if the use item button is down.
     */
    private static boolean isRightClicking()
    {
        Controller controller = Controllable.getController();
        return controller != null && ButtonBindings.USE_ITEM.isButtonDown();
    }

    /**
     * Checks if a controller is connected and if the attack button is down. A special except is
     * added when virtual mouse is enabled and it will ignore if the mouse is grabbed or not.
     */
    private static boolean isLeftClicking()
    {
        Minecraft mc = Minecraft.getInstance();
        Controller controller = Controllable.getController();
        if(controller != null && ButtonBindings.ATTACK.isButtonDown())
        {
            boolean usingVirtualMouse = (Config.CLIENT.options.virtualMouse.get() && Controllable.getInput().getLastUse() > 0);
            return mc.screen == null && (mc.mouseHandler.isMouseGrabbed() || usingVirtualMouse);
        }
        return false;
    }

    @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "HEAD"), cancellable = true)
    private void isEntityGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if(this.player != null && this.player.isSpectator() && ButtonBindings.HIGHLIGHT_PLAYERS.isButtonDown() && entity.getType() == EntityType.PLAYER)
        {
            cir.setReturnValue(true);
        }
    }
}
