package com.mrcrayfish.controllable.client.binding;

import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.context.GlobalContext;
import com.mrcrayfish.controllable.client.binding.context.InGameContext;
import com.mrcrayfish.controllable.client.binding.context.InScreenContext;
import com.mrcrayfish.controllable.client.binding.handlers.EmptyHandler;
import com.mrcrayfish.controllable.client.binding.handlers.MovementInputHandler;
import com.mrcrayfish.controllable.client.binding.handlers.OnPressAndReleaseHandler;
import com.mrcrayfish.controllable.client.binding.handlers.OnPressHandler;
import com.mrcrayfish.controllable.client.InputHandler;
import com.mrcrayfish.controllable.client.binding.handlers.impl.AttackHandler;
import com.mrcrayfish.controllable.client.binding.handlers.impl.DropHandler;
import com.mrcrayfish.controllable.client.binding.handlers.impl.SneakHandler;
import com.mrcrayfish.controllable.client.gui.screens.SettingsScreen;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.util.MouseHooks;
import com.mrcrayfish.controllable.platform.ClientServices;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class ButtonBindings
{
    public static final ButtonBinding JUMP = new ButtonBinding(Buttons.A, "key.jump", "key.categories.movement", InGameContext.INSTANCE, MovementInputHandler.create(context -> {
        context.input().jumping = true;
        context.controller().updateInputTime();
    }));

    public static final ButtonBinding SNEAK = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, "key.sneak", "key.categories.movement", InGameContext.INSTANCE, new SneakHandler());

    public static final ButtonBinding SPRINT = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "key.sprint", "key.categories.movement", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.player().ifPresent(player -> {
                if(context.minecraft().options.toggleSprint().get()) {
                    context.minecraft().options.keySprint.setDown(true);
                } else {
                    boolean canSprint = !player.isSprinting() && !player.hasEffect(MobEffects.BLINDNESS);
                    boolean hasRequiredFood = (float) player.getFoodData().getFoodLevel() > 6.0F || player.getAbilities().mayfly;
                    boolean hasImpulse = player.isUnderWater() ? player.input.hasForwardImpulse() : (double) player.input.forwardImpulse >= 0.8D;
                    boolean canSwimInFluid = ClientServices.CLIENT.canLocalPlayerSwimInFluid(player);
                    boolean usingItem = player.isUsingItem();
                    if(canSprint && canSwimInFluid && hasImpulse && hasRequiredFood && !usingItem) {
                        player.setSprinting(true);
                    }
                }
            });
        });
    }));

    public static final ButtonBinding OPEN_INVENTORY = new ButtonBinding(Buttons.Y, "controllable.key.open_inventory", "key.categories.inventory", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            Minecraft mc = context.minecraft();
            if(mc.gameMode != null && mc.player != null) {
                if(mc.gameMode.isServerControlledInventory()) {
                    mc.player.sendOpenInventory();
                } else {
                    mc.getTutorial().onOpenInventory();
                    mc.setScreen(new InventoryScreen(mc.player));
                }
            }
        });
    }));

    public static final ButtonBinding CLOSE_INVENTORY = new ButtonBinding(Buttons.Y, "controllable.key.close_inventory", "key.categories.inventory", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                screen.keyPressed(GLFW.GLFW_KEY_ESCAPE, GLFW.glfwGetKeyScancode(GLFW.GLFW_KEY_ESCAPE), 0);
            });
        });
    }));

    public static final ButtonBinding SWAP_HANDS = new ButtonBinding(Buttons.X, "key.swapOffhand", "key.categories.gameplay", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            Minecraft mc = context.minecraft();
            if(mc.player != null && !mc.player.isSpectator() && mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            }
        });
    }));

    public static final ButtonBinding DROP_ITEM = new ButtonBinding(Buttons.DPAD_DOWN, "key.drop", "key.categories.gameplay", InGameContext.INSTANCE, new DropHandler());

    public static final ButtonBinding ATTACK = new ButtonBinding(Buttons.RIGHT_TRIGGER, "key.attack", "key.categories.gameplay", InGameContext.INSTANCE, new AttackHandler());

    public static final ButtonBinding USE_ITEM = new ButtonBinding(Buttons.LEFT_TRIGGER, "key.use", "key.categories.gameplay", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.player().ifPresent(player -> {
                if(!player.isUsingItem()) {
                    ClientServices.CLIENT.startUseItem(context.minecraft());
                }
            });
        });
    }));

    public static final ButtonBinding PICK_BLOCK = new ButtonBinding(Buttons.DPAD_LEFT, "key.pickItem", "key.categories.gameplay", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            ClientServices.CLIENT.pickBlock(context.minecraft());
        });
    }));

    public static final ButtonBinding PLAYER_LIST = new ButtonBinding(Buttons.SELECT, "key.playerlist", "key.categories.multiplayer", InGameContext.INSTANCE, OnPressHandler.create(context -> Optional.of(() -> {})));

    public static final ButtonBinding TOGGLE_PERSPECTIVE = new ButtonBinding(Buttons.DPAD_UP, "key.togglePerspective", "key.categories.gameplay", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            Minecraft mc = Minecraft.getInstance();
            CameraType cameraType = mc.options.getCameraType();
            mc.options.setCameraType(cameraType.cycle());
            if(cameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson()) {
                mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
            }
        });
    }));

    public static final ButtonBinding SCREENSHOT = new ButtonBinding(-1, "key.screenshot", "key.categories.misc", GlobalContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            Minecraft mc = context.minecraft();
            Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), (component) -> {
                mc.execute(() -> mc.gui.getChat().addMessage(component));
            });
        });
    }));

    public static final ButtonBinding SCROLL_HOTBAR_LEFT = new ButtonBinding(Buttons.LEFT_BUMPER, "controllable.key.previous_hotbar_item", "key.categories.gameplay", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.player().ifPresent(player -> {
                player.getInventory().swapPaint(1);
            });
        });
    }));

    public static final ButtonBinding SCROLL_HOTBAR_RIGHT = new ButtonBinding(Buttons.RIGHT_BUMPER, "controllable.key.next_hotbar_item", "key.categories.gameplay", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.player().ifPresent(player -> {
                player.getInventory().swapPaint(-1);
            });
        });
    }));

    /*
        TODO implement unpause

        if(context.screen().get() instanceof PauseScreen) {
            context.minecraft().setScreen(null);
            return true;
        }
     */
    public static final ButtonBinding PAUSE_GAME = new ButtonBinding(Buttons.START, "controllable.key.pause_game", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.minecraft().pauseGame(false);
        });
    }));

    public static final ButtonBinding UNPAUSE_GAME = new ButtonBinding(Buttons.START, "controllable.key.unpause_game", "key.categories.misc", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                if(screen instanceof PauseScreen) {
                    context.minecraft().setScreen(null);
                }
            });
        });
    }));

    public static final ButtonBinding NEXT_CREATIVE_TAB = new ButtonBinding(Buttons.LEFT_BUMPER, "controllable.key.previous_creative_tab", "key.categories.inventory", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                if(screen instanceof CreativeModeInventoryScreen) {
                    InputHandler.navigateCreativeTabs((CreativeModeInventoryScreen) screen, -1);
                    context.minecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                } else if(screen instanceof RecipeUpdateListener listener) {
                    InputHandler.navigateRecipePage(listener.getRecipeBookComponent(), -1);
                }
            });
        });
    }));

    public static final ButtonBinding PREVIOUS_CREATIVE_TAB = new ButtonBinding(Buttons.RIGHT_BUMPER, "controllable.key.next_creative_tab", "key.categories.inventory", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                if(screen instanceof CreativeModeInventoryScreen) {
                    InputHandler.navigateCreativeTabs((CreativeModeInventoryScreen) screen, 1);
                    context.minecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                } else if(screen instanceof RecipeUpdateListener listener) {
                    InputHandler.navigateRecipePage(listener.getRecipeBookComponent(), 1);
                }
            });
        });
    }));

    public static final ButtonBinding NEXT_RECIPE_TAB = new ButtonBinding(Buttons.LEFT_TRIGGER, "controllable.key.previous_recipe_tab", "key.categories.inventory", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                if(screen.children().stream().anyMatch(listener -> listener instanceof TabNavigationBar)) {
                    InputHandler.navigateTabBar(screen, -1);
                } else if(screen instanceof RecipeUpdateListener listener) {
                    InputHandler.navigateRecipeTab(listener.getRecipeBookComponent(), -1);
                }
            });
        });
    }));

    public static final ButtonBinding PREVIOUS_RECIPE_TAB = new ButtonBinding(Buttons.RIGHT_TRIGGER, "controllable.key.next_recipe_tab", "key.categories.inventory", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                if(screen.children().stream().anyMatch(listener -> listener instanceof TabNavigationBar)) {
                    InputHandler.navigateTabBar(screen, 1);
                } else if(screen instanceof RecipeUpdateListener listener) {
                    InputHandler.navigateRecipeTab(listener.getRecipeBookComponent(), 1);
                }
            });
        });
    }));

    public static final ButtonBinding NAVIGATE_UP = new ButtonBinding(Buttons.DPAD_UP, "controllable.key.move_up", "key.categories.ui", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        if(!Controllable.getCursor().isEnabled())
            return Optional.empty();
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                InputHandler.navigateCursor(screen, InputHandler.Navigate.UP);
            });
        });
    }));

    public static final ButtonBinding NAVIGATE_DOWN = new ButtonBinding(Buttons.DPAD_DOWN, "controllable.key.move_down", "key.categories.ui", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        if(!Controllable.getCursor().isEnabled())
            return Optional.empty();
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                InputHandler.navigateCursor(screen, InputHandler.Navigate.DOWN);
            });
        });
    }));

    public static final ButtonBinding NAVIGATE_LEFT = new ButtonBinding(Buttons.DPAD_LEFT, "controllable.key.move_left", "key.categories.ui", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        if(!Controllable.getCursor().isEnabled())
            return Optional.empty();
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                InputHandler.navigateCursor(screen, InputHandler.Navigate.LEFT);
            });
        });
    }));

    public static final ButtonBinding NAVIGATE_RIGHT = new ButtonBinding(Buttons.DPAD_RIGHT, "controllable.key.move_right", "key.categories.ui", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        if(!Controllable.getCursor().isEnabled())
            return Optional.empty();
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                InputHandler.navigateCursor(screen, InputHandler.Navigate.RIGHT);
            });
        });
    }));

    public static final ButtonBinding PICKUP_ITEM = new ButtonBinding(Buttons.A, "controllable.key.pickup_item", "key.categories.inventory", InScreenContext.INSTANCE, OnPressAndReleaseHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                MouseHooks.invokeMouseClick(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                // If invokeMouseClick closed the screen, and the button is the same as the jump
                // button, the player will jump as soon as the screen is closed. To prevent this,
                // the jump binding is simply unpressed.
                if(context.minecraft().screen == null) {
                    if(ButtonBindings.JUMP.getButton() == ButtonBindings.PICKUP_ITEM.getButton()) {
                        ButtonBindings.JUMP.resetPressedState();
                    }
                }
                if(Config.CLIENT.options.quickCraft.get()) {
                    InputHandler.craftRecipeBookItem();
                }
            });
        });
    }, context -> {
        return context.screen().map(screen -> {
            MouseHooks.invokeMouseReleased(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT);
            return true;
        }).orElse(false);
    }));

    public static final ButtonBinding QUICK_MOVE = new ButtonBinding(Buttons.B, "controllable.key.quick_move", "key.categories.inventory", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                context.player().ifPresent(player -> {
                    if(player.inventoryMenu.getCarried().isEmpty()) {
                        MouseHooks.invokeMouseClick(screen, GLFW.GLFW_MOUSE_BUTTON_LEFT);
                        if(Config.CLIENT.options.quickMoveSound.get()) {
                            context.minecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WOODEN_BUTTON_CLICK_ON, 1.75F, 1.0F));
                        }
                    } else {
                        MouseHooks.invokeMouseReleased(screen, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                    }
                });
            });
        });
    }));

    public static final ButtonBinding SPLIT_STACK = new ButtonBinding(Buttons.X, "controllable.key.split_stack", "key.categories.inventory", InScreenContext.INSTANCE, OnPressAndReleaseHandler.create(context -> {
        return Optional.of(() -> {
            context.screen().ifPresent(screen -> {
                MouseHooks.invokeMouseClick(screen, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            });
        });
    }, context -> {
        return context.screen().map(screen -> {
            MouseHooks.invokeMouseReleased(screen, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            return true;
        }).orElse(false);
    }));

    public static final ButtonBinding SOCIAL_INTERACTIONS = new ButtonBinding(-1, "key.socialInteractions", "key.categories.multiplayer", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.player().ifPresent(player -> {
                Minecraft mc = context.minecraft();
                IntegratedServer server = mc.getSingleplayerServer();
                if(!mc.isLocalServer() && (server == null || !server.isPublished())) {
                    Component message = Component.translatable("multiplayer.socialInteractions.not_available");
                    player.displayClientMessage(message, true);
                    mc.getNarrator().sayNow(message);
                } else {
                    mc.setScreen(new SocialInteractionsScreen());
                }
            });
        });
    }));

    public static final ButtonBinding ADVANCEMENTS = new ButtonBinding(-1, "key.advancements", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.player().ifPresent(player -> {
                context.minecraft().setScreen(new AdvancementsScreen(player.connection.getAdvancements()));
            });
        });
    }));

    public static final ButtonBinding HIGHLIGHT_PLAYERS = new ButtonBinding(-1, "key.spectatorOutlines", "key.categories.misc", InGameContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding CINEMATIC_CAMERA = new ButtonBinding(-1, "key.smoothCamera", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            context.minecraft().options.smoothCamera = !context.minecraft().options.smoothCamera;
        });
    }));

    public static final ButtonBinding FULLSCREEN = new ButtonBinding(-1, "key.fullscreen", "key.categories.misc", GlobalContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            Minecraft mc = context.minecraft();
            mc.getWindow().toggleFullScreen();
            mc.options.fullscreen().set(mc.getWindow().isFullscreen());
            mc.options.save();
        });
    }));

    public static final ButtonBinding DEBUG_INFO = new ButtonBinding(-1, "controllable.key.debug_info", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> context.minecraft().getDebugOverlay().toggleOverlay());
    }));

    public static final ButtonBinding RADIAL_MENU = new ButtonBinding(Buttons.DPAD_RIGHT, "controllable.key.radial_menu", "key.categories.gameplay", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> {
            if(!context.simulated()) {
                Controllable.getRadialMenu().interact();
            }
        });
    }));

    public static final ButtonBinding HOTBAR_SLOT_1 = new ButtonBinding(-1, "controllable.key.hotbar_slot_1", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 0));
    }));

    public static final ButtonBinding HOTBAR_SLOT_2 = new ButtonBinding(-1, "controllable.key.hotbar_slot_2", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 1));
    }));

    public static final ButtonBinding HOTBAR_SLOT_3 = new ButtonBinding(-1, "controllable.key.hotbar_slot_3", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 2));
    }));

    public static final ButtonBinding HOTBAR_SLOT_4 = new ButtonBinding(-1, "controllable.key.hotbar_slot_4", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 3));
    }));

    public static final ButtonBinding HOTBAR_SLOT_5 = new ButtonBinding(-1, "controllable.key.hotbar_slot_5", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 4));
    }));

    public static final ButtonBinding HOTBAR_SLOT_6 = new ButtonBinding(-1, "controllable.key.hotbar_slot_6", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 5));
    }));

    public static final ButtonBinding HOTBAR_SLOT_7 = new ButtonBinding(-1, "controllable.key.hotbar_slot_7", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 6));
    }));

    public static final ButtonBinding HOTBAR_SLOT_8 = new ButtonBinding(-1, "controllable.key.hotbar_slot_8", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 7));
    }));

    public static final ButtonBinding HOTBAR_SLOT_9 = new ButtonBinding(-1, "controllable.key.hotbar_slot_9", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.navigateToHotbarSlot(context, 8));
    }));

    public static final ButtonBinding TOGGLE_CRAFT_BOOK = new ButtonBinding(Buttons.LEFT_THUMB_STICK, "controllable.key.toggle_craft_book", "key.categories.inventory", InScreenContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> InputHandler.toggleCraftBook(context));
    }));

    public static final ButtonBinding OPEN_CONTROLLABLE_SETTINGS = new ButtonBinding(-1, "controllable.key.open_controllable_settings", "key.categories.misc", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> context.minecraft().setScreen(new SettingsScreen(null, 1)));
    }));

    public static final ButtonBinding OPEN_CHAT = new ButtonBinding(-1, "key.chat", "key.categories.multiplayer", InGameContext.INSTANCE, OnPressHandler.create(context -> {
        return Optional.of(() -> ClientServices.CLIENT.openChatScreen(""));
    }));

    public static final ButtonBinding MOVE_CURSOR_UP = new ButtonBinding(Buttons.LEFT_THUMB_STICK_UP, "controllable.key.move_cursor_up", "key.categories.ui", InScreenContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding MOVE_CURSOR_DOWN = new ButtonBinding(Buttons.LEFT_THUMB_STICK_DOWN, "controllable.key.move_cursor_down", "key.categories.ui", InScreenContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding MOVE_CURSOR_LEFT = new ButtonBinding(Buttons.LEFT_THUMB_STICK_LEFT, "controllable.key.move_cursor_left", "key.categories.ui", InScreenContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding MOVE_CURSOR_RIGHT = new ButtonBinding(Buttons.LEFT_THUMB_STICK_RIGHT, "controllable.key.move_cursor_right", "key.categories.ui", InScreenContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding SCROLL_UP = new ButtonBinding(Buttons.RIGHT_THUMB_STICK_UP, "controllable.key.scroll_up", "key.categories.ui", InScreenContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding SCROLL_DOWN = new ButtonBinding(Buttons.RIGHT_THUMB_STICK_DOWN, "controllable.key.scroll_down", "key.categories.ui", InScreenContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding WALK_FORWARDS = new ButtonBinding(Buttons.LEFT_THUMB_STICK_UP, "key.forward", "key.categories.movement", InGameContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding WALK_BACKWARDS = new ButtonBinding(Buttons.LEFT_THUMB_STICK_DOWN, "key.back", "key.categories.movement", InGameContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding STRAFE_LEFT = new ButtonBinding(Buttons.LEFT_THUMB_STICK_LEFT, "key.left", "key.categories.movement", InGameContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding STRAFE_RIGHT = new ButtonBinding(Buttons.LEFT_THUMB_STICK_RIGHT, "key.right", "key.categories.movement", InGameContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding LOOK_UP = new ButtonBinding(Buttons.RIGHT_THUMB_STICK_UP, "controllable.key.look_up", "key.categories.movement", InGameContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding LOOK_DOWN = new ButtonBinding(Buttons.RIGHT_THUMB_STICK_DOWN, "controllable.key.look_down", "key.categories.movement", InGameContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding LOOK_LEFT = new ButtonBinding(Buttons.RIGHT_THUMB_STICK_LEFT, "controllable.key.look_left", "key.categories.movement", InGameContext.INSTANCE, EmptyHandler.INSTANCE);

    public static final ButtonBinding LOOK_RIGHT = new ButtonBinding(Buttons.RIGHT_THUMB_STICK_RIGHT, "controllable.key.look_right", "key.categories.movement", InGameContext.INSTANCE, EmptyHandler.INSTANCE);
}
