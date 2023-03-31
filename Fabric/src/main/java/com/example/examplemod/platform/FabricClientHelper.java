package com.example.examplemod.platform;

import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.ControllerManager;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.IControllerList;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.event.Value;
import com.mrcrayfish.controllable.platform.services.IClientHelper;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FabricClientHelper implements IClientHelper
{
    @Override
    public List<RadialMenuHandler.AbstractRadialItem> sendLegacyGatherRadialMenuItemsEvent()
    {
        return Collections.emptyList();
    }

    @Override
    public void sendLegacyGatherActionsEvent(Map<ButtonBinding, Action> actionMap, ActionVisibility visibility) {}

    @Override
    public boolean sendLegacyRenderAvailableActionsEvent()
    {
        return false;
    }

    @Override
    public boolean sendLegacyRenderPlayerPreviewEvent()
    {
        return false;
    }

    @Override
    public boolean sendLegacyControllerEventTurn(Controller controller, Value<Float> yawSpeed, Value<Float> pitchSpeed)
    {
        return false;
    }

    @Override
    public boolean sendLegacyControllerEventButtonInput(Controller controller, Value<Integer> newButton, int button, boolean state)
    {
        return false;
    }

    @Override
    public boolean sendLegacyControllerEventButton(Controller controller)
    {
        return false;
    }

    @Override
    public boolean sendLegacyControllerEventMove(Controller controller)
    {
        return false;
    }

    @Override
    public List<NavigationPoint> sendLegacyGatherNavigationPoints()
    {
        return Collections.emptyList();
    }

    @Override
    public float getGuiFarPlane()
    {
        return 2000F;
    }

    @Override
    public boolean sendScreenInput(Screen screen, int key, int action, int modifiers)
    {
        return false;
    }

    @Override
    public void sendMouseDrag(Screen screen, double dragX, double dragY, double finalMouseX, double finalMouseY, int activeButton)
    {

    }

    @Override
    public void sendScreenMouseClickPre(Screen screen, double mouseX, double mouseY, int button)
    {

    }

    @Override
    public void sendScreenMouseReleasedPre(Screen screen, double mouseX, double mouseY, int button)
    {

    }

    @Override
    public List<GuiMessage.Line> getChatTrimmedMessages(ChatComponent chat)
    {
        return null;
    }

    @Override
    public IControllerList<?> createControllerList(ControllerManager manager, Minecraft minecraft, int width, int height)
    {
        return null;
    }

    @Override
    public List<NavigationPoint> getJeiNavigationPoints()
    {
        return null;
    }

    @Override
    public int getMinecraftFramerateLimit()
    {
        return 0;
    }

    @Override
    public Slot getSlotUnderMouse(AbstractContainerScreen<?> screen)
    {
        return null;
    }

    @Override
    public int getAbstractListTop(AbstractSelectionList<?> list)
    {
        return 0;
    }

    @Override
    public int getAbstractListBottom(AbstractSelectionList<?> list)
    {
        return 0;
    }

    @Override
    public int getActiveMouseButton()
    {
        return 0;
    }

    @Override
    public void setActiveMouseButton(int button)
    {

    }

    @Override
    public double getLastMouseEventTime()
    {
        return 0;
    }

    @Override
    public void setLastMouseEventTime(double time)
    {

    }

    @Override
    public void startUseItem(Minecraft mc)
    {

    }

    @Override
    public int getRightClickDelay(Minecraft mc)
    {
        return 0;
    }

    @Override
    public void startAttack(Minecraft mc)
    {

    }

    @Override
    public void pickBlock(Minecraft mc)
    {

    }

    @Override
    public List<Renderable> getScreenRenderables(Screen screen)
    {
        return null;
    }

    @Override
    public int getScreenTop(AbstractContainerScreen<?> screen)
    {
        return 0;
    }

    @Override
    public int getScreenLeft(AbstractContainerScreen<?> screen)
    {
        return 0;
    }

    @Override
    public void gatherCreativeTabNavigationPoints(CreativeModeInventoryScreen screen, List<NavigationPoint> points)
    {

    }

    @Override
    public boolean canLocalPlayerSwimInFluid(LocalPlayer player)
    {
        return false;
    }

    @Override
    public void scrollCreativeTabs(CreativeModeInventoryScreen screen, int dir)
    {

    }

    @Override
    public float getCreativeScrollOffset(CreativeModeInventoryScreen screen)
    {
        return 0;
    }

    @Override
    public void setCreativeScrollOffset(CreativeModeInventoryScreen screen, float offset)
    {

    }

    @Override
    public int getAbstractListRowBottom(AbstractSelectionList<?> list, int index)
    {
        return 0;
    }

    @Override
    public int getAbstractListRowTop(AbstractSelectionList<?> list, int index)
    {
        return 0;
    }

    @Override
    public int getListItemHeight(AbstractSelectionList<?> list)
    {
        return 0;
    }

    @Override
    public ResourceLocation getImageButtonResource(ImageButton btn)
    {
        return null;
    }
}
