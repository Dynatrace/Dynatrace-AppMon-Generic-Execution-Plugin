// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ConnectionMethod.java

package com.dynatrace.diagnostics.remoteconnection;

import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.GEPluginProperties;
import java.io.*;

// Referenced classes of package com.dynatrace.diagnostics.remoteconnection:
//            LocalConnectionMethod, SSHConnectionMethod, GEReturnObject

public abstract class ConnectionMethod
{

    public ConnectionMethod()
    {
    }

    public abstract GEReturnObject executeCommand(String as[], String s, long l, GEPluginProperties gepluginproperties)
        throws Exception;

    public void setup(String s3, String s4, String s5, int j)
        throws Exception
    {
    }

    public void teardown()
        throws Exception
    {
    }

    protected String readInputStream(InputStream is, long size)
        throws IOException
    {
        StringBuilder strBuild = new StringBuilder();
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(is, DEFAULT_ENCODING));
        int bufSize = 0;
        do
        {
            String line = stdoutReader.readLine();
            if(line != null && (long)(bufSize += line.length()) > size)
                return strBuild.append(line).append(" ...buffer size is exceeded").append("\n").toString();
            if(line != null)
            {
                strBuild.append((new StringBuilder(String.valueOf(line))).append("\n").toString());
            } else
            {
                int index = strBuild.lastIndexOf("\n");
                return index >= 0 ? strBuild.substring(0, index) : "";
            }
        } while(true);
    }

    public static ConnectionMethod getConnectionMethod(String method)
    {
        if(method.equals("LOCAL"))
            return new LocalConnectionMethod();
        else
            return new SSHConnectionMethod();
    }

    protected static final String PROPS_FILE_ENCODING = "file.encoding";
    protected static final String DEFAULT_ENCODING = System.getProperty("file.encoding");

}
