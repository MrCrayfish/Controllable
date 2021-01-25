package com.mrcrayfish.controllable.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ButtonBinding implements Comparable<ButtonBinding>
{
    static final List<ButtonBinding> BINDINGS = new ArrayList<>();

    private final int defaultButton;
    private int button;
    private String descriptionKey;
    private String category;
    private boolean pressed;
    private int pressedTime;

    ButtonBinding(int button, String descriptionKey, String category)
    {
        this.defaultButton = button;
        this.button = button;
        this.descriptionKey = descriptionKey;
        this.category = category;
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

    public String getCategory()
    {
        return this.category;
    }

    public boolean isDefault()
    {
        return this.button == this.defaultButton;
    }

    public boolean isButtonPressed()
    {
        return this.pressed && this.pressedTime == 0;
    }

    public boolean isButtonDown()
    {
        return this.pressed;
    }

    public void reset()
    {
        this.button = this.defaultButton;
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

    @Override
    public int compareTo(ButtonBinding o)
    {
        return I18n.format(this.descriptionKey).compareTo(I18n.format(o.descriptionKey));
    }
}
