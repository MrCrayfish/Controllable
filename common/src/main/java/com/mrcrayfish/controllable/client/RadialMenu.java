package com.mrcrayfish.controllable.client;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.gui.RadialMenuAction;
import com.mrcrayfish.controllable.client.gui.screens.RadialMenuConfigureScreen;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.settings.Thumbstick;
import com.mrcrayfish.controllable.util.Utils;
import com.mrcrayfish.framework.api.event.client.FrameworkClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Author: MrCrayfish
 */
public class RadialMenu
{
    private static final ResourceLocation TEXTURE = Utils.resource("textures/gui/controller.png");
    private static final int ANIMATE_DURATION = 5;
    private static RadialMenu instance;

    private boolean initialized;
    private boolean loaded;
    private boolean visible;
    private int animateTicks;
    private int prevAnimateTicks;
    private Set<RadialMenuAction> actions = new LinkedHashSet<>();
    private AbstractRadialItem settingsItem;
    private AbstractRadialItem closeItem;
    private List<AbstractRadialItem> allItems = new ArrayList<>();
    private List<AbstractRadialItem> leftItems = new ArrayList<>();
    private List<AbstractRadialItem> rightItems = new ArrayList<>();
    private AbstractRadialItem selected;

    @ApiStatus.Internal
    public RadialMenu()
    {
        Preconditions.checkState(instance == null, "Only one instance of RadialMenu is allowed");
        instance = this;
    }

    @ApiStatus.Internal
    public void registerEvents()
    {
        if(!this.initialized)
        {
            FrameworkClientTickEvents.START_CLIENT.register(this::onClientTickStart);
            FrameworkClientTickEvents.END_CLIENT.register(this::onClientTickEnd);
            this.initialized = true;
        }
    }

    public void load()
    {
        if(this.loaded)
            return;

        Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("radial_menu_items.json");
        if(Files.exists(path))
        {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()), Charsets.UTF_8)))
            {
                JsonArray bindings = new Gson().fromJson(reader, JsonArray.class);
                bindings.forEach(element ->
                {
                    JsonObject object = element.getAsJsonObject();
                    String key = GsonHelper.getAsString(object, "key");
                    String colorName = GsonHelper.getAsString(object, "color");
                    ButtonBinding binding = Controllable.getBindingRegistry().getBindingByDescriptionKey(key);
                    if(binding != null)
                    {
                        ChatFormatting color = ChatFormatting.getByName(colorName);
                        if(color == null || color.getColor() == null)
                        {
                            color = ChatFormatting.YELLOW;
                        }
                        this.actions.add(new RadialMenuAction(binding, color));
                    }
                });
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            this.actions.addAll(this.createDefaultActions());
            this.save();
        }

        this.loaded = true;
    }

    private void save()
    {
        JsonArray array = new JsonArray();
        this.actions.forEach(data -> {
            JsonObject object = new JsonObject();
            object.addProperty("key", data.getBinding().getDescription());
            object.addProperty("color", data.getColor().name());
            array.add(object);
        });
        try
        {
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(array);
            Path path = Utils.getConfigDirectory().resolve(Constants.MOD_ID).resolve("radial_menu_items.json");
            Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<RadialMenuAction> createDefaultActions()
    {
        List<RadialMenuAction> defaults = new ArrayList<>();
        defaults.add(new RadialMenuAction(ButtonBindings.OPEN_CONTROLLABLE_SETTINGS, ChatFormatting.BLUE));
        defaults.add(new RadialMenuAction(ButtonBindings.ADVANCEMENTS, ChatFormatting.YELLOW));
        defaults.add(new RadialMenuAction(ButtonBindings.SCREENSHOT, ChatFormatting.YELLOW));
        defaults.add(new RadialMenuAction(ButtonBindings.FULLSCREEN, ChatFormatting.YELLOW));
        return defaults;
    }

    @ApiStatus.Internal
    public void interact()
    {
        if(this.visible)
        {
            if(this.selected != null)
            {
                this.selected.onUseItem(this);
            }
        }
        else
        {
            this.load();
            this.setVisibility(true);
            this.populateAndConstruct();
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_TAKE_RESULT, 1.5F));
        }
    }

    public AbstractRadialItem getSelected()
    {
        return this.selected;
    }

    public LinkedHashSet<RadialMenuAction> getActions()
    {
        return new LinkedHashSet<>(this.actions);
    }

    public void setActions(Collection<RadialMenuAction> actions)
    {
        this.actions = new LinkedHashSet<>(actions);
        this.save();
        this.populateAndConstruct();
    }

    public void removeBinding(ButtonBinding binding)
    {
        if(this.actions.removeIf(data -> data.getBinding() == binding))
        {
            this.save();
            this.populateAndConstruct();
        }
    }

    private void setVisibility(boolean visible)
    {
        this.visible = visible;
    }

    private void clearAnimation()
    {
        this.animateTicks = 0;
        this.prevAnimateTicks = 0;
    }

    private void populateAndConstruct()
    {
        this.rightItems.clear();
        this.leftItems.clear();

        List<AbstractRadialItem> items = new ArrayList<>();
        this.actions.forEach(binding -> items.add(new ButtonBindingItem(binding)));
        //while(this.items.size() < MIN_ITEMS - 1) this.items.add(new EmptyRadialItem());
        //this.items.add(new RadialSettingsItem());

        int rightSize = items.size() / 2;
        for(int i = 0; i < rightSize; i++)
        {
            float gap = 180F / (rightSize + 1);
            AbstractRadialItem item = items.get(i);
            item.setAngle(gap * i + gap);
            this.rightItems.add(item);
        }

        int leftSize = items.size() - rightSize;
        for(int i = rightSize; i < items.size(); i++)
        {
            float gap = 180F / (leftSize + 1);
            AbstractRadialItem item = items.get(i);
            item.setAngle(360F - gap * (i - rightSize) - gap);
            this.leftItems.add(items.get(i));
        }

        // These are hardcoded
        this.settingsItem = new RadialSettingsItem();
        this.settingsItem.setAngle(0F);
        items.add(this.settingsItem);

        this.closeItem = new CloseRadialMenuItem();
        this.closeItem.setAngle(180F);
        items.add(this.closeItem);
        this.selected = this.closeItem;

        this.allItems = items;
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    private void onClientTickStart()
    {
        Controller controller = Controllable.getController();
        if(this.visible && (controller == null || !controller.isBeingUsed()))
        {
            this.setVisibility(false);
        }
    }

    public void onRenderEnd(GuiGraphics graphics, DeltaTracker tracker)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.options.hideGui || mc.screen != null)
            return;

        if(Controllable.getController() != null)
        {
            if(this.visible || this.animateTicks > 0 || this.prevAnimateTicks > 0)
            {
                this.renderRadialMenu(graphics, tracker);
            }
        }
    }

    private void onClientTickEnd()
    {
        this.prevAnimateTicks = this.animateTicks;

        if(this.visible)
        {
            if(this.animateTicks < ANIMATE_DURATION)
            {
                this.animateTicks++;
            }
        }
        else if(this.animateTicks > 0)
        {
            this.animateTicks--;
        }
    }

    private void renderRadialMenu(GuiGraphics graphics, DeltaTracker tracker)
    {
        this.updateSelected();

        Minecraft mc = Minecraft.getInstance();
        graphics.pose().pushMatrix();

        float animation = Mth.lerp(tracker.getGameTimeDeltaPartialTick(false), this.prevAnimateTicks, this.animateTicks) / 5F;
        float c1 = 1.70158F;
        float c3 = c1 + 1;
        animation = (float) (1 + c3 * Math.pow(animation - 1, 3) + c1 * Math.pow(animation - 1, 2));

        // Draw background
        graphics.fill(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0x78101010);

        graphics.pose().translate(0, -10);
        graphics.pose().translate((int) (mc.getWindow().getGuiScaledWidth() / 2F), (int) (mc.getWindow().getGuiScaledHeight() / 2F));

        //matrixStack.scale(animation, animation, animation);

        graphics.pose().pushMatrix();
        this.settingsItem.draw(graphics, mc, false, this.selected == this.settingsItem, animation);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        this.closeItem.draw(graphics, mc, false, this.selected == this.closeItem, animation);
        graphics.pose().popMatrix();

        this.drawRadialItems(this.rightItems, graphics, mc, animation);
        this.drawRadialItems(this.leftItems, graphics, mc, animation);

        graphics.pose().popMatrix();
    }

    // TODO draw minimised version if too many entries (aka only draw the action name, not the category too)
    private void drawRadialItems(List<AbstractRadialItem> items, GuiGraphics graphics, Minecraft mc, float animation)
    {
        for(int i = 0; i < items.size(); i++)
        {
            AbstractRadialItem item = items.get(i);
            graphics.pose().pushMatrix();
            if(i == 0) graphics.pose().translate(0, -10);
            if(i == items.size() - 1) graphics.pose().translate(0, 10);
            boolean left = item.angle >= 180F;
            float x = (float) Math.cos(Math.toRadians(item.angle - 90F)) * 70F;
            float y = (float) Math.sin(Math.toRadians(item.angle - 90F)) * 70F;
            graphics.pose().translate((int) x, (int) y);
            item.draw(graphics, mc, left, this.selected == item, animation);
            graphics.pose().popMatrix();
        }
    }

    private void updateSelected()
    {
        // Don't update if not visible (stops updating during closing animation)
        if(!this.visible)
            return;

        // Ignore if no controller plugged in
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        float threshold = 0.5F;
        float inputX = Config.CLIENT.options.radialThumbstick.get() == Thumbstick.RIGHT ? controller.getRThumbStickXValue() : controller.getLThumbStickXValue();
        float inputY = Config.CLIENT.options.radialThumbstick.get() == Thumbstick.RIGHT ? controller.getRThumbStickYValue() : controller.getLThumbStickYValue();

        // Don't update selected if thumbstick is not above a certain threshold
        if(Math.abs(inputX) <= threshold && Math.abs(inputY) <= threshold)
            return;

        // Finds the closest radial item based on the direction of the right controller thumbstick
        float selectedAngle = (float) (Mth.wrapDegrees(Math.toDegrees(Math.atan2(inputY, inputX)) - 90) + 180);
        Optional<AbstractRadialItem> closest = this.allItems.stream().min((o1, o2) -> Mth.degreesDifferenceAbs(o1.angle, selectedAngle) > Mth.degreesDifferenceAbs(o2.angle, selectedAngle) ? 1 : 0);
        if(closest.isEmpty())
            return;

        // Don't update if the closest is the same as the currently selected item
        if(closest.get() == this.selected)
            return;

        this.selected = closest.get();
        Minecraft mc = Minecraft.getInstance();

        if(Config.CLIENT.options.navigateSound.get())
        {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 1.5F));
        }
    }

    private Optional<AbstractRadialItem> getSelectedItem()
    {
        return Optional.empty();
    }

    /**
     * The base radial item
     */
    public abstract static class AbstractRadialItem
    {
        protected Component label;
        protected Component description;
        private float angle;

        protected AbstractRadialItem(Component label)
        {
            this(label, null);
        }

        protected AbstractRadialItem(Component label, Component description)
        {
            this.label = label;
            this.description = description;
        }

        public Component getLabel()
        {
            return this.label;
        }

        @Nullable
        public Component getDescription()
        {
            return this.description;
        }

        public boolean isEmpty()
        {
            return false;
        }

        public abstract void onUseItem(RadialMenu handler);

        protected abstract void draw(GuiGraphics graphics, Minecraft mc, boolean left, boolean selected, float animation);

        protected void playSound(SoundEvent event, float pitch)
        {
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch));
        }

        /**
         * Internal to determine the closest item
         */
        void setAngle(float angle)
        {
            this.angle = angle;
        }
    }

    /**
     * A simple radial item to close the radial menu
     */
    public static final class CloseRadialMenuItem extends AbstractRadialItem
    {
        private static final Component LABEL = Component.translatable("controllable.gui.close");

        public CloseRadialMenuItem()
        {
            super(Component.translatable("controllable.gui.radial.close"));
        }

        @Override
        public void onUseItem(RadialMenu handler)
        {
            handler.setVisibility(false);
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_TAKE_RESULT, 1.3F));
        }

        @Override
        protected void draw(GuiGraphics graphics, Minecraft mc, boolean left, boolean selected, float animation)
        {
            int color = selected ? 0xFFCCCCCC : mc.options.getBackgroundColor(0.7F);
            float alpha = ARGB.alpha(color) / 255F;
            float red = ARGB.red(color) / 255F;
            float green = ARGB.green(color) / 255F;
            float blue = ARGB.blue(color) / 255F;

            graphics.pose().translate(0, 100);

            alpha = Math.min(1.0F, alpha * animation);
            color = ARGB.colorFromFloat(red, green, blue, alpha);

            // Draw background
            // TODO 1.21.6
            graphics.fill(-14, -14, 14, -15, color);
            graphics.fill(-15, -14, 15, 14, color);
            graphics.fill(-14, 14, 14, 15, color);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, -10, -10, 98, 15, 20, 20, 10, 10, 256, 256);

            if(selected)
            {
                graphics.drawCenteredString(mc.font, LABEL, 0, -30, 0xFFFFFFFF);
            }
        }
    }

    /**
     * A simple radial item to close the radial menu
     */
    public static final class RadialSettingsItem extends AbstractRadialItem
    {
        private static final Component LABEL = Component.translatable("controllable.gui.configure");

        public RadialSettingsItem()
        {
            super(Component.translatable("controllable.gui.radial.settings"));
        }

        @Override
        public void onUseItem(RadialMenu handler)
        {
            handler.setVisibility(false);
            handler.clearAnimation();
            Minecraft.getInstance().setScreen(new RadialMenuConfigureScreen(null));
        }

        @Override
        protected void draw(GuiGraphics graphics, Minecraft mc, boolean left, boolean selected, float animation)
        {
            int color = selected ? 0xFFCCCCCC : mc.options.getBackgroundColor(0.7F);
            float alpha = ARGB.alpha(color) / 255F;
            float red = ARGB.red(color) / 255F;
            float green = ARGB.green(color) / 255F;
            float blue = ARGB.blue(color) / 255F;

            graphics.pose().translate(0, -90);

            alpha = Math.min(1.0F, alpha * animation);
            color = ARGB.colorFromFloat(red, green, blue, alpha);

            // TODO 1.21.6
            graphics.fill(-14, -14, 14, -15, color);
            graphics.fill(-15, -14, 15, 14, color);
            graphics.fill(-14, 14, 14, 15, color);

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, -10, -10, 88, 15, 20, 20, 10, 10, 256, 256);

            if(selected)
            {
                graphics.drawCenteredString(mc.font, LABEL, 0, 21, 0xFFFFFFFF);
            }
        }
    }

    /**
     * A radial item that takes a button binding. Using this item will virtually enables the button
     * binding and doesn't require the real assigned button to be pressed. This also works for
     * bindings that don't have a button bound to them.
     */
    private static class ButtonBindingItem extends AbstractRadialItem
    {
        public RadialMenuAction entry;

        public ButtonBindingItem(RadialMenuAction entry)
        {
            super(Component.translatable(entry.getBinding().getLabelKey()).withStyle(entry.getColor()), Component.translatable(entry.getBinding().getCategory()));
            this.entry = entry;
        }

        @Override
        public void onUseItem(RadialMenu radialMenu)
        {
            radialMenu.setVisibility(false);
            radialMenu.clearAnimation();
            Controller controller = Controllable.getController();
            if(controller != null)
            {
                Controllable.getInputHandler().handleBindingPressed(controller, this.entry.getBinding(), true);
            }
        }

        @Override
        protected void draw(GuiGraphics graphics, Minecraft mc, boolean left, boolean selected, float animation)
        {
            graphics.pose().pushMatrix();

            int color = selected ? 0xFFCCCCCC : mc.options.getBackgroundColor(0.7F);
            float alpha = ARGB.alpha(color) / 255F;
            float red = ARGB.red(color) / 255F;
            float green = ARGB.green(color) / 255F;
            float blue = ARGB.blue(color) / 255F;

            float start = 0;
            float end = 150F;
            alpha *= animation;

            if(left)
            {
                start -= 150;
                end -= 150;
            }

            start *= (left ? animation : 1);
            end *= (left ? 1 : animation);

            graphics.pose().translate((1.0F - animation) * (left ? -20 : 20), 0);

            // Draw background
            // TODO 1.21.6
            graphics.fill((int) (start + 1), -15, (int) (end - 1), -14, color);
            graphics.fill((int) start, -14, (int) end, 14, color);
            graphics.fill((int) (start + 1), 14, (int) (end - 1), 15, color);

            // Middle
//            consumer.addVertex(poseStack.last().pose(), start, -14, 0).setColor(red, green, blue, left ? 0 : alpha);
//            consumer.addVertex(poseStack.last().pose(), start, 14, 0).setColor(red, green, blue, left ? 0 : alpha);
//            consumer.addVertex(poseStack.last().pose(), end, 14, 0).setColor(red, green, blue, left ? alpha : 0);
//            consumer.addVertex(poseStack.last().pose(), end, -14, 0).setColor(red, green, blue, left ? alpha : 0);

            // Bottom (offset by 1)
//            consumer.addVertex(poseStack.last().pose(), start + 1, 14, 0).setColor(red, green, blue, left ? 0 : alpha);
//            consumer.addVertex(poseStack.last().pose(), start + 1, 15, 0).setColor(red, green, blue, left ? 0 : alpha);
//            consumer.addVertex(poseStack.last().pose(), end - 1, 15, 0).setColor(red, green, blue, left ? alpha : 0);
//            consumer.addVertex(poseStack.last().pose(), end - 1, 14, 0).setColor(red, green, blue, left ? alpha : 0);

            if(this.label != null)
            {
                int offset = !left ? 5 : -mc.font.width(this.label) - 5;
                graphics.drawString(mc.font, this.label, offset, -10, 0xFFFFFFFF);
            }

            if(this.description != null)
            {
                int offset = !left ? 5 : -mc.font.width(this.description) - 5;
                graphics.drawString(mc.font, this.description, offset, 2, 0xFFFFFFFF);
            }

            graphics.pose().popMatrix();
        }
    }
}
