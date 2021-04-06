package com.mrcrayfish.controllable.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * Author: MrCrayfish
 */
public class RadialMenuHandler
{
    private static RadialMenuHandler instance;

    public static RadialMenuHandler instance()
    {
        if(instance == null)
        {
            instance = new RadialMenuHandler();
        }
        return instance;
    }

    private boolean open;
    private int animateTicks;
    private int prevAnimateTicks;
    private int selectedRadialIndex = -1;

    private RadialMenuHandler() {}

    public void toggleMenu()
    {
        this.open = !this.open;
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundHandler().play(SimpleSound.master(SoundEvents.ENTITY_ITEM_PICKUP, this.open ? 0.6F : 0.5F));
    }

    public boolean isOpen()
    {
        return this.open;
    }

    public int getSelectedRadialIndex()
    {
        return this.selectedRadialIndex;
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

            if(this.open || this.animateTicks > 0 || this.prevAnimateTicks > 0)
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

        if(this.open)
        {
            if(this.animateTicks < 5)
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

        float animateProgress = MathHelper.lerp(partialTicks,this.prevAnimateTicks, this.animateTicks) / 5F;
        float c1 = 1.70158F;
        float c3 = c1 + 1;
        animateProgress = (float) (1 + c3 * Math.pow(animateProgress - 1, 3) + c1 * Math.pow(animateProgress - 1, 2));

        double selectedAngle = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(controller.getRThumbStickYValue(), controller.getRThumbStickXValue())) - 90) + 180;
        boolean canSelect = Math.abs(controller.getRThumbStickYValue()) > 0.5F || Math.abs(controller.getRThumbStickXValue()) > 0.5F;
        int segments = 12; // Segments will be based on bound actions
        double segmentSize = 360.0 / segments;
        float innerRadius = 60F * animateProgress;
        float outerRadius = 100F * animateProgress;

        int radialIndex = canSelect ? (int) (selectedAngle / segmentSize) : -1;
        if(radialIndex != this.selectedRadialIndex)
        {
            this.setSelectedRadialIndex(radialIndex);
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
            //float newAlpha = selected ? Math.min(1.0F, wheelAlpha + 0.3F) : wheelAlpha;
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

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private void setSelectedRadialIndex(int index)
    {
        this.selectedRadialIndex = index;
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundHandler().play(SimpleSound.master(SoundEvents.ENTITY_ITEM_PICKUP, 1.5F));
    }
}
