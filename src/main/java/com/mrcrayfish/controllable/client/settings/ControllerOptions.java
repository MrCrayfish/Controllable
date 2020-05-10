package com.mrcrayfish.controllable.client.settings;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ControllerType;
import com.mrcrayfish.controllable.client.CursorType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.AbstractOption;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IStringSerializable;
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

    public static final BooleanOption FORCE_FEEDBACK = new ControllableBooleanOption("controllable.options.forceFeedback", gameSettings -> {
        return Controllable.getOptions().forceFeedback;
    }, (gameSettings, value) -> {
        Controllable.getOptions().forceFeedback = value;
    });

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

    public static final BooleanOption CONSOLE_HOTBAR = new ControllableBooleanOption("controllable.options.consoleHotbar", gameSettings -> {
        return Controllable.getOptions().consoleHotbar;
    }, (gameSettings, value) -> {
        Controllable.getOptions().consoleHotbar = value;
    });

    public static final ControllableEnumOption<CursorType> CURSOR_TYPE = new ControllableEnumOption<>("controllable.options.cursorType", CursorType.class, gameSettings -> {
        return Controllable.getOptions().cursorType;
    }, (gameSettings, cursorType) -> {
        Controllable.getOptions().cursorType = cursorType;
    }, (gameSettings, controllableEnumOption) -> {
        CursorType cursorType = controllableEnumOption.get(gameSettings);
        return I18n.format("controllable.cursor." + cursorType.getName());
    });

    public static final ControllableEnumOption<ControllerType> CONTROLLER_TYPE = new ControllableEnumOption<>("controllable.options.controllerType", ControllerType.class, gameSettings -> {
        return Controllable.getOptions().controllerType;
    }, (gameSettings, controllerType) -> {
        Controllable.getOptions().controllerType = controllerType;
    }, (gameSettings, controllableEnumOption) -> {
        ControllerType controllerType = controllableEnumOption.get(gameSettings);
        return I18n.format("controllable.controller." + controllerType.getName());
    });

    public static final BooleanOption INVERT_LOOK = new ControllableBooleanOption("controllable.options.invertLook", gameSettings -> {
        return Controllable.getOptions().invertLook;
    }, (gameSettings, value) -> {
        Controllable.getOptions().invertLook = value;
    });

    public static final SliderPercentageOption DEAD_ZONE = new ControllableSliderPercentageOption("controllable.options.deadZone", 0.0, 1.0, 0.01F, gameSettings -> {
        return Controllable.getOptions().deadZone;
    }, (gameSettings, value) -> {
        Controllable.getOptions().deadZone = MathHelper.clamp(value, 0.0, 1.0);
    }, (gameSettings, option) -> {
        double deadZone = Controllable.getOptions().deadZone;
        return I18n.format("controllable.options.deadZone.format", FORMAT.format(deadZone));
    });

    public static final SliderPercentageOption ROTATION_SPEED = new ControllableSliderPercentageOption("controllable.options.rotationSpeed", 1.0, 200.0, 1.0F, gameSettings -> {
        return Controllable.getOptions().rotationSpeed;
    }, (gameSettings, value) -> {
        Controllable.getOptions().rotationSpeed = MathHelper.clamp(value, 1.0, 200.0);
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

    public static final SliderPercentageOption ATTACK_SPEED = new ControllableSliderPercentageOption("controllable.options.attackSpeed", 5, 40, 1, gameSettings -> (double) Controllable.getOptions().attackSpeed, (gameSettings, value) -> Controllable.getOptions().attackSpeed = (int) MathHelper.clamp(value, 5, 40), (gameSettings, sliderPercentageOption) -> {
        int attackSpeed = Controllable.getOptions().attackSpeed;
        return I18n.format("controllable.options.attackSpeed.format", FORMAT.format(attackSpeed));
    });

    public static final AbstractOption TOGGLE_SPRINT = new ControllableBooleanOption("controllable.options.toggleSprint", gameSettings -> Controllable.getOptions().toggleSprint, (gameSettings, aBoolean) -> Controllable.getOptions().toggleSprint = aBoolean);

    public static final AbstractOption TOGGLE_AIM = new ControllableBooleanOption("controllable.options.aimAssist", gameSettings -> Controllable.getOptions().aimAssist, (gameSettings, aBoolean) -> Controllable.getOptions().aimAssist = aBoolean);

    public static final SliderPercentageOption AIM_ASSIST_INTENSITY = new ControllableSliderPercentageOption("controllable.options.aimAssistIntensity", 1, 100, 1, gameSettings -> (double) Controllable.getOptions().aimAssistIntensity, (gameSettings, value) -> Controllable.getOptions().aimAssistIntensity = (int) MathHelper.clamp(value, 1, 100), (gameSettings, sliderPercentageOption) -> {
        int assistIntensity = Controllable.getOptions().aimAssistIntensity;
        return I18n.format("controllable.options.aimAssistIntensity.format", assistIntensity);
    });

    public static final AbstractOption HOSTILE_AIM_MODE = new ControllableEnumOption<>("controllable.options.aimAssist.hostile", AimAssistMode.class, gameSettings -> Controllable.getOptions().hostileAimMode, (gameSettings, mode) -> Controllable.getOptions().hostileAimMode = mode, (gameSettings, mode) -> I18n.format("controllable.options.aimAssistMode." + mode.get(gameSettings).getName()));
    public static final AbstractOption ANIMAL_AIM_MODE = new ControllableEnumOption<>("controllable.options.aimAssist.animal", AimAssistMode.class, gameSettings -> Controllable.getOptions().animalAimMode, (gameSettings, mode) -> Controllable.getOptions().animalAimMode = mode, (gameSettings, mode) -> I18n.format("controllable.options.aimAssistMode." + mode.get(gameSettings).getName()));
    public static final AbstractOption PLAYER_AIM_MODE = new ControllableEnumOption<>("controllable.options.aimAssist.player", AimAssistMode.class, gameSettings -> Controllable.getOptions().playerAimMode, (gameSettings, mode) -> Controllable.getOptions().playerAimMode = mode, (gameSettings, mode) -> I18n.format("controllable.options.aimAssistMode." + mode.get(gameSettings).getName()));

    public static final AbstractOption TOGGLE_IGNORE_SAME_TEAM = new ControllableBooleanOption("controllable.options.aimAssist.ignoreSameTeam",
            gameSettings -> Controllable.getOptions().ignoreSameTeam,
            (gameSettings, aBoolean) -> Controllable.getOptions().ignoreSameTeam = aBoolean);

    public static final AbstractOption TOGGLE_IGNORE_SAME_TEAM_FRIENDLY_FIRE = new ControllableBooleanOption("controllable.options.aimAssist.ignoreSameTeamFriendlyFire",
            gameSettings -> Controllable.getOptions().ignoreSameTeamFriendlyFire,
            (gameSettings, aBoolean) -> Controllable.getOptions().ignoreSameTeamFriendlyFire = aBoolean);

    public static final AbstractOption TOGGLE_IGNORE_PETS = new ControllableBooleanOption("controllable.options.aimAssist.ignorePets",
            gameSettings -> Controllable.getOptions().ignorePets,
            (gameSettings, aBoolean) -> Controllable.getOptions().ignorePets = aBoolean);


    public static final Splitter COLON_SPLITTER = Splitter.on(':');

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
    private int attackSpeed = 5;
    private boolean toggleSprint = false;

    private boolean aimAssist = true;
    private int aimAssistIntensity = 90; //Percentage
    private AimAssistMode hostileAimMode = AimAssistMode.BOTH;
    private AimAssistMode animalAimMode = AimAssistMode.AIM;
    private AimAssistMode playerAimMode = AimAssistMode.BOTH;
    private boolean ignoreSameTeam = true;
    private boolean ignoreSameTeamFriendlyFire = true;
    private boolean ignorePets = true;

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
                        case "forceFeedback":
                            this.forceFeedback = Boolean.parseBoolean(value);
                            break;
                        case "autoSelect":
                            this.autoSelect = Boolean.parseBoolean(value);
                            break;
                        case "renderMiniPlayer":
                            this.renderMiniPlayer = Boolean.parseBoolean(value);
                            break;
                        case "virtualMouse":
                            this.virtualMouse = Boolean.parseBoolean(value);
                            break;
                        case "consoleHotbar":
                            this.consoleHotbar = Boolean.parseBoolean(value);
                            break;
                        case "cursorType":
                            this.cursorType = CursorType.byId(value);
                            break;
                        case "controllerType":
                            this.controllerType = ControllerType.byName(value);
                            break;
                        case "invertLook":
                            this.invertLook = Boolean.parseBoolean(value);
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
                        case "attackSpeed":
                            this.attackSpeed = Integer.parseInt(value);
                            break;
                        case "toggleSprint":
                            this.toggleSprint = Boolean.parseBoolean(value);
                            break;
                        case "aimAssist":
                            this.aimAssist = Boolean.parseBoolean(value);
                            break;
                        case "aimAssistIntensity":
                            this.aimAssistIntensity = Integer.parseInt(value);
                            break;
                        case "hostileAimMode":
                            this.hostileAimMode = AimAssistMode.byName(value);
                            break;
                        case "animalAimMode":
                            this.animalAimMode = AimAssistMode.byName(value);
                            break;
                        case "playerAimMode":
                            this.playerAimMode = AimAssistMode.byName(value);
                            break;
                        case "ignorePets":
                            this.ignorePets = Boolean.parseBoolean(value);
                            break;
                        case "ignoreSameTeam":
                            this.ignoreSameTeam = Boolean.parseBoolean(value);
                            break;
                        case "ignoreSameTeamFriendlyFire":
                            this.ignoreSameTeamFriendlyFire = Boolean.parseBoolean(value);
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
            writer.println("attackSpeed:" + this.attackSpeed);
            writer.println("toggleSprint:" + this.toggleSprint);
            writer.println("aimAssist:" + this.aimAssist);
            writer.println("aimAssistIntensity:" + this.aimAssistIntensity);
            writer.println("hostileAimMode:" + this.hostileAimMode);
            writer.println("animalAimMode:" + this.animalAimMode);
            writer.println("playerAimMode:" + this.playerAimMode);
            writer.println("ignoreSameTeam: " + ignoreSameTeam);
            writer.println("ignoreSameTeamFriendlyFire: " + ignoreSameTeamFriendlyFire);
            writer.println("ignorePets:" + this.ignorePets);
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
        return this.deadZone;
    }

    public double getRotationSpeed()
    {
        return this.rotationSpeed;
    }

    public int getAttackSpeed()
    {
        return attackSpeed;
    }

    public boolean isToggleSprint()
    {
        return toggleSprint;
    }

    public double getMouseSpeed()
    {
        return this.mouseSpeed;
    }

    public boolean isAimAssist()
    {
        return aimAssist;
    }

    public int getAimAssistIntensity()
    {
        return aimAssistIntensity;
    }

    public AimAssistMode getHostileAimMode()
    {
        return hostileAimMode;
    }

    public AimAssistMode getAnimalAimMode()
    {
        return animalAimMode;
    }

    public AimAssistMode getPlayerAimMode()
    {
        return playerAimMode;
    }

    public boolean isIgnoreSameTeam()
    {
        return ignoreSameTeam;
    }

    public boolean isIgnorePets()
    {
        return ignorePets;
    }

    public boolean isIgnoreSameTeamFriendlyFire()
    {
        return ignoreSameTeamFriendlyFire;
    }

    public enum AimAssistMode implements IStringSerializable
    {
        NONE("none"),
        SENSITIVITY("sensitivity"),
        AIM("aim"),
        BOTH("both");

        private String strMode;

        AimAssistMode(String strMode)
        {
            this.strMode = strMode;
        }

        public static AimAssistMode byName(String value)
        {
            for(AimAssistMode aimAssistMode : values())
            {
                if(aimAssistMode.strMode.equalsIgnoreCase(value))
                    return aimAssistMode;
            }
            return null;
        }

        @Override
        public String getName()
        {
            return strMode;
        }

        public boolean sensitivity()
        {
            return this == SENSITIVITY || this == BOTH;
        }

        public boolean aim()
        {
            return this == AIM || this == BOTH;
        }

        public boolean on() {
            return aim() || sensitivity();
        }
    }
}
