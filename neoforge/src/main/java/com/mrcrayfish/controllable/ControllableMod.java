package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

/**
 * Author: MrCrayfish
 */
@Mod(Constants.MOD_ID)
public class ControllableMod
{
    public ControllableMod(IEventBus bus)
    {
        bus.addListener(this::onLoadComplete);
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
    }

    private void onLoadComplete(FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            BindingRegistry.getInstance().load();
            Controllable.getManager().onClientFinishedLoading();
        });
    }
}
