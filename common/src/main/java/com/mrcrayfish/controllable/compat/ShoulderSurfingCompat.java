package com.mrcrayfish.controllable.compat;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import net.neoforged.fml.ModList;

public class ShoulderSurfingCompat
{
    public static void init()
    {
        if (isShoulderSurfingLoaded())
        {
            try
            {
                // Use reflection so your mod compiles and runs if Shoulder Surfing is NOT installed
                Class<?> inputHandler = Class.forName("com.github.exopandora.shouldersurfing.client.InputHandler");
                String[][] registrations = {
                    { "CAMERA_LEFT", "adjust_camera_left" },
                    { "CAMERA_RIGHT", "adjust_camera_right" },
                    { "CAMERA_IN", "adjust_camera_in" },
                    { "CAMERA_OUT", "adjust_camera_out" },
                    { "CAMERA_UP", "adjust_camera_up" },
                    { "CAMERA_DOWN", "adjust_camera_down" },
                    { "SWAP_SHOULDER", "swap_shoulder" },
                    { "TOGGLE_FIRST_PERSON", "toggle_first_person" },
                    { "TOGGLE_THIRD_PERSON_FRONT", "toggle_third_person_front" },
                    { "TOGGLE_THIRD_PERSON_BACK", "toggle_third_person_back" },
                    { "FREE_LOOK", "free_look" },
                    { "TOGGLE_CAMERA_COUPLING", "toggle_camera_coupling" },
                    { "TOGGLE_X_OFFSET_PRESETS", "toggle_x_offset_presets" },
                    { "TOGGLE_Y_OFFSET_PRESETS", "toggle_y_offset_presets" },
                    { "TOGGLE_Z_OFFSET_PRESETS", "toggle_z_offset_presets" },
                    { "ENTER_FIRST_PERSON", "enter_first_person" },
                    { "ENTER_THIRD_PERSON_FRONT", "enter_third_person_front" },
                    { "ENTER_THIRD_PERSON_BACK", "enter_third_person_back" },
                    { "ENTER_SHOULDER_SURFING", "enter_shoulder_surfing" }
                };

                for (String[] reg : registrations)
                {
                    Object keyMapping = inputHandler.getField(reg[0]).get(null);
                    Controllable.getBindingRegistry().register(
                        new KeyAdapterBinding(
                            keyMapping,
                            "shouldersurfing.key." + reg[1],
                            "key.categories.shouldersurfing"
                        )
                    );
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