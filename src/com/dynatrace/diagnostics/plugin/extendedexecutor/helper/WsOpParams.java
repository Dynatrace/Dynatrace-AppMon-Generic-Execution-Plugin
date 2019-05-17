// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WsOpParams.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.util.LinkedHashMap;
import java.util.List;

public class WsOpParams
{

    public WsOpParams(List argList, LinkedHashMap complexTypes)
    {
        this.argList = argList;
        this.complexTypes = complexTypes;
    }

    public List getArgList()
    {
        return argList;
    }

    public LinkedHashMap getComplexTypes()
    {
        return complexTypes;
    }

    private List argList;
    private LinkedHashMap complexTypes;
}
