// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GEPluginConstants.java

package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import com.dynatrace.diagnostics.pdk.Status;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Referenced classes of package com.dynatrace.diagnostics.plugin.extendedexecutor.helper:
//            BaseMeasure

public interface GEPluginConstants
{

    public static final String CONFIG_METHOD = "method";
    public static final String CONFIG_AUTH_METHOD = "authMethod";
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_USER = "user";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_PASSPHRASE = "publicKeyPassphrase";
    public static final String CONFIG_KEY_FILE = "keyFile";
    public static final String CONFIG_IS_MULTILINE = "isMultiline";
    public static final String CONFIG_COMMAND = "command";
    public static final String CONFIG_COMMAND_MULTILINE = "commandMultiline";
    public static final String CONFIG_IS_WS_RETURNED_MEASURES = "isWSReturnedMeasures";
    public static final String CONFIG_RETURNED_MEASURES = "returnedMeasures";
    public static final String CONFIG_RC_MEASURE_NAME = "rcMeasureName";
    public static final String CONFIG_DATE_FORMAT = "dateFormat";
    public static final String CONFIG_TARGET_TIMEZONE = "targetTimezone";
    public static final String CONFIG_IS_ESCAPE_CHARS = "isEscapeChars";
    public static final String CONFIG_CAPTURE = "capture";
    public static final String CONFIG_OUTPUT_BUFFER_SIZE = "outputBufferSize";
    public static final String CONFIG_REGEX = "regex";
    public static final String CONFIG_SUCCESS_DEFINITION = "successDefinition";
    public static final String CONFIG_TIMEOUT = "Timeout";
    public static final String CONFIG_TRIGGER_INCIDENT = "triggerIncident";
    public static final String CONFIG_DT_SERVER = "dtServer";
    public static final String CONFIG_PROTOCOL = "protocol";
    public static final String CONFIG_DT_PORT = "dtPort";
    public static final String CONFIG_DT_USER = "dtUser";
    public static final String CONFIG_DT_PASSWORD = "dtPassword";
    public static final String CONFIG_PROFILE = "profile";
    public static final String CONFIG_INCIDENT = "incident";
    public static final String CONFIG_INCIDENT_SEVERITY = "severity";
    public static final String CONFIG_IS_WS = "isWS";
    public static final String CONFIG_WSDL = "wsdl";
    public static final String CONFIG_WS_OPERATION_NAME = "wsOperation";
    public static final String CONFIG_WS_IS_XPATH_SYNTAX = "isXPathSyntax";
    public static final String CONFIG_WS_PARAMETERS = "wsParameters";
    public static final String CONFIG_WS_IS_AUTHENTICATION = "isWSAuth";
    public static final String CONFIG_WS_USER = "wsUser";
    public static final String CONFIG_WS_PASSWORD = "wsPassword";
    public static final String CONFIG_WS_AUTHENTICATION_METHOD = "wsAuthMethod";
    public static final String CONFIG_WS_PROXY_HOST = "wsProxyHost";
    public static final String CONFIG_WS_PROXY_PORT = "wsProxyPort";
    public static final String CONFIG_WS_PROXY_USER = "wsProxyUser";
    public static final String CONFIG_WS_PROXY_PASSWORD = "wsProxyPassword";
    public static final String CONFIG_WS_USE_PREFIX = "wsUsePrefix";
    public static final String CONFIG_WS_IS_DOT_NET = "isDotNET";
    public static final Map BINDING_ID_MAP = new HashMap() {

        private static final long serialVersionUID = 0x8867c0625a3a4054L;

            
            {
                put("SOAP11", "http://schemas.xmlsoap.org/wsdl/soap/http");
                put("SOAP12", "http://www.w3.org/2003/05/soap/bindings/HTTP/");
                put("HTTP", "http://www.w3.org/2004/08/wsdl/http");
            }
    }
;
    public static final int DEFAULT_EXTERNAL_RESOLVER_TIMEOUT = 30000;
    public static final String SPLIT_QUEUE = "Return Code";
    public static final String SPLIT_RETURNED_MEASURES = "Returned Measures";
    public static final String RETURNED_MEASURES_PREFIX = "***ReturnedMeasures:";
    public static final String METRIC_GROUP = "Generic Execution Monitor";
    public static final String MSR_EXEC_SUCCESS = "executionSuccess";
    public static final String MSR_RETURNED_MEASURES = "returnedMeasures";
    public static final String MSR_RETURN_CODE = "returnCode";
    public static final String AUTH_METHOD_PUBLIC_KEY = "PublicKey";
    public static final String AUTH_METHOD_PASSWORD = "Password";
    public static final String SUCCESS_DEFINITION_ON_MATCH = "on match";
    public static final String SUCCESS_DEFINITION_ON_NO_MATCH = "on no match";
    public static final String METHOD_SSH = "SSH";
    public static final String METHOD_LOCAL = "Local";
    public static final int DEFAULT_PORT = 22;
    public static final long DEFAULT_TIMEOUT = 60000L;
    public static final boolean IS_ESCAPE_HTML4 = false;
    public static final Status STATUS_SUCCESS = new Status(com.dynatrace.diagnostics.pdk.Status.StatusCode.Success);
    public static final int XML_INITIAL_LENGTH = 1024;
    public static final int DEFAULT_STRING_LENGTH = 256;
    public static final int MAX_POOL_SIZE = 3;
    public static final String EMPTY_STRING = "";
    public static final String DEFAULT_ENCODING = System.getProperty("file.encoding");
    public static final String OUTPUT_FROM_REMOTE_COMMAND = "Output string from the command '%s' %s is '%s'";
    public static final String OUTPUT_FROM_LOCAL_COMMAND = "%s from the command '%s' %s is '%s'";
    public static final String OS_NAME = System.getProperty("os.name");
    public static final String LS_LOCAL = System.getProperty("line.separator");
    public static final String LS_REMOTE = "\\r?\\n|\\r";
    public static final int WAIT_FOR_REMOTE_JOB_TIMEOUT = 0x493e0;
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
    public static final String COMMAND_MULTILINE_IS_NULL_OR_EMPTY = "The 'commandMultiline' parameter is null or empty.";
    public static final String SUCCESS_DEFINITION_IS_INCORRECT = "SuccessDefinition must not be null or empty for given regex '%s'.";
    public static final String OUTPUT_IS_NULL = "Null output returned from the '%s' command";
    public static final BaseMeasure DEFAULT_BASE_MEASURE = new BaseMeasure("returnedMeasures", "Generic Execution Monitor");
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    public static final String UTF8 = "UTF-8";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String POST_DATA_MESSAGE_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><incident><message>";
    public static final String POST_DATA_MESSAGE_2 = "</message><description>Incident was triggered by the Generic Execution plugin at ";
    public static final String POST_DATA_MESSAGE_2_1 = ". Executed command is '";
    public static final String POST_DATA_MESSAGE_3 = "'</description><severity>";
    public static final String POST_DATA_MESSAGE_4 = "</severity><state>InProgress</state></incident>";
    public static final Set ALL_TIME_ZONES = new HashSet(Arrays.asList(TimeZone.getAvailableIDs()));
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

}
