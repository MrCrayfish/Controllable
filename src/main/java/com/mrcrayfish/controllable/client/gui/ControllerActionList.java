package com.mrcrayfish.controllable.client.gui;

import com.github.fernthedev.config.common.exceptions.ConfigLoadException;
import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.ControllerProperties;
import com.mrcrayfish.controllable.registry.ActionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ControllerActionList extends AbstractOptionList<ControllerActionList.Entry> {



    public abstract static class Entry extends AbstractOptionList.Entry<ControllerActionList.Entry> { }


    private final ControllerActionMenu controlsScreen;
    private int maxListLabelWidth;

    public ControllerActionList(ControllerActionMenu controls, Minecraft mcIn) {
        super(mcIn, controls.width + 45, controls.height, 43, controls.height - 32, 20);
        this.controlsScreen = controls;
        Map<String, ButtonBinding> akeybinding = Controllable.getButtonRegistry().getButtonBindings();
        akeybinding = new TreeMap<>(akeybinding);

        final String[] s = {null};

        akeybinding.forEach((action, buttonBinding) -> {
            ActionData actionData = Controllable.getButtonRegistry().getAction(action);


            String category = actionData.getCategoryKey();

            if (!category.equals(s[0])) {
                s[0] = category;
                this.addEntry(new ControllerActionList.CategoryEntry(category));
            }

            int i = mcIn.fontRenderer.getStringWidth(I18n.format(actionData.getActionKey()));
            if (i > this.maxListLabelWidth) {
                this.maxListLabelWidth = i;
            }

            this.addEntry(new ControllerActionList.KeyEntry(buttonBinding, action, actionData));
        });

    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

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

        private final String keyDesc;
        private final Button btnChangeKeyBinding;
        private final Button btnReset;
        private final Button btnSetNone;

        private KeyEntry(final ButtonBinding buttonBinding, final String action, final ActionData actionData) {
            this.buttonBinding = buttonBinding;
            this.keyDesc = I18n.format(actionData.getActionKey());

            this.btnChangeKeyBinding = new Button(0, 0, 75 + 20 /*Forge: add space*/, 20, this.keyDesc, (p_214386_2_) -> {
                controlsScreen.controllerButtonId = this.buttonBinding;
                controlsScreen.entry = this;
                controlsScreen.action = action;
            }) {
                protected String getNarrationMessage() {
                    return I18n.format(actionData.getActionKey());
                }
            };

            this.btnSetNone = new Button(0, 0, 40 /*Forge: add space*/, 20, I18n.format("gui.none"), (p_214386_2_) -> {
                buttonBinding.setButton(-1);
                try {
                    ControllerProperties.saveMappings();
                } catch (ConfigLoadException e) {
                    throw new IllegalStateException("Unable to save config", e);
                }
            }) {
                protected String getNarrationMessage() {
                    return I18n.format(actionData.getActionKey());
                }
            };

            this.btnReset = new Button(0, 0, 50, 20, I18n.format("controls.reset"), (p_214387_2_) -> {
                this.buttonBinding.resetButton();
                try {
                    ControllerProperties.saveMappings();
                } catch (ConfigLoadException e) {
                    throw new IllegalStateException("Unable to save config", e);
                }

            }) {
                protected String getNarrationMessage() {
                    return I18n.format("narrator.controls.unbound", ControllerActionList.KeyEntry.this.keyDesc);
                }
            };
        }


        @Override
        public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_) {
            boolean flag = controlsScreen.controllerButtonId == this.buttonBinding;
            minecraft.fontRenderer.drawString(this.keyDesc, (float)(p_render_3_ + 90 - maxListLabelWidth), (float)(p_render_2_ + p_render_5_ / 2 - 9 / 2), 16777215);

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


            String buttonStr;
            try {
                buttonStr = Buttons.buttonNameFromId(buttonBinding.getButtonId());
            } catch (IndexOutOfBoundsException ignored) {
                buttonStr = TextFormatting.YELLOW + "UNKNOWN_BUTTON";
            }

            this.btnChangeKeyBinding.setMessage(buttonStr);
            boolean flag1 = false;
//            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
            if (!buttonBinding.isInvalid()) {
                for(ButtonBinding buttonBindingCheck : Controllable.getButtonRegistry().getButtonBindings().values()) {
                    if (buttonBindingCheck != this.buttonBinding && this.buttonBinding.conflicts(buttonBindingCheck)) {
                        flag1 = true;
                        break;

//                        keyCodeModifierConflict &= buttonBindingCheck.hasKeyCodeModifierConflict(this.keybinding);
                    }
                }
            }

            if (flag) {
                this.btnChangeKeyBinding.setMessage(TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.btnChangeKeyBinding.getMessage() + TextFormatting.WHITE + " <");
            } else if (flag1) {
                this.btnChangeKeyBinding.setMessage((TextFormatting.RED) + this.btnChangeKeyBinding.getMessage());
            }

            this.btnChangeKeyBinding.render(p_render_6_, p_render_7_, p_render_9_);
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
