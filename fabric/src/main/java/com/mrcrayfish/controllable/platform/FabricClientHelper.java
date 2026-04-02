package com.mrcrayfish.controllable.platform;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import com.mrcrayfish.controllable.client.binding.context.GlobalContext;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.util.ReflectUtil;
import com.mrcrayfish.controllable.integration.ArchitecturySupport;
import com.mrcrayfish.controllable.platform.services.IClientHelper;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class FabricClientHelper implements IClientHelper
{
    @Override
    public boolean sendScreenInput(Screen screen, int key, int action, int modifiers)
    {
        KeyEvent event = new KeyEvent(key, GLFW.glfwGetKeyScancode(key), modifiers);
        if(action == GLFW.GLFW_RELEASE)
        {
            if(!ScreenKeyboardEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, event))
                return true;

            boolean handled = false;
            ScreenKeyboardEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, event);
            if(Controllable.isArchitecturyLoaded())
            {
                if(ArchitecturySupport.sendScreenKeyReleased(screen, key, event.scancode(), modifiers))
                {
                    handled = true;
                }
            }
            else if(screen.keyReleased(event))
            {
                handled = true;
            }
            ScreenKeyboardEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, event);
            return handled;
        }
        else if(action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        {
            screen.afterKeyboardAction();

            if(!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, event))
                return true;

            boolean handled = false;
            ScreenKeyboardEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, event);
            if(Controllable.isArchitecturyLoaded())
            {
                if(ArchitecturySupport.sendScreenKeyPressed(screen, key, event.scancode(), modifiers))
                {
                    handled = true;
                }
            }
            else if(screen.keyPressed(event))
            {
                handled = true;
            }
            ScreenKeyboardEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, event);
            return handled;
        }
        return false;
    }

    @Override
    public void sendMouseDrag(Screen screen, double dragX, double dragY, double finalMouseX, double finalMouseY, int activeButton)
    {
        Minecraft mc = Minecraft.getInstance();
        double finalDragX = dragX * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
        double finalDragY = dragY * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
        if(Controllable.isArchitecturyLoaded())
        {
            ArchitecturySupport.sendMouseDrag(screen, finalMouseX, finalMouseY, finalDragX, finalDragY, activeButton);
        }
        else
        {
            MouseButtonEvent event = new MouseButtonEvent(finalMouseX, finalMouseY, new MouseButtonInfo(activeButton, 0));
            screen.mouseDragged(event, finalDragX, finalDragY);
        }
    }

    @Override
    public boolean sendScreenMouseClick(Screen screen, double mouseX, double mouseY, int button, boolean doubleClick)
    {
        MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
        if(!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, event))
            return false;

        boolean handled;
        ScreenMouseEvents.beforeMouseClick(screen).invoker().beforeMouseClick(screen, event);
        if(Controllable.isArchitecturyLoaded())
        {
            handled = ArchitecturySupport.sendScreenMouseClick(screen, mouseX, mouseY, button);
        }
        else
        {
            handled = screen.mouseClicked(event, doubleClick);
        }
        handled |= ScreenMouseEvents.afterMouseClick(screen).invoker().afterMouseClick(screen, event, handled);
        return handled;
    }

    @Override
    public void sendScreenMouseReleased(Screen screen, double mouseX, double mouseY, int button)
    {
        MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
        if(!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, event))
            return;

        ScreenMouseEvents.beforeMouseRelease(screen).invoker().beforeMouseRelease(screen, event);

        boolean handled;
        if(Controllable.isArchitecturyLoaded())
        {
            handled = ArchitecturySupport.sendScreenMouseReleased(screen, mouseX, mouseY, button);
        }
        else
        {
            handled = screen.mouseReleased(event);
        }
        ScreenMouseEvents.afterMouseRelease(screen).invoker().afterMouseRelease(screen, event, handled);
    }

    @Override
    public List<GuiMessage.Line> getChatTrimmedMessages(ChatComponent chat)
    {
        return chat.trimmedMessages;
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
        var info = Minecraft.getInstance().mouseHandler.activeButton;
        return info != null ? info.button() : -1;
    }

    @Override
    public void setActiveMouseButton(int button)
    {
        if(button == -1)
        {
            Minecraft.getInstance().mouseHandler.activeButton = null;
        }
        else
        {
            Minecraft.getInstance().mouseHandler.activeButton = new MouseButtonInfo(button, 0);
        }
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
        mc.pickBlockOrEntity();
    }

    @Override
    public List<Renderable> getScreenRenderables(Screen screen)
    {
        return new ArrayList<>(Screens.getWidgets(screen));
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
    public int getAbstractListRowBottom(AbstractSelectionList<?> list, int index)
    {
        return list.getRowBottom(index);
    }

    @Override
    public int getAbstractListRowTop(AbstractSelectionList<?> list, int index)
    {
        return list.getRowTop(index);
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
        blank.cachedTooltip = lines;
        blank.splitWithLanguage = Language.getInstance();
    }

    @Override
    public int getKeyValue(KeyMapping mapping)
    {
        return KeyMappingHelper.getBoundKeyOf(mapping).getValue();
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
        Minecraft.getInstance().openChatScreen(ChatComponent.ChatMethod.MESSAGE);
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

    @Override
    public void updateMoveVector(ClientInput input, Vector2f vec)
    {
        ReflectUtil.updateMoveVector(input, vec);
    }

    @Override
    public boolean canLocalPlayerStartSprinting(LocalPlayer player)
    {
        return player.canStartSprinting();
    }

    @Override
    public boolean isOverlayRecipeButtonCraftable(AbstractWidget widget)
    {
        return widget instanceof OverlayRecipeComponent.OverlayRecipeButton btn && btn.isCraftable;
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
