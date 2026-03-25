package com.mrcrayfish.controllable.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.binding.handlers.ButtonHandler;
import com.mrcrayfish.controllable.client.binding.handlers.action.*;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.Context;
import com.mrcrayfish.controllable.client.binding.handlers.action.context.MovementInputContext;
import com.mrcrayfish.controllable.client.gui.navigation.*;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.settings.AnalogMovement;
import com.mrcrayfish.controllable.client.settings.Thumbstick;
import com.mrcrayfish.controllable.client.util.*;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.integration.EmiSupport;
import com.mrcrayfish.controllable.integration.JeiSupport;
import com.mrcrayfish.controllable.integration.ReiSupport;
import com.mrcrayfish.controllable.mixin.client.OverlayRecipeComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookComponentAccessor;
import com.mrcrayfish.controllable.mixin.client.RecipeBookPageAccessor;
import com.mrcrayfish.controllable.platform.ClientServices;
import com.mrcrayfish.framework.api.event.FrameworkTickEvents;
import com.mrcrayfish.framework.api.event.client.FrameworkClientTickEvents;
import com.mrcrayfish.framework.api.event.client.FrameworkInputEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.*;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.*;
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
            FrameworkClientTickEvents.START_CLIENT.register(this::onStartClickTick);
            FrameworkClientTickEvents.END_CLIENT.register(this::onEndClickTick);
            FrameworkTickEvents.START_PLAYER.register(this::onStartPlayerTick);
            FrameworkTickEvents.END_PLAYER.register(this::onEndPlayerTick);
            FrameworkInputEvents.CLIENT_INPUT_UPDATE.register(this::updateInput);
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

    private void updateInput(Player player, ClientInput input)
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
            context.mutableInput().apply();
            return false;
        });

        if(mc.screen == null && controller != null)
        {
            if((!Controllable.getRadialMenu().isVisible() || Config.CLIENT.options.radialThumbstick.get() != Thumbstick.LEFT) && !EventHelper.postMoveEvent())
            {
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

                MutableClientInput mutableInput = new MutableClientInput(input);
                boolean up = false;
                boolean down = false;
                boolean left = false;
                boolean right = false;

                if(Math.abs(inputY) > 0)
                {
                    up = inputY < 0;
                    down = inputY > 0;
                    mutableInput.setForwardImpulse(-inputY);
                    controller.updateInputTime();
                }

                float threshold = localPlayer.getVehicle() instanceof AbstractBoat ? 0.5F : 0;
                if(Math.abs(inputX) > threshold)
                {
                    right = inputX > 0;
                    left = inputX < 0;
                    mutableInput.setLeftImpulse(-inputX);
                    controller.updateInputTime();
                }

                // Update key presses if there is a change
                if(up || down || left || right)
                {
                    mutableInput.setForward(up);
                    mutableInput.setBackward(down);
                    mutableInput.setLeft(left);
                    mutableInput.setRight(right);
                }

                mutableInput.apply();
            }
        }
    }

    public static void navigateToHotbarSlot(Context context, int index)
    {
        if(context.screen().isEmpty()) {
            context.player().ifPresent(player -> {
                player.getInventory().setSelectedSlot(index);
            });
        }
    }

    public static void toggleCraftBook(Context context)
    {
        context.screen().ifPresent(screen -> {
            Optional<RecipeBookComponent<?>> optional = findRecipeBookComponent(screen);
            if(optional.isEmpty())
                return;
            // Since no reference to craft book button, instead search for it and invoke press.
            ClientServices.CLIENT.getScreenRenderables(screen).stream().filter(widget -> {
                return widget instanceof ImageButton btn && RecipeBookComponent.RECIPE_BUTTON_SPRITES.equals(ClientServices.CLIENT.getImageButtonResource(btn));
            }).findFirst().ifPresent(btn -> ((Button) btn).onPress(new MouseButtonInfo(0, 0)));
            boolean visible = optional.get().isVisible();
            Minecraft.getInstance()
                .getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, visible ? 1.0F : 0.95F));
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

    private static Optional<RecipeBookComponent<?>> findRecipeBookComponent(Screen screen)
    {
        if(screen instanceof RecipeUpdateListener)
        {
            for(GuiEventListener listener : screen.children())
            {
                if(listener instanceof RecipeBookComponent<?> component)
                {
                    return Optional.of(component);
                }
            }
        }
        return Optional.empty();
    }

    public static void navigateRecipeTab(Screen screen, int dir)
    {
        Optional<RecipeBookComponent<?>> optional = findRecipeBookComponent(screen);
        if(optional.isEmpty())
            return;

        RecipeBookComponent<?> component = optional.get();
        if(!component.isVisible())
            return;

        RecipeBookComponentAccessor accessor = ((RecipeBookComponentAccessor) component);
        RecipeBookTabButton currentTab = accessor.controllableGetCurrentTab();
        List<RecipeBookTabButton> tabs = accessor.controllableGetRecipeTabs();
        int currentTabIndex = tabs.indexOf(currentTab);

        RecipeBookTabButton newTab = null;
        currentTabIndex += dir;
        while(currentTabIndex >= 0 && currentTabIndex < tabs.size())
        {
            if(tabs.get(currentTabIndex).visible)
            {
                newTab = tabs.get(currentTabIndex);
                break;
            }
            currentTabIndex += dir;
        }

        if(newTab != null)
        {
            currentTab.unselect();
            accessor.controllableSetCurrentTab(newTab);
            newTab.select();
            boolean filtering = accessor.controllableIsFiltering();
            accessor.controllableUpdateCollections(true, filtering);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    public static void navigateRecipePage(Screen screen, int dir)
    {
        Optional<RecipeBookComponent<?>> optional = findRecipeBookComponent(screen);
        if(optional.isEmpty())
            return;

        RecipeBookComponent<?> component = optional.get();
        if(!component.isVisible())
            return;

        RecipeBookPageAccessor page = (RecipeBookPageAccessor)((RecipeBookComponentAccessor) component).controllableGetRecipeBookPage();
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
            mc.schedule(() -> // Run next frame to allow lists to update widget positions
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
            Optional<RecipeBookComponent<?>> optional = findRecipeBookComponent(screen);
            if(optional.isPresent())
            {
                RecipeBookComponent<?> component = optional.get();
                if(component.isVisible())
                {
                    widgets.add(((RecipeBookComponentAccessor) component).controllableGetFilterButton());
                    widgets.addAll(((RecipeBookComponentAccessor) component).controllableGetRecipeTabs());

                    RecipeBookPage page = ((RecipeBookComponentAccessor) component).controllableGetRecipeBookPage();
                    OverlayRecipeComponent overlay = ((RecipeBookPageAccessor) page).controllableGetOverlay();
                    if(overlay.isVisible())
                    {
                        widgets.addAll(((OverlayRecipeComponentAccessor) overlay).controllableGetRecipeButtons());
                    }
                    else
                    {
                        RecipeBookPage recipeBookPage = ((RecipeBookComponentAccessor) component).controllableGetRecipeBookPage();
                        widgets.addAll(((RecipeBookPageAccessor) recipeBookPage).controllableGetButtons());
                        widgets.add(((RecipeBookPageAccessor) recipeBookPage).controllableGetForwardButton());
                        widgets.add(((RecipeBookPageAccessor) recipeBookPage).controllableGetBackButton());
                    }
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
            for(int index = offsetIndex; index < offsetIndex + 12 && index < menu.getVisibleRecipes().size(); index++)
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
            //points.addAll(EmiSupport.getNavigationPoints(screen));
        }

        if(Controllable.isReiLoaded() && ClientHelper.isPlayingGame())
        {
            // TODO 26.1 reimplement when possible
            //points.addAll(ReiSupport.getNavigationPoints(screen));
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

        Optional<RecipeBookComponent<?>> optional = findRecipeBookComponent(mc.screen);
        if(optional.isEmpty())
            return;

        RecipeBookComponent<?> component = optional.get();
        if(!component.isVisible())
            return;

        if(!(screen.getMenu() instanceof RecipeBookMenu))
            return;

        RecipeBookPage page = ((RecipeBookComponentAccessor) component).controllableGetRecipeBookPage();
        RecipeButton button = ((RecipeBookPageAccessor) page).controllableGetButtons().stream().filter(RecipeButton::isHoveredOrFocused).findFirst().orElse(null);
        if(button != null)
        {
            RecipeBookMenu menu = (RecipeBookMenu) screen.getMenu();
            Optional<Slot> result = menu.slots.stream()
                .filter(slot -> slot instanceof ResultSlot || slot instanceof FurnaceResultSlot) // TODO find a better solution
                .findFirst();
            result.ifPresent(slot -> {
                int screenLeft = ClientServices.CLIENT.getScreenLeft(screen);
                int screenTop = ClientServices.CLIENT.getScreenTop(screen);
                if(menu.getCarried().isEmpty()) {
                    MouseHooks.invokeMouseClick(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT, screenLeft + slot.x + 8, screenTop + slot.y + 8);
                } else {
                    MouseHooks.invokeMouseReleased(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT, screenLeft + slot.x + 8, screenTop + slot.y + 8);
                }
            });
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
