// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LocalConnectionMethod.java

package com.dynatrace.diagnostics.remoteconnection;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.extendedexecutor.GenericExecutor;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

// Referenced classes of package com.dynatrace.diagnostics.remoteconnection:
//            ConnectionMethod, GEReturnObject

public class LocalConnectionMethod extends ConnectionMethod
    implements GEPluginConstants
{

    public LocalConnectionMethod()
    {
    }

    public GEReturnObject executeCommand(String command[], String env, long size, GEPluginProperties props)
        throws Exception
    {
        Process process;
        InputStream in;
        InputStream err;
        OutputStream out;
        OutputStream outErr;
        GEReturnObject obj;
        process = null;
        in = null;
        err = null;
        out = null;
        outErr = null;
        obj = null;
        try
        {
            ProcessBuilder pb = new ProcessBuilder(command);
            process = pb.start();
            Set callables = new HashSet();
            callables.add((new ProcessInput(in = process.getInputStream(), out = new ByteArrayOutputStream(), size)).getCallable());
            callables.add((new ProcessInput(err = process.getErrorStream(), outErr = new ByteArrayOutputStream(), size)).getCallable());
            callables.add((new ProcessWaitFor(process)).getCallable());
            Set tasks = getFutureTasks(callables);
            obj = getReturnObject(tasks, props, Arrays.toString(command));
            if(obj != null)
            {
                if(out != null)
                    obj.setStdout(out.toString());
                if(outErr != null)
                    obj.setStderr(outErr.toString());
            }
        }
        catch(Exception e)
        {
            throw e;
        }
        break MISSING_BLOCK_LABEL_295;
        Exception exception;
        exception;
        if(process != null)
        {
            OutputStream op = process.getOutputStream();
            process.destroy();
            if(op != null)
                try
                {
                    op.close();
                }
                catch(Exception _ex) { }
        }
        if(in != null)
            try
            {
                in.close();
            }
            catch(Exception _ex) { }
        if(err != null)
            try
            {
                err.close();
            }
            catch(Exception _ex) { }
        if(out != null)
            try
            {
                out.close();
            }
            catch(Exception _ex) { }
        if(outErr != null)
            try
            {
                outErr.close();
            }
            catch(Exception _ex) { }
        throw exception;
        if(process != null)
        {
            OutputStream op = process.getOutputStream();
            process.destroy();
            if(op != null)
                try
                {
                    op.close();
                }
                catch(Exception _ex) { }
        }
        if(in != null)
            try
            {
                in.close();
            }
            catch(Exception _ex) { }
        if(err != null)
            try
            {
                err.close();
            }
            catch(Exception _ex) { }
        if(out != null)
            try
            {
                out.close();
            }
            catch(Exception _ex) { }
        if(outErr != null)
            try
            {
                outErr.close();
            }
            catch(Exception _ex) { }
        return obj;
    }

    private Set getFutureTasks(Set callables)
    {
        Set tasks = new HashSet();
        FutureTask task;
        for(Iterator iterator = callables.iterator(); iterator.hasNext(); tasks.add(task))
        {
            Callable callable = (Callable)iterator.next();
            task = new FutureTask(callable);
            THREAD_POOL.execute(task);
        }

        return tasks;
    }

    private GEReturnObject getReturnObject(Set tasks, GEPluginProperties props, String cmd)
        throws UnsupportedEncodingException
    {
        Integer rc = null;
        StringBuilder sb = new StringBuilder();
        for(Iterator iterator = tasks.iterator(); iterator.hasNext();)
        {
            FutureTask task = (FutureTask)iterator.next();
            Object obj = null;
            try
            {
                obj = task.get(props.getTimeout(), TimeUnit.MILLISECONDS);
            }
            catch(TimeoutException e)
            {
                String msg = (new StringBuilder("executeCommand method: local method: command '")).append(cmd).append(": the timeout of ").append(props.getTimeout()).append(" milliseconds was exceeded. Stacktrace is '").append(HelperUtils.getExceptionAsString(e)).append("'").toString();
                GenericExecutor.log.severe(msg);
                throw new RuntimeException(msg, e);
            }
            catch(CancellationException e)
            {
                String msg = (new StringBuilder("executeCommand method: local method: command '")).append(cmd).append(": the computation was cancelled because of timeout of ").append(props.getTimeout()).append(" milliseconds was exceeded. Stacktrace is '").append(HelperUtils.getExceptionAsString(e)).append("'").toString();
                GenericExecutor.log.severe(msg);
                throw new RuntimeException(msg, e);
            }
            catch(ExecutionException e)
            {
                String msg = (new StringBuilder("executeCommand method: local method: command '")).append(cmd).append(": the computation threw an exception. Stacktrace is '").append(HelperUtils.getExceptionAsString(e)).append("'").toString();
                GenericExecutor.log.severe(msg);
                throw new RuntimeException(msg, e);
            }
            catch(InterruptedException e)
            {
                String msg = (new StringBuilder("executeCommand method: local method: command '")).append(cmd).append(": the current thread was interrupted while waiting because of timeout of ").append(props.getTimeout()).append(" milliseconds was exceeded. Stacktrace is '").append(HelperUtils.getExceptionAsString(e)).append("'").toString();
                GenericExecutor.log.severe(msg);
                throw new RuntimeException(msg, e);
            }
            catch(Exception e)
            {
                String msg = (new StringBuilder("executeCommand method: local method: command '")).append(cmd).append(" hasn't completed successfully. Stacktrace is '").append(HelperUtils.getExceptionAsString(e)).append("'").toString();
                GenericExecutor.log.severe(msg);
                throw new RuntimeException(msg, e);
            }
            if(task.isDone())
            {
                if(obj instanceof String)
                    sb.append("\n").append("*** stream ***").append("\n").append((String)obj).append("\n").append("*** End of stream ***");
                else
                if(obj instanceof Integer)
                {
                    rc = (Integer)obj;
                } else
                {
                    String msg = (new StringBuilder("executeCommand method: local method: command '")).append(cmd).append(": returned un-expected object type '").append(obj.getClass().getCanonicalName()).append("'. Expected object types are String and Integer.").toString();
                    GenericExecutor.log.severe(msg);
                    throw new RuntimeException(msg);
                }
            } else
            if(task.isCancelled())
                sb.append((new StringBuilder("executeCommand method: local method: command '")).append(cmd).append(": was cancelled").toString());
        }

        GEReturnObject rcObj = new GEReturnObject();
        rcObj.setOutput(sb.toString());
        rcObj.setRc(rc);
        return rcObj;
    }
}
