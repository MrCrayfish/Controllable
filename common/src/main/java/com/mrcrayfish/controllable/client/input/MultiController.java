package com.mrcrayfish.controllable.client.input;

import net.minecraft.util.Mth;

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

    private float findBiggestDelta(Function<Controller, Float> func)
    {
        float delta = 0;
        float result = 0;
        for(Controller controller : this.controllers)
        {
            float value = func.apply(controller);
            if(Mth.abs(value) > delta)
            {
                result = value;
                delta = Mth.abs(value);
            }
        }
        return result;
    }

    @Override
    protected float internalGetLTriggerValue()
    {
        return this.findBiggestDelta(Controller::internalGetLTriggerValue);
    }

    @Override
    protected float internalGetRTriggerValue()
    {
        return this.findBiggestDelta(Controller::internalGetRTriggerValue);
    }

    @Override
    protected float internalGetLThumbStickXValue()
    {
        return this.findBiggestDelta(Controller::internalGetLThumbStickXValue);
    }

    @Override
    protected float internalGetLThumbStickYValue()
    {
        return this.findBiggestDelta(Controller::internalGetLThumbStickYValue);
    }

    @Override
    protected float internalGetRThumbStickXValue()
    {
        return this.findBiggestDelta(Controller::internalGetRThumbStickXValue);
    }

    @Override
    protected float internalGetRThumbStickYValue()
    {
        return this.findBiggestDelta(Controller::internalGetRThumbStickYValue);
    }

    @Override
    public DeviceInfo getInfo()
    {
        return null;
    }
}
