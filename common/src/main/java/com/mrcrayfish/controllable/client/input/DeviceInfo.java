package com.mrcrayfish.controllable.client.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public record DeviceInfo(@Nullable String name,
                         @Nullable String guid,
                         @Nullable String serial,
                         int type,
                         int vendor,
                         int product,
                         int productVersion,
                         int firmware,
                         int buttons,
                         int axes)
{
    public static final DeviceInfo EMPTY = new DeviceInfo(null, null, null, -1, -1, -1, -1, -1, -1, -1);

    public static @Nullable DeviceInfo fromJson(JsonElement element)
    {
        if(!element.isJsonObject())
            return null;

        JsonObject object = (JsonObject) element;
        String name = GsonHelper.getAsString(object, "name", null);
        String guid = GsonHelper.getAsString(object, "guid", null);
        String serial = GsonHelper.getAsString(object, "serial", null);
        int type = GsonHelper.getAsInt(object, "type", -1);
        int vendor = GsonHelper.getAsInt(object, "vendor", -1);
        int product = GsonHelper.getAsInt(object, "product", -1);
        int productVersion = GsonHelper.getAsInt(object, "product_version", -1);
        int firmware = GsonHelper.getAsInt(object, "firmware", -1);
        int buttons = GsonHelper.getAsInt(object, "buttons", -1);
        int axes = GsonHelper.getAsInt(object, "axes", -1);
        return new DeviceInfo(name, guid, serial, type, vendor, product, productVersion, firmware, buttons, axes);
    }

    public JsonObject toJson()
    {
        JsonObject object = new JsonObject();
        if(this.name != null) object.addProperty("name", this.name);
        if(this.guid != null) object.addProperty("guid", this.guid);
        if(this.serial != null) object.addProperty("serial", this.serial);
        if(this.type != -1) object.addProperty("type", this.type);
        if(this.vendor != -1) object.addProperty("vendor", this.vendor);
        if(this.product != -1) object.addProperty("product", this.product);
        if(this.productVersion != -1) object.addProperty("product_version", this.productVersion);
        if(this.firmware != -1) object.addProperty("firmware", this.firmware);
        if(this.buttons != -1) object.addProperty("buttons", this.buttons);
        if(this.axes != -1) object.addProperty("axes", this.axes);
        return object;
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof DeviceInfo that))
            return false;

        if(this.type != that.type)
            return false;

        if(this.axes != that.axes)
            return false;

        if(this.vendor != that.vendor)
            return false;

        if(this.product != that.product)
            return false;

        if(this.productVersion != that.productVersion)
            return false;

        if(this.firmware != that.firmware)
            return false;

        if(this.buttons != that.buttons)
            return false;

        if(!Objects.equals(this.name, that.name))
            return false;

        if(!Objects.equals(this.guid, that.guid))
            return false;

        return Objects.equals(this.serial, that.serial);
    }
}
