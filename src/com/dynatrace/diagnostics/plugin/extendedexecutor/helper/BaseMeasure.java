// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BaseMeasure.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;


public class BaseMeasure
{

    public BaseMeasure(String baseMeasure, String metricGroup)
    {
        this.metricGroup = metricGroup;
        this.baseMeasure = baseMeasure;
    }

    public String getMetricGroup()
    {
        return metricGroup;
    }

    public void setMetricGroup(String metricGroup)
    {
        this.metricGroup = metricGroup;
    }

    public String getBaseMeasure()
    {
        return baseMeasure;
    }

    public void setBaseMeasure(String baseMeasure)
    {
        this.baseMeasure = baseMeasure;
    }

    public String toString()
    {
        return (new StringBuilder("BaseMeasure [metricGroup=")).append(metricGroup).append(", baseMeasure=").append(baseMeasure).append("]").toString();
    }

    private String metricGroup;
    private String baseMeasure;
}
