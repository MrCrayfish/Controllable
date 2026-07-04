package com.mrcrayfish.controllable.client.input.steam;

import com.google.common.io.ByteStreams;
import com.mrcrayfish.controllable.Config;
import com.mrcrayfish.controllable.Constants;
import com.mrcrayfish.controllable.client.input.AdaptiveControllerManager;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.input.DeviceInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;
import org.hid4java.ScanMode;
import org.hid4java.jna.HidApi;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SteamControllerManager extends AdaptiveControllerManager
{
    private static final int VALVE_VENDOR_ID = 0x28DE;
    private static final int VENDOR_USAGE_PAGE = 0xFF00;
    private static final Set<Integer> STEAM_CONTROLLER_PRODUCTS = Set.of(0x1302, 0x1303, 0x1304, 0x1142);
    private static final int JID_BASE = 0x5C000000;

    private HidServices hidServices;
    private List<SteamControllerDevice> devices = List.of();
    private long lastRefreshTime;

    @Override
    public void init()
    {
        try
        {
            HidServicesSpecification specification = new HidServicesSpecification();
            specification.setAutoStart(false);
            specification.setAutoDataRead(false);
            specification.setScanMode(ScanMode.NO_SCAN);
            // macos opens HID devices exclusively by default.
            // non-exclusive opens coexist with Steam, Lunar, and macos
            HidApi.darwinOpenDevicesNonExclusive = true;
            this.hidServices = HidManager.getHidServices(specification);
            this.hidServices.start();
            this.refreshDevices(true);
            this.controllers = this.createRawControllerMap();
        }
        catch(RuntimeException e)
        {
            Constants.LOG.error("Unable to initialize Steam Controller HID backend", e);
            this.devices = List.of();
            this.controllers = Map.of();
        }
    }

    @Override
    public void dispose()
    {
        this.setActiveController(null);
        if(this.hidServices != null)
        {
            try
            {
                this.hidServices.stop();
            }
            catch(RuntimeException e)
            {
                Constants.LOG.debug("Unable to stop Steam Controller HID backend cleanly", e);
            }
            this.hidServices = null;
        }
    }

    @Override
    public Controller createController(int deviceIndex, Number jid)
    {
        SteamControllerDevice device = this.findDevice(deviceIndex, jid);
        if(device == null)
            throw new IllegalArgumentException("Unknown Steam Controller device: index=" + deviceIndex + " jid=" + jid);
        return new SteamController(device);
    }

    @Override
    @Nullable
    public Controller connectToBestGameController()
    {
        this.refreshDevices(false);
        List<DeviceInfo> lastDevices = this.getLastDevices();
        if(!lastDevices.isEmpty())
        {
            for(DeviceInfo info : lastDevices)
            {
                for(SteamControllerDevice device : this.devices)
                {
                    if(device.toDeviceInfo().equals(info))
                    {
                        SteamController controller = new SteamController(device);
                        if(this.setActiveController(controller))
                            return controller;
                    }
                }
            }
        }

        if(!this.devices.isEmpty())
        {
            SteamController controller = new SteamController(this.devices.getFirst());
            if(this.setActiveController(controller))
                return controller;
        }
        return null;
    }

    @Override
    public void updateMappings(InputStream is) throws IOException
    {
        ByteStreams.exhaust(is);
        Constants.LOG.info("Steam Controller HID backend uses built-in Triton mappings");
    }

    @Override
    protected int getRawControllerCount()
    {
        this.refreshDevices(false);
        return this.devices.size();
    }

    @Override
    protected Map<Number, Pair<Integer, String>> createRawControllerMap()
    {
        Map<Number, Pair<Integer, String>> controllers = new HashMap<>();
        for(SteamControllerDevice device : this.devices)
        {
            controllers.put(device.jid(), Pair.of(device.index(), device.name()));
        }
        return controllers;
    }

    @Override
    public void tick()
    {
        this.refreshDevices(false);
        Map<Number, Pair<Integer, String>> nextControllers = this.createRawControllerMap();
        if(!nextControllers.keySet().equals(this.controllers.keySet()))
        {
            Controller activeController = this.getActiveController();
            if(activeController != null && !nextControllers.containsKey(activeController.getJid()))
            {
                this.sendControllerToast(false, activeController);
                this.setActiveController(null);
                activeController = null;
            }
            this.controllers = nextControllers;
            if(this.ready && activeController == null && Config.CLIENT.options.autoSelect.get())
            {
                Controller connected = this.connectToBestGameController();
                this.sendControllerToast(true, connected);
            }
        }

        Controller activeController = this.getActiveController();
        if(activeController instanceof SteamController steamController)
        {
            steamController.tick();
            if(!steamController.isOpen())
            {
                this.sendControllerToast(false, steamController);
                this.setActiveController(null);
            }
        }
    }

    private SteamControllerDevice findDevice(int deviceIndex, Number jid)
    {
        this.refreshDevices(false);
        for(SteamControllerDevice device : this.devices)
        {
            if(device.index() == deviceIndex || device.jid() == jid.intValue())
                return device;
        }
        return null;
    }

    private void refreshDevices(boolean force)
    {
        if(this.hidServices == null)
            return;

        long now = System.currentTimeMillis();
        if(!force && now - this.lastRefreshTime < 1000)
            return;

        this.lastRefreshTime = now;
        List<SteamControllerDevice> nextDevices = new ArrayList<>();
        List<HidDevice> allDevices = this.enumerateValveDevices().stream()
            .sorted(Comparator.comparing(device -> device.getPath() == null ? "" : device.getPath()))
            .toList();

        List<HidDevice> preferred = allDevices.stream()
            .filter(SteamControllerManager::isSteamController)
            .filter(device -> usagePage(device) == VENDOR_USAGE_PAGE)
            .toList();
        // steam controller exposes multiple HID interfaces, so the vendor page is the one that carries Valve reports
        List<HidDevice> candidates = preferred.isEmpty() ? allDevices.stream().filter(SteamControllerManager::isSteamController).toList() : preferred;

        Set<String> seenPaths = new HashSet<>();
        for(HidDevice device : candidates)
        {
            if(device.getPath() == null || !seenPaths.add(device.getPath()))
                continue;

            int index = nextDevices.size();
            int product = device.getProductId();
            int jid = JID_BASE | ((product & 0xFFFF) << 8) | (index & 0xFF);
            String name = device.getProduct() != null && !device.getProduct().isBlank() ? device.getProduct() : "Steam Controller";
            if(!name.toLowerCase().contains("steam"))
            {
                name = "Steam Controller";
            }
            nextDevices.add(new SteamControllerDevice(index, jid, device, name + " (Steam HID)",
                device.getSerialNumber(), device.getVendorId(), product, releaseNumber(device)));
        }
        this.devices = List.copyOf(nextDevices);
    }

    private List<HidDevice> enumerateValveDevices()
    {
        try
        {
            return this.hidServices.getAttachedHidDevices().stream()
                .filter(device -> device.getVendorId() == VALVE_VENDOR_ID)
                .toList();
        }
        catch(RuntimeException e)
        {
            Constants.LOG.debug("Unable to enumerate Steam Controller HID devices", e);
            return List.of();
        }
    }

    private static boolean isSteamController(HidDevice device)
    {
        return device.getVendorId() == VALVE_VENDOR_ID && STEAM_CONTROLLER_PRODUCTS.contains(device.getProductId());
    }

    private static int releaseNumber(HidDevice device)
    {
        return device.getReleaseNumber() & 0xFFFF;
    }

    private static int usagePage(HidDevice device)
    {
        return device.getUsagePage() & 0xFFFF;
    }
}
