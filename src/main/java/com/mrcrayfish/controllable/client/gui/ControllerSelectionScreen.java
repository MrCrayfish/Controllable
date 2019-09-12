package com.mrcrayfish.controllable.client.gui;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.Mappings;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Author: MrCrayfish
 */
public class ControllerSelectionScreen extends Screen
{
    private int controllerCount;
    private ControllerManager manager;
    private ControllerList listControllers;
    private Screen previousScreen;
    private Button btnSelect;
    private Button btnConfigure;
    private Button btnCancel;

    public ControllerSelectionScreen(ControllerManager manager, Screen previousScreen)
    {
        super(new TranslationTextComponent("controllable.selectController.title"));
        this.manager = manager;
        this.previousScreen = previousScreen;
        this.controllerCount = manager.getNumControllers();
    }

    @Override
    protected void init()
    {
        this.listControllers = new ControllerList(this.manager, this.minecraft, this.width, this.height, 32, this.height - 44, 20);
        this.children.add(this.listControllers);
        this.btnSelect = this.addButton(new Button(this.width / 2 - 154, this.height - 32, 100, 20, I18n.format("controllable.gui.select"), this::handleSelect));
        this.btnConfigure = this.addButton(new Button(this.width / 2 - 50, this.height - 32, 100, 20, I18n.format("controllable.gui.configure"), this::handleConfigure));
        this.btnCancel = this.addButton(new Button(this.width / 2 + 54, this.height - 32, 100, 20, I18n.format("controllable.gui.cancel"), this::handleCancel));
        this.btnConfigure.active = this.listControllers.getSelected() != null;
        this.btnSelect.active = this.listControllers.getSelected() != null;
    }

    @Override
    public void tick()
    {
        if(this.controllerCount != this.manager.getNumControllers())
        {
            this.controllerCount = this.manager.getNumControllers();
            this.listControllers.reload();
            this.btnConfigure.active = this.listControllers.getSelected() != null;
            this.btnSelect.active = this.listControllers.getSelected() != null;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        this.listControllers.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.font, I18n.format("controllable.gui.title.select_controller"), this.width / 2, 20, 16777215);
        super.render(mouseX, mouseY, partialTicks);
    }

    private void handleSelect(Button button)
    {
        if(listControllers.getSelected() == null)
            return;

        ControllerIndex index = manager.getControllerIndex(listControllers.getSelected().getController().getIndex());
        Controller controller = new Controller(index);
        Mappings.updateControllerMappings(controller);
        Controllable.setController(controller);
        this.minecraft.displayGuiScreen(null);
    }

    private void handleConfigure(Button button)
    {
        this.minecraft.displayGuiScreen(new ControllerLayoutScreen(this));
    }

    private void handleCancel(Button button)
    {
        this.minecraft.displayGuiScreen(this.previousScreen);
    }
}
