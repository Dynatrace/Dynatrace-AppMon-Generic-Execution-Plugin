// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GEAction.java

package com.dynatrace.diagnostics.plugin.extendedexecutor;

import com.dynatrace.diagnostics.pdk.*;

// Referenced classes of package com.dynatrace.diagnostics.plugin.extendedexecutor:
//            GenericExecutor

public class GEAction extends GenericExecutor
    implements Action
{

    public GEAction()
    {
    }

    public Status setup(ActionEnvironment env)
        throws Exception
    {
        return super.setup(env);
    }

    public Status execute(ActionEnvironment env)
        throws Exception
    {
        return super.execute(env);
    }

    public void teardown(ActionEnvironment env)
        throws Exception
    {
        super.teardown(env);
    }
}
