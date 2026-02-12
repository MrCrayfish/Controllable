package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingMovementInput;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingOnRender;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingOnTick;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingPressed;
import com.mrcrayfish.controllable.client.binding.handlers.action.BindingReleased;
import com.mrcrayfish.controllable.client.binding.handlers.ButtonHandler;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.MovementInputContext;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.ListEntryNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.ListWidgetNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.Navigatable;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.SkipItem;
import com.mrcrayfish.controllable.client.gui.navigation.SlotNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.WidgetNavigationPoint;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.settings.AnalogMovement;
import com.mrcrayfish.controllable.client.settings.Thumbstick;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.EventHelper;
import com.mrcrayfish.controllable.client.util.InputHelper;
import com.mrcrayfish.controllable.client.util.MouseHooks;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.integration.EmiSupport;
import com.mrcrayfish.controllable.integration.JeiSupport;
import com.mrcrayfish.controllable.integration.ReiSupport;
import com.mrcrayfish.controllable.mixin.client.OverlayRecipeComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookPageAccessor;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.framework.api.event.ClientEvents;
import com.mrcrayfish.framework.api.event.TickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class InputHandler
{
    private static InputHandler instance;

    private final Multimap<BindingOnTick.TickPhase, PriorityHandler<BindingOnTick>> activeTickHandlers = TreeMultimap.create();
    private final Multimap<BindingOnRender.RenderPhase, PriorityHandler<BindingOnRender>> activeRenderHandlers = TreeMultimap.create();
    private final Set<PriorityHandler<BindingMovementInput>> activeMovementInputHandlers = new TreeSet<>();
    private @Nullable ButtonBinding activeVirtualBinding;
    private boolean initialized;

    @ApiStatus.Internal
    public InputHandler()
    {
        Preconditions.checkState(instance == null, "Only one instance of InputHandler is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void registerEvents()
    {
        if(!this.initialized)
        {
            TickEvents.START_CLIENT.register(this::onStartClickTick);
            TickEvents.END_CLIENT.register(this::onEndClickTick);
            TickEvents.START_PLAYER.register(this::onStartPlayerTick);
            TickEvents.END_PLAYER.register(this::onEndPlayerTick);
            ClientEvents.PLAYER_INPUT_UPDATE.register(this::updateInput);
            this.initialized = true;
        }
    }

    @Nullable
    public ButtonBinding getActiveVirtualBinding()
    {
        return this.activeVirtualBinding;
    }

    @ApiStatus.Internal
    public void handleButtonInput(Controller controller, int button, boolean state)
    {
        if(controller == null)
            return;

        controller.updateInputTime();

        if(state)
        {
            for(ButtonBinding binding : Controllable.getBindingRegistry().getBindingsForButton(button))
            {
                // For multi-button bindings, check if all required buttons are pressed
                if(binding.isMultiButton())
                {
                    boolean allPressed = true;
                    for(int requiredButton : binding.getButtons())
                    {
                        if(!controller.getTrackedButtonStates().getState(requiredButton))
                        {
                            allPressed = false;
                            break;
                        }
                    }
                    
                    if(!allPressed)
                        continue;
                }
                
                if(this.handleBindingPressed(controller, binding, false))
                    break;
            }
        }
        else
        {
            for(ButtonBinding binding : Controllable.getBindingRegistry().getBindingsForButton(button))
            {
                ButtonHandler handler = binding.getHandler();
                if(!(handler instanceof BindingPressed))
                    continue;

                if(!binding.isButtonDown())
                    continue;

                ButtonBinding.setButtonState(binding, false);

                if(!(handler instanceof BindingReleased released))
                    continue;

                // Cancel the handler if context is no longer valid
                if(!binding.getContext().isActive())
                    continue;

                Minecraft mc = Minecraft.getInstance();
                Context context = new Context(binding, controller, mc, mc.player, mc.level, mc.screen, false);
                released.handleReleased(context);
                return;
            }
        }
    }

    @ApiStatus.Internal
    public boolean handleBindingPressed(Controller controller, ButtonBinding binding, boolean virtual)
    {
        if(binding.isButtonDown())
            return true;

        ButtonHandler handler = binding.getHandler();
        if(!(handler instanceof BindingPressed pressed))
            return false;

        if(!binding.getContext().isActive())
            return false;

        Minecraft mc = Minecraft.getInstance();
        Context context = new Context(binding, controller, mc, mc.player, mc.level, mc.screen, virtual);
        Optional<Runnable> action = pressed.createPressedHandler(context);
        if(action.isEmpty())
            return false;

        /* TODO Reconsider to enqueue the action, and then run at the same time keyboard actions are
                handled. This will better align with the time actions are expected to run. */

        ButtonBinding.setButtonState(binding, true);
        action.get().run();

        if(handler instanceof BindingOnTick tick)
            this.activeTickHandlers.put(tick.phase(), new PriorityHandler<>(binding, tick));
        if(handler instanceof BindingOnRender tick)
            this.activeRenderHandlers.put(tick.phase(), new PriorityHandler<>(binding, tick));
        if(handler instanceof BindingMovementInput input)
            this.activeMovementInputHandlers.add(new PriorityHandler<>(binding, input));

        if(virtual)
        {
            this.activeVirtualBinding = binding;
        }

        return true;
    }

    private void handleActiveVirtualBinding()
    {
        ButtonBinding virtualBinding = this.activeVirtualBinding;
        if(virtualBinding == null)
            return;

        if(virtualBinding.isButtonDown() && ButtonBindings.RADIAL_MENU.isButtonDown())
            return;

        this.activeVirtualBinding = null;
        ButtonBinding.setButtonState(virtualBinding, false);

        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        if(!virtualBinding.getContext().isActive())
            return;

        if(!(virtualBinding.getHandler() instanceof BindingReleased released))
            return;

        Minecraft mc = Minecraft.getInstance();
        Context context = new Context(virtualBinding, controller, mc, mc.player, mc.level, mc.screen, false);
        released.handleReleased(context);
    }

    private void onStartClickTick()
    {
        this.handleActiveVirtualBinding();
        this.runTickHandler(BindingOnTick.TickPhase.START_CLIENT);
    }

    private void onEndClickTick()
    {
        this.runTickHandler(BindingOnTick.TickPhase.END_CLIENT);
    }

    private void onStartPlayerTick(Player player)
    {
        this.runTickHandler(BindingOnTick.TickPhase.START_PLAYER);
    }

    private void onEndPlayerTick(Player player)
    {
        this.runTickHandler(BindingOnTick.TickPhase.END_PLAYER);
    }

    private void runTickHandler(BindingOnTick.TickPhase type)
    {
        Minecraft mc = Minecraft.getInstance();
        Controller controller = Controllable.getController();
        this.activeTickHandlers.get(type).removeIf(handler -> {
            if(controller == null)
                return true;
            ButtonBinding binding = handler.binding();
            if(!binding.isButtonDown() || !binding.getContext().isActive())
                return true;
            Context context = new Context(handler.binding, controller, mc, mc.player, mc.level, mc.screen, false);
            handler.handler().handleTick(context);
            return false;
        });
    }

    private void updateInput(Player player, Input input)
    {
        LocalPlayer localPlayer = (LocalPlayer) player;
        if(localPlayer == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        Controller controller = Controllable.getController();
        this.activeMovementInputHandlers.removeIf(handler -> {
            if(controller == null)
                return true;
            ButtonBinding binding = handler.binding();
            if(!binding.isButtonDown() || !binding.getContext().isActive())
                return true;
            MovementInputContext context = new MovementInputContext(handler.binding, controller, mc, mc.player, mc.level, mc.screen, false, input);
            handler.handler().handleMovementInput(context);
            return false;
        });

        if(mc.screen == null && controller != null)
        {
            if((!Controllable.getRadialMenu().isVisible() || Config.CLIENT.options.radialThumbstick.get() != Thumbstick.LEFT) && !EventHelper.postMoveEvent())
            {
                float sneakSpeed = (float) localPlayer.getAttributeValue(Attributes.SNEAKING_SPEED);
                float sneakBonus = localPlayer.isMovingSlowly() ? sneakSpeed : 1.0F;
                float inputX = InputHelper.getCombinedPressedValue(controller, ButtonBindings.STRAFE_LEFT, ButtonBindings.STRAFE_RIGHT);
                float inputY = InputHelper.getCombinedPressedValue(controller, ButtonBindings.WALK_FORWARDS, ButtonBindings.WALK_BACKWARDS);

                AnalogMovement movement = Config.CLIENT.options.analogMovement.get();
                if(movement != AnalogMovement.ALWAYS)
                {
                    ServerData data = mc.getCurrentServer();
                    if(movement != AnalogMovement.LOCAL_ONLY || data != null && data.type() == ServerData.Type.OTHER)
                    {
                        inputX = Math.abs(inputX) >= 0.5F ? Math.signum(inputX) : 0;
                        inputY = Math.abs(inputY) >= 0.5F ? Math.signum(inputY) : 0;
                    }
                }

                if(Math.abs(inputY) > 0)
                {
                    input.up = inputY < 0;
                    input.down = inputY > 0;
                    input.forwardImpulse = -inputY;
                    input.forwardImpulse *= sneakBonus;
                    controller.updateInputTime();
                }

                float threshold = localPlayer.getVehicle() instanceof Boat ? 0.5F : 0;
                if(Math.abs(inputX) > threshold)
                {
                    input.right = inputX > 0;
                    input.left = inputX < 0;
                    input.leftImpulse = -inputX;
                    input.leftImpulse *= sneakBonus;
                    controller.updateInputTime();
                }
            }
        }
    }

    public static void navigateToHotbarSlot(Context context, int index)
    {
        if(context.screen().isEmpty()) {
            context.player().ifPresent(player -> {
                player.getInventory().selected = index;
            });
        }
    }

    public static void toggleCraftBook(Context context)
    {
        context.screen().ifPresent(screen -> {
            if(screen instanceof RecipeUpdateListener listener) {
                // Since no reference to craft book button, instead search for it and invoke press.
                ClientServices.CLIENT.getScreenRenderables(screen).stream().filter(widget -> {
                    return widget instanceof ImageButton btn && RecipeBookComponent.RECIPE_BUTTON_SPRITES.equals(ClientServices.CLIENT.getImageButtonResource(btn));
                }).findFirst().ifPresent(btn -> ((Button) btn).onPress());
                boolean visible = listener.getRecipeBookComponent().isVisible();
                Minecraft.getInstance()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, visible ? 1.0F : 0.95F));
            }
        });
    }

    /**
     * Cycles the third person view. Minecraft doesn't have this code in a convenient method.
     */
    private void cycleThirdPersonView()
    {
        Minecraft mc = Minecraft.getInstance();
        CameraType cameraType = mc.options.getCameraType();
        mc.options.setCameraType(cameraType.cycle());
        if(cameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson())
        {
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
        }
    }

    public static void navigateCreativeTabs(CreativeModeInventoryScreen screen, int dir)
    {
        ClientServices.CLIENT.scrollCreativeTabs(screen, dir);
    }

    public static void navigateRecipeTab(RecipeBookComponent recipeBook, int dir)
    {
        if(!recipeBook.isVisible())
            return;
        RecipeBookComponentAccessor recipeBookMixin = ((RecipeBookComponentAccessor) recipeBook);
        RecipeBookTabButton currentTab = recipeBookMixin.controllableGetCurrentTab();
        List<RecipeBookTabButton> tabs = recipeBookMixin.controllableGetRecipeTabs();
        int currentTabIndex = tabs.indexOf(currentTab);
        RecipeBookTabButton newTab = null;
        if(dir > 0)
        {
            for(int i = currentTabIndex + 1; i < tabs.size(); i++)
            {
                if(tabs.get(i).visible)
                {
                    newTab = tabs.get(i);
                    break;
                }
            }
        }
        else
        {
            for(int i = currentTabIndex - 1; i >= 0; i--)
            {
                if(tabs.get(i).visible)
                {
                    newTab = tabs.get(i);
                    break;
                }
            }
        }
        if(newTab != null)
        {
            currentTab.setStateTriggered(false);
            recipeBookMixin.controllableSetCurrentTab(newTab);
            newTab.setStateTriggered(true);
            recipeBookMixin.controllableUpdateCollections(true);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    public static void navigateRecipePage(RecipeBookComponent recipeBook, int dir)
    {
        if(!recipeBook.isVisible())
            return;
        RecipeBookPageAccessor page = (RecipeBookPageAccessor)((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeBookPage();
        if(dir > 0 && page.controllableGetForwardButton().visible || dir < 0 && page.controllableGetBackButton().visible)
        {
            int currentPage = page.controllableGetCurrentPage();
            page.controllableSetCurrentPage(currentPage + dir);
            page.controllableUpdateButtonsForPage();
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    public static void navigateTabBar(Screen screen, int direction)
    {
        TabNavigationBar bar = screen.children().stream().filter(listener -> listener instanceof TabNavigationBar).map(listener -> (TabNavigationBar) listener).findFirst().orElse(null);
        if(bar != null)
        {
            List<TabButton> buttons = new ArrayList<>();
            bar.children().forEach(listener ->
            {
                if(listener instanceof TabButton button)
                {
                    buttons.add(button);
                }
            });
            int selectedIndex = buttons.stream().filter(TabButton::isSelected).map(buttons::indexOf).findFirst().orElse(-1);
            if(selectedIndex != -1)
            {
                int newIndex = selectedIndex + direction;
                if(newIndex >= 0 && newIndex < buttons.size())
                {
                    bar.selectTab(newIndex, true);
                }
            }
        }
    }

    public static void navigateCursor(Screen screen, Navigate navigate)
    {
        if(!Controllable.getCursor().isEnabled())
            return;

        int cursorScreenX = Controllable.getCursor().getScreenX();
        int cursorScreenY = Controllable.getCursor().getScreenY();
        List<NavigationPoint> points = gatherNavigationPoints(screen, navigate, cursorScreenX, cursorScreenY);

        // Get only the points that are in the target direction
        points.removeIf(p -> !navigate.canMoveTo().test(p, cursorScreenX, cursorScreenY));
        if(points.isEmpty())
            return;

        Vector2d cursorVec = new Vector2d(cursorScreenX, cursorScreenY);

        // Finds the navigation point that requires the least axis offset. The axis offset depends on
        // the direction of navigation. If we are navigating up, we want to look at the opposite of
        // y-axis.
        Optional<NavigationPoint> minimumPointOptional = points.stream()
            .min(navigate.oppositeAxisOffsetComparator(cursorScreenX, cursorScreenY));

        double additionalDelta = 50;
        double minimumDelta = navigate.oppositeAxisOffset().apply(minimumPointOptional.get(), cursorVec) + additionalDelta;
        Optional<NavigationPoint> targetPointOptional = points.stream()
            .filter(point -> navigate.oppositeAxisOffset().apply(point, cursorVec) <= minimumDelta)
            .min(Comparator.comparing(p -> p.distanceTo(cursorScreenX, cursorScreenY)));
        if(targetPointOptional.isPresent())
        {
            NavigationPoint targetPoint = targetPointOptional.get();
            targetPoint.onNavigate();
            Minecraft mc = Minecraft.getInstance();
            mc.tell(() -> // Run next frame to allow lists to update widget positions
            {
                VirtualCursor cursor = Controllable.getCursor();

                // Perform an initial mouse moved. This fixes an issue when dragging items over
                // container slots, the initial slot the user was hovering does not get filled
                // with the item.
                int targetCursorX = cursor.getX();
                int targetCursorY = cursor.getY();
                MouseHooks.invokeMouseMoved(screen, targetCursorX, targetCursorY, 0, 0);

                // Jump the cursor to the target point
                int windowPointX = (int) (targetPoint.getX() * mc.getWindow().getGuiScale());
                int windowPointY = (int) (targetPoint.getY() * mc.getWindow().getGuiScale());
                cursor.jumpCursorTo(windowPointX, windowPointY);

                if(Config.CLIENT.options.navigateSound.get())
                {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 2.0F));
                }
                MouseHooks.invokeMouseMoved(screen, windowPointX, windowPointY, windowPointX - targetCursorX, windowPointY - targetCursorY);
                cursor.setVisible(!targetPoint.shouldHide());
            });
        }
    }

    private static List<NavigationPoint> gatherNavigationPoints(Screen screen, Navigate navigate, int cursorX, int cursorY)
    {
        List<NavigationPoint> points = new ArrayList<>();
        List<AbstractWidget> widgets = new ArrayList<>();

        if(screen instanceof AbstractContainerScreen<?> containerScreen)
        {
            int guiLeft = ClientServices.CLIENT.getScreenLeft(containerScreen);
            int guiTop = ClientServices.CLIENT.getScreenTop(containerScreen);
            for(Slot slot : containerScreen.getMenu().slots)
            {
                if(ClientServices.CLIENT.getSlotUnderMouse(containerScreen) == slot)
                    continue;
                int posX = guiLeft + slot.x + 8;
                int posY = guiTop + slot.y + 8;
                points.add(new SlotNavigationPoint(posX, posY, slot));
            }
        }

        for(GuiEventListener listener : screen.children())
        {
            gatherNavigationPointsFromListener(listener, navigate, cursorX, cursorY, points, null, null);
        }

        if(screen instanceof RecipeUpdateListener)
        {
            RecipeBookComponent recipeBook = ((RecipeUpdateListener) screen).getRecipeBookComponent();
            if(recipeBook.isVisible())
            {
                widgets.add(((RecipeBookComponentAccessor) recipeBook).controllableGetFilterButton());
                widgets.addAll(((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeTabs());

                RecipeBookPage page = ((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeBookPage();
                OverlayRecipeComponent overlay = ((RecipeBookPageAccessor) page).controllableGetOverlay();
                if(overlay.isVisible())
                {
                    widgets.addAll(((OverlayRecipeComponentAccessor) overlay).controllableGetRecipeButtons());
                }
                else
                {
                    RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) recipeBook).controllableGetRecipeBookPage();
                    widgets.addAll(((RecipeBookPageAccessor) recipeBookPage).controllableGetButtons());
                    widgets.add(((RecipeBookPageAccessor) recipeBookPage).controllableGetForwardButton());
                    widgets.add(((RecipeBookPageAccessor) recipeBookPage).controllableGetBackButton());
                }
            }
        }

        // TODO should I look into abstracting this?

        if(screen instanceof EnchantmentScreen enchantmentScreen)
        {
            int startX = ClientServices.CLIENT.getScreenLeft(enchantmentScreen) + 60;
            int startY = ClientServices.CLIENT.getScreenTop(enchantmentScreen) + 14;
            int itemWidth = 108;
            int itemHeight = 19;
            for(int i = 0; i < 3; i++)
            {
                double itemX = startX + itemWidth / 2.0;
                double itemY = startY + itemHeight * i + itemHeight / 2.0;
                points.add(new BasicNavigationPoint(itemX, itemY));
            }
        }

        if(screen instanceof StonecutterScreen stonecutter)
        {
            StonecutterMenu menu = stonecutter.getMenu();
            int startX = ClientServices.CLIENT.getScreenLeft(stonecutter) + 52;
            int startY = ClientServices.CLIENT.getScreenTop(stonecutter) + 14;
            int buttonWidth = 16;
            int buttonHeight = 18;
            int offsetIndex = ClientServices.CLIENT.getStonecutterStartIndex(stonecutter);
            for(int index = offsetIndex; index < offsetIndex + 12 && index < menu.getNumRecipes(); index++)
            {
                int buttonIndex = index - offsetIndex;
                int buttonX = startX + buttonIndex % 4 * buttonWidth;
                int buttonY = startY + buttonIndex / 4 * buttonHeight + 2;
                points.add(new BasicNavigationPoint(buttonX + buttonWidth / 2.0, buttonY + buttonHeight / 2.0));
            }
        }

        if(screen instanceof LoomScreen loom)
        {
            List<Holder<BannerPattern>> patterns = loom.getMenu().getSelectablePatterns();
            int startX = ClientServices.CLIENT.getScreenLeft(loom) + 60;
            int startY = ClientServices.CLIENT.getScreenTop(loom) + 13;
            int buttonWidth = 14;
            int buttonHeight = 14;
            int offsetRow = ClientServices.CLIENT.getLoomStartRow(loom);
            for(int i = 0; i < 4; i++)
            {
                for(int j = 0; j < 4; j++)
                {
                    int buttonIndex = (i + offsetRow) * 4 + j;
                    if(buttonIndex >= patterns.size())
                        break;
                    int buttonX = startX + j * buttonWidth;
                    int buttonY = startY + i * buttonHeight;
                    points.add(new BasicNavigationPoint(buttonX + buttonWidth / 2.0, buttonY + buttonHeight / 2.0));
                }
            }
        }

        for(AbstractWidget widget : widgets)
        {
            if(widget == null || widget.isHovered() || !widget.visible || !widget.active)
                continue;
            points.add(new WidgetNavigationPoint(widget));
        }

        if(screen instanceof CreativeModeInventoryScreen creativeScreen)
        {
            ClientServices.CLIENT.gatherCreativeTabNavigationPoints(creativeScreen, points);
        }

        if(Controllable.isJeiLoaded() && ClientHelper.isPlayingGame())
        {
            points.addAll(JeiSupport.getNavigationPoints());
        }

        if(Controllable.isEmiLoaded() && ClientHelper.isPlayingGame())
        {
            points.addAll(EmiSupport.getNavigationPoints(screen));
        }

        if(Controllable.isReiLoaded() && ClientHelper.isPlayingGame())
        {
            points.addAll(ReiSupport.getNavigationPoints(screen));
        }

        // Gather any additional points from event
        ControllerEvents.GATHER_NAVIGATION_POINTS.post().handle(points);

        return points;
    }

    private static void gatherNavigationPointsFromListener(GuiEventListener listener, Navigate navigate, int cursorX, int cursorY, List<NavigationPoint> points, @Nullable AbstractSelectionList<?> list, @Nullable GuiEventListener entry)
    {
        if(listener instanceof Navigatable navigatable)
        {
            navigatable.elements().forEach(child ->
            {
                gatherNavigationPointsFromListener(child, navigate, cursorX, cursorY, points, list, entry);
            });
        }
        else if(listener instanceof AbstractSelectionList<?> selectionList)
        {
            gatherNavigationPointsFromAbstractList(selectionList, navigate, cursorX, cursorY, points);
        }
        else if(listener instanceof TabNavigationBar navigationBar)
        {
            navigationBar.children().forEach(child ->
            {
                if(child instanceof TabButton button)
                {
                    createWidgetNavigationPoint(button, points, list, entry);
                }
            });
        }
        else if(listener instanceof ContainerEventHandler handler)
        {
            handler.children().forEach(child ->
            {
                gatherNavigationPointsFromListener(child, navigate, cursorX, cursorY, points, list, entry);
            });
        }
        else if(listener instanceof AbstractWidget widget && widget.active && widget.visible)
        {
            createWidgetNavigationPoint(widget, points, list, entry);
        }
    }

    private static void createWidgetNavigationPoint(AbstractWidget widget, List<NavigationPoint> points, @Nullable AbstractSelectionList<?> list, @Nullable GuiEventListener entry)
    {
        if(widget == null || widget.isHovered() || !widget.visible || !widget.active)
            return;
        if(list != null && entry != null)
        {
            points.add(new ListWidgetNavigationPoint(widget, list, entry));
        }
        else
        {
            points.add(new WidgetNavigationPoint(widget));
        }
    }

    private static void gatherNavigationPointsFromAbstractList(AbstractSelectionList<?> list, Navigate navigate, int cursorX, int cursorY, List<NavigationPoint> points)
    {
        List<? extends GuiEventListener> children = list.children();
        int dir = navigate == Navigate.UP ? -1 : 1;
        int itemHeight = ClientServices.CLIENT.getListItemHeight(list);
        for(int i = 0; i < children.size(); i++)
        {
            GuiEventListener entry = children.get(i);
            int rowTop = ClientServices.CLIENT.getAbstractListRowTop(list, i);
            int rowBottom = ClientServices.CLIENT.getAbstractListRowBottom(list, i);
            int listTop = ClientServices.CLIENT.getAbstractListTop(list);
            int listBottom = ClientServices.CLIENT.getAbstractListBottom(list);
            if(rowTop > listTop - itemHeight && rowBottom < listBottom + itemHeight)
            {
                if(navigate == Navigate.UP || navigate == Navigate.DOWN)
                {
                    if(!(entry instanceof SkipItem) || (i != 0 && i != children.size() - 1))
                    {
                        points.add(new ListEntryNavigationPoint(list, entry, i, dir));
                    }
                }
                gatherNavigationPointsFromListener(entry, navigate, cursorX, cursorY, points, list, entry);
            }
            else if(list.isMouseOver(cursorX, cursorY))
            {
                points.add(new ListEntryNavigationPoint(list, entry, i, dir));
            }
        }
    }

    public static void craftRecipeBookItem()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        if(!(mc.screen instanceof AbstractContainerScreen<?> screen) || !(mc.screen instanceof RecipeUpdateListener listener))
            return;

        if(!listener.getRecipeBookComponent().isVisible())
            return;

        if(!(screen.getMenu() instanceof RecipeBookMenu<?, ?>))
            return;

        RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) listener.getRecipeBookComponent()).controllableGetRecipeBookPage();
        RecipeButton recipeButton = ((RecipeBookPageAccessor) recipeBookPage).controllableGetButtons().stream().filter(RecipeButton::isHoveredOrFocused).findFirst().orElse(null);
        if(recipeButton != null)
        {
            RecipeBookMenu<?, ?> menu = (RecipeBookMenu<?, ?>) screen.getMenu();
            Slot slot = menu.getSlot(menu.getResultSlotIndex());
            int screenLeft = ClientServices.CLIENT.getScreenLeft(screen);
            int screenTop = ClientServices.CLIENT.getScreenTop(screen);
            if(menu.getCarried().isEmpty())
            {
                MouseHooks.invokeMouseClick(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT, screenLeft + slot.x + 8, screenTop + slot.y + 8);
            }
            else
            {
                MouseHooks.invokeMouseReleased(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT, screenLeft + slot.x + 8, screenTop + slot.y + 8);
            }
        }
    }

    public void clearActiveHandlers()
    {
        this.activeTickHandlers.clear();
        this.activeRenderHandlers.clear();
        this.activeMovementInputHandlers.clear();
    }

    public enum Navigate
    {
        UP((p, x, y) -> p.getY() < y, (p, v) -> Math.abs(p.getX() - v.x)),
        DOWN((p, x, y) -> p.getY() > y + 1, (p, v) -> Math.abs(p.getX() - v.x)),
        LEFT((p, x, y) -> p.getX() < x, (p, v) -> Math.abs(p.getY() - v.y)),
        RIGHT((p, x, y) -> p.getX() > x + 1, (p, v) -> Math.abs(p.getY() - v.y));

        private final NavigatePredicate predicate;
        private final BiFunction<? super NavigationPoint, Vector2d, Double> keyExtractor;

        Navigate(NavigatePredicate predicate, BiFunction<? super NavigationPoint, Vector2d, Double> keyExtractor)
        {
            this.predicate = predicate;
            this.keyExtractor = keyExtractor;
        }

        public NavigatePredicate canMoveTo()
        {
            return this.predicate;
        }

        public BiFunction<? super NavigationPoint, Vector2d, Double> oppositeAxisOffset()
        {
            return this.keyExtractor;
        }

        public Comparator<NavigationPoint> oppositeAxisOffsetComparator(int cursorX, int cursorY)
        {
            return Comparator.comparing(p -> this.keyExtractor.apply(p, new Vector2d(cursorX, cursorY)));
        }
    }

    private interface NavigatePredicate
    {
        boolean test(NavigationPoint point, int cursorScreenX, int cursorScreenY);
    }

    private static class PriorityHandler<T> implements Comparable<PriorityHandler<T>>
    {
        private final ButtonBinding binding;
        private final T handler;
        private final int priority;

        public PriorityHandler(ButtonBinding binding, T handler)
        {
            this.binding = binding;
            this.handler = handler;
            this.priority = binding.getContext().priority();
        }

        public ButtonBinding binding()
        {
            return this.binding;
        }

        public T handler()
        {
            return this.handler;
        }

        @Override
        public int compareTo(PriorityHandler<T> o)
        {
            int result = -Integer.compare(this.priority, o.priority);
            if(result == 0)
            {
                return this.binding.getDescription().compareTo(o.binding.getDescription());
            }
            return result;
        }

        @Override
        public final boolean equals(Object o)
        {
            if(!(o instanceof PriorityHandler<?> that))
                return false;
            return this.binding.equals(that.binding);
        }

        @Override
        public int hashCode()
        {
            return this.binding.hashCode();
        }

        @Override
        public String toString()
        {
            return this.priority + " " + this.binding.getDescription();
        }
    }
}
