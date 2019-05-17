// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   WSElement.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import com.predic8.schema.Element;
import groovy.xml.QName;

public class WSElement extends Element
{

    public WSElement(String name, QName qName)
    {
        this.name = name;
        this.qName = qName;
    }

    public String getName()
    {
        return name;
    }

    public QName getType()
    {
        return qName;
    }

    String name;
    QName qName;
}
