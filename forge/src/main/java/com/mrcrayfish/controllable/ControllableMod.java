package com.mrcrayfish.controllable;

import com.mrcrayfish.controllable.client.ClientBootstrap;
import com.mrcrayfish.controllable.client.ScreenEvents;
import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Author: MrCrayfish
 */
@Mod(Constants.MOD_ID)
public class ControllableMod
{
    public ControllableMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onLoadComplete);
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            ClientBootstrap.init();
            MinecraftForge.EVENT_BUS.register(new ScreenEvents());
        });
    }

    private void onLoadComplete(FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            BindingRegistry.getInstance().load();
            Controllable.getManager().onClientFinishedLoading();
        });
    }
}
