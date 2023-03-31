package com.mrcrayfish.controllable.platform;

import com.mrcrayfish.controllable.platform.services.IClientHelper;
import com.mrcrayfish.framework.Constants;

import java.util.ServiceLoader;

public class ClientServices
{
    public static final IClientHelper CLIENT = load(IClientHelper.class);

    public static <T> T load(Class<T> clazz)
    {
        final T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}