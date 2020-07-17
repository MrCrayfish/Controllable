package com.mrcrayfish.controllable.client.gui;

import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.Reference;
import com.mrcrayfish.controllable.client.*;
import com.mrcrayfish.controllable.registry.ActionDataDescription;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

/**
 * @author Fernthedev
 * {@link "https://github.com/Fernthedev"}
 */
public class ControllerActionList extends AbstractOptionList<ControllerActionList.Entry> {

    private static final ResourceLocation CONTROLLER_BUTTONS = new ResourceLocation(Reference.MOD_ID, "textures/gui/buttons.png");

    public abstract static class Entry extends AbstractOptionList.Entry<ControllerActionList.Entry> { }


    private final ControllerActionMenu controlsScreen;
    private int maxListLabelWidth;

    public ControllerActionList(ControllerActionMenu controls, Minecraft mcIn) {
        super(mcIn, controls.width + 45, controls.height, 43, controls.height - 32, 20);
        this.controlsScreen = controls;
        Map<String, ButtonBinding> akeybinding = Controllable.getButtonRegistry().getButtonBindings();
        akeybinding = new TreeMap<>(akeybinding);


        Map<String, List<ControllerActionList.KeyEntry>> catList = new HashMap<>();


        akeybinding.forEach((action, buttonBinding) -> {
            ActionDataDescription actionDataDescription = Controllable.getButtonRegistry().getAction(action);


            String category = actionDataDescription.getCategoryTranslateKey();

            int i = mcIn.fontRenderer.getStringWidth(I18n.format(actionDataDescription.getActionTranslateKey()));
            if (i > this.maxListLabelWidth) {
                this.maxListLabelWidth = i;
            }

            if (!catList.containsKey(category)) catList.put(category, new ArrayList<>());

            catList.get(category).add(new ControllerActionList.KeyEntry(buttonBinding, action, actionDataDescription));
        });


        catList.forEach((category, entry) -> {
            this.addEntry(new ControllerActionList.CategoryEntry(category));
            entry.forEach(this::addEntry);
        });

    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    @OnlyIn(Dist.CLIENT)
    public class CategoryEntry extends ControllerActionList.Entry {
        private final String labelText;
        private final int labelWidth;

        public CategoryEntry(String name) {
            this.labelText = I18n.format(name);
            this.labelWidth = minecraft.fontRenderer.getStringWidth(this.labelText);
        }

        public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_) {
            minecraft.fontRenderer.drawString(this.labelText, (float)(minecraft.currentScreen.width / 2 - this.labelWidth / 2), (float)(p_render_2_ + p_render_5_ - 9 - 1), 16777215);
        }

        @Override
        public boolean changeFocus(boolean p_changeFocus_1_) {
            return false;
        }

        public List<? extends IGuiEventListener> children() {
            return Collections.emptyList();
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public class KeyEntry extends ControllerActionList.Entry {

        private final ButtonBinding buttonBinding;

        private final String actionDescription;
        private final Button btnChangeKeyBinding;
        private final Button btnReset;
        private final Button btnSetNone;

        private final ActionDataDescription actionDataDescription;

        private KeyEntry(final ButtonBinding buttonBinding, final String action, final ActionDataDescription actionDataDescription) {
            this.buttonBinding = buttonBinding;
            this.actionDataDescription = actionDataDescription;
            this.actionDescription = I18n.format(actionDataDescription.getActionTranslateKey());

            this.btnChangeKeyBinding = new Button(0, 0, 75 + 20 /*Forge: add space*/, 20, actionDescription, (p_214386_2_) -> {
                controlsScreen.controllerButtonId = this.buttonBinding;
                controlsScreen.entry = this;
                controlsScreen.action = action;
            }) {
                protected String getNarrationMessage() {
                    return I18n.format(actionDataDescription.getActionTranslateKey());
                }
            };

            this.btnSetNone = new Button(0, 0, 40 /*Forge: add space*/, 20, I18n.format("gui.none"), (p_214386_2_) -> {
                buttonBinding.setButton(-1);
                try {
                    ControllerProperties.saveActionRegistry();
                } catch (ConfigLoadException e) {
                    throw new IllegalStateException("Unable to save config", e);
                }
            }) {
                protected String getNarrationMessage() {
                    return I18n.format(actionDataDescription.getActionTranslateKey());
                }
            };

            this.btnReset = new Button(0, 0, 50, 20, I18n.format("controls.reset"), (p_214387_2_) -> {
                this.buttonBinding.resetButton();
                try {
                    ControllerProperties.saveActionRegistry();
                } catch (ConfigLoadException e) {
                    throw new IllegalStateException("Unable to save config", e);
                }

            }) {
                protected String getNarrationMessage() {
                    return I18n.format("narrator.controls.unbound", KeyEntry.this.actionDescription);
                }
            };
        }


        @Override
        public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_) {
            boolean selected = controlsScreen.controllerButtonId == this.buttonBinding;
            minecraft.fontRenderer.drawString(this.actionDescription, (float)(p_render_3_ + 40 - maxListLabelWidth), (float)(p_render_2_ + p_render_5_ / 2 - 9 / 2), 16777215);

            this.btnSetNone.x = p_render_3_ + 60;
            this.btnSetNone.y = p_render_2_;
            this.btnSetNone.active = !buttonBinding.isInvalid();
            this.btnSetNone.render(p_render_6_, p_render_7_, p_render_9_);

            this.btnReset.x = p_render_3_ + 190 + 20;
            this.btnReset.y = p_render_2_;
            this.btnReset.active = !buttonBinding.isDefault();
            this.btnReset.render(p_render_6_, p_render_7_, p_render_9_);

            this.btnChangeKeyBinding.x = p_render_3_ + 105;
            this.btnChangeKeyBinding.y = p_render_2_;


            // DRAW TEXT

            String buttonStr;
            if(buttonBinding.getButtonId() == -1)
            {
                buttonStr = TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "None";
            }
            else
            {
                try
                {
                    buttonStr = Buttons.buttonNameFromId(buttonBinding.getButtonId());
                }
                catch(IndexOutOfBoundsException ignored)
                {
                    buttonStr = TextFormatting.YELLOW + "UNKNOWN_BUTTON";
                }
            }

            if (controlsScreen.showIcons()) buttonStr = "  ";

            this.btnChangeKeyBinding.setMessage(buttonStr);
            boolean conflictsWithAnother = false;
            //            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
            if(!buttonBinding.isInvalid())
            {
                for(String action : Controllable.getButtonRegistry().getButtonBindings().keySet())
                {
                    ButtonBinding buttonBindingCheck = Controllable.getButtonRegistry().getButton(action);
                    ActionDataDescription actionDataDescription = Controllable.getButtonRegistry().getAction(action);
                    if(buttonBindingCheck != this.buttonBinding && this.buttonBinding.conflicts(buttonBindingCheck, actionDataDescription))
                    {
                        conflictsWithAnother = true;
                        break;

                        //                        keyCodeModifierConflict &= buttonBindingCheck.hasKeyCodeModifierConflict(this.keybinding);
                    }
                }
            }



            if(selected)
            {
                this.btnChangeKeyBinding.setMessage(TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.getMessage() + TextFormatting.WHITE + " <");
            }
            else if(conflictsWithAnother)
            {
                if (controlsScreen.showIcons())
                    this.btnChangeKeyBinding.setMessage(TextFormatting.RED + "> " +this.btnChangeKeyBinding.getMessage() + " <");
                else
                    this.btnChangeKeyBinding.setMessage(TextFormatting.RED + this.btnChangeKeyBinding.getMessage());
            }


            this.btnChangeKeyBinding.render(p_render_6_, p_render_7_, p_render_9_);


            if (controlsScreen.showIcons())
                drawIcon();

        }

        protected void drawIcon() {
            // Draw icon

            int remappedButton = buttonBinding.getButtonId();
            Controller controller = Controllable.getController();

            if (controller != null)
            {
                Mappings.Entry mapping = controller.getMapping();
                if(mapping != null)
                {
                    remappedButton = mapping.remap(buttonBinding.getButtonId());
                }
            }

            int texU = remappedButton * 13;
            int texV = Controllable.getOptions().getControllerType().ordinal() * 13;
            int size = 13;

            int x = btnChangeKeyBinding.x + (btnChangeKeyBinding.getWidth() - 13) / 2;
            int y = btnChangeKeyBinding.y + (btnChangeKeyBinding.getHeight() - 13) / 2;

            minecraft.getTextureManager().bindTexture(CONTROLLER_BUTTONS);


            /* Draw buttons icon */
            blit(x, y, texU, texV, size, size, 256, 256);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return ImmutableList.of(this.btnChangeKeyBinding, this.btnReset);
        }

        @Override
        public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
            if (this.btnChangeKeyBinding.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
                return true;
            } else if(btnSetNone.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_) ){
                return true;
            } else {
                return this.btnReset.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
            }
        }

        @Override
        public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
            return this.btnChangeKeyBinding.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_) || this.btnReset.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }
    }

}
