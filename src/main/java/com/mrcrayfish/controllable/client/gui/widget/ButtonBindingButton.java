package com.mrcrayfish.controllable.client.gui.widget;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.RenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingButton extends PressableButton
{
    private ButtonBinding binding;

    public ButtonBindingButton(int x, int y, ButtonBinding binding, Consumer<GuiButton> onPress)
    {
        super(x, y, 20, 20, "", onPress);
        this.binding = binding;
    }

    public ButtonBinding getBinding()
    {
        return this.binding;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        super.drawButton(mc, mouseX, mouseY, partialTicks);

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        int texU = this.binding.getButton() * 13;
        int texV = Controllable.getOptions().getControllerType().ordinal() * 13;
        int size = 13;
        Minecraft.getMinecraft().getTextureManager().bindTexture(RenderEvents.CONTROLLER_BUTTONS);
        this.drawTexturedModalRect(this.x + (this.width - size) / 2 + 1, this.y + 3, texU, texV, size, size);
    }
}
