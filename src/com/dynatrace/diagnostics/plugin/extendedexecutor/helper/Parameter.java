// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Parameter.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;


public class Parameter
{

    public String toString()
    {
        return (new StringBuilder("Parameter [name=")).append(name).append(", type=").append(type).append("]").toString();
    }

    public Parameter(String name, String type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    private String name;
    private String type;
}
