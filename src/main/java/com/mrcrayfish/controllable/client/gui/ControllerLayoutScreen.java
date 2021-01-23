package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
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
    private Screen parentScreen;
    private Map<Integer, Integer> reassignments;
    private Button resetButton;

    protected ControllerLayoutScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.binding"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init()
    {
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

        this.controllerButtons.add(new ControllerButton(this, Buttons.A, 29, 9, 7, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.B, 32, 6, 13, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.X, 26, 6, 16, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.Y, 29, 3, 10, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.LEFT_BUMPER, 5, -2, 25, 0, 7, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.RIGHT_BUMPER, 26, -2, 32, 0, 7, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.LEFT_TRIGGER, 5, -10, 39, 0, 7, 6, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.RIGHT_TRIGGER, 26, -10, 39, 0, 7, 6, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.DPAD_DOWN, 6, 9, 19, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.DPAD_RIGHT, 9, 6, 19, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.DPAD_LEFT, 3, 6, 19, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.DPAD_UP, 6, 3, 19, 0, 3, 3, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.SELECT, 14, 4, 22, 0, 3, 2, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.START, 21, 4, 22, 0, 3, 2, 5));
        this.controllerButtons.add(new ControllerButton(this, Buttons.HOME, 17, 8, 46, 0, 4, 4, 5));
        this.controllerButtons.add(new ControllerAxis(this, Buttons.LEFT_THUMB_STICK, 9, 12, 0, 0, 7, 7, 5));
        this.controllerButtons.add(new ControllerAxis(this, Buttons.RIGHT_THUMB_STICK, 22, 12, 0, 0, 7, 7, 5));

        this.addButton(new Button(this.width / 2 - 154, this.height - 32, 100, 20, new TranslationTextComponent("gui.done"), (button) -> {
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
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderDirtBackground(0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
        int width = 38 * 5;
        int height = 29 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50;
        blit(matrixStack, x, y, width, height, 50, 0, 38, 29, 256, 256); //TODO test
        RenderSystem.disableBlend();
        this.controllerButtons.forEach(controllerButton -> controllerButton.draw(matrixStack, x, y, mouseX, mouseY, this.configureButton == controllerButton.button));
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if(this.configureButton != -1)
        {
            this.fillGradient(matrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
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
            //TODO make it so assignments are not applied immediately until applied until "Done" is pressed
            if(button != this.configureButton)
            {
                this.reassignments.putIfAbsent(this.configureButton, -1);
                this.reassignments.put(button, this.configureButton);
            }
            else
            {
                Integer oldButton = this.reassignments.remove(button);
                if(oldButton != null)
                {
                    this.reassignments.values().removeIf(b -> b == (int) oldButton);
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

    public Map<Integer, Integer> getReassignments()
    {
        return this.reassignments;
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
}
