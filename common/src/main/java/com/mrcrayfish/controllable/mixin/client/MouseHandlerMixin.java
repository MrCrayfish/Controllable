package com.mrcrayfish.controllable.mixin.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.VirtualCursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
    @Inject(method = "onMove", at = @At(value = "HEAD"))
    private void controllableOnMouseMoved(long windowId, double mouseX, double mouseY, CallbackInfo ci)
    {
        if(windowId == Minecraft.getInstance().getWindow().getWindow())
        {
            Controllable.getCursor().setMode(VirtualCursor.CursorMode.MOUSE);
        }
    }
}
