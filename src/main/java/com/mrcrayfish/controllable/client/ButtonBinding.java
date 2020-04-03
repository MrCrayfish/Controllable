package com.mrcrayfish.controllable.client;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ButtonBinding
{
    private static final List<ButtonBinding> BINDINGS = new ArrayList<>();

    private int button;

    @Getter
    private final int defaultId;

    private boolean pressed;
    private int pressedTime;

    public ButtonBinding(int button, int defaultId)
    {
        this.button = button;
        this.defaultId = defaultId;
        BINDINGS.add(this);
    }

    public void resetButton() {
        button = defaultId;
    }

    public int getButtonId()
    {
        return button;
    }

    public void setButton(int button)
    {
        this.button = button;
    }

    public boolean isButtonPressed()
    {
        return pressed && pressedTime == 0;
    }

    public boolean isButtonDown()
    {
        return pressed;
    }

    public static void tick()
    {
        for(ButtonBinding binding : BINDINGS)
        {
            if(binding.isButtonDown())
            {
                binding.pressedTime--;
            }
        }
    }

    public static void setButtonState(int button, boolean state)
    {
        for(ButtonBinding binding : BINDINGS)
        {
            if(binding.getButtonId() == button)
            {
                binding.pressed = state;
                binding.pressedTime = 0;
            }
        }
    }

    /**
     * Resets all buttons states. Called when a GUI is opened.
     */
    public static void resetButtonStates()
    {
        for(ButtonBinding binding : BINDINGS)
        {
            binding.pressed = false;
        }
    }

    public boolean isDefault() {
        return button == defaultId;
    }

    public boolean isInvalid() {
        return button < 0 || button > Buttons.LENGTH;
    }

    public boolean conflicts(ButtonBinding buttonBinding) {
        return button == buttonBinding.getButtonId();
    }
}
