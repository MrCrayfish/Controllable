package com.mrcrayfish.controllable.client.settings;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.nbt.CompoundNBT;
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

    public static final BooleanOption AUTO_SELECT = new ControllableBooleanOption("controllable.options.autoSelect", gameSettings -> {
        return Controllable.getOptions().autoSelect;
    }, (gameSettings, value) -> {
        Controllable.getOptions().autoSelect = value;
    });

    public static final BooleanOption RENDER_MINI_PLAYER = new ControllableBooleanOption("controllable.options.renderMiniPlayer", gameSettings -> {
        return Controllable.getOptions().renderMiniPlayer;
    }, (gameSettings, value) -> {
        Controllable.getOptions().renderMiniPlayer = value;
    });

    public static final BooleanOption VIRTUAL_MOUSE = new ControllableBooleanOption("controllable.options.virtualMouse", gameSettings -> {
        return Controllable.getOptions().virtualMouse;
    }, (gameSettings, value) -> {
        Controllable.getOptions().virtualMouse = value;
    });

    public static final SliderPercentageOption DEAD_ZONE = new ControllableSliderPercentageOption("controllable.options.deadZone", 0.0, 1.0, 0.01F, gameSettings -> {
        return Controllable.getOptions().deadZone;
    }, (gameSettings, value) -> {
        Controllable.getOptions().deadZone = MathHelper.clamp(value, 0.0, 1.0);
    }, (gameSettings, option) -> {
        double deadZone = Controllable.getOptions().deadZone;
        return I18n.format("controllable.options.deadZone.format", FORMAT.format(deadZone));
    });

    public static final SliderPercentageOption ROTATION_SPEED = new ControllableSliderPercentageOption("controllable.options.rotationSpeed", 1.0, 50.0, 1.0F, gameSettings -> {
        return Controllable.getOptions().rotationSpeed;
    }, (gameSettings, value) -> {
        Controllable.getOptions().rotationSpeed = MathHelper.clamp(value, 1.0, 50.0);
    }, (gameSettings, option) -> {
        double rotationSpeed = Controllable.getOptions().rotationSpeed;
        return I18n.format("controllable.options.rotationSpeed.format", FORMAT.format(rotationSpeed));
    });

    public static final SliderPercentageOption MOUSE_SPEED = new ControllableSliderPercentageOption("controllable.options.mouseSpeed", 1.0, 50.0, 1.0F, gameSettings -> {
        return Controllable.getOptions().mouseSpeed;
    }, (gameSettings, value) -> {
        Controllable.getOptions().mouseSpeed = MathHelper.clamp(value, 1.0, 50.0);
    }, (gameSettings, option) -> {
        double mouseSpeed = Controllable.getOptions().mouseSpeed;
        return I18n.format("controllable.options.mouseSpeed.format", FORMAT.format(mouseSpeed));
    });

    public static final Splitter COLON_SPLITTER = Splitter.on(':');

    private File optionsFile;
    private boolean autoSelect = true;
    private boolean renderMiniPlayer = true;
    private boolean virtualMouse = true;
    private double deadZone = 0.1;
    private double rotationSpeed = 20.0;
    private double mouseSpeed = 30.0;

    public ControllerOptions(File dataDir)
    {
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
            CompoundNBT compound = new CompoundNBT();

            for(String line : lines)
            {
                try
                {
                    Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(line).iterator();
                    compound.putString(iterator.next(), iterator.next());
                }
                catch(Exception var10)
                {
                    Controllable.LOGGER.warn("Skipping bad option: {}", line);
                }
            }

            for(String key : compound.keySet())
            {
                String value = compound.getString(key);

                try
                {
                    switch(key)
                    {
                        case "autoSelect":
                            this.autoSelect = Boolean.valueOf(value);
                            break;
                        case "renderMiniPlayer":
                            this.renderMiniPlayer = Boolean.valueOf(value);
                            break;
                        case "virtualMouse":
                            this.virtualMouse = Boolean.valueOf(value);
                            break;
                        case "deadZone":
                            this.deadZone = Double.parseDouble(value);
                            break;
                        case "rotationSpeed":
                            this.rotationSpeed = Double.parseDouble(value);
                            break;
                        case "mouseSpeed":
                            this.mouseSpeed = Double.parseDouble(value);
                            break;
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
            writer.println("renderMiniPlayer:" + this.renderMiniPlayer);
            writer.println("virtualMouse:" + this.virtualMouse);
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
        return this.autoSelect;
    }

    public boolean isRenderMiniPlayer()
    {
        return renderMiniPlayer;
    }

    public boolean isVirtualMouse()
    {
        return virtualMouse;
    }

    public double getDeadZone()
    {
        return this.deadZone;
    }

    public double getRotationSpeed()
    {
        return this.rotationSpeed;
    }

    public double getMouseSpeed()
    {
        return this.mouseSpeed;
    }

}
