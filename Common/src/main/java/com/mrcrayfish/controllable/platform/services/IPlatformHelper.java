package com.mrcrayfish.controllable.platform.services;

import java.nio.file.Path;

/**
 * Author: MrCrayfish
 */
public interface IPlatformHelper
{
   default boolean isForge()
   {
      return false;
   }

   default boolean isFabric()
   {
      return false;
   }

   Path getConfigPath();
}
