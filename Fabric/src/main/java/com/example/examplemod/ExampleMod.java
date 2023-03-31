package com.example.examplemod;

import net.fabricmc.api.ModInitializer;

public class ExampleMod implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        Constants.LOG.info("Hello Fabric world!");
    }
}
