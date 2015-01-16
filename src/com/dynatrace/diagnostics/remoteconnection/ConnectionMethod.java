// Decompiled by DJ v3.12.12.96 Copyright 2011 Atanas Neshkov  Date: 6/25/2013 8:23:50 AM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: fullnames lnc 
// Source File Name:   ConnectionMethod.java

package com.dynatrace.diagnostics.remoteconnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// Referenced classes of package com.dynatrace.diagnostics.remoteconnection:
//            LocalConnectionMethod, SSHConnectionMethod

public abstract class ConnectionMethod
{
			protected static final String PROPS_FILE_ENCODING = "file.encoding";
			protected static final String DEFAULT_ENCODING = System.getProperty(PROPS_FILE_ENCODING);
			
            public ConnectionMethod()
            {
            }

            public abstract GEReturnObject executeCommand(java.lang.String s, java.lang.String s1, long size)
                throws java.lang.Exception;

            public void setup(java.lang.String s, java.lang.String s1, java.lang.String s2, int i)
                throws java.lang.Exception
            {
            }

            public void teardown()
                throws java.lang.Exception
            {
            }

            protected java.lang.String readInputStream(java.io.InputStream is, long size)
                throws java.io.IOException
            {
/*  19*/        java.lang.StringBuilder strBuild = new StringBuilder();
/*  20*/        java.io.BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(is, DEFAULT_ENCODING));
				int bufSize = 0;
/*  23*/        do
                {
/*  23*/            java.lang.String line = stdoutReader.readLine();
					if (line != null && (bufSize += line.length()) > size) {
						return strBuild.append(line).append((" ...buffer size is exceeded")).append("\n").toString();
					}
/*  24*/            if(line != null)
                    {
/*  26*/                strBuild.append((new StringBuilder(java.lang.String.valueOf(line))).append("\n").toString());
                    } else
                    {
/*  28*/                int index = strBuild.lastIndexOf("\n");
/*  29*/                return index < 0 ? "" : strBuild.substring(0, index);
                    }
                } while(true);
            }

            public static com.dynatrace.diagnostics.remoteconnection.ConnectionMethod getConnectionMethod(java.lang.String method)
            {
/*  35*/        if(method.equals("LOCAL"))
/*  36*/            return new LocalConnectionMethod();
/*  38*/        else
/*  38*/            return new SSHConnectionMethod();
            }
}
