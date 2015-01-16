package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import com.dynatrace.diagnostics.pdk.Status;

public interface GEPluginConstants {
	// Configuration constants
	public static final String CONFIG_METHOD = "method";
	public static final String CONFIG_AUTH_METHOD = "authMethod";
	public static final String CONFIG_HOST = "host";
	public static final String CONFIG_PORT = "port";
	public static final String CONFIG_USER = "user";
	public static final String CONFIG_PASSWORD = "password";
	public static final String CONFIG_PASSPHRASE = "publicKeyPassphrase";
	public static final String CONFIG_KEY_FILE = "keyFile";
	public static final String CONFIG_COMMAND = "command";
	public static final String CONFIG_RETURNED_MEASURES = "returnedMeasures";
	public static final String CONFIG_RC_MEASURE_NAME = "rcMeasureName";
	public static final String CONFIG_DATE_FORMAT = "dateFormat";
	public static final String CONFIG_CAPTURE = "capture";
	public static final String CONFIG_OUTPUT_BUFFER_SIZE = "outputBufferSize";
	public static final String CONFIG_REGEX = "regex";
	public static final String CONFIG_SUCCESS_DEFINITION = "successDefinition";
	
	// configuration constants for WS
	public static final String CONFIG_IS_WS = "isWS";
	public static final String CONFIG_WSDL = "wsdl";
	public static final String CONFIG_WS_OPERATION_NAME = "wsOperation";
	public static final String CONFIG_WS_PARAMETERS = "wsParameters";
	public static final String CONFIG_WS_PROXY_HOST = "wsProxyHost";
	public static final String CONFIG_WS_PROXY_PORT = "wsProxyPort";
	public static final String CONFIG_WS_PROXY_USER = "wsProxyUser";
	public static final String CONFIG_WS_PROXY_PASSWORD = "wsProxyPassword";
	public static final String CONFIG_WS_USE_PREFIX = "wsUsePrefix";
	public static final String CONFIG_WS_IS_DOT_NET = "isDotNET";
	
	static final Map<String , String> BINDING_ID_MAP = new HashMap<String , String>() {
		private static final long serialVersionUID = -8617707833297059756L;
	{
	    put("SOAP11", SOAPBinding.SOAP11HTTP_BINDING);
	    put("SOAP12", SOAPBinding.SOAP12HTTP_BINDING);
	    put("HTTP",   HTTPBinding.HTTP_BINDING);
	}};
	
	public static final int DEFAULT_EXTERNAL_RESOLVER_TIMEOUT = 30000;
	
	// end of WS parameters
	
	// Metric group and metrics
	public static final String SPLIT_QUEUE = "Return Code";
	public static final String SPLIT_RETURNED_MEASURES = "Returned Measures";
	public static final String RETURNED_MEASURES_PREFIX = "***ReturnedMeasures:";
	public static final String METRIC_GROUP = "Generic Execution Monitor";
	public static final String MSR_EXEC_SUCCESS = "executionSuccess";
	public static final String MSR_RETURNED_MEASURES = "returnedMeasures";
	public static final String MSR_RETURN_CODE = "returnCode";

	// Default parameters values
	public static final String AUTH_METHOD_PUBLIC_KEY = "PublicKey";
	public static final String AUTH_METHOD_PASSWORD = "Password";
	public static final String SUCCESS_DEFINITION_ON_MATCH = "on match";
	public static final String SUCCESS_DEFINITION_ON_NO_MATCH = "on no match";
	public static final String METHOD_SSH = "SSH";
	public static final String METHOD_LOCAL = "Local";
	public static final int DEFAULT_PORT = 22;
	public static boolean IS_ESCAPE_HTML4 = false;

	// Miscellaneous constants
	public static final Status STATUS_SUCCESS = new Status(Status.StatusCode.Success);
	public static final int XML_INITIAL_LENGTH = 1024; // 1K
	public static final int DEFAULT_STRING_LENGTH = 256;
	public static final  String EMPTY_STRING = "";
	public static final String UTF8 = "UTF-8";
	public static final String DEFAULT_ENCODING = System.getProperty("file.encoding");
	public static final String OUTPUT_FROM_COMMAND = "Output string from the command '%s' %s is '%s'";
	public static final String OS_NAME = System.getProperty("os.name");
	public static final String LS = System.getProperty("line.separator");
	
	// Error messages	
	public static final String METHOD_IS_NULL = "The 'method' parameter is null.";
	public static final String METHOD_IS_INCORRECT = "The 'method' parameter is '%s' what is incorrect.";
	public static final String AUTH_METHOD_IS_NULL = "The 'authMethod' parameter is null.";
	public static final String AUTH_METHOD_IS_INCORRECT = "The 'authMethod' parameter is '%s' what is incorrect.";
	public static final String HOST_IS_NULL_OR_EMPTY = "The 'host' parameter is null or is an empty string.";
	public static final String PORT_IS_NULL = "The 'port' parameter is null.";
	public static final String PORT_IS_NON_POSITIVE = "The 'port' parameter '%d' is incorrect. Port Should be a positive number.";
	public static final String USER_IS_NULL_OR_EMPTY = "The 'user' parameter is null or empty.";
	public static final String PASSWORD_IS_NULL = "The 'password' parameter is null.";
	public static final String KEY_FILE_IS_NULL_OR_EMPTY = "The 'keyFile' parameter is null or empty.";
	public static final String COMMAND_IS_NULL_OR_EMPTY = "The 'command' parameter is null or empty.";
	public static final String SUCCESS_DEFINITION_IS_INCORRECT = "SuccessDefinition must not be null or empty for given regex '%s'.";
	public static final String OUTPUT_IS_NULL = "Null output returned from the '%s' command";
}
