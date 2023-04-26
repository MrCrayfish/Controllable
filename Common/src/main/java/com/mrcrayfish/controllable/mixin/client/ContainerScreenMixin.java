package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Author: MrCrayfish
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin
{
    @Shadow
    private ItemStack lastQuickMoved;

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", ordinal = 1))
    private void controllableOnClicked(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        ClientServices.CLIENT.clickSlot(screen, slot, slotId, button, type);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", ordinal = 9))
    private void controllableOnReleased(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        ClientServices.CLIENT.clickSlot(screen, slot, slotId, button, type);
    }

    /**
     * Checks if a controller is connected and the quick move button is down
     */
    private static boolean canQuickMove()
    {
        Controller controller = Controllable.getController();
        return controller != null && ButtonBindings.QUICK_MOVE.isButtonPressed();
    }
}
