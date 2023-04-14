package com.mrcrayfish.controllable.platform;

import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.ForgeCompatBindingContext;
import com.mrcrayfish.controllable.client.IBindingContext;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.util.ReflectUtil;
import com.mrcrayfish.controllable.event.ControllerEvent;
import com.mrcrayfish.controllable.event.GatherActionsEvent;
import com.mrcrayfish.controllable.event.GatherNavigationPointsEvent;
import com.mrcrayfish.controllable.event.GatherRadialMenuItemsEvent;
import com.mrcrayfish.controllable.event.RenderAvailableActionsEvent;
import com.mrcrayfish.controllable.event.RenderPlayerPreviewEvent;
import com.mrcrayfish.controllable.event.Value;
import com.mrcrayfish.controllable.integration.JeiSupport;
import com.mrcrayfish.controllable.platform.services.IClientHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.gui.CreativeTabsScreenPage;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class ForgeClientHelper implements IClientHelper
{
    public final Map<IKeyConflictContext, IBindingContext> keyContextMap = new Object2ObjectOpenHashMap<>();

    @Override
    @SuppressWarnings("deprecation")
    public List<RadialMenuHandler.AbstractRadialItem> sendLegacyGatherRadialMenuItemsEvent()
    {
        GatherRadialMenuItemsEvent event = new GatherRadialMenuItemsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        return event.getItems();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void sendLegacyGatherActionsEvent(Map<ButtonBinding, Action> actionMap, ActionVisibility visibility)
    {
        MinecraftForge.EVENT_BUS.post(new GatherActionsEvent(actionMap, visibility));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean sendLegacyRenderAvailableActionsEvent()
    {
        return MinecraftForge.EVENT_BUS.post(new RenderAvailableActionsEvent());
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean sendLegacyRenderPlayerPreviewEvent()
    {
        return MinecraftForge.EVENT_BUS.post(new RenderPlayerPreviewEvent());
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean sendLegacyControllerEventTurn(Controller controller, Value<Float> yawSpeed, Value<Float> pitchSpeed)
    {
        var event = new ControllerEvent.Turn(controller, yawSpeed.get(), pitchSpeed.get());
        if(MinecraftForge.EVENT_BUS.post(event))
            return true;
        yawSpeed.set(event.getYawSpeed());
        pitchSpeed.set(event.getPitchSpeed());
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean sendLegacyControllerEventButtonInput(Controller controller, Value<Integer> newButton, int button, boolean state)
    {
        var event = new ControllerEvent.ButtonInput(controller, button, state);
        if(MinecraftForge.EVENT_BUS.post(event))
            return true;
        newButton.set(event.getModifiedButton());
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean sendLegacyControllerEventButton(Controller controller)
    {
        return MinecraftForge.EVENT_BUS.post(new ControllerEvent.Button(controller));
    }

    @Override
    public boolean sendLegacyControllerEventMove(Controller controller)
    {
        return MinecraftForge.EVENT_BUS.post(new ControllerEvent.Move(controller));
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<NavigationPoint> sendLegacyGatherNavigationPoints()
    {
        GatherNavigationPointsEvent event = new GatherNavigationPointsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        return event.getPoints();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public float getGuiFarPlane()
    {
        return net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public boolean sendScreenInput(Screen screen, int key, int action, int modifiers)
    {
        boolean[] cancelled = new boolean[]{false};
        Screen.wrapScreenError(() ->
        {
            if(action == GLFW.GLFW_RELEASE)
            {
                cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onScreenKeyReleasedPre(screen, key, -1, modifiers);
                if(!cancelled[0]) cancelled[0] = screen.keyReleased(key, -1, modifiers);
                if(!cancelled[0]) cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onScreenKeyReleasedPost(screen, key, -1, modifiers);
            }
            else if(action == GLFW.GLFW_PRESS)
            {
                cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onScreenKeyPressedPre(screen, key, -1, modifiers);
                if(!cancelled[0])  cancelled[0] = screen.keyPressed(key, -1, modifiers);
                if(!cancelled[0]) cancelled[0] = net.minecraftforge.client.ForgeHooksClient.onScreenKeyPressedPost(screen, key, -1, modifiers);
            }

        }, "keyPressed event handler", screen.getClass().getCanonicalName());
        return cancelled[0];
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void sendMouseDrag(Screen screen, double dragX, double dragY, double finalMouseX, double finalMouseY, int activeButton)
    {
        Screen.wrapScreenError(() ->
        {
            Minecraft mc = screen.getMinecraft();
            double finalDragX = dragX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double finalDragY = dragY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
            if(net.minecraftforge.client.ForgeHooksClient.onScreenMouseDragPre(screen, finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY))
            {
                return;
            }
            if(((GuiEventListener) screen).mouseDragged(finalMouseX, finalMouseY, mc.mouseHandler.activeButton, finalDragX, finalDragY))
            {
                return;
            }
            net.minecraftforge.client.ForgeHooksClient.onScreenMouseDragPost(screen, finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY);
        }, "mouseDragged event handler", ((GuiEventListener) screen).getClass().getCanonicalName());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void sendScreenMouseClickPre(Screen screen, double mouseX, double mouseY, int button)
    {
        Screen.wrapScreenError(() ->
        {
            boolean cancelled = ForgeHooksClient.onScreenMouseClickedPre(screen, mouseX, mouseY, button);
            if(!cancelled)
            {
                cancelled = screen.mouseClicked(mouseX, mouseY, button);
                ForgeHooksClient.onScreenMouseClickedPost(screen, mouseX, mouseY, button, cancelled);
            }
        }, "mouseClicked event handler", screen.getClass().getCanonicalName());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void sendScreenMouseReleasedPre(Screen screen, double mouseX, double mouseY, int button)
    {
        Screen.wrapScreenError(() ->
        {
            boolean cancelled = ForgeHooksClient.onScreenMouseReleasedPre(screen, mouseX, mouseY, button);
            if(!cancelled)
            {
                cancelled = screen.mouseReleased(mouseX, mouseY, button);
                ForgeHooksClient.onScreenMouseReleasedPost(screen, mouseX, mouseY, button, cancelled);
            }
        }, "mouseReleased event handler", screen.getClass().getCanonicalName());
    }

    @Override
    public List<GuiMessage.Line> getChatTrimmedMessages(ChatComponent chat)
    {
        return chat.trimmedMessages;
    }

    @Override
    public List<NavigationPoint> getJeiNavigationPoints()
    {
        if(!ModList.get().isLoaded("jei"))
            return Collections.emptyList();
        return JeiSupport.getNavigationPoints();
    }

    @Override
    public int getMinecraftFramerateLimit()
    {
        return Minecraft.getInstance().getFramerateLimit();
    }

    @Override
    public Slot getSlotUnderMouse(AbstractContainerScreen<?> screen)
    {
        return screen.getSlotUnderMouse();
    }

    @Override
    public int getAbstractListTop(AbstractSelectionList<?> list)
    {
        return list.getTop();
    }

    @Override
    public int getAbstractListBottom(AbstractSelectionList<?> list)
    {
        return list.getBottom();
    }

    @Override
    public int getActiveMouseButton()
    {
        return Minecraft.getInstance().mouseHandler.activeButton;
    }

    @Override
    public void setActiveMouseButton(int button)
    {
        Minecraft.getInstance().mouseHandler.activeButton = button;
    }

    @Override
    public double getLastMouseEventTime()
    {
        return Minecraft.getInstance().mouseHandler.lastMouseEventTime;
    }

    @Override
    public void setLastMouseEventTime(double time)
    {
        Minecraft.getInstance().mouseHandler.lastMouseEventTime = time;
    }

    @Override
    public void startUseItem(Minecraft mc)
    {
        mc.startUseItem();
    }

    @Override
    public int getRightClickDelay(Minecraft mc)
    {
        return mc.rightClickDelay;
    }

    @Override
    public void startAttack(Minecraft mc)
    {
        mc.startAttack();
    }

    @Override
    public void pickBlock(Minecraft mc)
    {
        mc.pickBlock();
    }

    @Override
    public List<Renderable> getScreenRenderables(Screen screen)
    {
        return screen.renderables;
    }

    @Override
    public int getScreenTop(AbstractContainerScreen<?> screen)
    {
        return screen.getGuiTop();
    }

    @Override
    public int getScreenLeft(AbstractContainerScreen<?> screen)
    {
        return screen.getGuiLeft();
    }

    @Override
    public void gatherCreativeTabNavigationPoints(CreativeModeInventoryScreen screen, List<NavigationPoint> points)
    {
        CreativeTabsScreenPage page = screen.getCurrentPage();
        page.getVisibleTabs().forEach(tab -> points.add(this.getCreativeTabPoint(screen, screen.getCurrentPage(), tab)));
    }

    @Override
    public boolean canLocalPlayerSwimInFluid(LocalPlayer player)
    {
        return !(player.isInWater() || player.isInFluidType((fluidType, height) -> player.canSwimInFluidType(fluidType))) || (player.isUnderWater() || player.canStartSwimming());
    }

    @Override
    public void scrollCreativeTabs(CreativeModeInventoryScreen screen, int dir)
    {
        try
        {
            List<CreativeTabsScreenPage> pages = ObfuscationReflectionHelper.getPrivateValue(CreativeModeInventoryScreen.class, screen, "pages");
            if(pages != null)
            {
                if(dir > 0)
                {
                    screen.setCurrentPage(pages.get(Math.min(pages.indexOf(screen.getCurrentPage()) + 1, pages.size() - 1)));
                }
                else if(dir < 0)
                {
                    screen.setCurrentPage(pages.get(Math.max(pages.indexOf(screen.getCurrentPage()) - 1, 0)));
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public float getCreativeScrollOffset(CreativeModeInventoryScreen screen)
    {
        return ReflectUtil.getCreativeScrollOffset(screen);
    }

    @Override
    public void setCreativeScrollOffset(CreativeModeInventoryScreen screen, float offset)
    {
        ReflectUtil.setCreativeScrollOffset(screen, offset);
    }

    @Override
    public int getAbstractListRowBottom(AbstractSelectionList<?> list, int index)
    {
        return ReflectUtil.getAbstractListRowBottom(list, index);
    }

    @Override
    public int getAbstractListRowTop(AbstractSelectionList<?> list, int index)
    {
        return ReflectUtil.getAbstractListRowTop(list, index);
    }

    @Override
    public int getListItemHeight(AbstractSelectionList<?> list)
    {
        return ReflectUtil.getAbstractListItemHeight(list);
    }

    @Override
    public ResourceLocation getImageButtonResource(ImageButton btn)
    {
        return ReflectUtil.getImageButtonResource(btn);
    }

    @Override
    public void pushLinesToTooltip(Tooltip blank, List<FormattedCharSequence> lines)
    {
        ReflectUtil.pushLinesToTooltip(blank, lines);
    }

    @Override
    public int getKeyValue(KeyMapping mapping)
    {
        return mapping.getKey().getValue();
    }

    @Override
    public void setKeyPressTime(KeyMapping mapping, int time)
    {
        ReflectUtil.setKeyPressTime(mapping, time);
    }

    @Override
    public IBindingContext createBindingContext(KeyMapping mapping)
    {
        return this.keyContextMap.computeIfAbsent(mapping.getKeyConflictContext(), ForgeCompatBindingContext::new);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void sendKeyInputEvent(int key, int scanCode, int action, int modifiers)
    {
        ForgeHooksClient.onKeyInput(key, 0, action, modifiers);
    }

    @Override
    public void clickSlot(AbstractContainerScreen<?> screen, Slot slotIn, int slotId, int mouseButton, ClickType type)
    {
        ReflectUtil.clickSlot(screen, slotIn, slotId, mouseButton, type);
    }

    @Override
    public void addRenderableToScreen(Screen screen, Renderable renderable)
    {
        screen.renderables.add(renderable);
    }

    @Override
    public Component getOptionInstanceName(OptionInstance<Boolean> option)
    {
        return option.caption;
    }

    @Override
    public Tooltip getOptionInstanceTooltip(OptionInstance<Boolean> option)
    {
        return option.tooltip.apply(true);
    }

    @Override
    public void openChatScreen(String s)
    {
        Minecraft.getInstance().openChatScreen(s);
    }

    private BasicNavigationPoint getCreativeTabPoint(AbstractContainerScreen<?> screen, CreativeTabsScreenPage page, CreativeModeTab tab)
    {
        int guiLeft = ClientServices.CLIENT.getScreenLeft(screen);
        int guiTop = ClientServices.CLIENT.getScreenTop(screen);
        boolean topRow = page.isTop(tab);
        int column = page.getColumn(tab);
        int width = 28;
        int height = 32;
        int x = guiLeft + width * column;
        int y = guiTop;
        x = tab.isAlignedRight() ? guiLeft + screen.getXSize() - width * (6 - column) : (column > 0 ? x + column : x);
        y = topRow ? y - width : y + (screen.getYSize() - 4);
        return new BasicNavigationPoint(x + width / 2.0, y + height / 2.0);
    }
}
