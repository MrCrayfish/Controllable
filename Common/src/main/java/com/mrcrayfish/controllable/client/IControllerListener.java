package com.mrcrayfish.controllable.client;

/**
 * Author: MrCrayfish
 */
public interface IControllerListener
{
    void connected(int jid);

    void disconnected(int jid);
}
