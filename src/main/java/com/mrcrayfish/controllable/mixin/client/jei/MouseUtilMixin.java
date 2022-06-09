package com.mrcrayfish.controllable.mixin.client.jei;

/**
 * JEI has a utility class to get the X and Y position of the mouse when it's not possible to
 * retrieve them. This causes issues when virtual mouse is turned on (which is most of the time).
 * This fixes compatibility with the mod.
 *
 * Author: MrCrayfish
 */
/*@Pseudo
@Mixin(MouseUtil.class)
public class MouseUtilMixin
{
    @Inject(method = "getX", at = @At(value = "TAIL"), remap = false, cancellable = true)
    private static void getX(CallbackInfoReturnable<Double> cir)
    {
        ControllerInput input = Controllable.getInput();
        if(isVirtualMouseActive(input))
        {
            Minecraft minecraft = Minecraft.getInstance();
            double mouseX = input.getVirtualMouseX() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth();
            cir.setReturnValue(mouseX);
        }
    }

    @Inject(method = "getY", at = @At(value = "TAIL"), remap = false, cancellable = true)
    private static void getY(CallbackInfoReturnable<Double> cir)
    {
        ControllerInput input = Controllable.getInput();
        if(isVirtualMouseActive(input))
        {
            Minecraft minecraft = Minecraft.getInstance();
            double mouseY = input.getVirtualMouseY() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight();
            cir.setReturnValue(mouseY);
        }
    }

    private static boolean isVirtualMouseActive(ControllerInput input)
    {
        return Controllable.getController() != null && Config.CLIENT.options.virtualMouse.get() && input.getLastUse() > 0;
    }
}*/
