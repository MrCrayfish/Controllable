package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: MrCrayfish
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin
{
    @Shadow
    private ItemStack lastQuickMoved;

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", ordinal = 1))
    private void onClicked(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        this.handleSlotClick(screen, slot, slotId, button, type);
    }

    @Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", ordinal = 9))
    private void onReleased(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button, ClickType type)
    {
        if(slotId != -999 && canQuickMove())
        {
            this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
            type = ClickType.QUICK_MOVE;
        }
        this.handleSlotClick(screen, slot, slotId, button, type);
    }

    private Method method;

    /**
     * Manually invokes handleMouseClick from ContainerScreen using Java reflection. Mixins does not
     * support the ability to invoke methods from sub classes when using Invoker annotation. If I am
     * doing something wrong with Invoker, please PR into my repo!
     */
    private void handleSlotClick(AbstractContainerScreen<?> screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        // Cache the method as it only needs to be found once.
        if(this.method == null)
        {
            this.method = ObfuscationReflectionHelper.findMethod(AbstractContainerScreen.class, "m_6597_", Slot.class, int.class, int.class, ClickType.class);
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
