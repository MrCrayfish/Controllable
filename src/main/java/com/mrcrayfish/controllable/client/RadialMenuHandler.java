package com.mrcrayfish.controllable.client;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.gui.ButtonBindingData;
import com.mrcrayfish.controllable.client.gui.RadialMenuConfigureScreen;
import com.mrcrayfish.controllable.event.GatherRadialMenuItemsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

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
                    String key = JSONUtils.getString(object, "key");
                    String colorName = JSONUtils.getString(object, "color");
                    ButtonBinding binding = BindingRegistry.getInstance().getBindingByDescriptionKey(key);
                    if(binding != null)
                    {
                        TextFormatting color = TextFormatting.getValueByName(colorName);
                        if(color == null || color.getColor() == null)
                        {
                            color = TextFormatting.YELLOW;
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
        defaults.add(new ButtonBindingData(ButtonBindings.ADVANCEMENTS, TextFormatting.YELLOW));
        defaults.add(new ButtonBindingData(ButtonBindings.DEBUG_INFO, TextFormatting.YELLOW));
        defaults.add(new ButtonBindingData(ButtonBindings.SCREENSHOT, TextFormatting.YELLOW));
        defaults.add(new ButtonBindingData(ButtonBindings.FULLSCREEN, TextFormatting.YELLOW));
        defaults.add(new ButtonBindingData(ButtonBindings.CINEMATIC_CAMERA, TextFormatting.YELLOW));
        defaults.add(new ButtonBindingData(ButtonBindings.HIGHLIGHT_PLAYERS, TextFormatting.YELLOW));
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
            mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_LOOM_TAKE_RESULT, 1.5F));
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

    @SubscribeEvent
    public void onRenderScreen(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.gameSettings.hideGUI || mc.currentScreen != null)
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

        MatrixStack matrixStack = new MatrixStack();
        Minecraft mc = Minecraft.getInstance();
        matrixStack.translate(0, -10, 0);
        matrixStack.translate((int) (mc.getMainWindow().getScaledWidth() / 2F), (int) (mc.getMainWindow().getScaledHeight() / 2F), 0);

        float animation = MathHelper.lerp(partialTicks, this.prevAnimateTicks, this.animateTicks) / 5F;
        float c1 = 1.70158F;
        float c3 = c1 + 1;
        animation = (float) (1 + c3 * Math.pow(animation - 1, 3) + c1 * Math.pow(animation - 1, 2));
        //matrixStack.scale(animation, animation, animation);

        matrixStack.push();
        this.settingsItem.draw(matrixStack, mc, false, this.selected == this.settingsItem, animation);
        matrixStack.pop();

        matrixStack.push();
        this.closeItem.draw(matrixStack, mc, false, this.selected == this.closeItem, animation);
        matrixStack.pop();

        this.drawRadialItems(this.rightItems, matrixStack, mc, animation);
        this.drawRadialItems(this.leftItems, matrixStack, mc, animation);
    }

    private void drawRadialItems(List<AbstractRadialItem> items, MatrixStack matrixStack, Minecraft mc, float animation)
    {
        for(int i = 0; i < items.size(); i++)
        {
            AbstractRadialItem item = items.get(i);

            matrixStack.push();
            if(i == 0) matrixStack.translate(0, -10, 0);
            if(i == items.size() - 1) matrixStack.translate(0, 10, 0);

            boolean left = item.angle >= 180F;
            float x = (float) Math.cos(Math.toRadians(item.angle - 90F)) * 70F;
            float y = (float) Math.sin(Math.toRadians(item.angle - 90F)) * 70F;
            matrixStack.translate((int) x, (int) y, 0);

            item.draw(matrixStack, mc, left, this.selected == item, animation);

            matrixStack.pop();
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

        // Don't update selected if thumbstick is not above a certain threshold
        if(Math.abs(controller.getRThumbStickYValue()) <= 0.5F && Math.abs(controller.getRThumbStickXValue()) <= 0.5F)
            return;

        // Finds the closest radial item based on the direction of the right controller thumbstick
        float selectedAngle = (float) (MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(controller.getRThumbStickYValue(), controller.getRThumbStickXValue())) - 90) + 180);
        Optional<AbstractRadialItem> closest = this.allItems.stream().min((o1, o2) -> MathHelper.degreesDifferenceAbs(o1.angle, selectedAngle) > MathHelper.degreesDifferenceAbs(o2.angle, selectedAngle) ? 1 : 0);
        if(!closest.isPresent())
            return;

        // Don't update if the closest is the same as the currently selected item
        if(closest.get() == this.selected)
            return;

        this.selected = closest.get();
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundHandler().play(SimpleSound.master(SoundEvents.ENTITY_ITEM_PICKUP, 1.5F));
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
        protected ITextComponent label;
        protected ITextComponent description;
        private float angle;

        protected AbstractRadialItem(ITextComponent label)
        {
            this(label, null);
        }

        protected AbstractRadialItem(ITextComponent label, ITextComponent description)
        {
            this.label = label;
            this.description = description;
        }

        public ITextComponent getLabel()
        {
            return this.label;
        }

        @Nullable
        public ITextComponent getDescription()
        {
            return this.description;
        }

        public boolean isEmpty()
        {
            return false;
        }

        public abstract void onUseItem(RadialMenuHandler handler);

        protected abstract void draw(MatrixStack matrixStack, Minecraft mc, boolean left, boolean selected, float animation);

        protected void playSound(SoundEvent event, float pitch)
        {
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundHandler().play(SimpleSound.master(event, pitch));
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
        private static final ITextComponent LABEL = new TranslationTextComponent("controllable.gui.close");

        public CloseRadialMenuItem()
        {
            super(new TranslationTextComponent("controllable.gui.radial.close"));
        }

        @Override
        public void onUseItem(RadialMenuHandler handler)
        {
            handler.setVisibility(false);
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_LOOM_TAKE_RESULT, 1.3F));
        }

        @Override
        protected void draw(MatrixStack matrixStack, Minecraft mc, boolean left, boolean selected, float animation)
        {
            float color = selected ? 1.0F : 0.1F;

            matrixStack.translate(0, 90, 0);

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();

            // Draw background
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(matrixStack.getLast().getMatrix(), -15, -15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), -15, 15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), 15, 15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), 15, -15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);

            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableCull();

            mc.getTextureManager().bindTexture(TEXTURE);
            AbstractGui.blit(matrixStack, -10, -10, 20, 20, 98, 15, 10, 10, 256, 256);

            if(selected)
            {
                AbstractGui.drawCenteredString(matrixStack, mc.fontRenderer, LABEL, 0, -30, 0xFFFFFF);
            }
        }
    }

    /**
     * A simple radial item to close the radial menu
     */
    public static final class RadialSettingsItem extends AbstractRadialItem
    {
        private static final ITextComponent LABEL = new TranslationTextComponent("controllable.gui.configure");

        public RadialSettingsItem()
        {
            super(new TranslationTextComponent("controllable.gui.radial.settings"));
        }

        @Override
        public void onUseItem(RadialMenuHandler handler)
        {
            handler.setVisibility(false);
            handler.clearAnimation();
            Minecraft.getInstance().displayGuiScreen(new RadialMenuConfigureScreen(handler.getBindings()));
        }

        @Override
        protected void draw(MatrixStack matrixStack, Minecraft mc, boolean left, boolean selected, float animation)
        {
            float color = selected ? 1.0F : 0.1F;

            matrixStack.translate(0, -90, 0);

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();

            // Draw background
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(matrixStack.getLast().getMatrix(), -15, -15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), -15, 15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), 15, 15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), 15, -15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);

            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableCull();

            mc.getTextureManager().bindTexture(TEXTURE);
            AbstractGui.blit(matrixStack, -10, -10, 20, 20, 88, 15, 10, 10, 256, 256);

            if(selected)
            {
                AbstractGui.drawCenteredString(matrixStack, mc.fontRenderer, LABEL, 0, 21, 0xFFFFFF);
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
            super(new TranslationTextComponent(entry.getBinding().getDescription()).mergeStyle(entry.getColor()), new TranslationTextComponent(entry.getBinding().getCategory()));
            this.entry = entry;
        }

        @Override
        public void onUseItem(RadialMenuHandler radialMenu)
        {
            radialMenu.setVisibility(false);
            radialMenu.clearAnimation();
            this.entry.getBinding().setActiveAndPressed();
            Controllable.getInput().handleButtonInput(Controllable.getController(), this.entry.getBinding().getButton(), true, true);
        }

        @Override
        protected void draw(MatrixStack matrixStack, Minecraft mc, boolean left, boolean selected, float animation)
        {
            matrixStack.push();

            float color = selected ? 1.0F : 0.1F;
            float end = (left ? -150F : 150F) * animation;

            matrixStack.translate((1.0F - animation) * (left ? -20 : 20), 0, 0);

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableCull();

            // Draw background
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(matrixStack.getLast().getMatrix(), 0, -15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), 0, 15, 0).color(color, color, color, 0.6F * animation).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), end, 15, 0).color(color, color, color, 0.0F).endVertex();
            buffer.pos(matrixStack.getLast().getMatrix(), end, -15, 0).color(color, color, color, 0.0F).endVertex();
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);

            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableCull();

            if(this.label != null)
            {
                int offset = !left ? 5 : -mc.fontRenderer.getStringPropertyWidth(this.label) - 5;
                AbstractGui.drawString(matrixStack, mc.fontRenderer, this.label, offset, -10, 0xFFFFFF);
            }

            if(this.description != null)
            {
                int offset = !left ? 5 : -mc.fontRenderer.getStringPropertyWidth(this.description) - 5;
                AbstractGui.drawString(matrixStack, mc.fontRenderer, this.description, offset, 2, 0xFFFFFF);
            }

            matrixStack.pop();
        }
    }
}
