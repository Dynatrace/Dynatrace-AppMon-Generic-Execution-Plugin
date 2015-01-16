// Decompiled by DJ v3.12.12.96 Copyright 2011 Atanas Neshkov  Date: 6/25/2013 8:19:01 AM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: fullnames lnc 
// Source File Name:   SSHConnectionMethod.java

package com.dynatrace.diagnostics.remoteconnection;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.StreamGobbler;
import java.io.File;
import java.io.IOException;

// Referenced classes of package com.dynatrace.diagnostics.remoteconnection:
//            ConnectionMethod

public class SSHConnectionMethod extends com.dynatrace.diagnostics.remoteconnection.ConnectionMethod
{

            public SSHConnectionMethod()
            {
            }

            public GEReturnObject executeCommand(java.lang.String cmd, java.lang.String env, long size)
                throws java.lang.Exception
            {
	/*  20*/    session = conn.openSession();
	/*  21*/    session.execCommand((new StringBuilder(java.lang.String.valueOf(env.isEmpty() ? "" : ((java.lang.Object) ((new StringBuilder(java.lang.String.valueOf(env))).append(" ").toString()))))).append("LANG=C ").append(cmd).toString());
	/*  23*/    java.lang.String output = readInputStream(new StreamGobbler(session.getStdout()), size);
				Integer rc = session.getExitStatus();
	/*  24*/    session.close();
				GEReturnObject obj;
				(obj = new GEReturnObject()).setOutput(output);
				obj.setRc(rc);
/*  29*/        return obj;
            }

            public void setup(java.lang.String host, java.lang.String user, java.lang.String pass, int port)
                throws java.lang.Exception
            {
/*  36*/        try
                {
/*  36*/            if(conn != null)
/*  37*/                conn.close();
/*  39*/            conn = new Connection(host, port);
/*  40*/            conn.connect();
/*  41*/            boolean isAuthenticated = conn.authenticateWithPassword(user, pass);
/*  44*/            if(!isAuthenticated)
/*  45*/                throw new IOException("Authentication failed.");
                }
/*  47*/        catch(java.io.IOException ex)
                {
/*  48*/            if(conn != null)
/*  49*/                conn.close();
/*  51*/            throw ex;
                }
            }

            public void setup(java.lang.String host, java.lang.String user, java.lang.String pass, int port, java.lang.String keyFile)
                throws java.lang.Exception
            {
/*  57*/        try
                {
/*  57*/            if(conn != null)
/*  58*/                conn.close();
/*  60*/            conn = new Connection(host, port);
/*  61*/            conn.connect();
/*  63*/            if(log.isLoggable(java.util.logging.Level.INFO))
/*  64*/                log.info("SSH Publickey authentication");
/*  66*/            boolean available = conn.isAuthMethodAvailable(user, "publickey");
/*  67*/            if(!available)
/*  68*/                throw new IOException("Authentication-Method not Available");
/*  71*/            java.io.File pemFile = new File(keyFile);
/*  73*/            boolean isAuthenticated = conn.authenticateWithPublicKey(user, pemFile, pass);
/*  76*/            if(!isAuthenticated)
/*  77*/                throw new IOException("Authentication failed.");
                }
/*  79*/        catch(java.io.IOException ex)
                {
/*  80*/            if(conn != null)
/*  81*/                conn.close();
/*  83*/            throw ex;
                }
            }

            public void teardown()
            {
/*  89*/        conn.close();
            }

            private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(SSHConnectionMethod.class.getName());
            private com.trilead.ssh2.Connection conn;
            private com.trilead.ssh2.Session session;

}
