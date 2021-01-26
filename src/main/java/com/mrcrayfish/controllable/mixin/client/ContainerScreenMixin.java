package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin
{
    @Shadow
    private ItemStack shiftClickedSlot;

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/inventory/ContainerScreen;handleMouseClick(Lnet/minecraft/inventory/container/Slot;IILnet/minecraft/inventory/container/ClickType;)V", ordinal = 1))
    private void onClicked(ContainerScreen screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.shiftClickedSlot = slotIn != null && slotIn.getHasStack() ? slotIn.getStack().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        this.handleMouseClick(screen, slotIn, slotId, mouseButton, type);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/inventory/ContainerScreen;handleMouseClick(Lnet/minecraft/inventory/container/Slot;IILnet/minecraft/inventory/container/ClickType;)V", ordinal = 9))
    private void onReleased(ContainerScreen screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.shiftClickedSlot = slotIn != null && slotIn.getHasStack() ? slotIn.getStack().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        this.handleMouseClick(screen, slotIn, slotId, mouseButton, type);
    }

    private Method method;

    /**
     * Manually invokes handleMouseClick from ContainerScreen using Java reflection. Mixins does not
     * support the ability to invoke methods from sub classes when using Invoker annotation. If I am
     * doing something wrong with Invoker, please PR into my repo!
     */
    private void handleMouseClick(ContainerScreen screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        // Cache the method as it only needs to be found once.
        if(this.method == null)
        {
            this.method = ObfuscationReflectionHelper.findMethod(ContainerScreen.class, "func_184098_a", Slot.class, int.class, int.class, ClickType.class);
        }
        try
        {
            this.method.invoke(screen, slotIn, slotId, mouseButton, type);
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a controller is connected and the quick move button is down
     */
    private static boolean canQuickMove()
    {
        Controller controller = Controllable.getController();
        return controller != null && Controllable.isButtonPressed(Buttons.B);
    }
}
