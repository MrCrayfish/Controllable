package com.mrcrayfish.controllable.client;

import com.mrcrayfish.controllable.client.gui.widget.TabNavigationHint;
import com.mrcrayfish.controllable.client.util.ReflectUtil;
import com.mrcrayfish.framework.api.event.ScreenEvents;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;

/**
 * Author: MrCrayfish
 */
public class ClientEvents
{
    private static boolean navigationBarCheck;

    public static void init()
    {
        //TODO move to common
        ScreenEvents.INIT.register(screen -> {
            navigationBarCheck = false;
        });
        ScreenEvents.BEFORE_DRAW.register((screen, poseStack, i, i1, v) -> {
            if(!navigationBarCheck) {
                navigationBarCheck = true;
                screen.children().stream().filter(e -> e instanceof TabNavigationBar).map(e -> (TabNavigationBar) e).findFirst().ifPresent(bar -> {
                    ReflectUtil.addRenderable(screen, new TabNavigationHint(bar.children()));
                });
            }
        });
    }
}
