package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginProxySelector extends ProxySelector {

	 @Override    
	 public List<Proxy> select(URI uri)      {         
		 // Setting up a new ProxySelector implementation         
		 Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 6670));  
		 System.out.println("PluginProxySelector: select: created proxy: localhost, 6670");
		 ArrayList<Proxy> list = new ArrayList<Proxy>();         
		 list.add(proxy);   
		 System.out.println("PluginProxySelector: select: " + Arrays.toString(list.toArray()));
		 return list;     
	}      
	 
	 @Override    
	 public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		 System.err.println("Connection to " + uri + " failed.");     
	} 

}
