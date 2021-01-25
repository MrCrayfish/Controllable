package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.ButtonStates;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.ButtonBindings;
import com.mrcrayfish.controllable.client.Controller;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingScreen extends Screen
{
    private Screen parentScreen;
    private Button buttonReset;
    private ButtonBindingList bindingList;
    private ButtonBinding selectedBinding = null;

    protected ButtonBindingScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.button_binding"));
        this.parentScreen = parentScreen;
    }

    void setSelectedBinding(ButtonBinding selectedBinding)
    {
        this.selectedBinding = selectedBinding;
    }

    boolean isWaitingForButtonInput()
    {
        return this.selectedBinding != null;
    }

    @Override
    protected void init()
    {
        this.bindingList = new ButtonBindingList(this, this.minecraft, this.width, this.height, 32, this.height - 44, 20);
        this.children.add(this.bindingList);

        this.buttonReset = this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslationTextComponent("controllable.gui.resetBinds"), (button) -> {
            ButtonBindings.getBindings().forEach(ButtonBinding::reset);
        }));
        this.buttonReset.active = ButtonBindings.getBindings().stream().noneMatch(ButtonBinding::isDefault);

        this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, DialogTexts.GUI_DONE, (button) -> {
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));
    }

    @Override
    public void tick()
    {
        this.buttonReset.active = !ButtonBindings.getBindings().stream().allMatch(ButtonBinding::isDefault);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.bindingList.render(matrixStack, this.selectedBinding == null ? mouseX : -1, this.selectedBinding == null ? mouseY : -1, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, this.selectedBinding == null ? mouseX : -1, this.selectedBinding == null ? mouseY : -1, partialTicks);

        if(this.selectedBinding != null)
        {
            RenderSystem.disableDepthTest();
            this.fillGradient(matrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
            drawCenteredString(matrixStack, this.font, new TranslationTextComponent("controllable.gui.layout.press_button"), this.width / 2, this.height / 2, 0xFFFFFFFF);
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.selectedBinding != null)
        {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods)
    {
        if(key == GLFW.GLFW_KEY_ESCAPE && this.selectedBinding != null)
        {
            this.selectedBinding = null;
            return true;
        }
        return super.keyPressed(key, scanCode, mods);
    }

    public boolean processButton(int index)
    {
        if(this.selectedBinding != null)
        {
            this.selectedBinding.setButton(index);
            this.selectedBinding = null;
            //TODO resolve conflicts
            return true;
        }
        return false;
    }
}
