package com.mrcrayfish.controllable.client.binding;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.mrcrayfish.controllable.client.binding.handlers.ButtonHandler;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.ApiStatus;

/**
 * Author: MrCrayfish
 */
public class ButtonBinding implements Comparable<ButtonBinding>
{
    private final int defaultButton;
    private final String descriptionKey;
    private final String category;
    private final BindingContext context;
    private final boolean reserved;
    private final ButtonHandler handler;
    private int button;
    private boolean pressed;

    public ButtonBinding(int button, String descriptionKey, String category, BindingContext context, ButtonHandler handler)
    {
        this(button, descriptionKey, category, context, false, handler);
    }

    ButtonBinding(int button, String descriptionKey, String category, BindingContext context, boolean reserved, ButtonHandler handler)
    {
        this.button = button;
        this.defaultButton = button;
        this.descriptionKey = descriptionKey;
        this.category = category;
        this.context = context;
        this.reserved = reserved;
        this.handler = handler;
    }

    public int getButton()
    {
        return this.button;
    }

    public String getLabelKey()
    {
        return this.descriptionKey;
    }

    public String getDescription()
    {
        return this.descriptionKey;
    }

    public String getCategory()
    {
        return this.category;
    }

    public BindingContext getContext()
    {
        return this.context;
    }

    public boolean isDefault()
    {
        return this.button == this.defaultButton;
    }

    protected void setPressed(boolean pressed)
    {
        this.pressed = pressed;
    }

    public boolean isNotReserved()
    {
        return !this.reserved;
    }

    public ButtonHandler getHandler()
    {
        return this.handler;
    }

    public boolean isButtonDown()
    {
        return this.pressed;
    }

    public void resetPressedState()
    {
        this.pressed = false;
    }

    public boolean isUnbound()
    {
        return this.button == -1;
    }

    @ApiStatus.Internal
    public void resetMappedButton()
    {
        this.button = this.defaultButton;
    }

    @ApiStatus.Internal
    public static void setButton(ButtonBinding binding, int button)
    {
        binding.button = button;
    }

    @ApiStatus.Internal
    public static void setButtonState(ButtonBinding binding, boolean state)
    {
        binding.setPressed(state);
    }

    /**
     * Resets all buttons states. Called when a GUI is opened.
     */
    @ApiStatus.Internal
    public static void resetButtonStates()
    {
        for(ButtonBinding binding : Controllable.getBindingRegistry().getRegisteredBindings())
        {
            binding.resetPressedState();
        }
    }

    @Override
    public int compareTo(ButtonBinding o)
    {
        return I18n.get(this.descriptionKey).compareTo(I18n.get(o.descriptionKey));
    }

    public boolean isConflictingContext()
    {
        for(ButtonBinding binding : Controllable.getBindingRegistry().getBindingsForButton(this.button))
        {
            if(this.conflicts(binding))
            {
                return true;
            }
        }
        return false;
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
