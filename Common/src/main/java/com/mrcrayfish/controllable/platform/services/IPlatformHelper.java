package com.mrcrayfish.controllable.platform.services;

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
}
