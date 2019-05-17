// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GETask.java

package com.dynatrace.diagnostics.plugin.extendedexecutor;

import com.dynatrace.diagnostics.pdk.*;

// Referenced classes of package com.dynatrace.diagnostics.plugin.extendedexecutor:
//            GenericExecutor

public class GETask extends GenericExecutor
    implements Task
{

    public GETask()
    {
    }

    public Status setup(TaskEnvironment env)
        throws Exception
    {
        return super.setup(env);
    }

    public Status execute(TaskEnvironment env)
        throws Exception
    {
        return super.execute(env);
    }

    public void teardown(TaskEnvironment env)
        throws Exception
    {
        super.teardown(env);
    }
}
