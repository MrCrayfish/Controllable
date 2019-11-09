package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ControllableMouseHelper extends MouseHelper {

    public ControllableMouseHelper(Minecraft mc) {
        super(mc);
        registerCallbacks(mc.mainWindow.getHandle());
    }
    @Override
    public boolean isMouseGrabbed() {
        return super.isMouseGrabbed();
    }

    @Override
    public void grabMouse() {
        if (!Controllable.getOptions().isVirtualMouse())
            super.grabMouse();
        else
            ObfuscationReflectionHelper.setPrivateValue(MouseHelper.class, this, true, "mouseGrabbed");

    }

    @Override
    public void ungrabMouse() {
        if (!Controllable.getOptions().isVirtualMouse())
            super.ungrabMouse();
        else
            ObfuscationReflectionHelper.setPrivateValue(MouseHelper.class, this, false, "mouseGrabbed");
    }
}
