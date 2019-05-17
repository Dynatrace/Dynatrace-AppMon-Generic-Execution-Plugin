// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WSReturnedMeasures.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.util.Arrays;

public class WSReturnedMeasures
{

    public WSReturnedMeasures(int size)
    {
        this.size = size;
        names = new String[size];
        values = new String[size];
    }

    public String[] getNames()
    {
        return names;
    }

    public void setNames(String names[])
    {
        this.names = names;
    }

    public String[] getValues()
    {
        return values;
    }

    public void setValues(String values[])
    {
        this.values = values;
    }

    public int getSize()
    {
        return size;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("WSReturnedMeasures [names=").append(Arrays.toString(names)).append(", values=").append(Arrays.toString(values)).append("]");
        return builder.toString();
    }

    private String names[];
    private String values[];
    private int size;
}
