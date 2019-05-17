// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PluginProxySelector.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

public class PluginProxySelector extends ProxySelector
{

    public PluginProxySelector()
    {
    }

    public List select(URI uri)
    {
        Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress("localhost", 6670));
        log.finer("PluginProxySelector: select: created proxy: localhost, 6670");
        ArrayList list = new ArrayList();
        list.add(proxy);
        log.finer((new StringBuilder("PluginProxySelector: select: ")).append(Arrays.toString(list.toArray())).toString());
        return list;
    }

    public void connectFailed(URI uri, SocketAddress sa, IOException ioe)
    {
        log.severe((new StringBuilder("Connection to ")).append(uri).append(" failed.").toString());
    }

    public static final Logger log = Logger.getLogger(com/dynatrace/diagnostics/plugin/extendedexecutor/helper/PluginProxySelector.getName());

}
