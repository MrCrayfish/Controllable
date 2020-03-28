package com.mrcrayfish.controllable;

import net.minecraft.client.resources.I18n;

public class LocaleUtil {

    public static String booleanLocale(boolean val) {
        return I18n.format(val ? "options.on" : "options.off");
    }

}
