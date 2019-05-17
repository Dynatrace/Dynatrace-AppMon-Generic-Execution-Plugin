// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GenericExecutor.java

package com.dynatrace.diagnostics.plugin.extendedexecutor;

import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.PluginEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.TaskEnvironment;
import com.dynatrace.diagnostics.plugin.actionhelper.ActionData;
import com.dynatrace.diagnostics.plugin.actionhelper.ActionHelper;
import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.BaseMeasure;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.GEPluginConstants;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.GEPluginProperties;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.NonActionFields;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.SoapData;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.WSElement;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.WSReturnedMeasures;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.WsOpParams;
import com.dynatrace.diagnostics.remoteconnection.ConnectionMethod;
import com.dynatrace.diagnostics.remoteconnection.GEReturnObject;
import com.dynatrace.diagnostics.remoteconnection.SSHConnectionMethod;
import com.dynatrace.diagnostics.sdk.Timestamp30Impl;
import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.Schema;
import com.predic8.schema.Sequence;
import com.predic8.schema.TypeDefinition;
import com.predic8.wsdl.AbstractAddress;
import com.predic8.wsdl.AbstractBinding;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.ExtensibilityOperation;
import com.predic8.wsdl.Input;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestCreator;
import com.predic8.wstool.creator.SOARequestCreator;
import com.predic8.xml.util.ExternalResolver;
import groovy.xml.MarkupBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

public class GenericExecutor
    implements GEPluginConstants
{

    public GenericExecutor()
    {
    }

    protected Status setup(PluginEnvironment env)
        throws Exception
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering setup method");
        try
        {
            if(!env.getConfigBoolean("isWS").booleanValue())
            {
                pp = setConfiguration(env);
            } else
            {
                pp = setConfigurationWs(env);
                pp.setWs(env.getConfigBoolean("isWS").booleanValue());
            }
        }
        catch(Exception e)
        {
            log.severe((new StringBuilder("setup method: ")).append(HelperUtils.getExceptionAsString(e)).toString());
            return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.ErrorInternalConfigurationProblem, e.getMessage(), e.getMessage(), e);
        }
        if(!pp.isWs())
            try
            {
                pp.setConnectionMethod(getConnection(pp));
            }
            catch(Exception e)
            {
                log.severe((new StringBuilder("setup method: ")).append(HelperUtils.getExceptionAsString(e)).toString());
                return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.ErrorInternalConfigurationProblem, e.getMessage(), e.getMessage(), e);
            }
        else
            try
            {
                WSDLParser parser = new WSDLParser();
                if(pp.getWsProxyHost() != null && !pp.getWsProxyHost().isEmpty())
                {
                    ExternalResolver er = new ExternalResolver();
                    er.setProxyHost(pp.getWsProxyHost());
                    er.setProxyPort((int)pp.getWsProxyPort());
                    er.setTimeout(30000);
                    parser.setResourceResolver(er);
                }
                Definitions defs;
                pp.setWsDefinitions(defs = parser.parse(pp.getWsWSDL()));
                pp.setWsTargetNamespace(defs.getTargetNamespace());
                String operation;
                if(!isOperation(defs, operation = pp.getWsOperationName()))
                {
                    String msg;
                    log.severe(msg = (new StringBuilder("setup method: Operation '")).append(operation).append("' is not found").toString());
                    throw new RuntimeException(msg);
                }
                SoapData soapData = getPortMain(defs, pp.getWsOperationName());
                pp.setSoapData(soapData);
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("setup method: soapData is'")).append(soapData.toString()).append("'").toString());
                if(pp.isWsXpathSyntax())
                {
                    pp.setWsPortTypeName(soapData.getPortTypeName());
                    pp.setWsBindingName(soapData.getBinding().getName());
                    pp.setWsSoapMessage(buildSoapMessage(pp.getWsDefinitions(), pp.getWsPortTypeName(), pp.getWsBindingName(), pp.getWsOperationName(), pp.getWsParmsSubstituter()));
                    if(log.isLoggable(Level.FINER))
                        log.finer((new StringBuilder("setup method: PortTypeName is '")).append(pp.getWsPortTypeName()).append("', bindingName is '").append(pp.getWsBindingName()).append("', SOAP message is '").append(pp.getWsSoapMessage()).append("'").toString());
                }
                pp.setWsPortName(soapData.getPortName());
                pp.setWsBindingId(soapData.getBindingId());
                pp.setWsLocation(soapData.getLocation());
                pp.setWsProtocol(soapData.getProtocol() != null ? soapData.getProtocol() : "");
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("setup method: PortName is '")).append(soapData.getPortName()).append("', bindingId is '").append(soapData.getBindingId()).append("', location is '").append(soapData.getLocation()).append("'").toString());
                if(!pp.isWsXpathSyntax())
                {
                    pp.setWsOpParams(getRequestParms(defs, soapData.getPortName(), operation));
                    if(log.isLoggable(Level.FINER))
                    {
                        int i = 0;
                        String structureName = null;
                        List elements = null;
                        log.finer("setup method: paramsMap entries");
                        for(Iterator iterator = pp.getWsOpParams().getComplexTypes().entrySet().iterator(); iterator.hasNext(); log.finer((new StringBuilder(String.valueOf(++i))).append(". ").append(structureName).append(": '").append(Arrays.toString(elements.toArray())).append("'").toString()))
                        {
                            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
                            structureName = ((Element)entry.getKey()).getName();
                            elements = (List)entry.getValue();
                        }

                    }
                }
            }
            catch(Throwable t)
            {
                Exception e = new Exception(t);
                String str = (new StringBuilder("setup method: ")).append(HelperUtils.getExceptionAsString(e)).toString();
                log.severe(str);
                return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.ErrorInternalConfigurationProblem, str, str, e);
            }
        return STATUS_SUCCESS;
    }

    private String buildSoapMessage(Definitions defs, String portTypeName, String bindingName, String operation, Properties parameters)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering buildSoapMessage method: portTypeName = '")).append(portTypeName).append("', bindingName = '").append(bindingName).append("', operation name = '").append(operation).append("', parameters = '").append(Arrays.toString(parameters.entrySet().toArray())).append("'").toString());
        StringWriter writer = new StringWriter();
        Map map = new HashMap();
        int i = 0;
        for(Iterator iterator = parameters.entrySet().iterator(); iterator.hasNext();)
        {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            String key;
            String value;
            map.put(key = (new StringBuilder("xpath:")).append((String)entry.getKey()).toString(), value = (String)entry.getValue());
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("buildSoapMessage method: entry # ")).append(++i).append(": key = '").append(key).append("', value = '").append(value).append("'").toString());
        }

        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("buildSoapMessage method: map = '")).append(Arrays.toString(map.entrySet().toArray())).append("'").toString());
        SOARequestCreator creator = new SOARequestCreator(defs, new RequestCreator(), new MarkupBuilder(writer));
        creator.setFormParams(map);
        creator.createRequest(portTypeName, operation, bindingName);
        String s = writer.getBuffer().toString();
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("buildSoapMessage method: Soap message = '")).append(s).append("'").toString());
        return s;
    }

    protected Status execute(PluginEnvironment env)
        throws Exception
    {
        String output;
        GEReturnObject obj;
        boolean partialSuccess;
        output = null;
        obj = null;
        if(log.isLoggable(Level.FINER))
            log.finer("Entering execute method");
        partialSuccess = false;
        ActionData action;
        action = null;
        if(env instanceof ActionEnvironment)
        {
            action = ActionHelper.populateSourceOfIncidents((ActionEnvironment)env);
            action.setDateFormat(pp.getDateFormat());
            Map actionSubstituter;
            pp.setActionSubstituter(actionSubstituter = ActionHelper.populateSubstituterMap((ActionEnvironment)env, action));
            pp.setActionStrSubstituter(new StrSubstitutor(actionSubstituter));
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("execute method: substituter map is '")).append(Arrays.toString(actionSubstituter.entrySet().toArray())).append("'").toString());
            if(!pp.isWs())
            {
                escapeChars(actionSubstituter, pp);
                if(!pp.getMethod().equalsIgnoreCase("Local"))
                {
                    action.setCommand(pp.getActionStrSubstituter().replace(pp.getCommand()));
                } else
                {
                    int i = 0;
                    String as[] = new String[pp.getTokenizedCommand().length];
                    String as1[];
                    int l = (as1 = pp.getTokenizedCommand()).length;
                    for(int j = 0; j < l; j++)
                    {
                        String token = as1[j];
                        as[i++] = pp.getActionStrSubstituter().replace(token);
                    }

                    action.setTokenizedCommand(as);
                }
                if(log.isLoggable(Level.FINE))
                    if(!pp.getMethod().equalsIgnoreCase("Local"))
                        log.fine((new StringBuilder("execute method: Substitution of Action variables : Prepared command to execute: '")).append(action.getCommand()).append("'").append(LS_LOCAL).toString());
                    else
                        log.fine((new StringBuilder("execute method: Substitution of Action variables : Prepared tokenized command to execute: '")).append(Arrays.toString(action.getTokenizedCommand())).append("'").append(LS_LOCAL).toString());
            }
        }
        Map nonActionSubstituter;
        pp.setNonActionSubstituter(nonActionSubstituter = getNonActionVarsSubstituter(pp));
        pp.setNonActionStrSubstituter(new StrSubstitutor(nonActionSubstituter));
        if(pp.isWs())
            break MISSING_BLOCK_LABEL_1803;
        String msg;
        try
        {
            if(action == null)
                if(!pp.getMethod().equalsIgnoreCase("Local"))
                {
                    (action = new ActionData()).setCommand(pp.getCommand());
                } else
                {
                    (action = new ActionData()).setCommand(pp.getCommand());
                    action.setTokenizedCommand(pp.getTokenizedCommand());
                }
            if(!pp.getMethod().equalsIgnoreCase("Local"))
            {
                action.setCommand(pp.getNonActionStrSubstituter().replace(action.getCommand()));
            } else
            {
                int i = 0;
                String as[] = new String[pp.getTokenizedCommand().length];
                String as2[];
                int i1 = (as2 = action.getTokenizedCommand()).length;
                for(int k = 0; k < i1; k++)
                {
                    String token = as2[k];
                    as[i++] = pp.getNonActionStrSubstituter().replace(token);
                }

                action.setTokenizedCommand(as);
                action.setCommand(pp.getNonActionStrSubstituter().replace(action.getCommand()));
            }
            if(log.isLoggable(Level.FINER))
                if(!pp.getMethod().equalsIgnoreCase("Local"))
                    log.finer((new StringBuilder("execute method: Substitution of Non-Action variables for remote command: Final command : Prepared command to execute: '")).append(action.getCommand()).append("'").append(LS_LOCAL).toString());
                else
                    log.finer((new StringBuilder("execute method: Substitution of Non-Action variables for local command: Prepared tokenized command to execute: '")).append(Arrays.toString(action.getTokenizedCommand())).append("'").append(LS_LOCAL).toString());
            if(!pp.getMethod().equalsIgnoreCase("Local"))
            {
                try
                {
                    obj = pp.getConnectionMethod().executeCommand(new String[] {
                        action.getCommand()
                    }, "", pp.getOutputBufferSize(), pp);
                    log.finer((new StringBuilder("Execute method: method is '")).append(pp.getMethod()).append("', GEReturnObject rc is ").append(obj.getRc()).append(", output is '").append(obj.getOutput()).append("'").toString());
                }
                catch(Exception e)
                {
                    String msg = HelperUtils.getExceptionAsString(e);
                    log.severe((new StringBuilder("Execute method: failed to execute remote command using SSH method. Exception is '")).append(msg).append("'").toString());
                    if(!(env instanceof ActionEnvironment))
                    {
                        if(pp.getConnectionMethod() != null)
                            pp.getConnectionMethod().teardown();
                        pp.setConnectionMethod(getConnection(pp));
                    }
                    throw e;
                }
            } else
            {
                obj = pp.getConnectionMethod().executeCommand(action.getTokenizedCommand(), "", pp.getOutputBufferSize(), pp);
                log.finer((new StringBuilder("Execute method: method is '")).append(pp.getMethod()).append("', GEReturnObject rc is ").append(obj.getRc()).append(", output is '").append(obj.getOutput()).append("'").toString());
            }
            output = obj.getOutput();
            if(log.isLoggable(Level.FINER))
                if(!pp.getMethod().equalsIgnoreCase("Local"))
                {
                    log.finer(output != null ? (new StringBuilder("execute method: ")).append(String.format("Output string from the command '%s' %s is '%s'", new Object[] {
                        action.getCommand(), LS_LOCAL, output
                    })).toString() : (new StringBuilder("execute method: ")).append(String.format("Null output returned from the '%s' command", new Object[] {
                        action.getCommand()
                    })).toString());
                } else
                {
                    log.finer(obj.getStdout() != null ? (new StringBuilder("execute method: ")).append(String.format("%s from the command '%s' %s is '%s'", new Object[] {
                        "STDOUT", Arrays.toString(action.getTokenizedCommand()), LS_LOCAL, obj.getStdout()
                    })).toString() : (new StringBuilder("execute method: ")).append(String.format("Null output returned from the '%s' command", new Object[] {
                        action.getCommand()
                    })).toString());
                    log.finer(obj.getStderr() != null ? (new StringBuilder("execute method: ")).append(String.format("%s from the command '%s' %s is '%s'", new Object[] {
                        "STDERR", Arrays.toString(action.getTokenizedCommand()), LS_LOCAL, obj.getStderr()
                    })).toString() : (new StringBuilder("execute method: ")).append(String.format("Null output returned from the '%s' command", new Object[] {
                        action.getCommand()
                    })).toString());
                }
            if(output == null)
            {
                msg = String.format("Null output returned from the '%s' command", new Object[] {
                    action.getCommand()
                });
                log.severe((new StringBuilder("execute method: '")).append(msg).toString());
                return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.PartialSuccess, msg, msg);
            }
        }
        catch(Exception e)
        {
            String msg = (new StringBuilder("execute method: exception was thrown when executing command '")).append(pp.getCommand()).append("'. Stack trace is '").append(HelperUtils.getExceptionAsString(e)).append("'").toString();
            log.severe(msg);
            return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.ErrorInfrastructure, msg, msg, e);
        }
        if(env instanceof MonitorEnvironment)
            setReturnedMeasures((MonitorEnvironment)env, pp, output);
        if(pp.isTriggerIncident() && (msg = obj.getStderr()) != null && !(msg = msg.trim()).isEmpty())
        {
            String postData = (new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><incident><message>")).append(StringEscapeUtils.escapeXml(obj.getStderr())).append("</message><description>Incident was triggered by the Generic Execution plugin at ").append((new Date()).toString()).append(". Executed command is '").append(StringEscapeUtils.escapeXml(action.getCommand())).append("'</description><severity>").append(pp.getSeverity()).append("</severity><state>InProgress</state></incident>").toString();
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("execute method: postData is '")).append(postData).append("'").toString());
            try
            {
                HelperUtils.createIncident(pp.getTargetUrl(), postData, pp.getDtUser(), pp.getDtPassword(), pp.getProtocol().equals("https"));
            }
            catch(Exception e)
            {
                msg = (new StringBuilder("execute method: exception was thrown by the HelperUtils.createIncident method: executed command is '")).append(action.getCommand()).append("'. Stack trace is '").append(HelperUtils.getExceptionAsString(e)).append("'").toString();
                log.severe(msg);
                partialSuccess = true;
            }
        }
        break MISSING_BLOCK_LABEL_2959;
        try
        {
            try
            {
                obj = new GEReturnObject();
                if(pp.getWsProxyHost() != null && !pp.getWsProxyHost().isEmpty())
                {
                    ProxySelector proxySelector = new ProxySelector() {

                        public List select(URI uri)
                        {
                            Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(pp.getWsProxyHost(), (int)pp.getWsProxyPort()));
                            ArrayList list = new ArrayList();
                            list.add(proxy);
                            if(GenericExecutor.log.isLoggable(Level.FINER))
                            {
                                GenericExecutor.log.finer("Inner ProxySelector: select method: created proxy: localhost, 6670");
                                GenericExecutor.log.finer((new StringBuilder("Inner ProxySelector: select method: ")).append(Arrays.toString(list.toArray())).toString());
                            }
                            return list;
                        }

                        public void connectFailed(URI uri, SocketAddress sa, IOException ioe)
                        {
                            GenericExecutor.log.severe((new StringBuilder("Inner ProxySelector:: connectFailed method: Connection to ")).append(uri).append(" failed. Exception message is '").append(ioe.getMessage()).append("'").toString());
                            throw new RuntimeException(ioe);
                        }

                        final GenericExecutor this$0;

            
            {
                this$0 = GenericExecutor.this;
                super();
            }
                    }
;
                    ProxySelector.setDefault(proxySelector);
                    if(pp.getWsProxyUser() != null && !pp.getWsProxyUser().isEmpty())
                    {
                        AuthCacheValue.setAuthCache(new AuthCacheImpl());
                        Authenticator.setDefault(new Authenticator() {

                            protected PasswordAuthentication getPasswordAuthentication()
                            {
                                PasswordAuthentication pa = new PasswordAuthentication(pp.getWsProxyUser(), pp.getWsProxyPassword().toCharArray());
                                if(GenericExecutor.log.isLoggable(Level.FINER))
                                    GenericExecutor.log.finer(String.format("getPasswordAuthentication method: user is '%s'; password is '%s'; class is '%s'", new Object[] {
                                        pa.getUserName(), new String(pa.getPassword()), pa.getClass().getCanonicalName()
                                    }));
                                return pa;
                            }

                            final GenericExecutor this$0;

            
            {
                this$0 = GenericExecutor.this;
                super();
            }
                        }
);
                    }
                }
                QName operationQName = new QName(pp.getWsTargetNamespace(), pp.getWsOperationName());
                QName portQName = new QName(pp.getWsTargetNamespace(), pp.getWsPortName());
                Service svc = Service.create(operationQName);
                svc.addPort(portQName, pp.getWsBindingId(), pp.getWsLocation());
                Dispatch dispatch = svc.createDispatch(portQName, javax/xml/soap/SOAPMessage, javax.xml.ws.Service.Mode.MESSAGE);
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("execute method: isDotNET = '")).append(pp.isDotNET()).append("'").toString());
                if(pp.isDotNET())
                {
                    dispatch.getRequestContext().put("javax.xml.ws.soap.http.soapaction.use", Boolean.valueOf(true));
                    dispatch.getRequestContext().put("javax.xml.ws.soap.http.soapaction.uri", pp.getSoapData().getSoapActionUri());
                    if(log.isLoggable(Level.FINER))
                        log.finer((new StringBuilder("execute method: soapActionUri is '")).append(pp.getSoapData().getSoapActionUri()).append("', request context is ").append(Arrays.toString(dispatch.getRequestContext().entrySet().toArray())).toString());
                }
                SOAPMessage request;
                if(pp.isWsXpathSyntax())
                {
                    InputStream is = new ByteArrayInputStream(applySubstituters(pp.getWsSoapMessage(), pp).getBytes());
                    if(pp.getWsProtocol().equals("SOAP12"))
                        request = MessageFactory.newInstance("SOAP 1.2 Protocol").createMessage(new MimeHeaders(), is);
                    else
                        request = MessageFactory.newInstance("SOAP 1.1 Protocol").createMessage(new MimeHeaders(), is);
                } else
                {
                    MessageFactory factory = MessageFactory.newInstance();
                    request = factory.createMessage();
                    SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
                    SOAPBody soapBody = request.getSOAPBody();
                    soapBody.addNamespaceDeclaration("q0", pp.getWsTargetNamespace());
                    SOAPBodyElement soapBodyElement;
                    if(pp.isWsUsePrefix())
                        soapBodyElement = soapBody.addBodyElement(envelope.createName(pp.getWsOperationName(), "q0", pp.getWsTargetNamespace()));
                    else
                        soapBodyElement = soapBody.addBodyElement(envelope.createName(pp.getWsOperationName()));
                    Element p;
                    for(Iterator iterator = pp.getWsOpParams().getArgList().iterator(); iterator.hasNext(); addElement(p, getComplexTypeStructure(p.getName(), pp.getWsOpParams()), soapBodyElement, pp))
                        p = (Element)iterator.next();

                }
                if(pp.isDotNET())
                {
                    MimeHeaders headers = request.getMimeHeaders();
                    headers.addHeader("SOAPAction", pp.getSoapData().getSoapActionUri());
                    SOAPHeader header = request.getSOAPHeader();
                    if(header != null)
                        header.detachNode();
                }
                if(pp.isWsAuth())
                {
                    MimeHeaders headers = request.getMimeHeaders();
                    headers.addHeader("Authorization", (new StringBuilder("Basic ")).append(pp.getWsAuthString()).toString());
                }
                if(log.isLoggable(Level.FINER))
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    request.writeTo(out);
                    String msgString = new String(out.toByteArray(), DEFAULT_ENCODING);
                    log.finer((new StringBuilder("execute method: SOAP request is '")).append(msgString).append("'").toString());
                }
                SOAPMessage response = (SOAPMessage)dispatch.invoke(request);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                Source sourceContent = response.getSOAPPart().getContent();
                StreamResult result = new StreamResult(outStream);
                transformer.transform(sourceContent, result);
                String wsOutput = outStream.toString(DEFAULT_ENCODING);
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("execute method: Web service returned string '")).append(wsOutput).append("'").toString());
                Map map = new HashMap();
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document document = docBuilder.parse(new InputSource(new StringReader(wsOutput)));
                buildMap(map, document.getDocumentElement());
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("execute method: returned by WS variables and their values are '")).append(Arrays.toString(map.entrySet().toArray())).append("'").toString());
                obj.setRc(Integer.valueOf(0));
                obj.setOutput(wsOutput);
                output = wsOutput;
                if((env instanceof MonitorEnvironment) && pp.isWSReturnedMeasures() && map.size() > 0)
                    setReturnedMeasuresWS((MonitorEnvironment)env, pp, map);
            }
            catch(Exception e)
            {
                StringBuilder sb = (new StringBuilder("execute method: exception is '")).append(HelperUtils.getExceptionAsString(e)).append("'");
                String msg;
                log.severe(msg = sb.toString());
                return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.ErrorInfrastructure, msg, msg);
            }
        }
        catch(Exception e)
        {
            String msg = HelperUtils.getExceptionAsString(e);
            log.severe((new StringBuilder("Execute method: exception is '")).append(msg).append("'").toString());
            return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.ErrorInfrastructure, msg, msg, e);
        }
        if(env instanceof MonitorEnvironment)
        {
            if(log.isLoggable(Level.FINER))
                log.finer("execute method: execute the setMatchRuleSuccessMeasure method");
            setMatchRuleSuccessMeasure((MonitorEnvironment)env, pp, output);
            if(obj != null)
            {
                if(log.isLoggable(Level.FINER))
                    log.finer("execute method: execute the setReturnCodeMeasure method");
                setReturnCodeMeasure((MonitorEnvironment)env, pp, obj);
            }
        }
        if(pp.isCapture())
        {
            if(log.isLoggable(Level.FINER))
                log.finer("execute method: returning success when isCapture is 'true'");
            if(partialSuccess)
                return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.PartialSuccess, "", output);
            else
                return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.Success, "", output);
        }
        if(log.isLoggable(Level.FINER))
            log.finer("execute method: returning success when isCapture is 'false'");
        if(partialSuccess)
            return new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.PartialSuccess);
        else
            return STATUS_SUCCESS;
    }

    protected void teardown(PluginEnvironment env)
        throws Exception
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering teardown method");
        if(pp.getConnectionMethod() != null)
            pp.getConnectionMethod().teardown();
    }

    private String applySubstituters(String value, GEPluginProperties props)
    {
        if(value == null || value.trim().isEmpty())
            return value;
        String s = value;
        if(props.getActionSubstituter() != null)
        {
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("applySubstituters method: Before applying action substituter and escape maps: string s is '")).append(s).append("'").toString());
            s = getStrSubstituterEscaped(props.getActionSubstituter()).replace(value);
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("applySubstituters method: After applying action substituter and escape maps: string s is '")).append(s).append("'").toString());
        }
        if(props.getNonActionStrSubstituter() != null)
            s = props.getNonActionStrSubstituter().replace(s);
        return s;
    }

    public static StrSubstitutor getStrSubstituterEscaped(Map map)
    {
        Map newMap = new HashMap();
        Set entries = map.entrySet();
        java.util.Map.Entry entry;
        for(Iterator iterator = entries.iterator(); iterator.hasNext(); newMap.put((String)entry.getKey(), StringEscapeUtils.escapeXml((String)entry.getValue())))
            entry = (java.util.Map.Entry)iterator.next();

        return new StrSubstitutor(newMap);
    }

    public static void buildMap(Map map, Node node)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering buildMap method: node name is '")).append(node.getNodeName()).append("'").toString());
        NodeList nodeList = node.getChildNodes();
        Node n;
        if(nodeList.getLength() == 1 && (n = nodeList.item(0)).getNodeType() == 3)
        {
            map.put(node.getNodeName().trim(), n.getNodeValue() == null ? null : ((Object) (n.getNodeValue().trim())));
            return;
        }
        for(int i = 0; i < nodeList.getLength(); i++)
        {
            Node currentNode = nodeList.item(i);
            if(currentNode.getNodeType() == 1)
                buildMap(map, currentNode);
        }

    }

    private void setReturnedMeasures(MonitorEnvironment env, GEPluginProperties props, String output)
        throws IOException
    {
        String record = null;
        if(log.isLoggable(Level.FINER))
            log.finer("Entering setReturnedMeasures method");
        BufferedReader reader = new BufferedReader(new StringReader(output));
        List records = IOUtils.readLines(reader);
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("setReturnedMeasures method: array of records from the output is ")).append(Arrays.toString(records.toArray())).toString());
        int i = 0;
        for(Iterator iterator = records.iterator(); iterator.hasNext();)
        {
            String r = (String)iterator.next();
            r = r.trim();
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("execute method: record #")).append(i++).append(" is '").append(r).append("'").toString());
            if(r.startsWith("***ReturnedMeasures:"))
            {
                record = r.substring("***ReturnedMeasures:".length()).trim();
                break;
            }
        }

        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("execute method: record string is ")).append(record == null ? "null" : record).toString());
        setReturnedMeasuresNoMeasuresPrefix(env, props, record);
    }

    private void setReturnedMeasuresNoMeasuresPrefix(MonitorEnvironment env, GEPluginProperties props, String record)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering setReturnedMeasuresNoMeasuresPrefix method, record is '")).append(record).append("'").toString());
        if(record != null && !record.isEmpty())
        {
            props.setMeasuresMap(buildMeasuresMap(env, props));
            if(log.isLoggable(Level.FINER) && props.getMeasuresMap() != null)
                log.finer((new StringBuilder("setReturnedMeasuresNoMeasuresPrefix method: measure map is ")).append(Arrays.toString(props.getMeasuresMap().entrySet().toArray())).toString());
            String values[] = record.split(";");
            setReturnedMeasures(env, props, values);
        }
    }

    private void setReturnedMeasuresWS(MonitorEnvironment env, GEPluginProperties props, Map map)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering setReturnedMeasuresWS method, map is '")).append(Arrays.toString(map.entrySet().toArray())).append("'").toString());
        WSReturnedMeasures wsMeasures = buildReturnedMeasuresWS(map);
        props.setReturnedMeasures(wsMeasures.getNames());
        setReturnedMeasures(env, props, wsMeasures.getValues());
    }

    private WSReturnedMeasures buildReturnedMeasuresWS(Map map)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering buildReturnedMeasuresWS method, map is '")).append(Arrays.toString(map.entrySet().toArray())).append("'").toString());
        if(map == null)
            return null;
        WSReturnedMeasures wsMeasures = new WSReturnedMeasures(map.size());
        String names[] = wsMeasures.getNames();
        String values[] = wsMeasures.getValues();
        int i = 0;
        for(Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)
        {
            java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
            names[i] = (String)entry.getKey();
            values[i] = (String)entry.getValue();
            i++;
        }

        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering buildReturnedMeasuresWS method, wsMeasures is '")).append(wsMeasures.toString()).append("'").toString());
        return wsMeasures;
    }

    protected void setReturnedMeasures(MonitorEnvironment env, GEPluginProperties props, String returnedValues[])
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering setReturnedMeasures method: returned values are ")).append(Arrays.toString(returnedValues)).toString());
        int i = 0;
        String returnedMeasures[] = props.getReturnedMeasures();
        Map map = props.getMeasuresMap();
        String as[];
        int k = (as = returnedValues).length;
        for(int j = 0; j < k; j++)
        {
            String returnedValue = as[j];
            Double d;
            try
            {
                d = Double.valueOf(returnedValue);
            }
            catch(NumberFormatException _ex)
            {
                d = Double.valueOf((0.0D / 0.0D));
            }
            String measure;
            if(i < returnedMeasures.length && (measure = returnedMeasures[i]) != null && !(measure = measure.trim()).isEmpty())
            {
                BaseMeasure bm;
                if(map != null)
                {
                    if(map.containsKey(measure))
                        bm = (BaseMeasure)map.get(measure);
                    else
                        bm = DEFAULT_BASE_MEASURE;
                } else
                {
                    bm = DEFAULT_BASE_MEASURE;
                }
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("setReturnedMeasures method: bm is ")).append(bm).append(", value is ").append(d).toString());
                Collection monitorMeasures = env.getMonitorMeasures(bm.getMetricGroup(), bm.getBaseMeasure());
                for(Iterator iterator = monitorMeasures.iterator(); iterator.hasNext();)
                {
                    MonitorMeasure monitorMeasure = (MonitorMeasure)iterator.next();
                    if(log.isLoggable(Level.FINER))
                        log.finer((new StringBuilder("setReturnedMeasures method: measure is ")).append(measure).append(", value is ").append(d).toString());
                    if(bm.getBaseMeasure().equals(measure))
                    {
                        monitorMeasure.setValue(d.doubleValue());
                    } else
                    {
                        MonitorMeasure dynamicMeasure = env.createDynamicMeasure(monitorMeasure, "Returned Measures", measure);
                        dynamicMeasure.setValue(d.doubleValue());
                    }
                }

                i++;
            }
        }

    }

    private Map buildMeasuresMap(MonitorEnvironment env, GEPluginProperties props)
    {
        BaseMeasure bm = null;
        Map map = null;
        String names[];
        if((names = props.getReturnedMeasures()) != null && names.length > 0)
        {
            map = new HashMap();
            String as[];
            int j = (as = names).length;
            for(int i = 0; i < j; i++)
            {
                String n = as[i];
                if(!n.trim().isEmpty())
                {
                    bm = (bm = getBaseMeasure(env, n)) == null ? DEFAULT_BASE_MEASURE : bm;
                    map.put(n, bm);
                }
            }

        }
        return map;
    }

    private BaseMeasure getBaseMeasure(MonitorEnvironment env, String name)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering getBaseMeasure method: name is '")).append(name).append("'").toString());
        Collection measures = env.getMonitorMeasures();
        if(log.isLoggable(Level.FINER))
        {
            int i = 0;
            MonitorMeasure m;
            for(Iterator iterator1 = measures.iterator(); iterator1.hasNext(); log.finer((new StringBuilder("getBaseMeasure method: monitor measure #")).append(++i).append(": metric name is '").append(m.getMetricName()).append("', measure name is '").append(m.getMeasureName()).append("', metric group is '").append(m.getMetricGroupName()).append("'").toString()))
                m = (MonitorMeasure)iterator1.next();

        }
        for(Iterator iterator = measures.iterator(); iterator.hasNext();)
        {
            MonitorMeasure m = (MonitorMeasure)iterator.next();
            if(m.getMeasureName().equals(name))
                return new BaseMeasure(name, m.getMetricGroupName());
        }

        return DEFAULT_BASE_MEASURE;
    }

    protected void setReturnCodeMeasure(MonitorEnvironment env, GEPluginProperties props, GEReturnObject obj)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering setReturnCodeMeasure method, GEReturnObject is '")).append(obj).append("'").toString());
        if(obj == null)
            return;
        Collection monitorMeasures = env.getMonitorMeasures("Generic Execution Monitor", "returnCode");
        for(Iterator iterator = monitorMeasures.iterator(); iterator.hasNext();)
        {
            MonitorMeasure monitorMeasure = (MonitorMeasure)iterator.next();
            monitorMeasure.setValue(obj.getRc() != null ? obj.getRc().intValue() : (0.0D / 0.0D));
            String measure;
            if((measure = props.getRcMeasureName()) != null && !measure.isEmpty())
            {
                MonitorMeasure dynamicMeasure = env.createDynamicMeasure(monitorMeasure, "Return Code", props.getRcMeasureName());
                dynamicMeasure.setValue(obj.getRc() != null ? obj.getRc().intValue() : (0.0D / 0.0D));
            }
        }

    }

    private GEPluginProperties setConfiguration(PluginEnvironment env)
        throws IOException
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering setConfiguration method");
        GEPluginProperties props = new GEPluginProperties();
        String method;
        if((method = env.getConfigString("method")) != null && !(method = method.trim().toUpperCase()).isEmpty() && checkMethod(method))
            props.setMethod(method);
        else
            throw new RuntimeException(method != null ? String.format("The 'method' parameter is '%s' what is incorrect.", new Object[] {
                method
            }) : "The 'method' parameter is null.");
        String value;
        if(method.equalsIgnoreCase("SSH"))
        {
            if((value = env.getConfigString("authMethod")) != null && !(value = value.trim().toUpperCase()).isEmpty() && checkAuthMethod(value))
                props.setAuthMethod(value);
            else
                throw new RuntimeException(value != null ? String.format("The 'authMethod' parameter is '%s' what is incorrect.", new Object[] {
                    value
                }) : "The 'authMethod' parameter is null.");
            if((value = env.getConfigString("host")) != null && !(value = value.trim().toUpperCase()).isEmpty())
                props.setHost(value);
            else
                throw new RuntimeException("The 'host' parameter is null or is an empty string.");
            Long port = Long.valueOf(env.getConfigLong("port") == null || env.getConfigLong("port").longValue() <= 0L ? 22L : env.getConfigLong("port").longValue());
            props.setPort(port.intValue());
            props.setUser(env.getConfigString("user").trim());
            props.setPassword(props.getAuthMethod().equalsIgnoreCase("PublicKey") ? env.getConfigPassword("publicKeyPassphrase") : env.getConfigPassword("password"));
            if(props.getAuthMethod().equalsIgnoreCase("PublicKey"))
                if((value = env.getConfigString("keyFile")) != null && !(value = value.trim()).isEmpty())
                    props.setKeyFile(value);
                else
                    throw new RuntimeException("The 'keyFile' parameter is null or empty.");
        }
        if((env instanceof MonitorEnvironment) || (env instanceof TaskEnvironment))
        {
            Long p = Long.valueOf(env.getConfigLong("port") == null || env.getConfigLong("port").longValue() <= 0L ? 22L : env.getConfigLong("port").longValue());
            props.setPort(p.intValue());
            if(env instanceof MonitorEnvironment)
                props.setHost(((MonitorEnvironment)env).getHost().getAddress());
            else
            if(env instanceof TaskEnvironment)
                props.setHost((value = env.getConfigString("host")) == null || (value = value.trim().toUpperCase()).isEmpty() ? "" : value);
        }
        props.setMultiline(env.getConfigBoolean("isMultiline").booleanValue());
        if(!props.isMultiline())
        {
            if((value = env.getConfigString("command")) != null && !(value = value.trim()).isEmpty())
            {
                props.setCommand(value);
                if(props.getMethod().equalsIgnoreCase("Local"))
                    props.setTokenizedCommand(prepareCommand(props.getCommand()));
            } else
            {
                throw new RuntimeException("The 'command' parameter is null or empty.");
            }
        } else
        if((value = env.getConfigString("commandMultiline")) != null && !(value = value.trim()).isEmpty())
        {
            BufferedReader reader = new BufferedReader(new StringReader(value));
            List list = IOUtils.readLines(reader);
            String sa[] = new String[list.size()];
            int i = 0;
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                String s = (String)iterator.next();
                sa[i++] = s;
            }

            props.setCommandMultiline(sa);
            sa = new String[props.getCommandMultiline().length];
            i = 0;
            String as[];
            int k = (as = props.getCommandMultiline()).length;
            for(int j = 0; j < k; j++)
            {
                String s = as[j];
                sa[i++] = s.trim();
            }

            props.setTokenizedCommand(sa);
        } else
        {
            throw new RuntimeException("The 'commandMultiline' parameter is null or empty.");
        }
        Long timeout = Long.valueOf(env.getConfigLong("Timeout") == null || env.getConfigLong("Timeout").longValue() <= 0L ? 60000L : env.getConfigLong("Timeout").longValue());
        props.setTimeout(timeout.longValue());
        if((value = env.getConfigString("rcMeasureName")) != null && !(value = value.trim()).isEmpty())
            props.setRcMeasureName(value);
        else
            props.setRcMeasureName(null);
        if((value = env.getConfigString("dateFormat")) != null && !(value = value.trim()).isEmpty())
            props.setDateFormat(value);
        else
            props.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        if((value = env.getConfigString("targetTimezone")) != null)
        {
            value = value.trim();
            if(!value.isEmpty() && !ALL_TIME_ZONES.contains(value))
            {
                String msg = (new StringBuilder("setConfiguration method: the Target Timezone parameter '")).append(value).append("' does not match any available timezones. Default timezome will be used").toString();
                String tz[];
                Arrays.sort(tz = TimeZone.getAvailableIDs());
                msg = (new StringBuilder(msg)).append(LS_LOCAL).append("    List of available timezones is ").append(Arrays.toString(tz)).toString();
                log.warning(msg);
                value = "";
            }
            props.setTargetTimezone(value);
            if(!value.isEmpty())
                props.getDateFormat().setTimeZone(props.getTargetTimezone());
        }
        props.setEscapeChars(env.getConfigBoolean("isEscapeChars").booleanValue());
        props.setCapture(env.getConfigBoolean("capture").booleanValue());
        props.setReturnedMeasures((value = env.getConfigString("returnedMeasures")) != null ? value.trim().split(";") : null);
        if(!props.isCapture() && value != null && !value.isEmpty())
            props.setCapture(true);
        props.setOutputBufferSize(env.getConfigLong("outputBufferSize").longValue());
        props.setRegex(env.getConfigString("regex"));
        if(props.getRegex() != null && !props.getRegex().isEmpty())
            props.setPattern(Pattern.compile(props.getRegex(), 32));
        if((value = env.getConfigString("successDefinition")) != null && !(value = value.trim()).isEmpty() && checkSuccessDefinition(value))
            props.setSuccessDefinition(value);
        else
        if(props.getRegex() != null && !props.getRegex().isEmpty())
            throw new RuntimeException(String.format("SuccessDefinition must not be null or empty for given regex '%s'.", new Object[] {
                props.getRegex()
            }));
        props.setTriggerIncident(env.getConfigBoolean("triggerIncident").booleanValue());
        if(props.isTriggerIncident())
        {
            if((value = env.getConfigString("dtServer")) != null && !(value = value.trim()).isEmpty())
            {
                props.setDtServer(value);
            } else
            {
                String msg = "setConfiguration method: Dynatrace Server parameter should be set and be none empty.";
                log.severe(msg);
                throw new RuntimeException(msg);
            }
            if((value = env.getConfigString("protocol")) != null && !(value = value.trim().toLowerCase()).isEmpty() && checkProtocol(value))
            {
                props.setProtocol(value);
            } else
            {
                String msg = "setConfiguration method: Protocol parameter should be set, be none empty, and equals or 'https' or 'http'.";
                log.severe(msg);
                throw new RuntimeException(msg);
            }
            props.setDtPort(env.getConfigLong("dtPort").longValue());
            if((value = env.getConfigString("dtUser")) != null && !(value = value.trim()).isEmpty())
            {
                props.setDtUser(value);
            } else
            {
                String msg = "setConfiguration method: Dynatrace User parameter should be set and be none empty.";
                log.severe(msg);
                throw new RuntimeException(msg);
            }
            if((value = env.getConfigPassword("dtPassword")) != null && !(value = value.trim()).isEmpty())
            {
                props.setDtPassword(value);
            } else
            {
                String msg = "setConfiguration method: Dynatrace User Password parameter should be set and be none empty.";
                log.severe(msg);
                throw new RuntimeException(msg);
            }
            if((value = env.getConfigString("profile")) != null && !(value = value.trim()).isEmpty())
            {
                props.setProfile(URLEncoder.encode(value, "UTF-8").replaceAll("\\+", "%20"));
            } else
            {
                String msg = "setConfiguration method: System Profile parameter should be set and be none empty.";
                log.severe(msg);
                throw new RuntimeException(msg);
            }
            if((value = env.getConfigString("incident")) != null && !(value = value.trim()).isEmpty())
            {
                props.setIncident(URLEncoder.encode(value, "UTF-8").replaceAll("\\+", "%20"));
            } else
            {
                String msg = "setConfiguration method: Incident Rule Name parameter should be set and be none empty.";
                log.severe(msg);
                throw new RuntimeException(msg);
            }
            if((value = env.getConfigString("severity")) != null && !(value = value.trim().toLowerCase()).isEmpty() && checkIncidentSeverity(value))
            {
                props.setSeverity(value);
            } else
            {
                String msg = "setConfiguration method: Incident Rule Name parameter should be set and be none empty.";
                log.severe(msg);
                throw new RuntimeException(msg);
            }
            StringBuilder sb = (new StringBuilder(props.getProtocol())).append("://").append(props.getDtServer()).append(":").append(props.getDtPort()).append("/rest/management/profiles/").append(props.getProfile()).append("/incidentrules/").append(props.getIncident()).append("/incidents/");
            props.setTargetUrl(sb.toString());
        }
        if(log.isLoggable(Level.FINER))
        {
            log.finer((new StringBuilder("GE Plugin Properties: method is '")).append(props.getMethod()).append("',").append(LS_LOCAL).append(" authMethod is '").append(props.getAuthMethod()).append("',").append(LS_LOCAL).append(" host is '").append(props.getHost()).append("',").append(LS_LOCAL).append(" port is '").append(props.getPort()).append("',").append(LS_LOCAL).append(" user is '").append(props.getUser()).append("',").append(LS_LOCAL).append(" password is '").append(props.getPassword()).append("',").append(LS_LOCAL).append(" publicKeyPassphrase is '").append(props.getPublicKeyPassphrase()).append("',").append(LS_LOCAL).append(" keyFile is '").append(props.getKeyFile()).append("',").append(LS_LOCAL).append(" command is '").append(props.isMultiline() ? Arrays.toString(props.getCommandMultiline()) : props.getCommand()).append("',").append(LS_LOCAL).append(" timeout is '").append(props.getTimeout()).append("',").append(LS_LOCAL).append(" dateFormat is '").append(props.getDateFormatString()).append("',").append(LS_LOCAL).append(" targetTimezone is '").append(props.getTargetTimezoneString()).append("',").append(LS_LOCAL).append(" capture is '").append(props.isCapture()).append("',").append(LS_LOCAL).append(" regex is '").append(props.getRegex()).append("',").append(LS_LOCAL).append(" successDefinition is '").append(props.getSuccessDefinition()).append("',").append(LS_LOCAL).append(" trigger incident is '").append(props.isTriggerIncident()).append("'").toString());
            if(props.isTriggerIncident())
                log.finer((new StringBuilder("Trigger Incident properties: dynatrace server is '")).append(props.getDtServer()).append("',").append(LS_LOCAL).append(" dynatrace web server protocol is '").append(props.getProtocol()).append("',").append(LS_LOCAL).append(" dynatrace web server port is '").append(props.getPort()).append("',").append(LS_LOCAL).append(" dynatrace user is '").append(props.getDtUser()).append("',").append(LS_LOCAL).append(" dynatrace user password is '").append(props.getPassword()).append("',").append(LS_LOCAL).append(" dynatrace system profile is '").append(props.getProfile()).append("',").append(LS_LOCAL).append(" dynatrace incident rule name is '").append(props.getIncident()).append("',").append(LS_LOCAL).append(" target url is '").append(props.getTargetUrl()).append("'").toString());
        }
        return props;
    }

    private static boolean checkProtocol(String protocol)
    {
        if(protocol == null || protocol.isEmpty())
            return false;
        return protocol.equalsIgnoreCase("https") || protocol.equalsIgnoreCase("http");
    }

    private static boolean checkIncidentSeverity(String severity)
    {
        if(severity == null || severity.isEmpty())
            return false;
        return severity.equalsIgnoreCase("severe") || severity.equalsIgnoreCase("warning") || severity.equalsIgnoreCase("informational");
    }

    private GEPluginProperties setConfigurationWs(PluginEnvironment env)
        throws UnsupportedEncodingException
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering setConfigurationWs method");
        GEPluginProperties props = new GEPluginProperties();
        props.setWsWSDL(env.getConfigString("wsdl").trim());
        props.setWsOperationName(env.getConfigString("wsOperation").trim());
        if(props.getWsOperationName().isEmpty())
        {
            String msg = "setConfigurationWs method: WS Operation cannot be empty";
            log.severe(msg);
            throw new RuntimeException(msg);
        }
        props.setWsXpathSyntax(env.getConfigBoolean("isXPathSyntax").booleanValue());
        props.setWsParameters(env.getConfigString("wsParameters"));
        Properties p = new Properties();
        try
        {
            p.load(new StringReader(props.getWsParameters()));
            props.setWsParmsSubstituter(p);
        }
        catch(IOException e)
        {
            String msg = (new StringBuilder("setConfigurationWs method: IOException was thrown. Error message is '")).append(e.getMessage()).append("'").toString();
            log.severe(msg);
            throw new RuntimeException(msg);
        }
        String value;
        if((value = env.getConfigString("dateFormat")) != null && !(value = value.trim()).isEmpty())
            props.setDateFormat(value);
        if((value = env.getConfigString("targetTimezone")) != null)
        {
            value = value.trim();
            if(!value.isEmpty() && !ALL_TIME_ZONES.contains(value))
            {
                String msg = (new StringBuilder("setConfiguration method: the Target Timezone parameter '")).append(value).append("' does not match any available timezones. Default timezome will be used").toString();
                String tz[];
                Arrays.sort(tz = TimeZone.getAvailableIDs());
                msg = (new StringBuilder(msg)).append(LS_LOCAL).append("    List of available timezones is ").append(Arrays.toString(tz)).toString();
                log.warning(msg);
                value = "";
            }
            props.setTargetTimezone(value);
            if(!value.isEmpty())
                props.getDateFormat().setTimeZone(props.getTargetTimezone());
        }
        props.setWsAuth(env.getConfigBoolean("isWSAuth").booleanValue());
        if(props.isWsAuth())
        {
            props.setWsUser(env.getConfigString("wsUser"));
            props.setWsPassword(env.getConfigPassword("wsPassword"));
            props.setWsAuthMethod(env.getConfigString("wsAuthMethod"));
            props.setWsAuthString(new String(Base64.encodeBase64((new StringBuilder(String.valueOf(props.getWsUser()))).append(":").append(props.getWsPassword()).toString().getBytes())));
        }
        props.setWsProxyHost(env.getConfigString("wsProxyHost").trim());
        props.setWsProxyPort(env.getConfigLong("wsProxyPort").longValue());
        props.setWsProxyUser(env.getConfigString("wsProxyUser").trim());
        props.setWsProxyPassword(env.getConfigPassword("wsProxyPassword").trim());
        props.setWsUsePrefix(env.getConfigBoolean("wsUsePrefix").booleanValue());
        props.setDotNET(env.getConfigBoolean("isDotNET").booleanValue());
        if((value = env.getConfigString("rcMeasureName")) != null && !(value = value.trim()).isEmpty())
            props.setRcMeasureName(value);
        else
            props.setRcMeasureName(null);
        props.setWSReturnedMeasures(env.getConfigBoolean("isWSReturnedMeasures").booleanValue());
        props.setCapture(env.getConfigBoolean("capture").booleanValue());
        props.setRegex(env.getConfigString("regex"));
        if(props.getRegex() != null && !props.getRegex().isEmpty())
            props.setPattern(Pattern.compile(props.getRegex(), 32));
        if((value = env.getConfigString("successDefinition")) != null && !(value = value.trim()).isEmpty() && checkSuccessDefinition(value))
            props.setSuccessDefinition(value);
        else
        if(props.getRegex() != null && !props.getRegex().isEmpty())
            throw new RuntimeException(String.format("SuccessDefinition must not be null or empty for given regex '%s'.", new Object[] {
                props.getRegex()
            }));
        if(log.isLoggable(Level.FINER))
        {
            log.finer((new StringBuilder("setConfigurationWs method: WSDL is '")).append(props.getWsWSDL()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: operation is '")).append(props.getWsOperationName()).append("'").toString());
            int i = 0;
            java.util.Map.Entry entry;
            for(Iterator iterator = props.getWsParmsSubstituter().entrySet().iterator(); iterator.hasNext(); log.finer((new StringBuilder("setConfigurationWs method: ")).append(++i).append(". '").append(entry.getKey()).append("' is '").append(entry.getValue()).append("'").toString()))
                entry = (java.util.Map.Entry)iterator.next();

            log.finer((new StringBuilder("setConfigurationWs method: dateFormat is '")).append(props.getDateFormatString()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: targetTimezone is '")).append(props.getTargetTimezoneString()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: isWsXpathSyntax is '")).append(props.isWsXpathSyntax()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: isWsAuth is '")).append(props.isWsAuth()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: wsUser is '")).append(props.getWsUser() != null ? (new StringBuilder(String.valueOf(props.getWsUser()))).append("'").toString() : "'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: wsPassword is '")).append(props.getWsPassword() != null ? (new StringBuilder(String.valueOf(props.getWsPassword()))).append("'").toString() : "'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: wsAuthMethod is '")).append(props.getWsAuthMethod() != null ? (new StringBuilder(String.valueOf(props.getWsAuthMethod()))).append("'").toString() : "'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: proxy host is '")).append(props.getWsProxyHost() != null ? (new StringBuilder(String.valueOf(props.getWsProxyHost()))).append("'").toString() : "'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: proxy port is '")).append(props.getWsProxyPort()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: proxy user is '")).append(props.getWsProxyUser() != null ? (new StringBuilder(String.valueOf(props.getWsProxyUser()))).append("'").toString() : "'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: proxy password is '")).append(props.getWsProxyPassword() != null ? (new StringBuilder(String.valueOf(props.getWsProxyPassword()))).append("'").toString() : "'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: isDotNET parameter is '")).append(props.isDotNET()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: WS Use Prefix parameter is '")).append(props.isWsUsePrefix()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: rcMeasureName parameter is '")).append((value = props.getRcMeasureName()) == null ? "null" : value).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: isWsReturnedMeasures parameter is '")).append(props.isWSReturnedMeasures()).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: regex parameter is '")).append((value = props.getRegex()) == null ? "null" : value).append("'").toString());
            log.finer((new StringBuilder("setConfigurationWs method: successDefinition parameter is '")).append((value = props.getSuccessDefinition()) == null ? "null" : value).append("'").toString());
        }
        return props;
    }

    private static boolean isOperation(Definitions defs, String operationName)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering isOperation method: operationName is '")).append(operationName != null ? operationName : "null").append("'").toString());
        if(operationName == null || operationName.isEmpty())
            return false;
        for(Iterator iterator = defs.getPortTypes().iterator(); iterator.hasNext();)
        {
            PortType pt = (PortType)iterator.next();
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("isOperation method: Port Type Name is '")).append(pt.getName()).append("'").toString());
            for(Iterator iterator1 = pt.getOperations().iterator(); iterator1.hasNext();)
            {
                Operation op = (Operation)iterator1.next();
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("isOperation method: Operation name is '")).append(op.getName()).append("'").toString());
                if(op.getName().equals(operationName))
                    return true;
            }

        }

        return false;
    }

    private static SoapData getPortMain(Definitions defs, String operation)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering getPort method");
        SoapData soapData = null;
        List services;
        if((services = defs.getServices()) != null && !services.isEmpty())
        {
            String msg;
            for(Iterator iterator = services.iterator(); iterator.hasNext();)
            {
                com.predic8.wsdl.Service service = (com.predic8.wsdl.Service)iterator.next();
                for(Iterator iterator1 = service.getPorts().iterator(); iterator1.hasNext();)
                {
                    Port pt = (Port)iterator1.next();
                    Binding bnd = pt.getBinding();
                    BindingOperation bndOp;
                    if((bndOp = bnd.getOperation(operation)) != null)
                    {
                        soapData = new SoapData();
                        soapData.setSoapActionUri(bndOp.getOperation().getSoapAction());
                        soapData.setPort(pt);
                        soapData.setPortName(pt.getName());
                        soapData.setLocation(pt.getAddress().getLocation());
                        soapData.setBinding(bnd);
                        soapData.setBindingName(bnd.getName());
                        soapData.setProtocol(bnd.getBinding().getProtocol());
                        if(!BINDING_ID_MAP.containsKey(soapData.getProtocol().toUpperCase()))
                        {
                            msg = (new StringBuilder("getPortMain method: protocol = '")).append(soapData.getProtocol()).append("' is not supported. Supported protocols are '").append(Arrays.toString(BINDING_ID_MAP.keySet().toArray())).append("'").toString();
                            log.severe(msg);
                            throw new RuntimeException(msg);
                        } else
                        {
                            soapData.setBindingId((String)BINDING_ID_MAP.get(soapData.getProtocol().toUpperCase()));
                            soapData.setPortTypeName(bnd.getPortType().getName());
                            return soapData;
                        }
                    }
                }

            }

            log.severe(msg = (new StringBuilder("getPortMain method: There are no bindings found in the WSDL for the operation name '")).append(operation).append("'").toString());
            throw new RuntimeException(msg);
        } else
        {
            String msg;
            log.severe(msg = "getPortMain method: Check wsdl. List of services is null or empty");
            throw new RuntimeException(msg);
        }
    }

    private static WsOpParams getRequestParms(Definitions defs, String portName, String operation)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering getRequestParms method");
        WsOpParams wsOpParams = new WsOpParams(new ArrayList(), new LinkedHashMap());
label0:
        for(Iterator iterator = defs.getPortTypes().iterator(); iterator.hasNext();)
        {
            PortType pt = (PortType)iterator.next();
            Iterator iterator1 = pt.getOperations().iterator();
            while(iterator1.hasNext()) 
            {
                Operation op = (Operation)iterator1.next();
                String opName = op.getName();
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("getRequestParms method: Operation name is '")).append(opName).append("'").toString());
                if(!opName.equals(operation))
                    continue;
                if(log.isLoggable(Level.FINER))
                    log.finer("getRequestParms method: Request Parameters");
                Message message;
                List parts;
                if(op.getInput() != null && (message = op.getInput().getMessage()) != null && (parts = message.getParts()) != null && !parts.isEmpty())
                {
                    for(Iterator iterator2 = parts.iterator(); iterator2.hasNext();)
                    {
                        Part part = (Part)iterator2.next();
                        Element e1;
                        groovy.xml.QName qName;
                        if((e1 = part.getElement()) != null && (qName = e1.getQname()) != null)
                        {
                            Element e = defs.getElement(qName);
                            if(e != null)
                            {
                                listParameters(e, wsOpParams);
                            } else
                            {
                                String msg;
                                log.severe(msg = "getRequestParms method: Check wsdl. Element is null");
                                throw new RuntimeException(msg);
                            }
                            break label0;
                        }
                        String name;
                        TypeDefinition type;
                        groovy.xml.QName qn;
                        String s;
                        if(e1 == null)
                            if(part.getType() != null && part.getType().getQname() != null && part.getType().getQname().getLocalPart() != null && !part.getType().getQname().getLocalPart().equals("complexType"))
                            {
                                if((name = part.getName()) != null && !part.getName().isEmpty() && (type = part.getType()) != null && (qn = type.getQname()) != null && (s = qn.getLocalPart()) != null && !s.isEmpty())
                                {
                                    WSElement wsElement = new WSElement(name, qn);
                                    wsOpParams.getArgList().add(wsElement);
                                    log.finer((new StringBuilder("getRequestParms method: name is '")).append(name).append("', type is '").append(qn.getLocalPart()).append("', namespaceURI is '").append(qn.getNamespaceURI()).append("'").toString());
                                } else
                                {
                                    String msg;
                                    log.severe(msg = "getRequestParms method: Check wsdl. Element of Part is null and other available information is incomplete");
                                    throw new RuntimeException(msg);
                                }
                            } else
                            {
                                String msg;
                                log.severe(msg = "getRequestParms method: Check wsdl. Element e1 of Part is null and other available information is incomplete");
                                throw new RuntimeException(msg);
                            }
                    }

                    continue;
                } else
                {
                    String msg;
                    log.severe(msg = "getRequestParms method: Check wsdl. Input is null or input message is null or parts is null or parts array is empty");
                    throw new RuntimeException(msg);
                }
            }
        }

        return wsOpParams;
    }

    private static void listParameters(Element element, WsOpParams wsOpParams)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering listParameters method");
        TypeDefinition tDef = element.getEmbeddedType();
        ComplexType ct = (ComplexType)tDef;
        if(ct != null && ct.getSequence() != null && ct.getSequence().getElements() != null && !ct.getSequence().getElements().isEmpty())
        {
            for(Iterator iterator = ct.getSequence().getElements().iterator(); iterator.hasNext();)
            {
                Element e = (Element)iterator.next();
                log.finer((new StringBuilder(String.valueOf(e.getName()))).append(" ").append(e.getType()).toString());
                wsOpParams.getArgList().add(e);
                if((!e.getType().getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema") || e.getType().getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema") && e.getType().getLocalPart().equals("complexType")) && !isParameterIn(wsOpParams.getComplexTypes(), e))
                {
                    wsOpParams.getComplexTypes().put(e, new ArrayList());
                    listParameters1(e, wsOpParams);
                }
            }

        } else
        if(element.getType() != null && element.getType().getLocalPart() != null && element.getType().getLocalPart().equals("complexType"))
        {
            ct = element.getSchema().getComplexType(element.getName());
            if(ct != null && ct.getSequence() != null && ct.getSequence().getElements() != null && !ct.getSequence().getElements().isEmpty())
            {
                List list;
                wsOpParams.getComplexTypes().put(element, list = new ArrayList());
                for(Iterator iterator1 = ct.getSequence().getElements().iterator(); iterator1.hasNext();)
                {
                    Element e = (Element)iterator1.next();
                    list.add(e);
                    if(e.getType().getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema") && e.getType().getLocalPart().equals("complexType"))
                    {
                        if(log.isLoggable(Level.FINER))
                            log.finer((new StringBuilder("listParameters1 method: Element name is '")).append(e.getName()).append("', element type is '").append(e.getType()).append("'").toString());
                        listParameters1(e, wsOpParams);
                    }
                }

            }
        } else
        {
            String msg = "listParameters method: operation has no arguments";
            log.warning(msg);
        }
    }

    private static boolean isParameterIn(Map map, Element parameter)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering isParameterIn method");
        for(Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
        {
            Element p = (Element)iterator.next();
            if(p.getName().equals(parameter.getName()))
                return true;
        }

        return false;
    }

    private static void listParameters1(Element element, WsOpParams wsOpParams)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering listParameters1 method");
        ComplexType ct = (ComplexType)element.getEmbeddedType();
        if(ct == null)
            if(element.getType().getLocalPart().equals("complexType"))
                ct = element.getSchema().getComplexType(element.getName());
            else
                ct = element.getSchema().getComplexType(element.getType().getLocalPart());
        if(ct == null)
            return;
        List list;
        if((list = getComplexTypeStructure(element.getName(), wsOpParams)) == null)
            wsOpParams.getComplexTypes().put(element, list = new ArrayList());
        for(Iterator iterator = ct.getSequence().getElements().iterator(); iterator.hasNext();)
        {
            Element e = (Element)iterator.next();
            list.add(e);
            if(e.getType() == null)
            {
                String msg = (new StringBuilder("listParameters1 method: Check wsdl. Element '")).append(element.getName()).append("' has no type").toString();
                log.severe(msg);
                throw new RuntimeException(msg);
            }
            if((!e.getType().getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema") || e.getType().getLocalPart().equals("complexType")) && !isParameterIn(wsOpParams.getComplexTypes(), e))
                listParameters(e, wsOpParams);
        }

    }

    private static List getComplexTypeStructure(String name, WsOpParams wsOpParams)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering getComplexTypeStructure method");
        LinkedHashMap complexTypes = wsOpParams.getComplexTypes();
        for(Iterator iterator = complexTypes.keySet().iterator(); iterator.hasNext();)
        {
            Element p = (Element)iterator.next();
            if(p.getName().equals(name))
                return (List)wsOpParams.getComplexTypes().get(p);
        }

        return null;
    }

    private static String getStringValue(Element element, GEPluginProperties props)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering getStringValue method");
        String key = element.getName();
        Properties customMap = props.getWsParmsSubstituter();
        String value;
        if((value = customMap.getProperty(key)) != null)
        {
            String s = value;
            if(props.getActionStrSubstituter() != null)
            {
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("getStringValue method: Before applying action substituter and escape maps: string s is '")).append(s).append("'").toString());
                s = StringEscapeUtils.escapeXml(props.getActionStrSubstituter().replace(value));
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("getStringValue method: After applying action substituter and escape maps: string s is '")).append(s).append("'").toString());
            }
            if(props.getNonActionStrSubstituter() != null)
                s = props.getNonActionStrSubstituter().replace(s);
            return s;
        } else
        {
            return null;
        }
    }

    private static void addElement(Element element, List list, SOAPElement soapElement, GEPluginProperties props)
        throws SOAPException
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering addElement method, element name is '")).append(element.getName()).append("'").toString());
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("addElement method: element name is '")).append(element.getName()).append("', element.getType().getNamespaceURI() is '").append(element.getType().getNamespaceURI()).append("', element.getType().getLocalPart() is '").append(element.getType().getLocalPart()).append("'").toString());
        if(element.getType().getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema") && !element.getType().getLocalPart().equals("complexType"))
        {
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("addElement method: add simple type: element name is '")).append(element.getName()).append("'").toString());
            String value;
            if((value = getStringValue(element, props)) == null || value.isEmpty())
            {
                if(!element.getMinOccurs().equals("0"))
                {
                    String msg = (new StringBuilder("addElement method: element '")).append(element.getName()).append("' should be present in the WS Parameters list as its minOccurs is '").append(element.getMinOccurs()).append("'").toString();
                    log.severe(msg);
                    throw new RuntimeException(msg);
                }
            } else
            if(props.isWsUsePrefix())
                soapElement.addChildElement(element.getName(), "q0").addTextNode(value);
            else
                soapElement.addChildElement(element.getName()).addTextNode(value);
        } else
        {
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("addElement method: add complex type: element name is '")).append(element.getName()).append("'").toString());
            SOAPElement structureElement;
            if(props.isWsUsePrefix())
                structureElement = soapElement.addChildElement(element.getName(), "q0");
            else
                structureElement = soapElement.addChildElement(element.getName()).addTextNode("");
            for(Iterator iterator = list.iterator(); iterator.hasNext();)
            {
                Element e = (Element)iterator.next();
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("addElement method: inside of for loop: element name is '")).append(e.getName()).append("', e.getType().getNamespaceURI() is '").append(e.getType().getNamespaceURI()).append("', e.getType().getLocalPart() is '").append(e.getType().getLocalPart()).append("'").toString());
                if(e.getType().getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema") && !e.getType().getLocalPart().equals("complexType"))
                {
                    if(log.isLoggable(Level.FINER))
                        log.finer((new StringBuilder("addElement method: inside of for loop: simple type: element name is '")).append(e.getName()).append("'").toString());
                    String value;
                    if((value = getStringValue(e, props)) == null || value.isEmpty())
                    {
                        if(!e.getMinOccurs().equals("0"))
                        {
                            String msg = (new StringBuilder("addElement method: structure element '")).append(e.getName()).append("' should be present in the WS Parameters list as its minOccurs is '").append(e.getMinOccurs()).append("'").toString();
                            log.severe(msg);
                            throw new RuntimeException(msg);
                        }
                    } else
                    if(props.isWsUsePrefix())
                        structureElement.addChildElement(e.getName(), "q0").addTextNode(value);
                    else
                        structureElement.addChildElement(e.getName()).addTextNode(value);
                } else
                {
                    if(log.isLoggable(Level.FINER))
                        log.finer((new StringBuilder("addElement method: inside of for loop: complex type: element name is '")).append(e.getName()).append("'").toString());
                    addElement(e, getComplexTypeStructure(e.getName(), props.getWsOpParams()), structureElement, props);
                }
            }

        }
    }

    private ConnectionMethod getConnection(GEPluginProperties props)
        throws Exception
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering getConnection method");
        ConnectionMethod connMethod = ConnectionMethod.getConnectionMethod(props.getMethod());
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("getConnection method: connection method class is '")).append(connMethod.getClass().getName()).append("'").toString());
        if(props.getMethod().equalsIgnoreCase("SSH"))
            if(props.getAuthMethod().equalsIgnoreCase("PublicKey"))
            {
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("getConnection method: use key file; host is '")).append(props.getHost()).append("', user is '").append(props.getUser()).append("', password is '").append(props.getPassword()).append("', port is '").append(props.getPort()).append("', key file is '").append(props.getKeyFile()).append("'").toString());
                ((SSHConnectionMethod)connMethod).setup(props.getHost(), props.getUser(), props.getPassword(), props.getPort(), props.getKeyFile());
            } else
            {
                if(log.isLoggable(Level.FINER))
                    log.finer((new StringBuilder("getConnection method: use password; host is '")).append(props.getHost()).append("', user is '").append(props.getUser()).append("', password is '").append(props.getPassword()).append("', port is '").append(props.getPort()).append("'").toString());
                ((SSHConnectionMethod)connMethod).setup(props.getHost(), props.getUser(), props.getPassword(), props.getPort());
            }
        return connMethod;
    }

    protected boolean checkSuccessDefinition(String sd)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering checkSuccessDefinition method");
        return sd.equalsIgnoreCase("on match") || sd.equalsIgnoreCase("on no match");
    }

    protected boolean checkMethod(String method)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering checkMethod method");
        return method.equalsIgnoreCase("SSH") || method.equalsIgnoreCase("Local");
    }

    protected boolean checkAuthMethod(String authMethod)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering checkAuthMethod method");
        return authMethod.equalsIgnoreCase("Password") || authMethod.equalsIgnoreCase("PublicKey");
    }

    private void setMatchRuleSuccessMeasure(MonitorEnvironment env, GEPluginProperties props, String output)
    {
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("Entering setMatchRuleSuccessMeasure method, output is '")).append(output).append("'").toString());
        if(output == null)
            return;
        boolean matchFound = false;
        String s;
        if(props.getRegex() != null && !props.getRegex().isEmpty() && (s = props.getSuccessDefinition()) != null && !s.isEmpty())
        {
            matchFound = props.getPattern().matcher(output).find();
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("setMatchRuleSuccessMeasure method: matchFound is '")).append(matchFound).append("'").toString());
            if(s.equalsIgnoreCase("on no match"))
                matchFound = !matchFound;
            if(log.isLoggable(Level.FINER))
                log.finer((new StringBuilder("setMatchRuleSuccessMeasure method: matchFound after applying successDefinition is '")).append(matchFound).append("'").toString());
            Collection measures;
            if((measures = env.getMonitorMeasures("Generic Execution Monitor", "executionSuccess")) != null)
            {
                MonitorMeasure measure;
                for(Iterator iterator = measures.iterator(); iterator.hasNext(); measure.setValue(matchFound ? 1 : 0))
                {
                    measure = (MonitorMeasure)iterator.next();
                    if(log.isLoggable(Level.FINER))
                        log.finer((new StringBuilder("setMatchRuleSuccessMeasure method: set value for the 'executionSuccess' measure with matchFound = '")).append(matchFound).append("'").toString());
                }

            }
        }
    }

    public static Map getNonActionVarsSubstituter(GEPluginProperties props)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering getNonActionVarsSubstituter method");
        Map map = new HashMap();
        String host;
        map.put(NonActionFields.HOST.name(), (host = props.getHost()) != null && !host.isEmpty() ? ((Object) (host)) : "-");
        map.put(NonActionFields.START_TIME.name(), ActionHelper.getDateAsString(new Timestamp30Impl((new Date()).getTime()), props.getDateFormat()));
        if(log.isLoggable(Level.FINER))
            log.finer((new StringBuilder("getNonActionVarsSubstituter method: non-action substituter map is '")).append(Arrays.toString(map.entrySet().toArray())).toString());
        return map;
    }

    public static void escapeChars(Map map, GEPluginProperties props)
    {
        if(log.isLoggable(Level.FINER))
            log.finer("Entering escapeChars method");
        if(props.isEscapeChars() && !props.getMethod().equalsIgnoreCase("Local"))
            HelperUtils.escapeCharsUnix(map);
    }

    private String[] prepareCommand(String subjectString)
    {
        ArrayList matchList = new ArrayList();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
        for(Matcher regexMatcher = regex.matcher(subjectString); regexMatcher.find(); matchList.add(regexMatcher.group()));
        if(log.isLoggable(Level.FINER))
        {
            StringBuilder sb = new StringBuilder("Prepared command to execute: ");
            String match;
            for(Iterator iterator = matchList.iterator(); iterator.hasNext(); sb.append(match).append(" "))
                match = (String)iterator.next();

            log.fine(sb.toString());
        }
        return (String[])matchList.toArray(new String[matchList.size()]);
    }

    private GEPluginProperties pp;
    public static final Logger log = Logger.getLogger(com/dynatrace/diagnostics/plugin/extendedexecutor/GenericExecutor.getName());


}
