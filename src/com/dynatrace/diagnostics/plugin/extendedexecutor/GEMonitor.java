// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GEMonitor.java

package com.dynatrace.diagnostics.plugin.extendedexecutor;

import com.dynatrace.diagnostics.pdk.*;

// Referenced classes of package com.dynatrace.diagnostics.plugin.extendedexecutor:
//            GenericExecutor

public class GEMonitor extends GenericExecutor
    implements Monitor
{

    public GEMonitor()
    {
    }

    public Status setup(MonitorEnvironment env)
        throws Exception
    {
        return super.setup(env);
    }

    public Status execute(MonitorEnvironment env)
        throws Exception
    {
        Status result = super.execute(env);
        return result;
    }

    public void teardown(MonitorEnvironment env)
        throws Exception
    {
        super.teardown(env);
    }
}
