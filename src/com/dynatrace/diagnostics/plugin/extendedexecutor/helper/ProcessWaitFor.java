// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProcessWaitFor.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.extendedexecutor.GenericExecutor;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class ProcessWaitFor
{

    public ProcessWaitFor(Process process)
    {
        this.process = process;
    }

    public Callable getCallable()
    {
        Callable callable = new Callable() {

            public Object call()
                throws Exception
            {
                int rc;
                try
                {
                    rc = process.waitFor();
                }
                catch(Exception e)
                {
                    String msg = (new StringBuilder("executeCommand method: InterruptedException was thrown when waiting for ProcessBuilder.waitFor method to complete. Stacktrace is '")).append(HelperUtils.getExceptionAsString(e)).append("'").toString();
                    GenericExecutor.log.severe(msg);
                    throw e;
                }
                return Integer.valueOf(rc);
            }

            final ProcessWaitFor this$0;

            
            {
                this$0 = ProcessWaitFor.this;
                super();
            }
        }
;
        return callable;
    }

    private final Process process;

}
