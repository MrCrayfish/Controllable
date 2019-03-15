package com.mrcrayfish.controllable.event;

import com.mrcrayfish.controllable.Controller;
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
        return controller;
    }

    @Cancelable
    public static class ButtonInput extends ControllerEvent
    {
        private int button;
        private boolean state;

        public ButtonInput(Controller controller, int button, boolean state)
        {
            super(controller);
            this.button = button;
            this.state = state;
        }

        public int getButton()
        {
            return button;
        }

        public boolean getState()
        {
            return state;
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
    public static class ControllerTurnEvent extends ControllerEvent
    {
        public ControllerTurnEvent(Controller controller)
        {
            super(controller);
        }
    }
}
