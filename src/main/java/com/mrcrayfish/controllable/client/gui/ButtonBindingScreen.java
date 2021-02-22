package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.widget.PressableButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingScreen extends GuiScreen
{
    private GuiScreen parentScreen;
    private GuiButton buttonReset;
    private ButtonBindingList bindingList;
    private ButtonBinding selectedBinding = null;

    protected ButtonBindingScreen(GuiScreen parentScreen)
    {
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
    public void initGui()
    {
        this.bindingList = new ButtonBindingList(this, this.mc, this.width + 10, this.height, 32, this.height - 44, 20);

        this.buttonReset = this.addButton(new PressableButton(this.width / 2 - 155, this.height - 29, 150, 20, I18n.format("controllable.gui.resetBinds"), (button) -> {
            BindingRegistry registry = BindingRegistry.getInstance();
            registry.getBindings().forEach(ButtonBinding::reset);
            registry.resetBindingHash();
            registry.save();
        }));
        this.buttonReset.enabled = BindingRegistry.getInstance().getBindings().stream().noneMatch(ButtonBinding::isDefault);

        this.addButton(new PressableButton(this.width / 2 - 155 + 160, this.height - 29, 150, 20, I18n.format("gui.done"), (button) -> {
            this.mc.displayGuiScreen(this.parentScreen);
            BindingRegistry registry = BindingRegistry.getInstance();
            registry.resetBindingHash();
            registry.save();
        }));
    }

    @Override
    public void updateScreen()
    {
        this.buttonReset.enabled = !BindingRegistry.getInstance().getBindings().stream().allMatch(ButtonBinding::isDefault);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        //draw title
        this.drawDefaultBackground();
        this.bindingList.drawScreen(this.selectedBinding == null ? mouseX : -1, this.selectedBinding == null ? mouseY : -1, partialTicks);
        this.drawCenteredString(this.mc.fontRenderer, new TextComponentTranslation("controllable.gui.title.button_binding").getFormattedText(), this.width / 2, 20, 0xFFFFFF);
        super.drawScreen(this.selectedBinding == null ? mouseX : -1, this.selectedBinding == null ? mouseY : -1, partialTicks);

        if(this.selectedBinding != null)
        {
            GlStateManager.disableDepth();
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
            this.drawCenteredString(this.mc.fontRenderer, new TextComponentTranslation("controllable.gui.layout.press_button").getFormattedText(), this.width / 2, this.height / 2, 0xFFFFFFFF);
            GlStateManager.enableDepth();
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.bindingList.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.bindingList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        this.bindingList.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char c, int code) throws IOException
    {
        if(code == Keyboard.KEY_ESCAPE && this.selectedBinding != null)
        {
            this.selectedBinding = null;
            return;
        }
        super.keyTyped(c, code);
    }

    public boolean processButton(int index)
    {
        if(this.selectedBinding != null)
        {
            this.selectedBinding.setButton(index);
            this.selectedBinding = null;
            BindingRegistry registry = BindingRegistry.getInstance();
            registry.resetBindingHash();
            registry.save();
            return true;
        }
        return false;
    }
}
