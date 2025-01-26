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
    public boolean rumble(float lowFrequency, float highFrequency, int timeInMs)
    {
        for(Controller controller : this.controllers)
        {
            if(controller.supportsRumble())
            {
                controller.rumble(lowFrequency, highFrequency, timeInMs);
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
    public float getLTriggerValue()
    {
        return this.averageOfInput(Controller::getLTriggerValue);
    }

    @Override
    public float getRTriggerValue()
    {
        return this.averageOfInput(Controller::getRTriggerValue);
    }

    @Override
    public float getLThumbStickXValue()
    {
        return this.averageOfInput(Controller::getLThumbStickXValue);
    }

    @Override
    public float getLThumbStickYValue()
    {
        return this.averageOfInput(Controller::getLThumbStickYValue);
    }

    @Override
    public float getRThumbStickXValue()
    {
        return this.averageOfInput(Controller::getRThumbStickXValue);
    }

    @Override
    public float getRThumbStickYValue()
    {
        return this.averageOfInput(Controller::getRThumbStickYValue);
    }

    @Override
    public DeviceInfo getInfo()
    {
        return null;
    }
}
