package com.mrcrayfish.controllable.platform;

import com.mrcrayfish.controllable.client.binding.context.NeoForgeKeyContext;
import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.util.ReflectUtil;
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
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: MrCrayfish
 */
public class ForgeClientHelper implements IClientHelper
{
    public final Map<IKeyConflictContext, BindingContext> keyContextMap = new Object2ObjectOpenHashMap<>();

    @Override
    public float getGuiFarPlane()
    {
        return ClientHooks.getGuiFarPlane();
    }

    @Override
    public boolean sendScreenInput(Screen screen, int key, int action, int modifiers)
    {
        AtomicBoolean handled = new AtomicBoolean();
        Screen.wrapScreenError(() -> {
            if(action == GLFW.GLFW_RELEASE) {
                if(!ClientHooks.onScreenKeyReleasedPre(screen, key, -1, modifiers)) {
                    if(!screen.keyReleased(key, -1, modifiers)) {
                        if(!ClientHooks.onScreenKeyReleasedPost(screen, key, -1, modifiers)) {
                            return;
                        }
                    }
                }
                handled.set(true);
            } else if(action == GLFW.GLFW_PRESS) {
                if(!ClientHooks.onScreenKeyPressedPre(screen, key, -1, modifiers)) {
                    if(!screen.keyPressed(key, -1, modifiers)) {
                        if(!ClientHooks.onScreenKeyPressedPost(screen, key, -1, modifiers)) {
                            return;
                        }
                    }
                }
                handled.set(true);
            }
        }, "Controllable keyPressed event handler", screen.getClass().getCanonicalName());
        return handled.get();
    }

    @Override
    public void sendMouseDrag(Screen screen, double dragX, double dragY, double finalMouseX, double finalMouseY, int activeButton)
    {
        Screen.wrapScreenError(() -> {
            Minecraft mc = screen.getMinecraft();
            double finalDragX = dragX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double finalDragY = dragY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
            if(ClientHooks.onScreenMouseDragPre(screen, finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY))
                return;
            if(screen.mouseDragged(finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY))
                return;
            ClientHooks.onScreenMouseDragPost(screen, finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY);
        }, "Controllable mouseDragged event handler", ((GuiEventListener) screen).getClass().getCanonicalName());
    }

    @Override
    public void sendScreenMouseClick(Screen screen, double mouseX, double mouseY, int button)
    {
        Screen.wrapScreenError(() -> {
            if(!ClientHooks.onScreenMouseClickedPre(screen, mouseX, mouseY, button)){
                boolean handled = screen.mouseClicked(mouseX, mouseY, button);
                ClientHooks.onScreenMouseClickedPost(screen, mouseX, mouseY, button, handled);
            }
        }, "Controllable mouseClicked event handler", screen.getClass().getCanonicalName());
    }

    @Override
    public void sendScreenMouseReleased(Screen screen, double mouseX, double mouseY, int button)
    {
        Screen.wrapScreenError(() -> {
            if(!ClientHooks.onScreenMouseReleasedPre(screen, mouseX, mouseY, button)) {
                boolean handled = screen.mouseReleased(mouseX, mouseY, button);
                ClientHooks.onScreenMouseReleasedPost(screen, mouseX, mouseY, button, handled);
            }
        }, "Controllable mouseReleased event handler", screen.getClass().getCanonicalName());
    }

    @Override
    public List<GuiMessage.Line> getChatTrimmedMessages(ChatComponent chat)
    {
        return chat.trimmedMessages;
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
        return list.getY();
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
        return Minecraft.getInstance().mouseHandler.lastHandleMovementTime;
    }

    @Override
    public void setLastMouseEventTime(double time)
    {
        Minecraft.getInstance().mouseHandler.lastHandleMovementTime = time;
    }

    @Override
    public void startUseItem(Minecraft mc)
    {
        mc.startUseItem();
    }

    @Override
    public boolean startAttack(Minecraft mc)
    {
        return mc.startAttack();
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
    public WidgetSprites getImageButtonResource(ImageButton btn)
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
    public BindingContext createBindingContext(KeyMapping mapping)
    {
        return this.keyContextMap.computeIfAbsent(mapping.getKeyConflictContext(), NeoForgeKeyContext::new);
    }

    @Override
    public void sendKeyInputEvent(int key, int scanCode, int action, int modifiers)
    {
        ClientHooks.onKeyInput(key, 0, action, modifiers);
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

    @Override
    public int getStonecutterStartIndex(StonecutterScreen screen)
    {
        return ReflectUtil.getStonecutterStartIndex(screen);
    }

    @Override
    public int getLoomStartRow(LoomScreen screen)
    {
        return ReflectUtil.getLoomStartRow(screen);
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
