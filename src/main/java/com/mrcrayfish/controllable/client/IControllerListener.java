package com.mrcrayfish.controllable.client;

import uk.co.electronstudio.sdl2gdx.SDL2Controller;

/**
 * Author: MrCrayfish
 */
public interface IControllerListener
{
    void connected(int jid);

    void disconnected(int jid);
}
