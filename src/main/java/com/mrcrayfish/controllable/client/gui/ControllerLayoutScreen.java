package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.ButtonStates;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import com.mrcrayfish.controllable.client.gui.widget.ImageStateButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
    private boolean validLayout;
    private Screen parentScreen;
    private LayoutButtonStates states = new LayoutButtonStates();
    private Mappings.Entry entry;
    private Button doneButton;
    private Button resetButton;
    private Button switchButton;

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
                this.entry = entry.copy();
            }
            else
            {
                this.entry = new Mappings.Entry(controller.getName(), controller.getName(), new HashMap<>());
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
            this.entry.getReassignments().clear();
        }));

        this.addButton(new Button(this.width / 2 + 54, this.height - 32, 100, 20, new TranslationTextComponent("gui.cancel"), (button) -> {
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));

        int width = 38 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50 - 35;
        this.switchButton = this.addButton(new ImageStateButton(x + width / 2 - 10, y + 90, 20, TEXTURE, 92, 0, () -> this.entry.isThumbsticksSwitched(), button -> {
            this.entry.setSwitchThumbsticks(!this.entry.isThumbsticksSwitched());
        }));
    }

    @Override
    public void tick()
    {
        this.resetButton.active = !this.entry.getReassignments().isEmpty();
        this.validLayout = this.entry.getReassignments().values().stream().noneMatch(b -> b == -1);
        this.doneButton.setMessage(new TranslationTextComponent("gui.done").mergeStyle(this.validLayout ? TextFormatting.WHITE : TextFormatting.RED));
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
        int y = this.height / 2 - 50 - 35;
        blit(matrixStack, x, y, width, height, 50, 0, 38, 25, 256, 256); //TODO test
        RenderSystem.disableBlend();
        this.controllerButtons.forEach(controllerButton -> controllerButton.draw(matrixStack, x, y, mouseX, mouseY, this.configureButton == controllerButton.button));
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        this.drawMultiLineCenteredString(matrixStack, this.font, new TranslationTextComponent("controllable.gui.layout.info").mergeStyle(TextFormatting.GRAY), x + width / 2, y + 135, width + 190, 0xFFFFFFFF);

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
            components.add(new TranslationTextComponent("controllable.gui.layout.button", new TranslationTextComponent(Buttons.NAMES[button.getButton()]).mergeStyle(TextFormatting.BLUE)));
            if(button.isMissingMapping())
            {
                components.add(new TranslationTextComponent("controllable.gui.layout.missing_mapping").mergeStyle(TextFormatting.RED));
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
                components.add(new TranslationTextComponent("controllable.gui.layout.mapped_to", new StringTextComponent(String.valueOf(remappedButton)).mergeStyle(TextFormatting.BLUE)));
            }
            components.add(new TranslationTextComponent("controllable.gui.layout.remap").mergeStyle(TextFormatting.GRAY));
            this.func_243308_b(matrixStack, components, mouseX, mouseY);
        }

        if(this.switchButton.isHovered())
        {
            List<IReorderingProcessor> components = new ArrayList<>();
            components.add(new TranslationTextComponent("controllable.gui.layout.switch_thumbsticks").func_241878_f());
            components.addAll(this.font.trimStringToWidth(new TranslationTextComponent("controllable.gui.layout.switch_thumbsticks.info").mergeStyle(TextFormatting.GRAY), 180));
            if(this.entry.isThumbsticksSwitched())
            {
                components.add(new TranslationTextComponent("controllable.gui.layout.switch_thumbsticks.enabled").mergeStyle(TextFormatting.BLUE).func_241878_f());
            }
            else
            {
                components.add(new TranslationTextComponent("controllable.gui.layout.switch_thumbsticks.disabled").mergeStyle(TextFormatting.RED).func_241878_f());
            }
            this.renderTooltip(matrixStack, components, mouseX, mouseY - 50);
        }

        if(!this.validLayout && this.doneButton.isHovered())
        {
            List<IReorderingProcessor> components = new ArrayList<>();
            components.add(new TranslationTextComponent("controllable.gui.layout.warning").mergeStyle(TextFormatting.RED).func_241878_f());
            components.addAll(this.font.trimStringToWidth(new TranslationTextComponent("controllable.gui.layout.invalid_layout").mergeStyle(TextFormatting.GRAY), 180));
            this.renderTooltip(matrixStack, components, mouseX, mouseY - 50);
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

    private void drawMultiLineCenteredString(MatrixStack matrixStack, FontRenderer font, ITextComponent component, int x, int y, int width, int color)
    {
        for(IReorderingProcessor s : font.trimStringToWidth(component, width))
        {
            font.func_238407_a_(matrixStack, s, (float) (x - font.func_243245_a(s) / 2.0), y, color);
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
