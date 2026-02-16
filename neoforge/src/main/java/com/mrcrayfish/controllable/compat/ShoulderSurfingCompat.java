package com.mrcrayfish.controllable.compat;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import net.neoforged.fml.ModList;
import net.minecraft.client.KeyMapping;

public class ShoulderSurfingCompat
{
    public static void init()
    {
        if (isShoulderSurfingLoaded())
        {
            try
            {
                Class<?> inputHandler = Class.forName("com.github.exopandora.shouldersurfing.client.InputHandler");
                String[] fields = {
                    "CAMERA_LEFT", "CAMERA_RIGHT", "CAMERA_IN", "CAMERA_OUT", "CAMERA_UP", "CAMERA_DOWN",
                    "SWAP_SHOULDER", "TOGGLE_FIRST_PERSON", "TOGGLE_THIRD_PERSON_FRONT", "TOGGLE_THIRD_PERSON_BACK",
                    "FREE_LOOK", "TOGGLE_CAMERA_COUPLING", "TOGGLE_X_OFFSET_PRESETS", "TOGGLE_Y_OFFSET_PRESETS",
                    "TOGGLE_Z_OFFSET_PRESETS", "ENTER_FIRST_PERSON", "ENTER_THIRD_PERSON_FRONT",
                    "ENTER_THIRD_PERSON_BACK", "ENTER_SHOULDER_SURFING"
                };

                for (String field : fields)
                {
                    // These are all static fields of type KeyMapping
                    KeyMapping keyMapping = (KeyMapping) inputHandler.getField(field).get(null);
                    Controllable.getBindingRegistry().register(
                        new KeyAdapterBinding(0, keyMapping)
                    );
                    System.out.println("[Controllable] Registered ShoulderSurfing key: " + field + " (" + keyMapping.getName() + ")");
                }
            }
            catch (Throwable t)
            {
                // Swallow exceptions—compat is best-effort only!
            }
        }
    }

    private static boolean isShoulderSurfingLoaded()
    {
        return ModList.get().isLoaded("shouldersurfing");
    }
}