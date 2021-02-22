package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.client.Controller;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Author: MrCrayfish
 */
public abstract class ControllerEvent extends Event
{
    private Controller controller;

    public ControllerEvent(Controller controller)
    {
        this.controller = controller;
    }

    public Controller getController()
    {
        return this.controller;
    }

    @Cancelable
    public static class ButtonInput extends ControllerEvent
    {
        private int originalButton;
        private int button;
        private boolean state;

        public ButtonInput(Controller controller, int button, boolean state)
        {
            super(controller);
            this.originalButton = button;
            this.button = button;
            this.state = state;
        }

        /**
         * Gets the button that was originally pressed.
         *
         * @return the original button
         */
        public int getButton()
        {
            return this.originalButton;
        }

        /**
         * Gets the button to actually press instead. If not changed, will be the same as the
         * original button.
         *
         * @return the original button
         */
        public int getModifiedButton()
        {
            return this.button;
        }

        /**
         * Changes the button that was pressed. Use with caution! If you put a button
         * into a pressed state and forget to release it, it may cause unintended behaviour.
         *
         * @param button the replacement button
         */
        public void setButton(int button)
        {
            this.button = button;
        }

        /**
         * Gets the state of the button. If true, it means it was pressed down otherwise it was
         * released.
         *
         * @return the state of the button
         */
        public boolean getState()
        {
            return this.state;
        }
    }

    @Cancelable
    public static class Button extends ControllerEvent
    {
        public Button(Controller controller)
        {
            super(controller);
        }
    }

    @Cancelable
    public static class Move extends ControllerEvent
    {
        public Move(Controller controller)
        {
            super(controller);
        }
    }

    @Cancelable
    public static class Turn extends ControllerEvent
    {
        private float yawSpeed;
        private float pitchSpeed;

        public Turn(Controller controller, float yawSpeed, float pitchSpeed)
        {
            super(controller);
            this.yawSpeed = yawSpeed;
            this.pitchSpeed = pitchSpeed;
        }

        /**
         * Gets the current yaw speed. This could be modified already from another event
         * subscription.
         *
         * @return the yaw speed
         */
        public float getYawSpeed()
        {
            return this.yawSpeed;
        }

        /**
         * Sets the speed when turning
         *
         * @param yawSpeed a new yaw speed
         */
        public void setYawSpeed(float yawSpeed)
        {
            this.yawSpeed = yawSpeed;
        }

        /**
         * Gets the current pitch speed. This could be modified already from another event
         * subscription.
         *
         * @return the pitch speed
         */
        public float getPitchSpeed()
        {
            return this.pitchSpeed;
        }

        /**
         * Sets the pitch speed when turning
         *
         * @param pitchSpeed a new pitch speed
         */
        public void setPitchSpeed(float pitchSpeed)
        {
            this.pitchSpeed = pitchSpeed;
        }
    }
}
