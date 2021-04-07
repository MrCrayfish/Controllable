package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class RadialMenuHandler
{
    private static final int MIN_ITEMS = 6;
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

    private boolean visible;
    private int animateTicks;
    private int prevAnimateTicks;
    private int selectedRadialIndex = 0;
    private List<AbstractRadialItem> items = new ArrayList<>();
    private ITextComponent label;

    private RadialMenuHandler() {}

    public void interact()
    {
        if(this.visible)
        {
            this.getSelectedItem().ifPresent(item ->
            {
                if(!item.isEmpty())
                {
                    item.onUseItem(this);
                }
            });
        }
        else
        {
            this.setVisibility(true);
            this.populateItems();
            this.selectedRadialIndex = 0;
            this.label = this.items.get(this.selectedRadialIndex).getLabel();
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundHandler().play(SimpleSound.master(SoundEvents.ENTITY_ITEM_PICKUP, this.visible ? 0.6F : 0.5F));
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

    private boolean isCompletelyVisible()
    {
        return this.visible && this.animateTicks == ANIMATE_DURATION && this.prevAnimateTicks == ANIMATE_DURATION;
    }

    private void populateItems()
    {
        this.items.clear();
        this.items.add(new CloseMenuItem());
        this.items.add(new ButtonBindingItem(ButtonBindings.JUMP));
        this.items.add(new ButtonBindingItem(ButtonBindings.SCREENSHOT));
        this.items.add(new ButtonBindingItem(ButtonBindings.FULLSCREEN));
        this.items.add(new ButtonBindingItem(ButtonBindings.INVENTORY));
        this.items.add(new ButtonBindingItem(ButtonBindings.ATTACK));
        this.items.add(new ButtonBindingItem(ButtonBindings.TOGGLE_PERSPECTIVE));
        this.items.add(new ButtonBindingItem(ButtonBindings.SNEAK));
        this.items.add(new ButtonBindingItem(ButtonBindings.PLAYER_LIST));
        while(this.items.size() < MIN_ITEMS - 1) this.items.add(new EmptyRadialItem());
        this.items.add(new RadialSettingsItem());
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public int getSelectedRadialIndex()
    {
        return this.selectedRadialIndex;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            if(this.visible && !Controllable.getInput().isControllerInUse())
            {
                this.setVisibility(false);
            }
        }
    }

    @SubscribeEvent
    public void onDrawCrossHair(RenderGameOverlayEvent.Pre event)
    {
        if(this.visible && event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS)
        {
            event.setCanceled(true);
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
            if(Controllable.getInput().getLastUse() <= 0)
                return;

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
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        float animateProgress = MathHelper.lerp(partialTicks, this.prevAnimateTicks, this.animateTicks) / 5F;
        float c1 = 1.70158F;
        float c3 = c1 + 1;
        animateProgress = (float) (1 + c3 * Math.pow(animateProgress - 1, 3) + c1 * Math.pow(animateProgress - 1, 2));

        double selectedAngle = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(controller.getRThumbStickYValue(), controller.getRThumbStickXValue())) - 90) + 180;
        boolean canSelect = Math.abs(controller.getRThumbStickYValue()) > 0.5F || Math.abs(controller.getRThumbStickXValue()) > 0.5F;
        int segments = this.items.size();
        double segmentSize = 360.0 / segments;
        float innerRadius = 60F * animateProgress;
        float outerRadius = 100F * animateProgress;

        if(canSelect)
        {
            this.setSelectedRadialIndex((int) (selectedAngle / segmentSize));
        }

        MatrixStack matrixStack = new MatrixStack();
        Minecraft mc = Minecraft.getInstance();
        matrixStack.translate(mc.getMainWindow().getScaledWidth() / 2F, mc.getMainWindow().getScaledHeight() / 2F, 0);

        int wheelColor = 0x88000000;
        float wheelAlpha = ((wheelColor >> 24 & 255) / 255F) * animateProgress;
        float wheelRed = (wheelColor >> 16 & 255) / 255F;
        float wheelGreen = (wheelColor >> 8 & 255) / 255F;
        float wheelBlue = (wheelColor & 255) / 255F;

        int highlightColor = 0xFFFFFF;
        float highlightRed = (highlightColor >> 16 & 255) / 255F;
        float highlightGreen = (highlightColor >> 8 & 255) / 255F;
        float highlightBlue = (highlightColor & 255) / 255F;

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for(int i = 0; i <= segments; i++)
        {
            double angle = segmentSize * i - 90;
            boolean selected = this.selectedRadialIndex == i - 1;
            float x = (float) Math.cos(Math.toRadians(angle));
            float y = (float) Math.sin(Math.toRadians(angle));
            if(!selected)
            {
                buffer.pos(matrixStack.getLast().getMatrix(), x * outerRadius, y * outerRadius, 0.0F).color(wheelRed, wheelGreen, wheelBlue, wheelAlpha).endVertex();
                buffer.pos(matrixStack.getLast().getMatrix(), x * innerRadius, y * innerRadius, 0.0F).color(wheelRed, wheelGreen, wheelBlue, wheelAlpha).endVertex();
            }
            else
            {
                buffer.pos(matrixStack.getLast().getMatrix(), x * outerRadius, y * outerRadius, 0.0F).color(highlightRed, highlightGreen, highlightBlue, wheelAlpha).endVertex();
                buffer.pos(matrixStack.getLast().getMatrix(), x * innerRadius, y * innerRadius, 0.0F).color(highlightRed, highlightGreen, highlightBlue, wheelAlpha).endVertex();
            }
        }
        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);

        RenderSystem.lineWidth(4F);
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for(int i = 0; i <= segments; i++)
        {
            double angle = segmentSize * i - 90;
            float x = (float) Math.cos(Math.toRadians(angle));
            float y = (float) Math.sin(Math.toRadians(angle));
            buffer.pos(matrixStack.getLast().getMatrix(), x * outerRadius, y * outerRadius, 0.0F).color(1.0F, 1.0F, 1.0F, 0.75F * animateProgress).endVertex();
        }
        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);

        RenderSystem.lineWidth(4F);
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for(int i = 0; i <= segments; i++)
        {
            double angle = segmentSize * i - 90;
            float x = (float) Math.cos(Math.toRadians(angle));
            float y = (float) Math.sin(Math.toRadians(angle));
            buffer.pos(matrixStack.getLast().getMatrix(), x * innerRadius, y * innerRadius, 0.0F).color(1.0F, 1.0F, 1.0F, 0.75F * animateProgress).endVertex();
        }
        buffer.finishDrawing();
        WorldVertexBufferUploader.draw(buffer);

        for(int i = 0; i <= segments; i++)
        {
            double angle = segmentSize * i - 90;
            float distance = innerRadius + (outerRadius - innerRadius) * 0.4F;
            float x = (float) Math.cos(Math.toRadians(angle + segmentSize / 2)) * distance - 8;
            float y = (float) Math.sin(Math.toRadians(angle + segmentSize / 2)) * distance - 8;
            RenderSystem.pushMatrix();
            RenderSystem.translatef(mc.getMainWindow().getScaledWidth() / 2F, mc.getMainWindow().getScaledHeight() / 2F, 0);
            RenderSystem.translatef(x, y, 0);
            RenderSystem.scalef(animateProgress, animateProgress, animateProgress);
            Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(new ItemStack(Items.APPLE), 0, 0);
            RenderSystem.popMatrix();
        }

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        if(this.label != null && this.visible)
        {
            matrixStack.push();
            matrixStack.scale(animateProgress, animateProgress, animateProgress);
            AbstractGui.drawCenteredString(matrixStack, mc.fontRenderer, this.label, 0, -5, 0xFFFFFF);
            matrixStack.pop();
        }
    }

    private void setSelectedRadialIndex(int index)
    {
        if(index != this.selectedRadialIndex)
        {
            this.selectedRadialIndex = index;
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundHandler().play(SimpleSound.master(SoundEvents.ENTITY_ITEM_PICKUP, 1.5F));
            this.getSelectedItem().ifPresent(item -> this.label = item.getLabel());
        }
    }

    private Optional<AbstractRadialItem> getSelectedItem()
    {
        if(this.selectedRadialIndex >= 0 && this.selectedRadialIndex < this.items.size())
        {
            return Optional.of(this.items.get(this.selectedRadialIndex));
        }
        return Optional.empty();
    }

    /**
     * The base radial item
     */
    public abstract static class AbstractRadialItem
    {
        private ITextComponent label;

        protected AbstractRadialItem(ITextComponent label)
        {
            this.label = label;
        }

        @Nullable
        public ITextComponent getLabel()
        {
            return this.label;
        }

        public boolean isEmpty()
        {
            return false;
        }

        public abstract void onUseItem(RadialMenuHandler handler);

        protected void playSound(SoundEvent event, float pitch)
        {
            Minecraft mc = Minecraft.getInstance();
            mc.getSoundHandler().play(SimpleSound.master(event, pitch));
        }
    }

    /**
     * A simple radial item to close the radial menu
     */
    public static final class CloseMenuItem extends AbstractRadialItem
    {
        public CloseMenuItem()
        {
            super(new TranslationTextComponent("controllable.gui.radial.close"));
        }

        @Override
        public void onUseItem(RadialMenuHandler handler)
        {
            handler.setVisibility(false);
        }
    }

    /**
     * An empty radial item to fill radial menu if there aren't enough items in the menu
     */
    public static final class EmptyRadialItem extends AbstractRadialItem
    {
        public EmptyRadialItem()
        {
            super(null);
        }

        @Override
        public boolean isEmpty()
        {
            return true;
        }

        @Override
        public void onUseItem(RadialMenuHandler handler)
        {
            handler.setVisibility(false);
        }
    }

    /**
     * A simple radial item to close the radial menu
     */
    public static final class RadialSettingsItem extends AbstractRadialItem
    {
        public RadialSettingsItem()
        {
            super(new TranslationTextComponent("controllable.gui.radial.settings"));
        }

        @Override
        public void onUseItem(RadialMenuHandler handler)
        {
            handler.setVisibility(false);
        }
    }

    /**
     * A radial item that takes a button binding. Using this item will virtually enables the button
     * binding and doesn't require the real assigned button to be pressed. This also works for
     * bindings that don't have a button bound to them.
     */
    private static class ButtonBindingItem extends AbstractRadialItem
    {
        public ButtonBinding binding;

        public ButtonBindingItem(ButtonBinding binding)
        {
            super(new TranslationTextComponent(binding.getDescription()));
            this.binding = binding;
        }

        @Override
        public void onUseItem(RadialMenuHandler radialMenu)
        {
            radialMenu.setVisibility(false);
            radialMenu.clearAnimation();
            this.binding.setActiveAndPressed();
            Controllable.getInput().handleButtonInput(Controllable.getController(), this.binding.getButton(), true, true);
        }
    }
}
