package com.mrcrayfish.controllable.client;

import org.libsdl.SDL_Error;
import uk.co.electronstudio.sdl2gdx.SDL2Controller;
import uk.co.electronstudio.sdl2gdx.SDL2ControllerManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class ControllerManager
{
    private SDL2ControllerManager manager = new SDL2ControllerManager();
    private Set<IControllerListener> listeners = new HashSet<>();
    private Map<Integer, String> controllers = new HashMap<>();

    public void update()
    {
        try
        {
            manager.pollState();
        }
        catch(SDL_Error error)
        {
            error.printStackTrace();
        }

        int connectedCount = 0;
        for(com.badlogic.gdx.controllers.Controller controller : this.manager.getControllers())
        {
            if(controller instanceof SDL2Controller)
            {
                connectedCount++;
            }
        }

        if(connectedCount == this.controllers.size())
            return;

        Map<Integer, String> oldControllers = this.controllers;
        Map<Integer, String> newControllers = new HashMap<>();

        for(int i = 0; i < this.manager.getControllers().size; i++)
        {
            com.badlogic.gdx.controllers.Controller controller = this.manager.getControllers().get(i);
            if(controller instanceof SDL2Controller)
            {
                SDL2Controller sdl2Controller = (SDL2Controller) controller;
                newControllers.put(sdl2Controller.joystick.instanceID.id, sdl2Controller.getName());
            }
        }

        this.controllers = newControllers;

        newControllers.forEach((jid, pair) ->
        {
            if(!oldControllers.containsKey(jid))
            {
                this.listeners.forEach(listener -> listener.connected(jid));
            }
        });

        oldControllers.forEach((jid, pair) ->
        {
            if(!newControllers.containsKey(jid))
            {
                this.listeners.forEach(listener -> listener.disconnected(jid));
            }
        });
    }

    @Nullable
    public String getName(int jid)
    {
        return this.controllers.get(jid);
    }

    @Nullable
    public SDL2Controller getSDL2ControllerById(int jid)
    {
        for(com.badlogic.gdx.controllers.Controller controller : this.manager.getControllers())
        {
            if(controller instanceof SDL2Controller && ((SDL2Controller) controller).joystick.instanceID.id == jid)
            {
                return (SDL2Controller) controller;
            }
        }
        return null;
    }

    public int getFirstControllerJid()
    {
        for(com.badlogic.gdx.controllers.Controller controller : this.manager.getControllers())
        {
            if(controller instanceof SDL2Controller)
            {
                return ((SDL2Controller) controller).joystick.instanceID.id;
            }
        }
        return -1;
    }

    public Map<Integer, String> getControllers()
    {
        return this.controllers;
    }

    public int getControllerCount()
    {
        return this.controllers.size();
    }

    public void addControllerListener(IControllerListener listener)
    {
        this.listeners.add(listener);
    }

    public void close()
    {
        this.manager.close();
    }
}
