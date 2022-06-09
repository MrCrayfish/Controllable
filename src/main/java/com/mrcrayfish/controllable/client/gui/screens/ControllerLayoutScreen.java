package com.mrcrayfish.controllable.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.controllable.ButtonStates;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import com.mrcrayfish.controllable.client.gui.ControllerAxis;
import com.mrcrayfish.controllable.client.gui.ControllerButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Button thumbstickButton;

    protected ControllerLayoutScreen(Screen parentScreen)
    {
        super(Component.translatable("controllable.gui.title.layout"));
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

        this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 32, 100, 20, Component.translatable("controllable.gui.save"), (button) -> {
            this.updateControllerMapping();
            this.minecraft.setScreen(this.parentScreen);
        }));

        this.resetButton = this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 32, 100, 20, Component.translatable("controllable.gui.reset"), (button) -> {
            this.entry.getReassignments().clear();
            this.entry.setSwitchThumbsticks(false);
            this.entry.setFlipLeftX(false);
            this.entry.setFlipLeftY(false);
            this.entry.setFlipRightX(false);
            this.entry.setFlipRightY(false);
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 54, this.height - 32, 100, 20, Component.translatable("gui.cancel"), (button) -> {
            this.minecraft.setScreen(this.parentScreen);
        }));

        int width = 38 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50 - 35;

        this.thumbstickButton = this.addRenderableWidget(new ImageButton(x + width / 2 - 10, y + 90, 20, TEXTURE, 92, 0, 16, 16, button -> {
            this.minecraft.setScreen(new ThumbstickSettingsScreen(this));
        }));
    }

    @Override
    public void tick()
    {
        boolean changed = !this.entry.getReassignments().isEmpty();
        changed |= this.entry.isThumbsticksSwitched();
        changed |= this.entry.isFlipLeftX();
        changed |= this.entry.isFlipLeftY();
        changed |= this.entry.isFlipRightX();
        changed |= this.entry.isFlipRightY();
        this.resetButton.active = changed;
        this.validLayout = this.entry.getReassignments().values().stream().noneMatch(b -> b == -1);
        this.doneButton.setMessage(Component.translatable("controllable.gui.save").withStyle(this.validLayout ? ChatFormatting.WHITE : ChatFormatting.RED));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        int width = 38 * 5;
        int height = 24 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50 - 35;
        blit(poseStack, x, y, width, height, 50, 0, 38, 24, 256, 256);
        this.controllerButtons.forEach(controllerButton -> controllerButton.draw(poseStack, x, y, mouseX, mouseY, this.configureButton == controllerButton.getButton()));
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        this.drawMultiLineCenteredString(poseStack, this.font, Component.translatable("controllable.gui.layout.info").withStyle(ChatFormatting.GRAY), x + width / 2, y + 135, width + 190, 0xFFFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTicks);

        if(this.configureButton != -1)
        {
            RenderSystem.disableDepthTest();
            this.fillGradient(poseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
            drawCenteredString(poseStack, this.font, Component.translatable("controllable.gui.layout.press_button"), this.width / 2, this.height / 2, 0xFFFFFFFF);
            RenderSystem.enableDepthTest();
            return;
        }

        ControllerButton button = this.controllerButtons.stream().filter(ControllerButton::isHovered).findFirst().orElse(null);
        if(button != null)
        {
            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("controllable.gui.layout.button", Component.translatable(Buttons.NAMES[button.getButton()]).withStyle(ChatFormatting.BLUE)));
            if(button.isMissingMapping())
            {
                components.add(Component.translatable("controllable.gui.layout.missing_mapping").withStyle(ChatFormatting.RED));
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
                components.add(Component.translatable("controllable.gui.layout.mapped_to", Component.literal(String.valueOf(remappedButton)).withStyle(ChatFormatting.BLUE)));
            }
            components.add(Component.translatable("controllable.gui.layout.remap").withStyle(ChatFormatting.GRAY));
            this.renderComponentTooltip(poseStack, components, mouseX, mouseY);
        }

        if(!this.validLayout && this.doneButton.isHoveredOrFocused())
        {
            List<FormattedCharSequence> components = new ArrayList<>();
            components.add(Component.translatable("controllable.gui.layout.warning").withStyle(ChatFormatting.RED).getVisualOrderText());
            components.addAll(this.font.split(Component.translatable("controllable.gui.layout.invalid_layout").withStyle(ChatFormatting.GRAY), 180));
            this.renderTooltip(poseStack, components, mouseX, mouseY - 50);
        }

        if(this.thumbstickButton.isHoveredOrFocused())
        {
            this.renderTooltip(poseStack, Component.translatable("controllable.gui.layout.thumbsticks"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(mouseButton == 0 && this.configureButton == -1)
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
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.0F));
            }
        }
        else if(this.states.getState(index))
        {
            this.states.setState(index, false);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.9F));
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

    private void drawMultiLineCenteredString(PoseStack matrixStack, Font font, Component component, int x, int y, int width, int color)
    {
        for(FormattedCharSequence s : font.split(component, width))
        {
            font.draw(matrixStack, s, (float) (x - font.width(s) / 2.0), y, color);
            y += font.lineHeight;
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
