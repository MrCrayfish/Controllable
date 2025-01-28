package com.mrcrayfish.controllable.platform.services;

import com.mrcrayfish.controllable.client.binding.IBindingContext;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public interface IClientHelper
{
    default float getGuiFarPlane()
    {
        return 2000F;
    }

    boolean sendScreenInput(Screen screen, int key, int action, int modifiers);

    void sendMouseDrag(Screen screen, double dragX, double dragY, double finalMouseX, double finalMouseY, int activeButton);

    void sendScreenMouseClickPre(Screen screen, double mouseX, double mouseY, int button);

    void sendScreenMouseReleasedPre(Screen screen, double mouseX, double mouseY, int button);

    List<GuiMessage.Line> getChatTrimmedMessages(ChatComponent chat);

    List<NavigationPoint> getJeiNavigationPoints();

    int getMinecraftFramerateLimit();

    Slot getSlotUnderMouse(AbstractContainerScreen<?> screen);

    int getAbstractListTop(AbstractSelectionList<?> list);

    int getAbstractListBottom(AbstractSelectionList<?> list);

    int getActiveMouseButton();

    void setActiveMouseButton(int button);

    double getLastMouseEventTime();

    void setLastMouseEventTime(double time);

    void startUseItem(Minecraft mc);

    int getRightClickDelay(Minecraft mc);

    void startAttack(Minecraft mc);

    void pickBlock(Minecraft mc);

    List<Renderable> getScreenRenderables(Screen screen);

    int getScreenTop(AbstractContainerScreen<?> screen);

    int getScreenLeft(AbstractContainerScreen<?> screen);

    void gatherCreativeTabNavigationPoints(CreativeModeInventoryScreen screen, List<NavigationPoint> points);

    boolean canLocalPlayerSwimInFluid(LocalPlayer player);

    void scrollCreativeTabs(CreativeModeInventoryScreen screen, int dir);

    float getCreativeScrollOffset(CreativeModeInventoryScreen screen);

    void setCreativeScrollOffset(CreativeModeInventoryScreen screen, float offset);

    int getAbstractListRowBottom(AbstractSelectionList<?> list, int index);

    int getAbstractListRowTop(AbstractSelectionList<?> list, int index);

    int getListItemHeight(AbstractSelectionList<?> list);

    ResourceLocation getImageButtonResource(ImageButton btn);

    void pushLinesToTooltip(Tooltip blank, List<FormattedCharSequence> lines);

    int getKeyValue(KeyMapping mapping);

    void setKeyPressTime(KeyMapping mapping, int time);

    IBindingContext createBindingContext(KeyMapping mapping);

    void sendKeyInputEvent(int key, int scanCode, int action, int modifiers);

    void clickSlot(AbstractContainerScreen<?> screen, Slot slotIn, int slotId, int mouseButton, ClickType type);

    void addRenderableToScreen(Screen screen, Renderable renderable);

    Component getOptionInstanceName(OptionInstance<Boolean> option);

    Tooltip getOptionInstanceTooltip(OptionInstance<Boolean> option);

    void openChatScreen(String s);

    int getStonecutterStartIndex(StonecutterScreen stonecutter);

    int getLoomStartRow(LoomScreen loom);
}
