package com.mrcrayfish.controllable.asm;

/**
 * Author: MrCrayfish
 */
public class ObfName
{
    private String obfName;
    private String deObfName;

    public ObfName(String obfName, String deObfName)
    {
        this.obfName = obfName;
        this.deObfName = deObfName;
    }

    public boolean equals(String name)
    {
        return this.obfName.equals(name) || this.deObfName.equals(name);
    }
}
