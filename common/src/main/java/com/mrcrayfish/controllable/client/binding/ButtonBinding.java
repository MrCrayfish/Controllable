package com.mrcrayfish.controllable.client.binding;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.mrcrayfish.controllable.client.binding.handlers.ButtonHandler;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Author: MrCrayfish
 */
public class ButtonBinding implements Comparable<ButtonBinding>
{
    private final int defaultButton;
    private final Set<Integer> defaultButtons;
    private final String descriptionKey;
    private final String category;
    private final BindingContext context;
    private final boolean reserved;
    private final ButtonHandler handler;
    private int button;
    private Set<Integer> buttons;
    private boolean pressed;

    private static Set<Integer> createButtonSet(int button)
    {
        return button >= 0 ? new TreeSet<>(Collections.singleton(button)) : new TreeSet<>();
    }

    public ButtonBinding(int button, String descriptionKey, String category, BindingContext context, ButtonHandler handler)
    {
        this(button, descriptionKey, category, context, false, handler);
    }

    ButtonBinding(int button, String descriptionKey, String category, BindingContext context, boolean reserved, ButtonHandler handler)
    {
        this.button = button;
        this.defaultButton = button;
        this.buttons = createButtonSet(button);
        this.defaultButtons = new TreeSet<>(this.buttons);
        this.descriptionKey = descriptionKey;
        this.category = category;
        this.context = context;
        this.reserved = reserved;
        this.handler = handler;
    }

    public ButtonBinding(Set<Integer> buttons, String descriptionKey, String category, BindingContext context, ButtonHandler handler)
    {
        this(buttons, descriptionKey, category, context, false, handler);
    }

    ButtonBinding(Set<Integer> buttons, String descriptionKey, String category, BindingContext context, boolean reserved, ButtonHandler handler)
    {
        this.buttons = new TreeSet<>(buttons);
        this.defaultButtons = new TreeSet<>(this.buttons);
        this.button = this.buttons.isEmpty() ? -1 : this.buttons.iterator().next();
        this.defaultButton = this.button;
        this.descriptionKey = descriptionKey;
        this.category = category;
        this.context = context;
        this.reserved = reserved;
        this.handler = handler;
    }

    public int getButton() { return this.button; }

    public Set<Integer> getButtons() { return Collections.unmodifiableSet(this.buttons); }

    public boolean isMultiButton() { return this.buttons.size() > 1; }

    public int getButtonCount() { return this.buttons.size(); }

    public String getLabelKey() { return this.descriptionKey; }

    public String getDescription() { return this.descriptionKey; }

    public String getCategory() { return this.category; }

    public BindingContext getContext() { return this.context; }

    public boolean isDefault() { return this.buttons.equals(this.defaultButtons); }

    protected void setPressed(boolean pressed) { this.pressed = pressed; }

    public boolean isNotReserved() { return !this.reserved; }

    public ButtonHandler getHandler() { return this.handler; }

    public boolean isButtonDown() { return this.pressed; }

    public void resetPressedState() { this.pressed = false; }

    public boolean isUnbound() { return this.button == -1; }

    @ApiStatus.Internal
    public void resetMappedButton()
    {
        this.button = this.defaultButton;
        this.buttons = new TreeSet<>(this.defaultButtons);
    }

    @ApiStatus.Internal
    public static void setButton(ButtonBinding binding, int button)
    {
        binding.button = button;
        binding.buttons = createButtonSet(button);
    }

    @ApiStatus.Internal
    public static void setButtons(ButtonBinding binding, Set<Integer> buttons)
    {
        binding.buttons = new TreeSet<>(buttons);
        binding.button = binding.buttons.isEmpty() ? -1 : binding.buttons.iterator().next();
    }

    @ApiStatus.Internal
    public static void setButtonState(ButtonBinding binding, boolean state)
    {
        binding.setPressed(state);
    }

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
        for(int btn : this.buttons)
        {
            for(ButtonBinding binding : Controllable.getBindingRegistry().getBindingsForButton(btn))
            {
                if(this.conflicts(binding))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean conflicts(ButtonBinding binding)
    {
        if(this == binding)
            return false;

        for(int btn : this.buttons)
        {
            if(binding.buttons.contains(btn) && this.context.conflicts(binding.context))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() { return this.descriptionKey.hashCode(); }

    @Override
    public boolean equals(Object obj) { return this == obj; }
}