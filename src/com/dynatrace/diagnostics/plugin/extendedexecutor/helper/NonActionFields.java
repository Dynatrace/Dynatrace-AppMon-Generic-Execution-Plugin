// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NonActionFields.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;


public final class NonActionFields extends Enum
{

    private NonActionFields(String s, int i)
    {
        super(s, i);
    }

    public static NonActionFields getValue(String value)
    {
        try
        {
            return valueOf(value.replaceAll("-", "_").toUpperCase());
        }
        catch(Exception _ex)
        {
            return UNKNOWN;
        }
    }

    public static NonActionFields[] values()
    {
        NonActionFields anonactionfields[];
        int i;
        NonActionFields anonactionfields1[];
        System.arraycopy(anonactionfields = ENUM$VALUES, 0, anonactionfields1 = new NonActionFields[i = anonactionfields.length], 0, i);
        return anonactionfields1;
    }

    public static NonActionFields valueOf(String s)
    {
        return (NonActionFields)Enum.valueOf(com/dynatrace/diagnostics/plugin/extendedexecutor/helper/NonActionFields, s);
    }

    public static final NonActionFields HOST;
    public static final NonActionFields PORT;
    public static final NonActionFields START_TIME;
    public static final NonActionFields UNKNOWN;
    private static final NonActionFields ENUM$VALUES[];

    static 
    {
        HOST = new NonActionFields("HOST", 0);
        PORT = new NonActionFields("PORT", 1);
        START_TIME = new NonActionFields("START_TIME", 2);
        UNKNOWN = new NonActionFields("UNKNOWN", 3);
        ENUM$VALUES = (new NonActionFields[] {
            HOST, PORT, START_TIME, UNKNOWN
        });
    }
}
