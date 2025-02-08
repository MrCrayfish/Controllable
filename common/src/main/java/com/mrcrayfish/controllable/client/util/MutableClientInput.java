package com.mrcrayfish.controllable.client.util;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;

/**
 * Author: MrCrayfish
 */
public class MutableClientInput
{
    private final ClientInput original;
    private boolean changed;
    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean jump;
    private boolean shift;
    private boolean sprint;
    private float leftImpulse;
    private float forwardImpulse;

    public MutableClientInput(ClientInput input)
    {
        this.original = input;
        this.forward = input.keyPresses.forward();
        this.backward = input.keyPresses.backward();
        this.left = input.keyPresses.left();
        this.right = input.keyPresses.right();
        this.jump = input.keyPresses.jump();
        this.shift = input.keyPresses.shift();
        this.sprint = input.keyPresses.sprint();
        this.leftImpulse = input.leftImpulse;
        this.forwardImpulse = input.forwardImpulse;
    }

    public void apply()
    {
        if(this.changed)
        {
            this.original.keyPresses = new Input(
                this.original.keyPresses.forward() || this.forward,
                this.original.keyPresses.backward() || this.backward,
                this.original.keyPresses.left() || this.left,
                this.original.keyPresses.right() || this.right,
                this.original.keyPresses.jump() || this.jump,
                this.original.keyPresses.shift() || this.shift,
                this.original.keyPresses.sprint() || this.sprint
            );
            this.original.leftImpulse = this.leftImpulse;
            this.original.forwardImpulse = this.forwardImpulse;
        }
    }

    public void setForward(boolean forward)
    {
        this.forward = forward;
        this.changed = true;
    }

    public void setBackward(boolean backward)
    {
        this.backward = backward;
        this.changed = true;
    }

    public void setLeft(boolean left)
    {
        this.left = left;
        this.changed = true;
    }

    public void setRight(boolean right)
    {
        this.right = right;
        this.changed = true;
    }

    public void setJump(boolean jump)
    {
        this.jump = jump;
        this.changed = true;
    }

    public void setShift(boolean shift)
    {
        this.shift = shift;
        this.changed = true;
    }

    public void setSprint(boolean sprint)
    {
        this.sprint = sprint;
        this.changed = true;
    }

    public void setLeftImpulse(float leftImpulse)
    {
        this.leftImpulse = leftImpulse;
        this.changed = true;
    }

    public void setForwardImpulse(float forwardImpulse)
    {
        this.forwardImpulse = forwardImpulse;
        this.changed = true;
    }
}
