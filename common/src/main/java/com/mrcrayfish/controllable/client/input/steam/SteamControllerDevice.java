package com.mrcrayfish.controllable.client.input.steam;

import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.DeviceInfo;
import org.hid4java.HidDevice;

record SteamControllerDevice(int index, int jid, HidDevice hidDevice, String name, String serial, int vendor, int product, int productVersion)
{
    String path()
    {
        return this.hidDevice.getPath();
    }

    DeviceInfo toDeviceInfo()
    {
        return new DeviceInfo(this.name, "steam-controller-hid", this.serial, -1, this.vendor, this.product, this.productVersion, -1, Buttons.LENGTH, 6);
    }
}
