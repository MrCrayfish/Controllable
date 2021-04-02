package com.mrcrayfish.controllable.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
public interface IControllerListener
{
    @OnlyIn(Dist.CLIENT)
    void connected(int jid);

    @OnlyIn(Dist.CLIENT)
    void disconnected(int jid);
}
