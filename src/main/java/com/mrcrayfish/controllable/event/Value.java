package com.mrcrayfish.controllable.event;

/**
 * Author: MrCrayfish
 */
public class Value<T>
{
    private T value;

    public Value(T initialValue)
    {
        this.value = initialValue;
    }

    public void set(T value)
    {
        this.value = value;
    }

    public T get()
    {
        return this.value;
    }
}