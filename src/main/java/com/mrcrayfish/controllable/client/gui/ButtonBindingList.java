package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.widget.ButtonBindingButton;
import com.mrcrayfish.controllable.client.gui.widget.IWidgetList;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingList extends GuiListExtended
{
    private ButtonBindingScreen screen;
    private final GuiListExtended.IGuiListEntry[] listEntries;
    private Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    public ButtonBindingList(ButtonBindingScreen screen, Minecraft mc, int widthIn, int heightIn, int topIn, int bottomIn, int itemHeightIn)
    {
        super(mc, widthIn, heightIn, topIn, bottomIn, itemHeightIn);
        this.screen = screen;

        // Initialize map with categories to have a predictable order (map is linked)
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());

        // Add all button bindings to the appropriate category or create a new one
        BindingRegistry.getInstance().getBindings().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
        {
            List<ButtonBinding> list = this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>());
            list.add(binding);
        });

        int size = 0;
        for(String entry : this.categories.keySet())
        {
            List<ButtonBinding> bindings = this.categories.get(entry);
            if(!bindings.isEmpty())
            {
                size += 1; //One for the category title
                size += this.categories.get(entry).size();
            }
        }
        this.listEntries = new IGuiListEntry[size];

        // Sorts the button binding list then adds new entries to the option list for each category
        int index = 0;
        for(String entry : this.categories.keySet())
        {
            List<ButtonBinding> bindings = this.categories.get(entry);
            if(!bindings.isEmpty())
            {
                Collections.sort(bindings);
                this.listEntries[index++] = new CategoryEntry(new TextComponentTranslation(entry));
                for(ButtonBinding binding : bindings)
                {
                    this.listEntries[index++] = new BindingEntry(binding);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if(this.screen.isWaitingForButtonInput())
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @Nullable
    public IGuiListEntry getListEntry(int index)
    {
        if(index >= 0 && index < this.getSize())
        {
            return this.listEntries[index];
        }
        return null;
    }

    @Override
    protected int getSize()
    {
        return this.listEntries.length;
    }

    abstract class Entry implements IGuiListEntry, IWidgetList
    {
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {};

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {};

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            return false;
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {};
    }

    protected class CategoryEntry extends Entry
    {
        private final ITextComponent label;
        private final int labelWidth;

        protected CategoryEntry(ITextComponent label)
        {
            this.label = label;
            this.labelWidth = ButtonBindingList.this.mc.fontRenderer.getStringWidth(this.label.getFormattedText());
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
        {
            int labelX = (int) (ButtonBindingList.this.mc.currentScreen.width / 2F - this.labelWidth / 2F);
            int labelY = y + slotHeight - 9 - 1;
            ButtonBindingList.this.mc.fontRenderer.drawString(this.label.getFormattedText(), labelX, labelY, 0xFFFFFFFF);
        }

        @Override
        public List<GuiButton> getWidgets()
        {
            return Collections.emptyList();
        }
    }

    protected class BindingEntry extends Entry
    {
        private ButtonBinding binding;
        private ITextComponent label;
        private ButtonBindingButton bindingButton;
        private ImageButton deleteButton;

        protected BindingEntry(ButtonBinding binding)
        {
            this.binding = binding;
            this.label = new TextComponentTranslation(binding.getDescription());
            this.bindingButton = new ButtonBindingButton(0, 0, binding, button -> {
                ButtonBindingList.this.screen.setSelectedBinding(this.binding);
            });
            this.deleteButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 108, 0, 16, 16, button -> {
                binding.reset();
                BindingRegistry registry = BindingRegistry.getInstance();
                registry.resetBindingHash();
                registry.save();
            });
        }

        @Override
        public List<GuiButton> getWidgets()
        {
            return ImmutableList.of(this.bindingButton, this.deleteButton);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void drawEntry(int index, int left, int top, int width, int slotHeight, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            TextFormatting color = this.binding.isConflictingContext() ? TextFormatting.RED : TextFormatting.GRAY;
            this.label.getStyle().setColor(color);
            ButtonBindingList.this.mc.fontRenderer.drawString(this.label.getFormattedText(), left - 15, top + 6, 0xFFFFFF);
            this.bindingButton.x = left + width - 45;
            this.bindingButton.y = top;
            this.bindingButton.drawButton(ButtonBindingList.this.mc, mouseX, mouseY, partialTicks);
            this.deleteButton.x = left + width - 20;
            this.deleteButton.y = top;
            this.deleteButton.enabled = !this.binding.isDefault();
            this.deleteButton.drawButton(ButtonBindingList.this.mc, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int button, int relativeX, int relativeY)
        {
            if(this.bindingButton.enabled && this.bindingButton.visible && this.bindingButton.isMouseOver())
            {
                if(button == 0)
                {
                    this.bindingButton.mousePressed(ButtonBindingList.this.mc, mouseX, mouseY);
                }
                else if(button == 1)
                {
                    this.binding.setButton(-1);
                }
                this.bindingButton.playPressSound(Minecraft.getMinecraft().getSoundHandler());
                return true;
            }
            if(this.deleteButton.enabled && this.deleteButton.visible && this.deleteButton.isMouseOver())
            {
                this.deleteButton.mousePressed(ButtonBindingList.this.mc, mouseX, mouseY);
                this.bindingButton.playPressSound(Minecraft.getMinecraft().getSoundHandler());
                return true;
            }
            return super.mousePressed(slotIndex, mouseX, mouseY, button, relativeX, relativeY);
        }
    }
}
