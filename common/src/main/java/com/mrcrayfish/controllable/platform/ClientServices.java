package com.mrcrayfish.controllable.platform;

import com.mrcrayfish.controllable.platform.services.IClientHelper;

public class ClientServices
{
    public static final IClientHelper CLIENT = Services.load(IClientHelper.class);
}