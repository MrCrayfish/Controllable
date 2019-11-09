package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ControllableMouseHelper extends MouseHelper {

    private final MouseHelper oldHelper;

    public ControllableMouseHelper(Minecraft mc) {
        super(mc);
        oldHelper = mc.mouseHelper;
    }

    @Override
    public void registerCallbacks(long handle) {
        oldHelper.registerCallbacks(handle);
    }

    @Override
    public void updatePlayerLook() {
        super.updatePlayerLook();
    }

    @Override
    public boolean isLeftDown() {
        return super.isLeftDown();
    }

    @Override
    public boolean isRightDown() {
        return super.isRightDown();
    }

    @Override
    public boolean isMiddleDown() {
        return super.isMiddleDown();
    }

    @Override
    public double getMouseX() {
        return super.getMouseX();
    }

    @Override
    public double getMouseY() {
        return super.getMouseY();
    }

    @Override
    public double getXVelocity() {
        return super.getXVelocity();
    }

    @Override
    public double getYVelocity() {
        return super.getYVelocity();
    }

    @Override
    public void setIgnoreFirstMove() {
        super.setIgnoreFirstMove();
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
            ObfuscationReflectionHelper.setPrivateValue(MouseHelper.class, oldHelper, true, "mouseGrabbed");

    }

    @Override
    public void ungrabMouse() {
        if (!Controllable.getOptions().isVirtualMouse())
            super.grabMouse();
        else
            ObfuscationReflectionHelper.setPrivateValue(MouseHelper.class, oldHelper, false, "mouseGrabbed");
    }
}
