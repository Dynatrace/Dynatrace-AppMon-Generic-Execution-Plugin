package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.remoteconnection.ConnectionMethod;
import com.predic8.schema.Element;
import com.predic8.wsdl.AbstractAddress;
import com.predic8.wsdl.Definitions;

public class GEPluginProperties {
	
	private static final Logger log = Logger.getLogger(GEPluginProperties.class.getName());
	
	private String method;
	private String authMethod;
	private String host;
	private int port;
	private String user = "";
	private String password = "";
	private String publicKeyPassphrase = "";
	private String keyFile;
	private String command;
	private String[] returnedMeasures;
	private String rcMeasureName;
	private String[] tokenizedCommand;
	private String[] transformedTC;
	private SimpleDateFormat dateFormat = null;
	private String dateFormatString;
	private boolean capture;
	private long outputBufferSize;
	private ConnectionMethod connectionMethod;
	private String regex;
	private Pattern pattern;
	private String successDefinition;
	// WS properties
	private boolean ws;
	private String wsWSDL;
	private String wsOperationName;
	private String wsPortName;
	private String wsParameters;
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
	private Map<Element, List<Element>> wsParamsMap;
	private WsOpParams wsOpParams;
	private Map<String, String> actionSubstituter;
	private Map<String, String> nonActionSubstituter;
	private StrSubstitutor actionStrSubstituter;
	private StrSubstitutor nonActionStrSubstituter;	
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getAuthMethod() {
		return authMethod;
	}
	public void setAuthMethod(String authMethod) {
		this.authMethod = authMethod;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPublicKeyPassphrase() {
		return publicKeyPassphrase;
	}
	public void setPublicKeyPassphrase(String publicKeyPassphrase) {
		this.publicKeyPassphrase = publicKeyPassphrase;
	}
	public String getKeyFile() {
		return keyFile;
	}
	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String[] getReturnedMeasures() {
		return returnedMeasures;
	}
	public void setReturnedMeasures(String[] returnedMeasures) {
		this.returnedMeasures = returnedMeasures;
	}
	public String getRcMeasureName() {
		return rcMeasureName;
	}
	public void setRcMeasureName(String rcMeasureName) {
		this.rcMeasureName = rcMeasureName;
	}
	public String[] getTokenizedCommand() {
		return tokenizedCommand;
	}
	public void setTokenizedCommand(String[] tokenizedCommand) {
		this.tokenizedCommand = tokenizedCommand;
	}
	public String[] getTransformedTC() {
		return transformedTC;
	}
	public void setTransformedTC(String[] transformedTC) {
		this.transformedTC = transformedTC;
	}
	public String getDateFormatString() {
		return dateFormatString;
	}
	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormatString) {
		try {
			this.dateFormatString = dateFormatString;
			this.dateFormat	= new SimpleDateFormat(dateFormatString);
		} catch(Exception e) {
			// dateFormat is null, so date format from default locale will be taken
			try {
				log.warning("setDateFormat method: the following date format '" + dateFormatString + "' threw exception '" + HelperUtils.getExceptionAsString(e) + "'");
			} catch (UnsupportedEncodingException e1) {
				// do nothing
			}
		}
	}
	public boolean isCapture() {
		return capture;
	}
	public void setCapture(boolean capture) {
		this.capture = capture;
	}
	public long getOutputBufferSize() {
		return outputBufferSize;
	}
	public void setOutputBufferSize(long outputBufferSize) {
		this.outputBufferSize = outputBufferSize;
	}
	public ConnectionMethod getConnectionMethod() {
		return connectionMethod;
	}
	public void setConnectionMethod(ConnectionMethod connectionMethod) {
		this.connectionMethod = connectionMethod;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	public Pattern getPattern() {
		return pattern;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	public String getSuccessDefinition() {
		return successDefinition;
	}
	public void setSuccessDefinition(String successDefinition) {
		this.successDefinition = successDefinition;
	}
	public boolean isWs() {
		return ws;
	}
	public void setWs(boolean ws) {
		this.ws = ws;
	}
	public String getWsWSDL() {
		return wsWSDL;
	}
	public void setWsWSDL(String wsWSDL) {
		this.wsWSDL = wsWSDL;
	}
	public String getWsOperationName() {
		return wsOperationName;
	}
	public void setWsOperationName(String wsOperationName) {
		this.wsOperationName = wsOperationName;
	}
	public String getWsPortName() {
		return wsPortName;
	}
	public void setWsPortName(String wsPortName) {
		this.wsPortName = wsPortName;
	}
	public String getWsParameters() {
		return wsParameters;
	}
	public void setWsParameters(String wsParameters) {
		this.wsParameters = wsParameters;
	}
	public String getWsProxyHost() {
		return wsProxyHost;
	}
	public void setWsProxyHost(String wsProxyHost) {
		this.wsProxyHost = wsProxyHost;
	}
	public long getWsProxyPort() {
		return wsProxyPort;
	}
	public void setWsProxyPort(long wsProxyPort) {
		this.wsProxyPort = wsProxyPort;
	}
	public String getWsProxyUser() {
		return wsProxyUser;
	}
	public void setWsProxyUser(String wsProxyUser) {
		this.wsProxyUser = wsProxyUser;
	}
	public String getWsProxyPassword() {
		return wsProxyPassword;
	}
	public void setWsProxyPassword(String wsProxyPassword) {
		this.wsProxyPassword = wsProxyPassword;
	}
	public boolean isWsUsePrefix() {
		return wsUsePrefix;
	}
	public void setWsUsePrefix(boolean wsUsePrefix) {
		this.wsUsePrefix = wsUsePrefix;
	}
	public boolean isDotNET() {
		return isDotNET;
	}
	public void setDotNET(boolean isDotNET) {
		this.isDotNET = isDotNET;
	}
	public Properties getWsParmsSubstituter() {
		return wsParmsSubstituter;
	}
	public void setWsParmsSubstituter(Properties wsParmsSubstituter) {
		this.wsParmsSubstituter = wsParmsSubstituter;
	}
	public String getWsProtocol() {
		return wsProtocol;
	}
	public void setWsProtocol(String wsProtocol) {
		this.wsProtocol = wsProtocol;
	}
	public String getWsBindingId() {
		return wsBindingId;
	}
	public void setWsBindingId(String wsBindingId) {
		this.wsBindingId = wsBindingId;
	}
public AbstractAddress getWsAddress() {
		return wsAddress;
	}
	public void setWsAddress(AbstractAddress wsAddress) {
		this.wsAddress = wsAddress;
	}
	public Definitions getWsDefinitions() {
		return wsDefinitions;
	}
	public void setWsDefinitions(Definitions wsDefinitions) {
		this.wsDefinitions = wsDefinitions;
	}
	//	public AbstractAddress getWsAddress() {
//		return wsAddress;
//	}
//	public void setWsAddress(AbstractAddress wsAddress) {
//		this.wsAddress = wsAddress;
//	}
//	public Definitions getWsDefinitions() {
//		return wsDefinitions;
//	}
//	public void setWsDefinitions(Definitions wsDefinitions) {
//		this.wsDefinitions = wsDefinitions;
//	}
	public String getWsTargetNamespace() {
		return wsTargetNamespace;
	}
	public void setWsTargetNamespace(String wsTargetNamespace) {
		this.wsTargetNamespace = wsTargetNamespace;
	}
	public String getWsLocation() {
		return wsLocation;
	}
	public void setWsLocation(String wsLocation) {
		this.wsLocation = wsLocation;
	}
	public Map<Element, List<Element>> getWsParamsMap() {
		return wsParamsMap;
	}
	public void setWsParamsMap(Map<Element, List<Element>> wsParamsMap) {
		this.wsParamsMap = wsParamsMap;
	}
	public WsOpParams getWsOpParams() {
		return wsOpParams;
	}
	public void setWsOpParams(WsOpParams wsOpParams) {
		this.wsOpParams = wsOpParams;
	}
	public Map<String, String> getActionSubstituter() {
		return actionSubstituter;
	}
	public void setActionSubstituter(Map<String, String> actionSubstituter) {
		this.actionSubstituter = actionSubstituter;
	}
	public Map<String, String> getNonActionSubstituter() {
		return nonActionSubstituter;
	}
	public void setNonActionSubstituter(Map<String, String> nonActionSubstituter) {
		this.nonActionSubstituter = nonActionSubstituter;
	}
	public StrSubstitutor getActionStrSubstituter() {
		return actionStrSubstituter;
	}
	public void setActionStrSubstituter(StrSubstitutor actionStrSubstituter) {
		this.actionStrSubstituter = actionStrSubstituter;
	}
	public StrSubstitutor getNonActionStrSubstituter() {
		return nonActionStrSubstituter;
	}
	public void setNonActionStrSubstituter(StrSubstitutor nonActionStrSubstituter) {
		this.nonActionStrSubstituter = nonActionStrSubstituter;
	}
	
}
