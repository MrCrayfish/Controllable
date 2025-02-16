package com.mrcrayfish.controllable.client.input;

import java.util.List;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public final class MultiController extends Controller
{
    private final List<Controller> controllers;

    MultiController(List<Controller> controllers)
    {
        this.controllers = controllers;
    }

    public List<Controller> getControllers()
    {
        return this.controllers;
    }

    @Override
    public boolean open()
    {
        this.controllers.forEach(Controller::open);
        return true;
    }

    @Override
    public void close()
    {
        this.controllers.forEach(Controller::close);
    }

    @Override
    public Number getJid()
    {
        return -1;
    }

    @Override
    public boolean isOpen()
    {
        return true;
    }

    @Override
    public ButtonStates captureButtonStates()
    {
        ButtonStates result = new ButtonStates();
        for(Controller controller : this.controllers)
        {
            ButtonStates captured = controller.captureButtonStates();
            for(int i = 0; i < captured.getSize(); i++)
            {
                if(captured.getState(i))
                {
                    result.setState(i, true);
                }
            }
        }
        return result;
    }

    @Override
    public String getName()
    {
        return "Multi Controller";
    }

    @Override
    public boolean supportsRumble()
    {
        return true;
    }

    @Override
    protected boolean internalRumble(float lowFrequency, float highFrequency, int timeInMs)
    {
        for(Controller controller : this.controllers)
        {
            if(controller.supportsRumble())
            {
                controller.internalRumble(lowFrequency, highFrequency, timeInMs);
            }
        }
        return false;
    }

    private float averageOfInput(Function<Controller, Float> func)
    {
        int count = 0;
        float total = 0;
        for(Controller controller : this.controllers)
        {
            float value = func.apply(controller);
            if(value != 0)
            {
                total += value;
                count++;
            }
        }
        return count > 0 ? total / count : 0;
    }

    @Override
    protected float internalGetLTriggerValue()
    {
        return this.averageOfInput(Controller::internalGetLTriggerValue);
    }

    @Override
    protected float internalGetRTriggerValue()
    {
        return this.averageOfInput(Controller::internalGetRTriggerValue);
    }

    @Override
    protected float internalGetLThumbStickXValue()
    {
        return this.averageOfInput(Controller::internalGetLThumbStickXValue);
    }

    @Override
    protected float internalGetLThumbStickYValue()
    {
        return this.averageOfInput(Controller::internalGetLThumbStickYValue);
    }

    @Override
    protected float internalGetRThumbStickXValue()
    {
        return this.averageOfInput(Controller::internalGetRThumbStickXValue);
    }

    @Override
    protected float internalGetRThumbStickYValue()
    {
        return this.averageOfInput(Controller::internalGetRThumbStickYValue);
    }

    @Override
    public DeviceInfo getInfo()
    {
        return null;
    }
}
