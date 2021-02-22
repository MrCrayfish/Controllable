package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
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
@Mixin(GuiContainer.class)
public abstract class ContainerScreenMixin
{
    @Shadow
    private ItemStack shiftClickedSlot;

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;handleMouseClick(Lnet/minecraft/inventory/Slot;IILnet/minecraft/inventory/ClickType;)V", ordinal = 1))
    private void onClicked(GuiContainer guiContainer, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.shiftClickedSlot = slotIn != null && slotIn.getHasStack() ? slotIn.getStack().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        this.handleMouseClick(guiContainer, slotIn, slotId, mouseButton, type);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;handleMouseClick(Lnet/minecraft/inventory/Slot;IILnet/minecraft/inventory/ClickType;)V", ordinal = 9))
    private void onReleased(GuiContainer guiContainer, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.shiftClickedSlot = slotIn != null && slotIn.getHasStack() ? slotIn.getStack().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        this.handleMouseClick(guiContainer, slotIn, slotId, mouseButton, type);
    }

    private Method method;

    /**
     * Manually invokes handleMouseClick from ContainerScreen using Java reflection. Mixins does not
     * support the ability to invoke methods from sub classes when using Invoker annotation. If I am
     * doing something wrong with Invoker, please PR into my repo!
     */
    private void handleMouseClick(GuiContainer screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        // Cache the method as it only needs to be found once.
        if(this.method == null)
        {
            this.method = ObfuscationReflectionHelper.findMethod(GuiContainer.class, "func_184098_a", void.class, Slot.class, int.class, int.class, ClickType.class);
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
