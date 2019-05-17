// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SSHConnectionMethod.java

package com.dynatrace.diagnostics.remoteconnection;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.extendedexecutor.GenericExecutor;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.GEPluginConstants;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.GEPluginProperties;
import com.trilead.ssh2.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Referenced classes of package com.dynatrace.diagnostics.remoteconnection:
//            ConnectionMethod, GEReturnObject

public class SSHConnectionMethod extends ConnectionMethod
    implements GEPluginConstants
{

    public SSHConnectionMethod()
    {
    }

    public GEReturnObject executeCommand(String cmd[], String env, long size, GEPluginProperties props)
        throws Exception
    {
        session = conn.openSession();
        session.execCommand((new StringBuilder(String.valueOf(env.isEmpty() ? "" : ((Object) ((new StringBuilder(String.valueOf(env))).append(" ").toString()))))).append("LANG=C ").append(cmd[0]).toString());
        String output = readInputStream(new StreamGobbler(session.getStdout()), size);
        int retval = session.waitForCondition(32, 0x493e0L);
        if((retval & 1) != 0)
        {
            throw new TimeoutException((new StringBuilder("executeCommand using SSH protocol: command '")).append(Arrays.toString(cmd)).append("' timed out because it was executed for more than ").append(5D).append(" minutes").toString());
        } else
        {
            Integer rc = session.getExitStatus();
            session.close();
            GEReturnObject obj;
            (obj = new GEReturnObject()).setOutput(output);
            obj.setRc(rc);
            return obj;
        }
    }

    public void setup(String host, String user, String pass, int port)
        throws Exception
    {
        if(GenericExecutor.log.isLoggable(Level.FINER))
            GenericExecutor.log.finer((new StringBuilder("Entering setup method: password authentication: host is '")).append(host).append("', user is '").append(user).append("', password is '").append(pass).append("', port is '").append(port).append("'").toString());
        try
        {
            if(conn != null)
                conn.close();
            conn = new Connection(host, port);
            conn.connect();
            if(!conn.isAuthMethodAvailable(user, "password"))
            {
                String msg = (new StringBuilder("setup method: password authentication method not supported by server '")).append(host).append("'").toString();
                GenericExecutor.log.severe(msg);
                throw new IOException(msg);
            }
            boolean isAuthenticated = conn.authenticateWithPassword(user, pass);
            if(!isAuthenticated)
            {
                String msg = (new StringBuilder("setup method: password authentication method: Authentication failed for user = '")).append(user).append("', password = '").append(pass).append("', host = '").append(", port = '").append(port).append("'").toString();
                GenericExecutor.log.severe(msg);
                throw new IOException(msg);
            }
        }
        catch(IOException ex)
        {
            if(conn != null)
                conn.close();
            String msg = (new StringBuilder("setup method: password authentication: IOException, stacktrace is '")).append(HelperUtils.getExceptionAsString(ex)).append("'").toString();
            GenericExecutor.log.severe(msg);
            GenericExecutor.log.severe(getAvailAuthMethods(conn, user));
            throw ex;
        }
    }

    public void setup(String host, String user, String pass, int port, String keyFile)
        throws Exception
    {
        if(GenericExecutor.log.isLoggable(Level.FINER))
            GenericExecutor.log.finer((new StringBuilder("Entering setup method: public key authentication: host is '")).append(host).append("', user is '").append(user).append("', password is '").append(pass).append("', port is '").append(port).append("', key file is '").append(keyFile).append("'").toString());
        try
        {
            if(conn != null)
                conn.close();
            conn = new Connection(host, port);
            conn.connect();
            if(GenericExecutor.log.isLoggable(Level.FINER))
                GenericExecutor.log.finer("SSH Publickey authentication");
            if(!conn.isAuthMethodAvailable(user, "publickey"))
            {
                String msg = (new StringBuilder("setup method: publickey authentication method not supported by server '")).append(host).append("'").toString();
                GenericExecutor.log.severe(msg);
                throw new IOException(msg);
            }
            File pemFile = new File(keyFile);
            boolean isAuthenticated = conn.authenticateWithPublicKey(user, pemFile, pass);
            if(!isAuthenticated)
                throw new IOException("Authentication failed.");
        }
        catch(IOException ex)
        {
            if(conn != null)
                conn.close();
            String msg = (new StringBuilder("setup method: password authentication: IOException, stacktrace is '")).append(HelperUtils.getExceptionAsString(ex)).append("'").toString();
            GenericExecutor.log.severe(msg);
            GenericExecutor.log.severe(getAvailAuthMethods(conn, user));
            throw ex;
        }
    }

    public void teardown()
    {
        conn.close();
    }

    public String getAvailAuthMethods(Connection conn, String user)
        throws IOException
    {
        StringBuilder sb = (new StringBuilder("getAvailAuthMethods method: Available authentication methods:")).append(LS_LOCAL);
        if(conn.isAuthMethodAvailable(user, "publickey"))
            sb.append("  - public key auth method supported by server").append(LS_LOCAL);
        else
            sb.append("  - public key auth method not supported by server").append(LS_LOCAL);
        if(conn.isAuthMethodAvailable(user, "keyboard-interactive"))
            sb.append("  - keyboard interactive auth method supported by server").append(LS_LOCAL);
        else
            sb.append("  - keyboard interactive auth method not supported by server").append(LS_LOCAL);
        if(conn.isAuthMethodAvailable(user, "password"))
            sb.append("  - password auth method supported by server").append(LS_LOCAL);
        else
            sb.append("  - password auth method not supported by server").append(LS_LOCAL);
        return sb.toString();
    }

    private Connection conn;
    private Session session;
}
