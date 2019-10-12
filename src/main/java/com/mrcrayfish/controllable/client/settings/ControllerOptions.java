package com.mrcrayfish.controllable.client.settings;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.option.OptionBoolean;
import com.mrcrayfish.controllable.client.gui.option.OptionSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ControllerOptions
{
    private static final DecimalFormat FORMAT = new DecimalFormat("0.0#");

    public static final OptionBoolean AUTO_SELECT = new OptionBoolean(() -> {
        return Controllable.getOptions().autoSelect;
    }, value -> {
        Controllable.getOptions().autoSelect = value;
    }, value -> {
        return I18n.format("controllable.options.autoSelect") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
    });

    public static final OptionSlider DEAD_ZONE = new OptionSlider(0.01F, 0.0F, 1.0F, () -> {
       return Controllable.getOptions().deadZone;
    }, value -> {
        Controllable.getOptions().deadZone = MathHelper.clamp(value, 0.0, 1.0);
    }, value -> {
        double deadZone = Controllable.getOptions().deadZone;
        return I18n.format("controllable.options.deadZone.format", FORMAT.format(deadZone));
    });

    public static final OptionSlider ROTATION_SPEED = new OptionSlider(1.0F, 1.0F, 50.0F, () -> {
        return Controllable.getOptions().rotationSpeed;
    }, value -> {
        Controllable.getOptions().rotationSpeed = MathHelper.clamp(value, 1.0, 50.0);
    }, value -> {
        double rotationSpeed = Controllable.getOptions().rotationSpeed;
        return I18n.format("controllable.options.rotationSpeed.format", FORMAT.format(rotationSpeed));
    });

    public static final OptionSlider MOUSE_SPEED = new OptionSlider(1.0F, 1.0F, 50.0F, () -> {
        return Controllable.getOptions().mouseSpeed;
    }, value -> {
        Controllable.getOptions().mouseSpeed = MathHelper.clamp(value, 1.0, 50.0);
    }, value -> {
        double mouseSpeed = Controllable.getOptions().mouseSpeed;
        return I18n.format("controllable.options.mouseSpeed.format", FORMAT.format(mouseSpeed));
    });

    public static final Splitter COLON_SPLITTER = Splitter.on(':');

    private Minecraft minecraft;
    private File optionsFile;
    private boolean autoSelect = true;
    private double deadZone = 0.1;
    private double rotationSpeed = 20.0;
    private double mouseSpeed = 30.0;

    public ControllerOptions(Minecraft minecraft, File dataDir)
    {
        this.minecraft = minecraft;
        this.optionsFile = new File(dataDir, "controllable-options.txt");
        this.loadOptions();
    }

    private void loadOptions()
    {
        try
        {
            if(!this.optionsFile.exists())
            {
                return;
            }

            List<String> lines = IOUtils.readLines(new FileInputStream(this.optionsFile), Charsets.UTF_8);
            NBTTagCompound compound = new NBTTagCompound();

            for(String line : lines)
            {
                try
                {
                    Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(line).iterator();
                    compound.setString(iterator.next(), iterator.next());
                }
                catch(Exception var10)
                {
                    Controllable.LOGGER.warn("Skipping bad option: {}", line);
                }
            }

            for(String key : compound.getKeySet())
            {
                String value = compound.getString(key);

                try
                {
                    if("autoSelect".equals(key))
                    {
                        this.autoSelect = Boolean.valueOf(value);
                    }
                    else if("deadZone".equals(key))
                    {
                        this.deadZone = Double.parseDouble(value);
                    }
                    else if("rotationSpeed".equals(key))
                    {
                        this.rotationSpeed = Double.parseDouble(value);
                    }
                    else if("mouseSpeed".equals(key))
                    {
                        this.mouseSpeed = Double.parseDouble(value);
                    }
                }
                catch(Exception e)
                {
                    Controllable.LOGGER.warn("Skipping bad option: {}:{}", key, value);
                }
            }
        }
        catch(Exception e)
        {
            Controllable.LOGGER.error("Failed to load options", e);
        }

    }

    public void saveOptions()
    {
        try(PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8)))
        {
            writer.println("autoSelect:" + this.autoSelect);
            writer.println("deadZone:" + FORMAT.format(this.deadZone));
            writer.println("rotationSpeed:" + FORMAT.format(this.rotationSpeed));
            writer.println("mouseSpeed:" + FORMAT.format(this.mouseSpeed));
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isAutoSelect()
    {
        return autoSelect;
    }

    public double getDeadZone()
    {
        return deadZone;
    }

    public double getRotationSpeed()
    {
        return rotationSpeed;
    }

    public double getMouseSpeed()
    {
        return mouseSpeed;
    }
}