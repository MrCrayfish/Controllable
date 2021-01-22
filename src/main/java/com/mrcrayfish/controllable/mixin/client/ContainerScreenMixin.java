package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Author: MrCrayfish
 */
@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin
{
    @Shadow
    private ItemStack shiftClickedSlot;

    @Invoker(value = "handleMouseClick")
    abstract void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type);

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/inventory/ContainerScreen;handleMouseClick(Lnet/minecraft/inventory/container/Slot;IILnet/minecraft/inventory/container/ClickType;)V", ordinal = 1))
    private void onClicked(ContainerScreen screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.shiftClickedSlot = slotIn != null && slotIn.getHasStack() ? slotIn.getStack().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        this.handleMouseClick(slotIn, slotId, mouseButton, type);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/inventory/ContainerScreen;handleMouseClick(Lnet/minecraft/inventory/container/Slot;IILnet/minecraft/inventory/container/ClickType;)V", ordinal = 9))
    private void onReleased(ContainerScreen screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.shiftClickedSlot = slotIn != null && slotIn.getHasStack() ? slotIn.getStack().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        this.handleMouseClick(slotIn, slotId, mouseButton, type);
    }

    /**
     * Checks if a controller is connected and the quick move button is down
     */
    private static boolean canQuickMove()
    {
        Controller controller = Controllable.getController();
        return controller != null && ButtonBindings.QUICK_MOVE.isButtonDown();
    }
}
