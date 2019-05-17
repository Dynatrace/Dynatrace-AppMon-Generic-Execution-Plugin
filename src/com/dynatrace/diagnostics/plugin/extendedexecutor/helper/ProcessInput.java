// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProcessInput.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.extendedexecutor.GenericExecutor;
import java.io.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;

// Referenced classes of package com.dynatrace.diagnostics.plugin.extendedexecutor.helper:
//            GEPluginConstants

public class ProcessInput
    implements GEPluginConstants
{

    public ProcessInput(InputStream in, OutputStream out, long size)
    {
        this.in = in;
        this.out = out;
        this.size = size;
    }

    public Callable getCallable()
    {
        Callable callable = new Callable() {

            public String call()
                throws Exception
            {
                try
                {
                    int i = -1;
                    byte buf[] = new byte[1024];
                    int bufSize = 0;
                    for(boolean isWriting = true; isWriting && (i = in.read(buf)) != -1;)
                        if((long)(bufSize += i) <= size)
                        {
                            out.write(buf, 0, i);
                        } else
                        {
                            byte bf1[] = new byte[i];
                            System.arraycopy(buf, 0, bf1, 0, i);
                            out.write(ArrayUtils.addAll(bf1, " ...buffer size is exceeded".getBytes(ProcessInput.DEFAULT_ENCODING)), 0, i + " ...buffer size is exceeded".length());
                            isWriting = false;
                        }

                }
                catch(Exception e)
                {
                    try
                    {
                        GenericExecutor.log.severe(HelperUtils.getExceptionAsString(e));
                    }
                    catch(UnsupportedEncodingException _ex) { }
                }
                return out.toString();
            }

            public volatile Object call()
                throws Exception
            {
                return call();
            }

            final ProcessInput this$0;

            
            {
                this$0 = ProcessInput.this;
                super();
            }
        }
;
        return callable;
    }

    private final InputStream in;
    private final OutputStream out;
    private final long size;



}
