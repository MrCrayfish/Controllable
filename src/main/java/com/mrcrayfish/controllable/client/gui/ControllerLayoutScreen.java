package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.ButtonStates;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import com.mrcrayfish.controllable.client.gui.widget.PressableButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ControllerLayoutScreen extends GuiScreen
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");

    private List<ControllerButton> controllerButtons = new ArrayList<>();

    private int configureButton = -1;
    private boolean validLayout;
    private GuiScreen parentScreen;
    private LayoutButtonStates states = new LayoutButtonStates();
    private Mappings.Entry entry;
    private GuiButton doneButton;
    private GuiButton resetButton;
    private GuiButton thumbstickButton;

    protected ControllerLayoutScreen(GuiScreen parentScreen)
    {
        this.parentScreen = parentScreen;

        Controller controller = Controllable.getController();
        if(controller != null)
        {
            Mappings.Entry entry = controller.getMapping();
            if(entry != null)
            {
                this.entry = entry.copy();
            }
            else
            {
                this.entry = new Mappings.Entry(controller.getName(), controller.getName(), new HashMap<>());
            }
        }
    }

    @Override
    public void initGui()
    {
        this.controllerButtons.clear();
        this.controllerButtons.add(new ControllerButton(this, Buttons.A, 29, 9, 7, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.B, 32, 6, 13, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.X, 26, 6, 16, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.Y, 29, 3, 10, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.LEFT_BUMPER, 5, -2, 25, 0, 7, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.RIGHT_BUMPER, 26, -2, 32, 0, 7, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.LEFT_TRIGGER, -10, 0, 39, 0, 7, 6, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.RIGHT_TRIGGER, 41, 0, 39, 0, 7, 6, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.DPAD_DOWN, 6, 9, 19, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.DPAD_RIGHT, 9, 6, 19, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.DPAD_LEFT, 3, 6, 19, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.DPAD_UP, 6, 3, 19, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.SELECT, 14, 4, 22, 0, 3, 2, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.START, 21, 4, 22, 0, 3, 2, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.HOME, 17, 8, 46, 0, 4, 4, 5));
        this.controllerButtons.add(new ControllerAxis(this, Buttons.LEFT_THUMB_STICK, 9, 12, 0, 0, 7, 7, 5));
        this.controllerButtons.add(new ControllerAxis(this, Buttons.RIGHT_THUMB_STICK, 22, 12, 0, 0, 7, 7, 5));

        this.doneButton = this.addButton(new PressableButton(this.width / 2 - 154, this.height - 32, 100, 20, I18n.format("controllable.gui.save"), (button) -> {
            this.updateControllerMapping();
            this.mc.displayGuiScreen(this.parentScreen);
        }));

        this.resetButton = this.addButton(new PressableButton(this.width / 2 - 50, this.height - 32, 100, 20, I18n.format("controllable.gui.reset"), (button) -> {
            this.entry.getReassignments().clear();
            this.entry.setSwitchThumbsticks(false);
            this.entry.setFlipLeftX(false);
            this.entry.setFlipLeftY(false);
            this.entry.setFlipRightX(false);
            this.entry.setFlipRightY(false);
        }));

        this.addButton(new PressableButton(this.width / 2 + 54, this.height - 32, 100, 20, I18n.format("gui.cancel"), (button) -> {
            this.mc.displayGuiScreen(this.parentScreen);
        }));

        int width = 38 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50 - 35;

        this.thumbstickButton = this.addButton(new ImageButton(x + width / 2 - 10, y + 90, 20, TEXTURE, 92, 0, 16, 16, button -> {
            this.mc.displayGuiScreen(new ThumbstickSettingsScreen(this));
        }));
    }

    @Override
    public void updateScreen()
    {
        boolean changed = !this.entry.getReassignments().isEmpty();
        changed |= this.entry.isThumbsticksSwitched();
        changed |= this.entry.isFlipLeftX();
        changed |= this.entry.isFlipLeftY();
        changed |= this.entry.isFlipRightX();
        changed |= this.entry.isFlipRightY();
        this.resetButton.enabled = changed;
        this.validLayout = this.entry.getReassignments().values().stream().noneMatch(b -> b == -1);
        ITextComponent save = new TextComponentTranslation("controllable.gui.save");
        save.getStyle().setColor(this.validLayout ? TextFormatting.WHITE : TextFormatting.RED);
        this.doneButton.displayString = save.getFormattedText();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        int width = 38 * 5;
        int height = 24 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50 - 35;
        drawScaledCustomSizeModalRect(x, y, 50, 0, 38, 24, width, height, 256, 256);
        this.controllerButtons.forEach(controllerButton -> controllerButton.draw(x, y, mouseX, mouseY, this.configureButton == controllerButton.button));
        this.drawCenteredString(this.fontRenderer, I18n.format("controllable.gui.title.layout"), this.width / 2, 20, 0xFFFFFF);

        ITextComponent info = new TextComponentTranslation("controllable.gui.layout.info");
        info.getStyle().setColor(TextFormatting.GRAY);
        this.drawMultiLineCenteredString(this.fontRenderer, info.getFormattedText(), x + width / 2, y + 135, width + 190, 0xFFFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if(this.configureButton != -1)
        {
            GlStateManager.disableDepth();
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
            this.drawCenteredString(this.fontRenderer, I18n.format("controllable.gui.layout.press_button"), this.width / 2, this.height / 2, 0xFFFFFFFF);
            GlStateManager.enableDepth();
            return;
        }

        ControllerButton button = this.controllerButtons.stream().filter(ControllerButton::isHovered).findFirst().orElse(null);
        if(button != null)
        {
            List<String> components = new ArrayList<>();
            ITextComponent buttonName = new TextComponentTranslation(Buttons.NAMES[button.getButton()]);
            buttonName.getStyle().setColor(TextFormatting.BLUE);
            components.add(I18n.format("controllable.gui.layout.button", buttonName.getFormattedText()));
            if(button.isMissingMapping())
            {
                ITextComponent missing = new TextComponentTranslation("controllable.gui.layout.missing_mapping");
                missing.getStyle().setColor(TextFormatting.RED);
                components.add(missing.getFormattedText());
            }
            else
            {
                int remappedButton = button.getButton();
                if(!button.isMissingMapping())
                {
                    Map<Integer, Integer> reassignments = this.entry.getReassignments();
                    for(Integer key : reassignments.keySet())
                    {
                        if(reassignments.get(key) == remappedButton)
                        {
                            remappedButton = key;
                            break;
                        }
                    }
                }
                ITextComponent mapped = new TextComponentTranslation("controllable.gui.layout.mapped_to", new TextComponentString(String.valueOf(remappedButton)));
                mapped.getStyle().setColor(TextFormatting.BLUE);
                components.add(mapped.getFormattedText());
            }
            ITextComponent remap = new TextComponentTranslation("controllable.gui.layout.remap");
            remap.getStyle().setColor(TextFormatting.GRAY);
            components.add(remap.getFormattedText());
            this.drawHoveringText(components, mouseX, mouseY);
        }

        if(!this.validLayout && this.doneButton.isMouseOver())
        {
            List<String> components = new ArrayList<>();
            ITextComponent warning = new TextComponentTranslation("controllable.gui.layout.warning");
            warning.getStyle().setColor(TextFormatting.RED);
            components.add(warning.getFormattedText());
            ITextComponent invalid = new TextComponentTranslation("controllable.gui.layout.invalid_layout");
            invalid.getStyle().setColor(TextFormatting.GRAY);
            components.addAll(this.fontRenderer.listFormattedStringToWidth(invalid.getFormattedText(), 180));
            this.drawHoveringText(components, mouseX, mouseY - 50);
        }

        if(this.thumbstickButton.isMouseOver())
        {
            this.drawHoveringText(I18n.format("controllable.gui.layout.thumbsticks"), mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if(mouseButton == 0)
        {
            ControllerButton button = this.controllerButtons.stream().filter(ControllerButton::isHovered).findFirst().orElse(null);
            if(button != null)
            {
                this.configureButton = button.getButton();
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if(keyCode == Keyboard.KEY_ESCAPE && this.configureButton != -1)
        {
            this.configureButton = -1;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    public boolean onButtonInput(int button)
    {
        if(this.configureButton != -1)
        {
            Map<Integer, Integer> reassignments = this.entry.getReassignments();
            if(button != this.configureButton)
            {
                // Sets the target
                reassignments.putIfAbsent(this.configureButton, -1);

                // Reset any assignments that targets the configuration button and set to -1
                for(Integer key : reassignments.keySet())
                {
                    if(reassignments.get(key) == this.configureButton)
                    {
                        reassignments.put(key, -1);
                    }
                }

                // Finally set the new mapping
                reassignments.put(button, this.configureButton);
            }
            else
            {
                // Remove reassignment because button is back to it's default mapping
                reassignments.remove(button);

                // Reset any assignments that targets the button and set to -1
                for(Integer key : reassignments.keySet())
                {
                    if(reassignments.get(key) == button)
                    {
                        reassignments.put(key, -1);
                    }
                }
            }
            this.configureButton = -1;
            return true;
        }
        return false;
    }

    private void updateControllerMapping()
    {
        Controller controller = Controllable.getController();
        if(controller != null)
        {
            controller.setMapping(this.entry);
            this.entry.save();
        }
    }

    public void processButton(int index, ButtonStates newStates)
    {
        boolean state = newStates.getState(index);

        if(state && this.onButtonInput(index))
        {
            return;
        }

        Controller controller = Controllable.getController();
        if(controller == null)
        {
            return;
        }

        index = this.remap(index);

        //No binding so don't perform any action
        if(index == -1)
        {
            return;
        }

        if(state)
        {
            if(!this.states.getState(index))
            {
                this.states.setState(index, true);
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF, 1.0F));
            }
        }
        else if(this.states.getState(index))
        {
            this.states.setState(index, false);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON, 0.9F));
        }
    }

    public int remap(int button)
    {
        return this.entry.remap(button);
    }

    public boolean isButtonPressed(int button)
    {
        return this.states.getState(button);
    }

    public Map<Integer, Integer> getReassignments()
    {
        return this.entry.getReassignments();
    }

    public Mappings.Entry getEntry()
    {
        return this.entry;
    }

    private void drawMultiLineCenteredString(FontRenderer font, String component, int x, int y, int width, int color)
    {
        for(String s : font.listFormattedStringToWidth(component, width))
        {
            font.drawString(s, (int) (x - font.getStringWidth(s) / 2.0), y, color);
            y += font.FONT_HEIGHT;
        }
    }

    public static class LayoutButtonStates extends ButtonStates
    {
        @Override
        public void setState(int index, boolean state)
        {
            super.setState(index, state);
        }
    }
}
