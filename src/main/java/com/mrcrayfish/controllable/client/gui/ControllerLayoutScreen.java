package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ControllerLayoutScreen extends Screen
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");

    private List<ControllerButton> controllerButtons = new ArrayList<>();

    private int configureButton = -1;
    private Screen parentScreen;

    protected ControllerLayoutScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.layout"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init()
    {
        int v = Controllable.getOptions().getIcons().ordinal() * 13;
        controllerButtons.add(new ControllerButton(Buttons.A, 29, 9, 7, v, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.B, 32, 6, 13, v, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.X, 26, 6, 16, v, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.Y, 29, 3, 10, v, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.LEFT_BUMPER, 5, -2, 25, v, 7, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.RIGHT_BUMPER, 26, -2, 32, v, 7, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.LEFT_TRIGGER, 5, -10, 39, v, 7, 6, 5));
        controllerButtons.add(new ControllerButton(Buttons.RIGHT_TRIGGER, 26, -10, 39, v, 7, 6, 5));
        controllerButtons.add(new ControllerButton(Buttons.DPAD_DOWN, 6, 9, 19, v, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.DPAD_RIGHT, 9, 6, 19, v, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.DPAD_LEFT, 3, 6, 19, v, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.DPAD_UP, 6, 3, 19, v, 3, 3, 5));
        controllerButtons.add(new ControllerButton(Buttons.SELECT, 14, 4, 22, v, 3, 2, 5));
        controllerButtons.add(new ControllerButton(Buttons.START, 21, 4, 22, v, 3, 2, 5));
        controllerButtons.add(new ControllerButton(Buttons.HOME, 17, 8, 46, v, 4, 4, 5));
        controllerButtons.add(new ControllerAxis(Buttons.LEFT_THUMB_STICK, 9, 12, 0, v, 7, 7, 5));
        controllerButtons.add(new ControllerAxis(Buttons.RIGHT_THUMB_STICK, 22, 12, 0, v, 7, 7, 5));

        this.addButton(new Button(this.width / 2 - 100, this.height - 32, 200, 20, I18n.format("gui.done"), (button) -> {
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
        int width = 38 * 5;
        int height = 29 * 5;
        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - 50;
        blit(x, y, width, height, 50, 0, 38, 29, 256, 256);
        GlStateManager.disableBlend();
        controllerButtons.forEach(controllerButton -> controllerButton.draw(x, y, mouseX, mouseY, configureButton == controllerButton.button));
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 20, 0xFFFFFF);
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        if(mouseButton == 0)
        {
            ControllerButton button = controllerButtons.stream().filter(ControllerButton::isHovered).findFirst().orElse(null);
            if(button != null)
            {
                configureButton = button.getButton();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods)
    {
        if(key == GLFW.GLFW_KEY_ESCAPE && configureButton != -1)
        {
            configureButton = -1;
            return true;
        }
        return super.keyPressed(key, scanCode, mods);
    }

    public boolean onButtonInput(int button)
    {
        if(configureButton != -1)
        {
            Controller controller = Controllable.getController();
            if(controller != null)
            {
                Mappings.Entry entry = controller.getMapping();
                if(entry == null)
                {
                    entry = new Mappings.Entry(controller.getName(), controller.getName(), new HashMap<>());
                    controller.setMapping(entry);
                }
                if(button != configureButton)
                {
                    entry.getReassignments().putIfAbsent(configureButton, -1);
                    entry.getReassignments().put(button, configureButton);
                }
                else
                {
                    Integer originalButton = entry.getReassignments().inverse().get(configureButton);
                    if(originalButton != null)
                    {
                        entry.getReassignments().remove(originalButton);
                    }
                    entry.getReassignments().remove(button);
                }
                configureButton = -1;
                entry.save();
                return true;
            }
        }
        return false;
    }
}
