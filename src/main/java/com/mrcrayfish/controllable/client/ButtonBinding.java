package com.mrcrayfish.controllable.client;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.settings.IKeyConflictContext;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ButtonBinding implements Comparable<ButtonBinding>
{
    private final int defaultButton;
    private int button;
    private String descriptionKey;
    private String category;
    private IKeyConflictContext context;
    private boolean pressed;
    private int pressedTime;
    private boolean reserved;

    public ButtonBinding(int button, String descriptionKey, String category, IKeyConflictContext context)
    {
        this(button, descriptionKey, category, context, false);
    }

    ButtonBinding(int button, String descriptionKey, String category, IKeyConflictContext context, boolean reserved)
    {
        this.defaultButton = button;
        this.button = button;
        this.descriptionKey = descriptionKey;
        this.category = category;
        this.context = context;
        this.reserved = reserved;
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
        return this.pressed && this.pressedTime == 0 && this.isActiveAndValidContext();
    }

    public boolean isNotReserved()
    {
        return !this.reserved;
    }

    public boolean isButtonDown()
    {
        return this.pressed && this.isActiveAndValidContext();
    }

    public void reset()
    {
        this.button = this.defaultButton;
    }

    public static void tick()
    {
        for(ButtonBinding binding : BindingRegistry.getInstance().getRegisteredBindings())
        {
            if(binding.isButtonDown())
            {
                binding.pressedTime--;
            }
        }
    }

    public static void setButtonState(int button, boolean state)
    {
        List<ButtonBinding> bindings = BindingRegistry.getInstance().getBindingListForButton(button);
        for(ButtonBinding binding : bindings)
        {
            binding.pressed = state;
            if(state)
            {
                binding.pressedTime = 0;
            }
        }
    }

    /**
     * Resets all buttons states. Called when a GUI is opened.
     */
    public static void resetButtonStates()
    {
        for(ButtonBinding binding : BindingRegistry.getInstance().getRegisteredBindings())
        {
            binding.pressed = false;
        }
    }

    @Override
    public int compareTo(ButtonBinding o)
    {
        return I18n.format(this.descriptionKey).compareTo(I18n.format(o.descriptionKey));
    }

    public boolean isConflictingContext()
    {
        List<ButtonBinding> bindings = BindingRegistry.getInstance().getBindingListForButton(this.button);

        if(bindings == null)
            return false;

        for(ButtonBinding binding : bindings)
        {
            if(this.conflicts(binding))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the context is active and that this binding does not conflict with any other binding.
     */
    private boolean isActiveAndValidContext()
    {
        return this.context.isActive() && !this.isConflictingContext();
    }

    /**
     * Tests if the given binding conflicts with this binding
     *
     * @param binding the binding to test against
     * @return true if the bindings conflict
     */
    private boolean conflicts(ButtonBinding binding)
    {
        return this != binding && this.button == binding.getButton() && this.context.conflicts(binding.context);
    }

    @Override
    public int hashCode()
    {
        return this.descriptionKey.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj;
    }
}
