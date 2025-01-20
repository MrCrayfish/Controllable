package com.mrcrayfish.controllable.client.gui.screens;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.gui.ButtonBindingData;
import com.mrcrayfish.controllable.client.gui.Icons;
import com.mrcrayfish.controllable.client.gui.RadialItemList;
import com.mrcrayfish.controllable.client.util.ClientHelper;
import com.mrcrayfish.controllable.client.util.ScreenHelper;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class RadialMenuConfigureScreen extends Screen
{
    private final List<ButtonBindingData> bindings;
    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private RadialItemList list;

    public RadialMenuConfigureScreen(LinkedHashSet<ButtonBindingData> bindings)
    {
        super(Component.translatable("controllable.gui.title.radial_menu_configure"));
        this.bindings = new ArrayList<>(bindings);
    }

    @Override
    protected void init()
    {
        LinearLayout headerLayout = this.layout.addToHeader(LinearLayout.vertical());
        headerLayout.addChild(new StringWidget(this.title, this.font));

        this.list = new RadialItemList(this.minecraft, this.bindings);
        this.layout.addToContents(this.list);

        LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(4));
        Component saveLabel = ClientHelper.join(Icons.SAVE, Component.translatable("controllable.gui.save"));
        footerLayout.addChild(ScreenHelper.button(this.width / 2 - 155, this.height - 29, 100, 20, saveLabel, buttons -> {
            Controllable.getRadialMenu().setBindings(new LinkedHashSet<>(this.bindings));
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));
        Component addLabel = ClientHelper.join(Icons.ADD, Component.translatable("controllable.gui.add_binding"));
        footerLayout.addChild(ScreenHelper.button(this.width / 2 - 50, this.height - 29, 100, 20, addLabel, buttons -> {
            Objects.requireNonNull(this.minecraft).setScreen(new RadialMenuAddBindingsScreen(this));
        }));
        footerLayout.addChild(ScreenHelper.button(this.width / 2 + 55, this.height - 29, 100, 20, CommonComponents.GUI_CANCEL, buttons -> {
            Objects.requireNonNull(this.minecraft).setScreen(null);
        }));

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements()
    {
        this.layout.arrangeElements();
        this.list.updateEntries();
        this.list.updateSize(this.width, this.layout);
    }

    public List<ButtonBindingData> getBindings()
    {
        return this.bindings;
    }

    public void scrollToBottomAndSelectLast()
    {
        this.list.setScrollAmount(this.list.getMaxScroll());
        this.list.setSelected(this.list.children().getLast());
    }
}
