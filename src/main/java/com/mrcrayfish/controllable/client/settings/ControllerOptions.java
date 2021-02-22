package com.mrcrayfish.controllable.client.settings;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ControllerType;
import com.mrcrayfish.controllable.client.CursorType;
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

    public static final ControllableOptionBoolean FORCE_FEEDBACK = new ControllableOptionBoolean("controllable.options.forceFeedback", () -> {
        return Controllable.getOptions().forceFeedback;
    }, value -> {
        Controllable.getOptions().forceFeedback = value;
    }, value -> {
        return I18n.format("controllable.options.forceFeedback") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
    });

    public static final ControllableOptionBoolean AUTO_SELECT = new ControllableOptionBoolean("controllable.options.autoSelect", () -> {
        return Controllable.getOptions().autoSelect;
    }, value -> {
        Controllable.getOptions().autoSelect = value;
    }, value -> {
        return I18n.format("controllable.options.autoSelect") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
    });

    public static final ControllableOptionBoolean RENDER_MINI_PLAYER  = new ControllableOptionBoolean("controllable.options.renderMiniPlayer", () -> {
        return Controllable.getOptions().renderMiniPlayer;
    }, value -> {
        Controllable.getOptions().renderMiniPlayer = value;
    }, value -> {
        return I18n.format("controllable.options.renderMiniPlayer") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
    });

    public static final ControllableOptionBoolean VIRTUAL_MOUSE  = new ControllableOptionBoolean("controllable.options.virtualMouse", () -> {
        return Controllable.getOptions().virtualMouse;
    }, value -> {
        Controllable.getOptions().virtualMouse = value;
    }, value -> {
        return I18n.format("controllable.options.virtualMouse") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
    });

    public static final ControllableOptionBoolean CONSOLE_HOTBAR = new ControllableOptionBoolean("controllable.options.consoleHotbar", () -> {
        return Controllable.getOptions().consoleHotbar;
    }, value -> {
        Controllable.getOptions().consoleHotbar = value;
    }, value -> {
        return I18n.format("controllable.options.consoleHotbar") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
    });

    public static final ControllableOptionEnum<CursorType> CURSOR_TYPE = new ControllableOptionEnum<>("controllable.options.cursorType", () -> {
        return Controllable.getOptions().cursorType;
    }, value -> {
        Controllable.getOptions().cursorType = value;
    }, value -> {
        return I18n.format("controllable.options.cursorType") + ": " + I18n.format("controllable.cursor." + value.getName());
    });

    public static final ControllableOptionEnum<ControllerType> CONTROLLER_TYPE = new ControllableOptionEnum<>("controllable.options.controllerType", () -> {
        return Controllable.getOptions().controllerType;
    }, value -> {
        Controllable.getOptions().controllerType = value;
    }, value -> {
        return I18n.format("controllable.options.controllerType") + ": " + I18n.format("controllable.controller." + value.getName());
    });

    public static final ControllableOptionBoolean INVERT_LOOK = new ControllableOptionBoolean("controllable.options.invertLook", () -> {
        return Controllable.getOptions().invertLook;
    }, value -> {
        Controllable.getOptions().invertLook = value;
    }, value -> {
        return I18n.format("controllable.options.invertLook") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
    });

    public static final ControllableOptionSlider DEAD_ZONE = new ControllableOptionSlider("controllable.options.deadZone", 0.01F, 0.0F, 1.0F, () -> {
       return Controllable.getOptions().deadZone;
    }, value -> {
        Controllable.getOptions().deadZone = MathHelper.clamp(value, 0.0, 1.0);
    }, value -> {
        double deadZone = Controllable.getOptions().deadZone;
        return I18n.format("controllable.options.deadZone.format", FORMAT.format(deadZone));
    });

    public static final ControllableOptionSlider ROTATION_SPEED = new ControllableOptionSlider("controllable.options.rotationSpeed", 1.0F, 1.0F, 50.0F, () -> {
        return Controllable.getOptions().rotationSpeed;
    }, value -> {
        Controllable.getOptions().rotationSpeed = MathHelper.clamp(value, 1.0, 50.0);
    }, value -> {
        double rotationSpeed = Controllable.getOptions().rotationSpeed;
        return I18n.format("controllable.options.rotationSpeed.format", FORMAT.format(rotationSpeed));
    });

    public static final ControllableOptionSlider MOUSE_SPEED = new ControllableOptionSlider("controllable.options.mouseSpeed", 1.0F, 1.0F, 50.0F, () -> {
        return Controllable.getOptions().mouseSpeed;
    }, value -> {
        Controllable.getOptions().mouseSpeed = MathHelper.clamp(value, 1.0, 50.0);
    }, value -> {
        double mouseSpeed = Controllable.getOptions().mouseSpeed;
        return I18n.format("controllable.options.mouseSpeed.format", FORMAT.format(mouseSpeed));
    });

    public static final ControllableOptionEnum<ActionVisibility> SHOW_ACTIONS = new ControllableOptionEnum<>("controllable.options.showActions", () -> {
        return Controllable.getOptions().actionVisibility;
    }, value -> {
        Controllable.getOptions().actionVisibility = value;
    }, value -> {
        return I18n.format("controllable.options.showActions.format", I18n.format("controllable.actionVisibility." + value.getName()));
    });

    public static final ControllableOptionBoolean QUICK_CRAFT = new ControllableOptionBoolean("controllable.options.quickCraft", () -> {
        return Controllable.getOptions().quickCraft;
    }, value -> {
        Controllable.getOptions().quickCraft = value;
    }, value -> {
        return I18n.format("controllable.options.quickCraft") + ": " + (value ? I18n.format("options.on") : I18n.format("options.off"));
    });

    public static final Splitter COLON_SPLITTER = Splitter.on(':');

    private Minecraft minecraft;
    private File optionsFile;
    private boolean forceFeedback = true;
    private boolean autoSelect = true;
    private boolean renderMiniPlayer = true;
    private boolean virtualMouse = true;
    private boolean consoleHotbar = false;
    private CursorType cursorType = CursorType.LIGHT;
    private ControllerType controllerType = ControllerType.DEFAULT;
    private boolean invertLook = false;
    private double deadZone = 0.15;
    private double rotationSpeed = 25.0;
    private double mouseSpeed = 30.0;
    private ActionVisibility actionVisibility = ActionVisibility.MINIMAL;
    private boolean quickCraft = true;

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
                    switch(key)
                    {
                        case "forceFeedback":
                            this.forceFeedback = Boolean.valueOf(value);
                            break;
                        case "autoSelect":
                            this.autoSelect = Boolean.valueOf(value);
                            break;
                        case "cursorType":
                            this.cursorType = CursorType.byId(value);
                            break;
                        case "controllerType":
                            this.controllerType = ControllerType.byName(value);
                            break;
                        case "invertLook":
                            this.invertLook = Boolean.valueOf(value);
                            break;
                        case "deadZone":
                            this.deadZone = Double.parseDouble(value);
                            break;
                        case "virtualMouse":
                            this.virtualMouse = Boolean.valueOf(value);
                            break;
                        case "consoleHotbar":
                            this.consoleHotbar = Boolean.valueOf(value);
                            break;
                        case "rotationSpeed":
                            this.rotationSpeed = Double.parseDouble(value);
                            break;
                        case "mouseSpeed":
                            this.mouseSpeed = Double.parseDouble(value);
                            break;
                        case "renderMiniPlayer":
                            this.renderMiniPlayer = Boolean.valueOf(value);
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
            writer.println("forceFeedback:" + this.forceFeedback);
            writer.println("autoSelect:" + this.autoSelect);
            writer.println("renderMiniPlayer:" + this.renderMiniPlayer);
            writer.println("virtualMouse:" + this.virtualMouse);
            writer.println("consoleHotbar:" + this.consoleHotbar);
            writer.println("cursorType:" + this.cursorType.getName());
            writer.println("controllerType:" + this.controllerType.getName());
            writer.println("invertLook:" + this.invertLook);
            writer.println("deadZone:" + FORMAT.format(this.deadZone));
            writer.println("rotationSpeed:" + FORMAT.format(this.rotationSpeed));
            writer.println("mouseSpeed:" + FORMAT.format(this.mouseSpeed));
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public boolean useForceFeedback()
    {
        return this.forceFeedback;
    }

    public boolean isAutoSelect()
    {
        return autoSelect;
    }

    public boolean isRenderMiniPlayer()
    {
        return renderMiniPlayer;
    }

    public boolean isVirtualMouse()
    {
        return virtualMouse;
    }

    public boolean useConsoleHotbar()
    {
        return consoleHotbar;
    }

    public CursorType getCursorType()
    {
        return cursorType;
    }

    public ControllerType getControllerType()
    {
        return controllerType;
    }

    public boolean isInvertLook()
    {
        return invertLook;
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

    public ActionVisibility getActionVisibility()
    {
        return actionVisibility;
    }

    public boolean isQuickCraft()
    {
        return quickCraft;
    }


}