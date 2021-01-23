package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.ButtonStates;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Author: MrCrayfish
 */
public class ControllerLayoutScreen extends Screen
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");

    private List<ControllerButton> controllerButtons = new ArrayList<>();

    private int configureButton = -1;
    private Screen parentScreen;
    private LayoutButtonStates states = new LayoutButtonStates();
    private Map<Integer, Integer> reassignments;
    private Button doneButton;
    private Button resetButton;

    protected ControllerLayoutScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.layout"));
        this.parentScreen = parentScreen;

        Controller controller = Controllable.getController();
        if(controller != null)
        {
            Mappings.Entry entry = controller.getMapping();
            if(entry != null)
            {
                this.reassignments = new HashMap<>(entry.getReassignments());
            }
            else
            {
                this.reassignments = new HashMap<>();
            }
        }
    }

    @Override
    protected void init()
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

        this.doneButton = this.addButton(new Button(this.width / 2 - 154, this.height - 32, 100, 20, new TranslationTextComponent("gui.done"), (button) -> {
            this.updateControllerMapping();
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));

        this.resetButton = this.addButton(new Button(this.width / 2 - 50, this.height - 32, 100, 20, new TranslationTextComponent("controllable.gui.reset"), (button) -> {
            this.reassignments.clear();
        }));

        this.addButton(new Button(this.width / 2 + 54, this.height - 32, 100, 20, new TranslationTextComponent("gui.cancel"), (button) -> {
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));
    }

    @Override
    public void tick()
    {
        this.resetButton.active = !this.reassignments.isEmpty();
        this.doneButton.active = this.reassignments.values().stream().noneMatch(b -> b == -1);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
        int width = 38 * 5;
        int height = 25 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50;
        blit(matrixStack, x, y, width, height, 50, 0, 38, 25, 256, 256); //TODO test
        RenderSystem.disableBlend();
        this.controllerButtons.forEach(controllerButton -> controllerButton.draw(matrixStack, x, y, mouseX, mouseY, this.configureButton == controllerButton.button));
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if(this.configureButton != -1)
        {
            this.fillGradient(matrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
            drawCenteredString(matrixStack, this.font, new TranslationTextComponent("controllable.gui.layout.press_button"), this.width / 2, this.height / 2, 0xFFFFFFFF);
            return;
        }

        ControllerButton button = this.controllerButtons.stream().filter(ControllerButton::isHovered).findFirst().orElse(null);
        if(button != null)
        {
            List<ITextComponent> components = new ArrayList<>();
            components.add(new TranslationTextComponent("controllable.gui.layout.button", new TranslationTextComponent(Buttons.NAMES[button.getButton()]).func_240699_a_(TextFormatting.BLUE)));
            if(button.isMissingMapping())
            {
                components.add(new TranslationTextComponent("controllable.gui.layout.missing_mapping").func_240699_a_(TextFormatting.RED));
            }
            else
            {
                int remappedButton = button.getButton();
                if(!button.isMissingMapping())
                {
                    Map<Integer, Integer> reassignments = this.reassignments;
                    for(Integer key : reassignments.keySet())
                    {
                        if(reassignments.get(key) == remappedButton)
                        {
                            remappedButton = key;
                            break;
                        }
                    }
                }
                components.add(new TranslationTextComponent("controllable.gui.layout.mapped_to", new StringTextComponent(String.valueOf(remappedButton)).func_240699_a_(TextFormatting.BLUE)));
            }
            components.add(new TranslationTextComponent("controllable.gui.layout.remap").func_240699_a_(TextFormatting.GOLD));
            this.func_243308_b(matrixStack, components, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(mouseButton == 0)
        {
            ControllerButton button = this.controllerButtons.stream().filter(ControllerButton::isHovered).findFirst().orElse(null);
            if(button != null)
            {
                this.configureButton = button.getButton();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods)
    {
        if(key == GLFW.GLFW_KEY_ESCAPE && this.configureButton != -1)
        {
            this.configureButton = -1;
            return true;
        }
        return super.keyPressed(key, scanCode, mods);
    }

    public boolean onButtonInput(int button)
    {
        if(this.configureButton != -1)
        {
            if(button != this.configureButton)
            {
                // Sets the target
                this.reassignments.putIfAbsent(this.configureButton, -1);

                // Reset any assignments that targets the configuration button and set to -1
                for(Integer key : this.reassignments.keySet())
                {
                    if(this.reassignments.get(key) == this.configureButton)
                    {
                        this.reassignments.put(key, -1);
                    }
                }

                // Finally set the new mapping
                this.reassignments.put(button, this.configureButton);
            }
            else
            {
                // Remove reassignment because button is back to it's default mapping
                this.reassignments.remove(button);

                // Reset any assignments that targets the button and set to -1
                for(Integer key : this.reassignments.keySet())
                {
                    if(this.reassignments.get(key) == button)
                    {
                        this.reassignments.put(key, -1);
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
            Mappings.Entry entry = controller.getMapping();
            if(entry == null)
            {
                entry = new Mappings.Entry(controller.getName(), controller.getName(), this.reassignments);
                controller.setMapping(entry);
            }
            else
            {
                entry.setReassignments(this.reassignments);
            }
            entry.save();
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
                Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0F));
            }
        }
        else if(this.states.getState(index))
        {
            this.states.setState(index, false);
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.9F));
        }
    }

    public int remap(int button)
    {
        Integer value = this.reassignments.get(button);
        if(value != null)
        {
            return value;
        }
        return button;
    }

    public boolean isButtonPressed(int button)
    {
        return this.states.getState(button);
    }

    public Map<Integer, Integer> getReassignments()
    {
        return this.reassignments;
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
