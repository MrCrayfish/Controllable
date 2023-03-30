package com.mrcrayfish.controllable.client;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.gui.ButtonBindingData;
import com.mrcrayfish.controllable.client.gui.screens.RadialMenuConfigureScreen;
import com.mrcrayfish.controllable.event.GatherRadialMenuItemsEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * Author: MrCrayfish
 */
public class RadialMenuHandler
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/controller.png");
    private static final int ANIMATE_DURATION = 5;

    private static RadialMenuHandler instance;

    public static RadialMenuHandler instance()
    {
        if(instance == null)
        {
            instance = new RadialMenuHandler();
        }
        return instance;
    }

    private boolean loaded;
    private boolean visible;
    private int animateTicks;
    private int prevAnimateTicks;
    private Set<ButtonBindingData> bindings = new LinkedHashSet<>();
    private AbstractRadialItem settingsItem;
    private AbstractRadialItem closeItem;
    private List<AbstractRadialItem> allItems = new ArrayList<>();
    private List<AbstractRadialItem> leftItems = new ArrayList<>();
    private List<AbstractRadialItem> rightItems = new ArrayList<>();
    private AbstractRadialItem selected;

    private RadialMenuHandler() {}

    private void load()
    {
        if(this.loaded)
            return;

        File file = new File(Controllable.getConfigFolder(), "controllable/radial_menu_items.json");
        if(file.exists())
        {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)))
            {
                JsonArray bindings = new Gson().fromJson(reader, JsonArray.class);
                bindings.forEach(element ->
                {
                    JsonObject object = element.getAsJsonObject();
                    String key = GsonHelper.getAsString(object, "key");
                    String colorName = GsonHelper.getAsString(object, "color");
                    ButtonBinding binding = BindingRegistry.getInstance().getBindingByDescriptionKey(key);
                    if(binding != null)
                    {
                        ChatFormatting color = ChatFormatting.getByName(colorName);
                        if(color == null || color.getColor() == null)
                        {
                            color = ChatFormatting.YELLOW;
                        }
                        this.bindings.add(new ButtonBindingData(binding, color));
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
            this.bindings.addAll(this.getBindings());
            this.save();
        }

        this.loaded = true;
    }

    private void save()
    {
        JsonArray array = new JsonArray();
        this.bindings.forEach(data -> {
            JsonObject object = new JsonObject();
            object.addProperty("key", data.getBinding().getDescription());
            object.addProperty("color", data.getColor().name());
            array.add(object);
        });

        String json = new GsonBuilder().setPrettyPrinting().create().toJson(array);
        File file = new File(Controllable.getConfigFolder(), "controllable/radial_menu_items.json");
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            writer.write(json);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<ButtonBindingData> getDefaults()
    {
        List<ButtonBindingData> defaults = new ArrayList<>();
        defaults.add(new ButtonBindingData(ButtonBindings.ADVANCEMENTS, ChatFormatting.YELLOW));
        defaults.add(new ButtonBindingData(ButtonBindings.DEBUG_INFO, ChatFormatting.AQUA));
        defaults.add(new ButtonBindingData(ButtonBindings.SCREENSHOT, ChatFormatting.GOLD));
        defaults.add(new ButtonBindingData(ButtonBindings.FULLSCREEN, ChatFormatting.YELLOW));
        defaults.add(new ButtonBindingData(ButtonBindings.CINEMATIC_CAMERA, ChatFormatting.GREEN));
        defaults.add(new ButtonBindingData(ButtonBindings.HIGHLIGHT_PLAYERS, ChatFormatting.GREEN));
        return defaults;
    }

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

    public LinkedHashSet<ButtonBindingData> getBindings()
    {
        return new LinkedHashSet<>(this.bindings);
    }

    public void setBindings(Set<ButtonBindingData> bindings)
    {
        this.bindings = bindings;
        this.save();
        this.populateAndConstruct();
    }

    public void removeBinding(ButtonBinding binding)
    {
        if(this.bindings.removeIf(data -> data.getBinding() == binding))
        {
            this.save();
            this.populateAndConstruct();
        }
    }

    public void setVisibility(boolean visible)
    {
        this.visible = visible;
    }

    public void clearAnimation()
    {
        this.animateTicks = 0;
        this.prevAnimateTicks = 0;
    }

    private void populateAndConstruct()
    {
        this.rightItems.clear();
        this.leftItems.clear();

        List<AbstractRadialItem> items = new ArrayList<>();
        this.bindings.forEach(binding -> items.add(new ButtonBindingItem(binding)));
        GatherRadialMenuItemsEvent event = new GatherRadialMenuItemsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        items.addAll(event.getItems());
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

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            if(this.visible && !Controllable.getInput().isControllerInUse())
            {
                //this.setVisibility(false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderScreen(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.options.hideGui || mc.screen != null)
            return;

        if(Controllable.getController() != null)
        {
            /*if(Controllable.getInput().getLastUse() <= 0)
                return;*/

            if(this.visible || this.animateTicks > 0 || this.prevAnimateTicks > 0)
            {
                this.renderRadialMenu(event.renderTickTime);
            }
        }
    }

    @SubscribeEvent
    public void onRenderScreen(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

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

    private void renderRadialMenu(float partialTicks)
    {
        this.updateSelected();
        
        PoseStack modelStack = RenderSystem.getModelViewStack();
        modelStack.pushPose();
        modelStack.setIdentity();
        modelStack.translate(0, 0, 1000F - net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        PoseStack poseStack = new PoseStack();

        float animation = Mth.lerp(partialTicks, this.prevAnimateTicks, this.animateTicks) / 5F;
        float c1 = 1.70158F;
        float c3 = c1 + 1;
        animation = (float) (1 + c3 * Math.pow(animation - 1, 3) + c1 * Math.pow(animation - 1, 2));

        // Draw background
        Minecraft mc = Minecraft.getInstance();
        Screen.fill(poseStack, 0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0x78101010);

        poseStack.translate(0, -10, 0);
        poseStack.translate((int) (mc.getWindow().getGuiScaledWidth() / 2F), (int) (mc.getWindow().getGuiScaledHeight() / 2F), 0);

        //matrixStack.scale(animation, animation, animation);

        poseStack.pushPose();
        this.settingsItem.draw(poseStack, mc, false, this.selected == this.settingsItem, animation);
        poseStack.popPose();

        poseStack.pushPose();
        this.closeItem.draw(poseStack, mc, false, this.selected == this.closeItem, animation);
        poseStack.popPose();

        this.drawRadialItems(this.rightItems, poseStack, mc, animation);
        this.drawRadialItems(this.leftItems, poseStack, mc, animation);

        modelStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void drawRadialItems(List<AbstractRadialItem> items, PoseStack matrixStack, Minecraft mc, float animation)
    {
        for(int i = 0; i < items.size(); i++)
        {
            AbstractRadialItem item = items.get(i);

            matrixStack.pushPose();
            if(i == 0) matrixStack.translate(0, -10, 0);
            if(i == items.size() - 1) matrixStack.translate(0, 10, 0);

            boolean left = item.angle >= 180F;
            float x = (float) Math.cos(Math.toRadians(item.angle - 90F)) * 70F;
            float y = (float) Math.sin(Math.toRadians(item.angle - 90F)) * 70F;
            matrixStack.translate((int) x, (int) y, 0);

            item.draw(matrixStack, mc, left, this.selected == item, animation);

            matrixStack.popPose();
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

        float xValue = Config.CLIENT.options.radialThumbstick.get() == Thumbstick.RIGHT ? controller.getRThumbStickXValue() : controller.getLThumbStickXValue();
        float yValue = Config.CLIENT.options.radialThumbstick.get() == Thumbstick.RIGHT ? controller.getRThumbStickYValue() : controller.getLThumbStickYValue();

        // Don't update selected if thumbstick is not above a certain threshold
        if(Math.abs(xValue) <= 0.5F && Math.abs(yValue) <= 0.5F)
            return;

        // Finds the closest radial item based on the direction of the right controller thumbstick
        float selectedAngle = (float) (Mth.wrapDegrees(Math.toDegrees(Math.atan2(yValue, xValue)) - 90) + 180);
        Optional<AbstractRadialItem> closest = this.allItems.stream().min((o1, o2) -> Mth.degreesDifferenceAbs(o1.angle, selectedAngle) > Mth.degreesDifferenceAbs(o2.angle, selectedAngle) ? 1 : 0);
        if(closest.isEmpty())
            return;

        // Don't update if the closest is the same as the currently selected item
        if(closest.get() == this.selected)
            return;

        this.selected = closest.get();
        Minecraft mc = Minecraft.getInstance();

        if(Config.CLIENT.options.uiSounds.get())
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

        public abstract void onUseItem(RadialMenuHandler handler);

        protected abstract void draw(PoseStack matrixStack, Minecraft mc, boolean left, boolean selected, float animation);

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
        public void onUseItem(RadialMenuHandler handler)
        {
            handler.setVisibility(false);
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_TAKE_RESULT, 1.3F));
        }

        @Override
        protected void draw(PoseStack matrixStack, Minecraft mc, boolean left, boolean selected, float animation)
        {
            int color = selected ? 0xFFCCCCCC : mc.options.getBackgroundColor(0.7F);
            float alpha = ((color >> 24) & 0xFF) / 255F;
            float red = ((color >> 16) & 0xFF) / 255F;
            float green = ((color >> 8) & 0xFF) / 255F;
            float blue = ((color >> 0) & 0xFF) / 255F;

            matrixStack.translate(0, 90, 0);

            //RenderSystem.disableTexture(); //TODO test this
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();

            alpha = Math.min(1.0F, alpha * animation);

            // Draw background
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            // Top (reduced width by 2)
            buffer.vertex(matrixStack.last().pose(), -14, -15, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), -14, -14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 14, -14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 14, -15, 0).color(red, green, blue, alpha).endVertex();
            // Middle
            buffer.vertex(matrixStack.last().pose(), -15, -14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), -15, 14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 15, 14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 15, -14, 0).color(red, green, blue, alpha).endVertex();
            // Bottom (reduced width by 2)
            buffer.vertex(matrixStack.last().pose(), -14, 14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), -14, 15, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 14, 15, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 14, 14, 0).color(red, green, blue, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            RenderSystem.disableBlend();
            //RenderSystem.enableTexture();
            RenderSystem.enableCull();

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Screen.blit(matrixStack, -10, -10, 20, 20, 98, 15, 10, 10, 256, 256);

            if(selected)
            {
                Screen.drawCenteredString(matrixStack, mc.font, LABEL, 0, -30, 0xFFFFFF);
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
        public void onUseItem(RadialMenuHandler handler)
        {
            handler.setVisibility(false);
            handler.clearAnimation();
            Minecraft.getInstance().setScreen(new RadialMenuConfigureScreen(handler.getBindings()));
        }

        @Override
        protected void draw(PoseStack matrixStack, Minecraft mc, boolean left, boolean selected, float animation)
        {
            int color = selected ? 0xFFCCCCCC : mc.options.getBackgroundColor(0.7F);
            float alpha = ((color >> 24) & 0xFF) / 255F;
            float red = ((color >> 16) & 0xFF) / 255F;
            float green = ((color >> 8) & 0xFF) / 255F;
            float blue = ((color >> 0) & 0xFF) / 255F;

            matrixStack.translate(0, -90, 0);

            //RenderSystem.disableTexture(); //TODO test
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();

            alpha = Math.min(1.0F, alpha * animation);

            // Draw background
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            // Top (reduced width by 2)
            buffer.vertex(matrixStack.last().pose(), -14, -15, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), -14, -14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 14, -14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 14, -15, 0).color(red, green, blue, alpha).endVertex();
            // Middle
            buffer.vertex(matrixStack.last().pose(), -15, -14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), -15, 14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 15, 14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 15, -14, 0).color(red, green, blue, alpha).endVertex();
            // Bottom (reduced width by 2)
            buffer.vertex(matrixStack.last().pose(), -14, 14, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), -14, 15, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 14, 15, 0).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrixStack.last().pose(), 14, 14, 0).color(red, green, blue, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());

            RenderSystem.disableBlend();
            //RenderSystem.enableTexture();
            RenderSystem.enableCull();

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Screen.blit(matrixStack, -10, -10, 20, 20, 88, 15, 10, 10, 256, 256);

            if(selected)
            {
                Screen.drawCenteredString(matrixStack, mc.font, LABEL, 0, 21, 0xFFFFFF);
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
        public ButtonBindingData entry;

        public ButtonBindingItem(ButtonBindingData entry)
        {
            super(Component.translatable(entry.getBinding().getLabelKey()).withStyle(entry.getColor()), Component.translatable(entry.getBinding().getCategory()));
            this.entry = entry;
        }

        @Override
        public void onUseItem(RadialMenuHandler radialMenu)
        {
            radialMenu.setVisibility(false);
            radialMenu.clearAnimation();
            this.entry.getBinding().setActiveAndPressed();
            Controllable.getInput().handleButtonInput(Controllable.getController(), -1, true, true);
        }

        @Override
        protected void draw(PoseStack poseStack, Minecraft mc, boolean left, boolean selected, float animation)
        {
            poseStack.pushPose();

            int color = selected ? 0xFFCCCCCC : mc.options.getBackgroundColor(0.7F);
            float alpha = ((color >> 24) & 0xFF) / 255F;
            float red = ((color >> 16) & 0xFF) / 255F;
            float green = ((color >> 8) & 0xFF) / 255F;
            float blue = ((color >> 0) & 0xFF) / 255F;

            float start = left ? -1 : 1;
            float end = (left ? -150F : 150F) * animation;

            poseStack.translate((1.0F - animation) * (left ? -20 : 20), 0, 0);

            //RenderSystem.disableTexture(); TODO test
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();

            // Draw background
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            // Top (offset by 1)
            buffer.vertex(poseStack.last().pose(), start, -15, 0).color(red, green, blue, alpha * animation).endVertex();
            buffer.vertex(poseStack.last().pose(), start, -14, 0).color(red, green, blue, alpha * animation).endVertex();
            buffer.vertex(poseStack.last().pose(), end, -14, 0).color(red, green, blue, 0.0F).endVertex();
            buffer.vertex(poseStack.last().pose(), end, -15, 0).color(red, green, blue, 0.0F).endVertex();

            // Middle
            buffer.vertex(poseStack.last().pose(), 0, -14, 0).color(red, green, blue, alpha * animation).endVertex();
            buffer.vertex(poseStack.last().pose(), 0, 14, 0).color(red, green, blue, alpha * animation).endVertex();
            buffer.vertex(poseStack.last().pose(), end, 14, 0).color(red, green, blue, 0.0F).endVertex();
            buffer.vertex(poseStack.last().pose(), end, -14, 0).color(red, green, blue, 0.0F).endVertex();

            // Bottom (offset by 1)
            buffer.vertex(poseStack.last().pose(), start, 14, 0).color(red, green, blue, alpha * animation).endVertex();
            buffer.vertex(poseStack.last().pose(), start, 15, 0).color(red, green, blue, alpha * animation).endVertex();
            buffer.vertex(poseStack.last().pose(), end, 15, 0).color(red, green, blue, 0.0F).endVertex();
            buffer.vertex(poseStack.last().pose(), end, 14, 0).color(red, green, blue, 0.0F).endVertex();

            BufferUploader.drawWithShader(buffer.end());

            RenderSystem.disableBlend();
            //RenderSystem.enableTexture();
            RenderSystem.enableCull();

            if(this.label != null)
            {
                int offset = !left ? 5 : -mc.font.width(this.label) - 5;
                Screen.drawString(poseStack, mc.font, this.label, offset, -10, 0xFFFFFF);
            }

            if(this.description != null)
            {
                int offset = !left ? 5 : -mc.font.width(this.description) - 5;
                Screen.drawString(poseStack, mc.font, this.description, offset, 2, 0xFFFFFF);
            }

            poseStack.popPose();
        }
    }
}
