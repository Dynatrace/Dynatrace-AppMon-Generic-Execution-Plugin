// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GEPluginProperties.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.extendedexecutor.GenericExecutor;
import com.dynatrace.diagnostics.remoteconnection.ConnectionMethod;
import com.predic8.wsdl.AbstractAddress;
import com.predic8.wsdl.Definitions;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.StrSubstitutor;

// Referenced classes of package com.dynatrace.diagnostics.plugin.extendedexecutor.helper:
//            GEPluginConstants, WsOpParams, SoapData

public class GEPluginProperties
    implements GEPluginConstants
{

    public GEPluginProperties()
    {
        user = "";
        password = "";
        publicKeyPassphrase = "";
        dateFormat = null;
    }

    public TimeZone getTargetTimezone()
    {
        return targetTimezone;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public String getAuthMethod()
    {
        return authMethod;
    }

    public void setAuthMethod(String authMethod)
    {
        this.authMethod = authMethod;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPublicKeyPassphrase()
    {
        return publicKeyPassphrase;
    }

    public void setPublicKeyPassphrase(String publicKeyPassphrase)
    {
        this.publicKeyPassphrase = publicKeyPassphrase;
    }

    public String getKeyFile()
    {
        return keyFile;
    }

    public void setKeyFile(String keyFile)
    {
        this.keyFile = keyFile;
    }

    public boolean isMultiline()
    {
        return multiline;
    }

    public void setMultiline(boolean multiline)
    {
        this.multiline = multiline;
    }

    public String getCommand()
    {
        return command;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }

    public String[] getCommandMultiline()
    {
        return commandMultiline;
    }

    public void setCommandMultiline(String commandMultiline[])
    {
        this.commandMultiline = commandMultiline;
    }

    public boolean isWSReturnedMeasures()
    {
        return isWSReturnedMeasures;
    }

    public void setWSReturnedMeasures(boolean isWSReturnedMeasures)
    {
        this.isWSReturnedMeasures = isWSReturnedMeasures;
    }

    public String[] getReturnedMeasures()
    {
        return returnedMeasures;
    }

    public void setReturnedMeasures(String returnedMeasures[])
    {
        this.returnedMeasures = returnedMeasures;
    }

    public String getRcMeasureName()
    {
        return rcMeasureName;
    }

    public void setRcMeasureName(String rcMeasureName)
    {
        this.rcMeasureName = rcMeasureName;
    }

    public String[] getTokenizedCommand()
    {
        return tokenizedCommand;
    }

    public void setTokenizedCommand(String tokenizedCommand[])
    {
        this.tokenizedCommand = tokenizedCommand;
    }

    public String[] getTransformedTC()
    {
        return transformedTC;
    }

    public void setTransformedTC(String transformedTC[])
    {
        this.transformedTC = transformedTC;
    }

    public String getDateFormatString()
    {
        return dateFormatString;
    }

    public SimpleDateFormat getDateFormat()
    {
        return dateFormat;
    }

    public void setDateFormat(String dateFormatString)
    {
        try
        {
            this.dateFormatString = dateFormatString;
            dateFormat = new SimpleDateFormat(dateFormatString);
        }
        catch(Exception e)
        {
            try
            {
                GenericExecutor.log.warning((new StringBuilder("setDateFormat method: the following date format '")).append(dateFormatString).append("' threw exception '").append(HelperUtils.getExceptionAsString(e)).append("'").toString());
            }
            catch(UnsupportedEncodingException _ex) { }
        }
    }

    public String getTargetTimezoneString()
    {
        return targetTimezoneString;
    }

    public void setTargetTimezone(String targetTimezoneString)
    {
        this.targetTimezoneString = targetTimezoneString;
        if(!targetTimezoneString.isEmpty())
            targetTimezone = TimeZone.getTimeZone(targetTimezoneString);
        else
            targetTimezone = TimeZone.getDefault();
    }

    public boolean isEscapeChars()
    {
        return isEscapeChars;
    }

    public void setEscapeChars(boolean isEscapeChars)
    {
        this.isEscapeChars = isEscapeChars;
    }

    public boolean isCapture()
    {
        return capture;
    }

    public void setCapture(boolean capture)
    {
        this.capture = capture;
    }

    public long getOutputBufferSize()
    {
        return outputBufferSize;
    }

    public void setOutputBufferSize(long outputBufferSize)
    {
        this.outputBufferSize = outputBufferSize;
    }

    public ConnectionMethod getConnectionMethod()
    {
        return connectionMethod;
    }

    public void setConnectionMethod(ConnectionMethod connectionMethod)
    {
        this.connectionMethod = connectionMethod;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
    }

    public String getSuccessDefinition()
    {
        return successDefinition;
    }

    public void setSuccessDefinition(String successDefinition)
    {
        this.successDefinition = successDefinition;
    }

    public Map getMeasuresMap()
    {
        return measuresMap;
    }

    public void setMeasuresMap(Map measuresMap)
    {
        this.measuresMap = measuresMap;
    }

    public boolean isTriggerIncident()
    {
        return triggerIncident;
    }

    public void setTriggerIncident(boolean triggerIncident)
    {
        this.triggerIncident = triggerIncident;
    }

    public String getDtServer()
    {
        return dtServer;
    }

    public void setDtServer(String dtServer)
    {
        this.dtServer = dtServer;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public long getDtPort()
    {
        return dtPort;
    }

    public void setDtPort(long dtPort)
    {
        this.dtPort = dtPort;
    }

    public String getDtUser()
    {
        return dtUser;
    }

    public void setDtUser(String dtUser)
    {
        this.dtUser = dtUser;
    }

    public String getDtPassword()
    {
        return dtPassword;
    }

    public void setDtPassword(String dtPassword)
    {
        this.dtPassword = dtPassword;
    }

    public String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        this.profile = profile;
    }

    public String getIncident()
    {
        return incident;
    }

    public void setIncident(String incident)
    {
        this.incident = incident;
    }

    public String getSeverity()
    {
        return severity;
    }

    public void setSeverity(String severity)
    {
        this.severity = severity;
    }

    public String getTargetUrl()
    {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl)
    {
        this.targetUrl = targetUrl;
    }

    public boolean isWs()
    {
        return ws;
    }

    public void setWs(boolean ws)
    {
        this.ws = ws;
    }

    public String getWsWSDL()
    {
        return wsWSDL;
    }

    public void setWsWSDL(String wsWSDL)
    {
        this.wsWSDL = wsWSDL;
    }

    public String getWsOperationName()
    {
        return wsOperationName;
    }

    public void setWsOperationName(String wsOperationName)
    {
        this.wsOperationName = wsOperationName;
    }

    public boolean isWsXpathSyntax()
    {
        return wsXpathSyntax;
    }

    public void setWsXpathSyntax(boolean wsXpathSyntax)
    {
        this.wsXpathSyntax = wsXpathSyntax;
    }

    public String getWsPortName()
    {
        return wsPortName;
    }

    public void setWsPortName(String wsPortName)
    {
        this.wsPortName = wsPortName;
    }

    public String getWsParameters()
    {
        return wsParameters;
    }

    public void setWsParameters(String wsParameters)
    {
        this.wsParameters = wsParameters;
    }

    public boolean isWsAuth()
    {
        return wsAuth;
    }

    public void setWsAuth(boolean wsAuth)
    {
        this.wsAuth = wsAuth;
    }

    public String getWsUser()
    {
        return wsUser;
    }

    public void setWsUser(String wsUser)
    {
        this.wsUser = wsUser;
    }

    public String getWsPassword()
    {
        return wsPassword;
    }

    public void setWsPassword(String wsPassword)
    {
        this.wsPassword = wsPassword;
    }

    public String getWsAuthMethod()
    {
        return wsAuthMethod;
    }

    public void setWsAuthMethod(String wsAuthMethod)
    {
        this.wsAuthMethod = wsAuthMethod;
    }

    public String getWsAuthString()
    {
        return wsAuthString;
    }

    public void setWsAuthString(String wsAuthString)
    {
        this.wsAuthString = wsAuthString;
    }

    public String getWsProxyHost()
    {
        return wsProxyHost;
    }

    public void setWsProxyHost(String wsProxyHost)
    {
        this.wsProxyHost = wsProxyHost;
    }

    public long getWsProxyPort()
    {
        return wsProxyPort;
    }

    public void setWsProxyPort(long wsProxyPort)
    {
        this.wsProxyPort = wsProxyPort;
    }

    public String getWsProxyUser()
    {
        return wsProxyUser;
    }

    public void setWsProxyUser(String wsProxyUser)
    {
        this.wsProxyUser = wsProxyUser;
    }

    public String getWsProxyPassword()
    {
        return wsProxyPassword;
    }

    public void setWsProxyPassword(String wsProxyPassword)
    {
        this.wsProxyPassword = wsProxyPassword;
    }

    public boolean isWsUsePrefix()
    {
        return wsUsePrefix;
    }

    public void setWsUsePrefix(boolean wsUsePrefix)
    {
        this.wsUsePrefix = wsUsePrefix;
    }

    public boolean isDotNET()
    {
        return isDotNET;
    }

    public void setDotNET(boolean isDotNET)
    {
        this.isDotNET = isDotNET;
    }

    public Properties getWsParmsSubstituter()
    {
        return wsParmsSubstituter;
    }

    public void setWsParmsSubstituter(Properties wsParmsSubstituter)
    {
        this.wsParmsSubstituter = wsParmsSubstituter;
    }

    public String getWsProtocol()
    {
        return wsProtocol;
    }

    public void setWsProtocol(String wsProtocol)
    {
        this.wsProtocol = wsProtocol;
    }

    public String getWsBindingId()
    {
        return wsBindingId;
    }

    public void setWsBindingId(String wsBindingId)
    {
        this.wsBindingId = wsBindingId;
    }

    public AbstractAddress getWsAddress()
    {
        return wsAddress;
    }

    public void setWsAddress(AbstractAddress wsAddress)
    {
        this.wsAddress = wsAddress;
    }

    public Definitions getWsDefinitions()
    {
        return wsDefinitions;
    }

    public void setWsDefinitions(Definitions wsDefinitions)
    {
        this.wsDefinitions = wsDefinitions;
    }

    public String getWsTargetNamespace()
    {
        return wsTargetNamespace;
    }

    public void setWsTargetNamespace(String wsTargetNamespace)
    {
        this.wsTargetNamespace = wsTargetNamespace;
    }

    public String getWsLocation()
    {
        return wsLocation;
    }

    public void setWsLocation(String wsLocation)
    {
        this.wsLocation = wsLocation;
    }

    public Map getWsParamsMap()
    {
        return wsParamsMap;
    }

    public void setWsParamsMap(Map wsParamsMap)
    {
        this.wsParamsMap = wsParamsMap;
    }

    public WsOpParams getWsOpParams()
    {
        return wsOpParams;
    }

    public void setWsOpParams(WsOpParams wsOpParams)
    {
        this.wsOpParams = wsOpParams;
    }

    public String getWsSoapMessage()
    {
        return wsSoapMessage;
    }

    public void setWsSoapMessage(String wsSoapMessage)
    {
        this.wsSoapMessage = wsSoapMessage;
    }

    public String getWsPortTypeName()
    {
        return wsPortTypeName;
    }

    public void setWsPortTypeName(String wsPortTypeName)
    {
        this.wsPortTypeName = wsPortTypeName;
    }

    public String getWsBindingName()
    {
        return wsBindingName;
    }

    public void setWsBindingName(String wsBindingName)
    {
        this.wsBindingName = wsBindingName;
    }

    public Map getActionSubstituter()
    {
        return actionSubstituter;
    }

    public void setActionSubstituter(Map actionSubstituter)
    {
        this.actionSubstituter = actionSubstituter;
    }

    public Map getNonActionSubstituter()
    {
        return nonActionSubstituter;
    }

    public void setNonActionSubstituter(Map nonActionSubstituter)
    {
        this.nonActionSubstituter = nonActionSubstituter;
    }

    public StrSubstitutor getActionStrSubstituter()
    {
        return actionStrSubstituter;
    }

    public void setActionStrSubstituter(StrSubstitutor actionStrSubstituter)
    {
        this.actionStrSubstituter = actionStrSubstituter;
    }

    public StrSubstitutor getNonActionStrSubstituter()
    {
        return nonActionStrSubstituter;
    }

    public void setNonActionStrSubstituter(StrSubstitutor nonActionStrSubstituter)
    {
        this.nonActionStrSubstituter = nonActionStrSubstituter;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public SoapData getSoapData()
    {
        return soapData;
    }

    public void setSoapData(SoapData soapData)
    {
        this.soapData = soapData;
    }

    private String method;
    private String authMethod;
    private String host;
    private int port;
    private String user;
    private String password;
    private String publicKeyPassphrase;
    private String keyFile;
    private boolean multiline;
    private String command;
    private String commandMultiline[];
    private boolean isWSReturnedMeasures;
    private String returnedMeasures[];
    private String rcMeasureName;
    private String tokenizedCommand[];
    private String transformedTC[];
    private SimpleDateFormat dateFormat;
    private String dateFormatString;
    private TimeZone targetTimezone;
    private String targetTimezoneString;
    private boolean isEscapeChars;
    private boolean capture;
    private long outputBufferSize;
    private ConnectionMethod connectionMethod;
    private String regex;
    private Pattern pattern;
    private String successDefinition;
    private Map measuresMap;
    private boolean triggerIncident;
    private String dtServer;
    private String protocol;
    private long dtPort;
    private String dtUser;
    private String dtPassword;
    private String profile;
    private String incident;
    private String severity;
    private String targetUrl;
    private boolean ws;
    private String wsWSDL;
    private String wsOperationName;
    private String wsPortName;
    private boolean wsXpathSyntax;
    private String wsParameters;
    private boolean wsAuth;
    private String wsUser;
    private String wsPassword;
    private String wsAuthMethod;
    private String wsAuthString;
    private String wsProxyHost;
    private long wsProxyPort;
    private String wsProxyUser;
    private String wsProxyPassword;
    private boolean wsUsePrefix;
    private boolean isDotNET;
    private Properties wsParmsSubstituter;
    private String wsProtocol;
    private String wsBindingId;
    private AbstractAddress wsAddress;
    private Definitions wsDefinitions;
    private String wsTargetNamespace;
    private String wsLocation;
    private Map wsParamsMap;
    private WsOpParams wsOpParams;
    private String wsSoapMessage;
    private String wsPortTypeName;
    private String wsBindingName;
    private Map actionSubstituter;
    private Map nonActionSubstituter;
    private StrSubstitutor actionStrSubstituter;
    private StrSubstitutor nonActionStrSubstituter;
    private long timeout;
    private SoapData soapData;
}
