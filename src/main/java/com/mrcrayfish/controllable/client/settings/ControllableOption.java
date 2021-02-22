package com.mrcrayfish.controllable.client.settings;

import com.mrcrayfish.controllable.client.IToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public abstract class ControllableOption<T> implements IToolTip
{
    private Supplier<T> getter;
    private Consumer<T> setter;
    private Function<T, String> formatter;
    private TextComponentTranslation toolTip;

    public ControllableOption(String titleKey, Supplier<T> getter, Consumer<T> setter, Function<T, String> formatter)
    {
        this.getter = getter;
        this.setter = setter;
        this.formatter = formatter;
        this.toolTip = new TextComponentTranslation(titleKey + ".desc");
    }

    public abstract GuiButton createOption(int id, int x, int y, int width);

    public Supplier<T> getGetter()
    {
        return getter;
    }

    public Consumer<T> getSetter()
    {
        return setter;
    }

    public Function<T, String> getFormatter()
    {
        return formatter;
    }

    @Override
    public List<String> getToolTip()
    {
        return Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(this.toolTip.getFormattedText(), 200);
    }
}
