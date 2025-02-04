package com.mrcrayfish.controllable.platform;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.mrcrayfish.controllable.client.binding.context.GlobalContext;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.util.ReflectUtil;
import com.mrcrayfish.controllable.integration.ArchitecturySupport;
import com.mrcrayfish.controllable.platform.services.IClientHelper;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
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
import net.minecraft.world.item.CreativeModeTabs;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FabricClientHelper implements IClientHelper
{
    @Override
    public boolean sendScreenInput(Screen screen, int key, int action, int modifiers)
    {
        AtomicBoolean handled = new AtomicBoolean(false);
        Screen.wrapScreenError(() -> {
            if(action == GLFW.GLFW_RELEASE) {
                if(!ScreenKeyboardEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, key, -1, modifiers)) {
                    handled.set(true);
                    return;
                }
                ScreenKeyboardEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, key, -1, modifiers);
                if(Controllable.isArchitecturyLoaded()) {
                    if(ArchitecturySupport.sendScreenKeyReleased(screen, key, -1, modifiers)) {
                        handled.set(true);
                    }
                } else if(screen.keyReleased(key, -1, modifiers)) {
                    handled.set(true);
                }
                ScreenKeyboardEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, key, -1, modifiers);
            } else if(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                screen.afterKeyboardAction();
                if(!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, key, -1, modifiers)) {
                    handled.set(true);
                    return;
                }
                ScreenKeyboardEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, key, -1, modifiers);
                if(Controllable.isArchitecturyLoaded()) {
                    if(ArchitecturySupport.sendScreenKeyPressed(screen, key, -1, modifiers)) {
                        handled.set(true);
                    }
                } else if(screen.keyPressed(key, -1, modifiers)) {
                    handled.set(true);
                }
                ScreenKeyboardEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, key, -1, modifiers);
            }
        }, "Controllable keyPressed event handler", screen.getClass().getCanonicalName());
        return handled.get();
    }

    @Override
    public void sendMouseDrag(Screen screen, double dragX, double dragY, double finalMouseX, double finalMouseY, int activeButton)
    {
        Minecraft mc = Minecraft.getInstance();
        double finalDragX = dragX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
        double finalDragY = dragY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
        Screen.wrapScreenError(() -> {
            if(Controllable.isArchitecturyLoaded()) {
                ArchitecturySupport.sendMouseDrag(screen, finalMouseX, finalMouseY, finalDragX, finalDragY, activeButton);
            } else {
                screen.mouseDragged(finalMouseX, finalMouseY, activeButton, finalDragX, finalDragY);
            }
        }, "Controllable mouseDragged event handler", screen.getClass().getCanonicalName());
    }

    @Override
    public void sendScreenMouseClick(Screen screen, double mouseX, double mouseY, int button)
    {
        Screen.wrapScreenError(() -> {
            if(!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, mouseX, mouseY, button)) {
                return;
            }
            ScreenMouseEvents.beforeMouseClick(screen).invoker().beforeMouseClick(screen, mouseX, mouseY, button);
            if(Controllable.isArchitecturyLoaded()) {
                ArchitecturySupport.sendScreenMouseClick(screen, mouseX, mouseY, button);
            } else {
                screen.mouseClicked(mouseX, mouseY, button);
            }
            ScreenMouseEvents.afterMouseClick(screen).invoker().afterMouseClick(screen, mouseX, mouseY, button);
        }, "Controllable mouseClicked event handler", screen.getClass().getCanonicalName());
    }

    @Override
    public void sendScreenMouseReleased(Screen screen, double mouseX, double mouseY, int button)
    {
        Screen.wrapScreenError(() -> {
            if(!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, mouseX, mouseY, button)) {
                return;
            }
            ScreenMouseEvents.beforeMouseRelease(screen).invoker().beforeMouseRelease(screen, mouseX, mouseY, button);
            if(Controllable.isArchitecturyLoaded()) {
                ArchitecturySupport.sendScreenMouseReleased(screen, mouseX, mouseY, button);
            } else {
                screen.mouseReleased(mouseX, mouseY, button);
            }
            ScreenMouseEvents.afterMouseRelease(screen).invoker().afterMouseRelease(screen, mouseX, mouseY, button);
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
        return screen.hoveredSlot;
    }

    @Override
    public int getAbstractListTop(AbstractSelectionList<?> list)
    {
        return list.getRectangle().top();
    }

    @Override
    public int getAbstractListBottom(AbstractSelectionList<?> list)
    {
        return list.getRectangle().bottom();
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
        return new ArrayList<>(Screens.getButtons(screen));
    }

    @Override
    public int getScreenTop(AbstractContainerScreen<?> screen)
    {
        return screen.topPos;
    }

    @Override
    public int getScreenLeft(AbstractContainerScreen<?> screen)
    {
        return screen.leftPos;
    }

    @Override
    public void gatherCreativeTabNavigationPoints(CreativeModeInventoryScreen screen, List<NavigationPoint> points)
    {
        CreativeModeTabs.tabs().forEach(creativeModeTab -> {
            if(screen.getPage(creativeModeTab) == screen.getCurrentPage()) {
                points.add(this.getCreativeTabPoint(screen, creativeModeTab));
            }
        });
    }

    @Override
    public boolean canLocalPlayerSwimInFluid(LocalPlayer player)
    {
        return !player.isInWater() || player.isUnderWater();
    }

    @Override
    public void scrollCreativeTabs(CreativeModeInventoryScreen screen, int dir)
    {
        if(dir > 0)
        {
            screen.switchToNextPage();
        }
        else
        {
            screen.switchToPreviousPage();
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
        return KeyBindingHelper.getBoundKeyOf(mapping).getValue();
    }

    @Override
    public void setKeyPressTime(KeyMapping mapping, int time)
    {
        ReflectUtil.setKeyPressTime(mapping, time);
    }

    @Override
    public BindingContext createBindingContext(KeyMapping mapping)
    {
        return GlobalContext.INSTANCE;
    }

    @Override
    public void sendKeyInputEvent(int key, int scanCode, int action, int modifiers)
    {
        // Do nothing on Fabric
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

    private BasicNavigationPoint getCreativeTabPoint(AbstractContainerScreen<?> screen, CreativeModeTab tab)
    {
        int guiLeft = ClientServices.CLIENT.getScreenLeft(screen);
        int guiTop = ClientServices.CLIENT.getScreenTop(screen);
        int column = tab.column();
        int width = 27;
        int height = 32;
        int x = guiLeft + width * column;
        int y = guiTop + (screen.imageHeight - 4);
        if(tab.isAlignedRight()) {
            x = guiLeft + screen.imageWidth - width * (7 - column) + 1;
        }
        if(tab.row() == CreativeModeTab.Row.TOP) {
            y = guiTop - width;
        }
        return new BasicNavigationPoint(x + width / 2.0, y + height / 2.0);
    }
}
