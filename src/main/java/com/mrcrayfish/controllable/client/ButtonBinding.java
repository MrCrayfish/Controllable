package com.mrcrayfish.controllable.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ButtonBinding
{
    private static final List<ButtonBinding> BINDINGS = new ArrayList<>();

    private int button;
    private String descriptionKey;
    private String category;
    private boolean ignoreConflict;
    private boolean pressed;
    private int pressedTime;

    public ButtonBinding(int button, String descriptionKey)
    {
        this(button, descriptionKey, false);
    }

    public ButtonBinding(int button, String descriptionKey, boolean ignoreConflict)
    {
        this.button = button;
        this.descriptionKey = descriptionKey;
        this.ignoreConflict = ignoreConflict;
        BINDINGS.add(this);
    }

    public int getButton()
    {
        return this.button;
    }

    public void setButton(int button)
    {
        this.button = button;
    }

    public String getDescription()
    {
        return this.descriptionKey;
    }

    public boolean isButtonPressed()
    {
        return this.pressed && this.pressedTime == 0;
    }

    public boolean isButtonDown()
    {
        return this.pressed;
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
            if(binding.getButton() == button)
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
}
