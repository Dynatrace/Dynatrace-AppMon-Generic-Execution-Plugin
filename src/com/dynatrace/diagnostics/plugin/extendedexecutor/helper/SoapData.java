// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SoapData.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Port;

// Referenced classes of package com.dynatrace.diagnostics.plugin.extendedexecutor.helper:
//            GEPluginConstants

public class SoapData
    implements GEPluginConstants
{

    public SoapData()
    {
        soapActionUri = "";
        portName = "";
        location = "";
        protocol = "";
        bindingId = "";
        portTypeName = "";
        bindingName = "";
        port = null;
        binding = null;
    }

    public String getSoapActionUri()
    {
        return soapActionUri;
    }

    public void setSoapActionUri(String soapActionUri)
    {
        this.soapActionUri = soapActionUri;
    }

    public Port getPort()
    {
        return port;
    }

    public void setPort(Port port)
    {
        this.port = port;
    }

    public String getPortName()
    {
        return portName;
    }

    public void setPortName(String portName)
    {
        this.portName = portName;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public Binding getBinding()
    {
        return binding;
    }

    public void setBinding(Binding binding)
    {
        this.binding = binding;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getBindingId()
    {
        return bindingId;
    }

    public void setBindingId(String bindingId)
    {
        this.bindingId = bindingId;
    }

    public String getPortTypeName()
    {
        return portTypeName;
    }

    public void setPortTypeName(String portTypeName)
    {
        this.portTypeName = portTypeName;
    }

    public String getBindingName()
    {
        return bindingName;
    }

    public void setBindingName(String bindingName)
    {
        this.bindingName = bindingName;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SoapData [soapActionUri=");
        builder.append(soapActionUri);
        builder.append(", portName=");
        builder.append(portName);
        builder.append(", location=");
        builder.append(location);
        builder.append(", protocol=");
        builder.append(protocol);
        builder.append(", bindingId=");
        builder.append(bindingId);
        builder.append(", portTypeName=");
        builder.append(portTypeName);
        builder.append(", bindingName=");
        builder.append(bindingName);
        builder.append(", port=");
        builder.append(port);
        builder.append(", binding=");
        builder.append(binding);
        builder.append("]");
        return builder.toString();
    }

    private String soapActionUri;
    private String portName;
    private String location;
    private String protocol;
    private String bindingId;
    private String portTypeName;
    private String bindingName;
    private Port port;
    private Binding binding;
}
