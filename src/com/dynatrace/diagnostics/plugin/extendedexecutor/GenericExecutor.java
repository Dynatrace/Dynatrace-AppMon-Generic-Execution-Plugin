package com.dynatrace.diagnostics.plugin.extendedexecutor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.commons.lang3.text.StrSubstitutor;

import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.PluginEnvironment;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;
import com.dynatrace.diagnostics.pdk.TaskEnvironment;
import com.dynatrace.diagnostics.plugin.actionhelper.ActionData;
import com.dynatrace.diagnostics.plugin.actionhelper.ActionHelper;
import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.GEPluginConstants;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.GEPluginProperties;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.NonActionFields;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.Pipe;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.WSElement;
import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.WsOpParams;
import com.dynatrace.diagnostics.remoteconnection.ConnectionMethod;
import com.dynatrace.diagnostics.remoteconnection.GEReturnObject;
import com.dynatrace.diagnostics.remoteconnection.SSHConnectionMethod;
import com.dynatrace.diagnostics.sdk.resources.BaseConstants;
import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.TypeDefinition;
import com.predic8.soamodel.Consts;
import com.predic8.wsdl.AbstractAddress;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.Port;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.xml.util.ExternalResolver;

public class GenericExecutor implements GEPluginConstants {
	private GEPluginProperties pp;

	private static final Logger log = Logger.getLogger(GenericExecutor.class.getName());
	
//	static DebugStructure dbgStr = new DebugStructure();
	
	protected Status setup(PluginEnvironment env) throws Exception {
  		String msg;
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering setup method");
  		}
  
  		// Set environmental configuration parameters
  		try {
  			if (!env.getConfigBoolean("isWS")) {
  				pp = setConfiguration(env);
  			} else {
  				pp = setConfigurationWs(env);
  				pp.setWs(env.getConfigBoolean("isWS"));
  			}
  		} catch (Exception e) {
  			log.severe("setup method: " + HelperUtils.getExceptionAsString(e));
  			return new Status(StatusCode.ErrorInternalConfigurationProblem, e.getMessage(), e.getMessage(), e);
  		}
  		
  		// set start/end incident date format
  		String value;
  		if ((value = env.getConfigString(CONFIG_DATE_FORMAT)) != null && !(value = value.trim()).isEmpty()) {
  			pp.setDateFormat(value);
  		}
  		
  		if (!pp.isWs()) {
  			// Setup connection method
  			try {
  				pp.setConnectionMethod(getConnection(pp));
  			} catch (Exception e) {		
  				log.severe("setup method: " + HelperUtils.getExceptionAsString(e));
  				return new Status(StatusCode.ErrorInternalConfigurationProblem, e.getMessage(), e.getMessage(), e);
  			}
  		} else {
  			// WS data
  			String operation;
  			Port port;
  			Binding binding;
  			String protocol;
  			String bindingId;
  			AbstractAddress address;
  			String portName;
  			
  			WSDLParser parser = new WSDLParser();
  			
  			// setup external resolver if proxy is used
  			if (pp.getWsProxyHost() != null && !pp.getWsProxyHost().isEmpty()) {
	  			ExternalResolver er = new ExternalResolver();
				er.setProxyHost(pp.getWsProxyHost());
				er.setProxyPort((int)pp.getWsProxyPort());
				er.setTimeout(DEFAULT_EXTERNAL_RESOLVER_TIMEOUT);
				parser.setResourceResolver(er);
  			}
  			
  			Definitions defs;
  			//   set definitions
  			pp.setWsDefinitions(defs = parser.parse(pp.getWsWSDL()));
  			//   set targetNamespace
  			pp.setWsTargetNamespace(defs.getTargetNamespace());
  			//   check if operation is present in the wsdl
  			if (!isOperation(defs, (operation = pp.getWsOperationName()))) {
  				log.severe(msg = "setup method: Operation '" + operation + "' is not found");
  				throw new RuntimeException(msg);
  			}
  			
  			portName = (port = getPort(defs, null)) != null ? port.getName() : null;
  			
  			//   first try to get protocol from the port
  			protocol = (port != null && (binding = port.getBinding()) != null ? binding.getProtocol().toString() : null);
  			//   if protocol is null or empty, try to get it from the bindings
  			if (protocol == null || protocol.isEmpty()) {
  				protocol = (binding = getBinding(defs, portName)) != null ? binding.getProtocol().toString() : null;
  			}
  			//   if protocol is still null use default SOAP11
  			if (protocol == null || protocol.isEmpty()) {
  				bindingId = SOAPBinding.SOAP11HTTP_BINDING;
  			} else {
  				bindingId = BINDING_ID_MAP.get(protocol.toUpperCase());
  			}
  			//   get location
  			String location = (port != null && (address = port.getAddress()) != null ? address.getLocation() : null);
  			//   check if pert name, binding id, and location are all populated
  			if (portName == null || portName.isEmpty() || bindingId == null || bindingId.isEmpty() || location == null || location.isEmpty()) {
  				msg = "setup method: there is no port or binding id or location found in the wsdl";
  				log.severe(msg);
  				throw new RuntimeException(msg);
  			}
  			
  			// setup plugin properties for portName, bindingId, and location
  			pp.setWsPortName(portName);
  			pp.setWsBindingId(bindingId);
  			pp.setWsLocation(location);
  			
  			if (log.isLoggable(Level.FINER)) {
  				log.finer("setup method: PortName is '" + portName + "', bindingId is '" + bindingId + "', location is '" + location + "'");
  			}
  			//   get operation's parameters
  			pp.setWsOpParams(getRequestParms(defs, portName, operation));
  			if (log.isLoggable(Level.FINER)) {
  				int i = 0;
  				String structureName = null;
  				List<Element> elements = null;
  				log.finer("setup method: paramsMap entries");
  				for (Entry<Element, List<Element>> entry : pp.getWsOpParams().getComplexTypes().entrySet()) {
  					structureName = entry.getKey().getName();
  					elements = entry.getValue();
  					log.finer(++i + ". " + structureName + ": '" + Arrays.toString(elements.toArray()) + "'");
  				}
  			}
  			
  		}
		return STATUS_SUCCESS;
	}

	protected Status execute(PluginEnvironment env) throws Exception {
  		String output = null;
  		GEReturnObject obj = null;
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering execute method");
  		}
  		try {
  		ActionData action = null;
  		if (env instanceof ActionEnvironment) {
  			// populate incidents for ActionEnvironment
  			action = ActionHelper.populateSourceOfIncidents((ActionEnvironment)env);
  			// set dateFormat
			action.setDateFormat(pp.getDateFormat());
  			// populate substituter map
  			Map<String, String> actionSubstituter;
  			pp.setActionSubstituter(actionSubstituter = ActionHelper.populateSubstituterMap((ActionEnvironment)env, action));
  			pp.setActionStrSubstituter(new StrSubstitutor(actionSubstituter));
  			if (log.isLoggable(Level.FINER)) {
  				log.finer("execute method: substituter map is '"  + Arrays.toString(actionSubstituter.entrySet().toArray()) + "'");
  			}
  			
  			// perform next steps for the command line only
  			if (!pp.isWs()) {
  				// quoting/escaping for special characters
  				// 03-06-2014 ET: change we do escaping only for remote method invocation
  				escapeChars(actionSubstituter, pp);			
  				
  				// apply substitution to the command
  				// 03-06-2014 ET: for the "Local" method we apply substitution for each command token separately
  				if (!pp.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
  					// remote method invocation
  					action.setCommand(pp.getActionStrSubstituter().replace(pp.getCommand()));
  				} else {
  					// Local method invocation
  					int i = 0;
  					String[] as = new String[pp.getTokenizedCommand().length];
  					for (String token : pp.getTokenizedCommand()) {
  						as[i++] = pp.getActionStrSubstituter().replace(token);
  					}
  					action.setTokenizedCommand(as);
  				}
  				
  				if (log.isLoggable(Level.FINE)) {
  					if (!pp.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
  						log.fine("execute method: Substitution of Action variables : Prepared command to execute: '"  + action.getCommand() + "'" + LS);
  					} else {
  						log.fine("execute method: Substitution of Action variables : Prepared tokenized command to execute: '"  + Arrays.toString(action.getTokenizedCommand()) + "'" + LS);
  					}
  				}
  			}
  		}
  		
  		// apply substitution to the non-action variables to the command
  		Map<String, String> nonActionSubstituter;
  		pp.setNonActionSubstituter(nonActionSubstituter = getNonActionVarsSubstituter(pp));
  		pp.setNonActionStrSubstituter(new StrSubstitutor(nonActionSubstituter));
  		//   perform next steps for the command only
		if (!pp.isWs()) {
  			if (action == null) {
  				if (!pp.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
  	  				// remote method invocation
  					(action = new ActionData()).setCommand(pp.getCommand());
  				} else {
  					// 03-06-2014 ET: Local method invocation
  					(action = new ActionData()).setCommand(pp.getCommand());
  					action.setTokenizedCommand(pp.getTokenizedCommand());
  				}
  			}
  			if (!pp.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
  				// remote method invocation
  				action.setCommand(pp.getNonActionStrSubstituter().replace(action.getCommand()));
  			} else {
  				// 03-06-2014 ET: Local method invocation
  				int i = 0;
				String[] as = new String[pp.getTokenizedCommand().length];
				for (String token : action.getTokenizedCommand()) {
					as[i++] = pp.getNonActionStrSubstituter().replace(token);
				}
				action.setTokenizedCommand(as);
  			}
  				
  			if (log.isLoggable(Level.FINE)) {
  				if (!pp.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
  					// remote method invocation
  					log.fine("execute method: Substitution of Non-Action variables : Final command : Prepared command to execute: '"  + action.getCommand() + "'" + LS);
  				} else {
  					// 03-06-2014 ET: Local method invocation
  					log.fine("execute method: Substitution of Action variables : Prepared tokenized command to execute: '"  + Arrays.toString(action.getTokenizedCommand()) + "'" + LS);
  				}
  			}
  				
  			// Execute command on the server 
  			if (!pp.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
  				// remote method invocation
  				obj = pp.getConnectionMethod().executeCommand(action.getCommand(), "", pp.getOutputBufferSize());
  				log.finer("Execute method: method is '" + pp.getMethod() + "', GEReturnObject rc is " + obj.getRc() + ", output is '" + obj.getOutput() + "'");
  			} else {
  				// 03-06-2014 ET: Local method invocation
  				obj = executeLocalCommand(action.getTokenizedCommand(), pp.getOutputBufferSize());
 				log.finer("Execute method: method is '" + pp.getMethod() + "', GEReturnObject rc is " + obj.getRc() + ", output is '" + obj.getOutput() + "'");
  			}
  			output = obj.getOutput();
  			if (log.isLoggable(Level.FINER)) {
  				if (!pp.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
  					// remote method invocation
  					log.finer(output == null ? "execute method: " + String.format(OUTPUT_IS_NULL, action.getCommand()) : "execute method: " + String.format(OUTPUT_FROM_COMMAND, action.getCommand(), LS, output));
  				} else {
  					// 03-06-2014 ET: Local method invocation
  					log.finer(output == null ? "execute method: " + String.format(OUTPUT_IS_NULL, action.getCommand()) : "execute method: " + String.format(OUTPUT_FROM_COMMAND, Arrays.toString(action.getTokenizedCommand()), LS, output));
  				}
  			} 
  			if (output == null) {
  				// output is null, setting up partial success status
  				String msg = String.format(OUTPUT_IS_NULL, action.getCommand());
  				log.severe("execute method: '" + msg);
  				return new Status(Status.StatusCode.PartialSuccess, msg, msg);
  			} else {
  				// set dynamic returned measures
  				if (env instanceof MonitorEnvironment) {
	  				String record = null;
	  				String[] records = output.trim().split(LS);
	  				for (String r : records) {
	  					if (r.startsWith(RETURNED_MEASURES_PREFIX)) {
	  						record = r.substring(RETURNED_MEASURES_PREFIX.length()).trim();
	  						break;
	  					}
	  				}
	  				if (record != null && !record.isEmpty()) {
	  					String[] values = record.split(BaseConstants.SCOLON);
  						//TODO add code here
  						setReturnedMeasures((MonitorEnvironment)env, pp, values);
	  				}
  				}
  			}
  			
  		} else {
  			try {
	  			// WS service call
	  			// setup default proxy selector and authenticator
	  			if (pp.getWsProxyHost() != null && !pp.getWsProxyHost().isEmpty()) {
		  			ProxySelector proxySelector = new ProxySelector() {
		  				@Override
		  				public List<Proxy> select(URI uri) {
		  					// Setting up a new ProxySelector implementation         
		  					Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(pp.getWsProxyHost(), (int)pp.getWsProxyPort())); 
		  					ArrayList<Proxy> list = new ArrayList<Proxy>();         
		  					list.add(proxy);  
		  					if (log.isLoggable(Level.FINER)) {
		  						log.finer("Inner ProxySelector: select method: created proxy: localhost, 6670");
		  						log.finer("Inner ProxySelector: select method: " + Arrays.toString(list.toArray()));
		  					}
		  					return list;     
		  				}
		  				@Override
		  				public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		  					log.severe("Inner ProxySelector:: connectFailed method: Connection to " + uri + " failed. Exception message is '" + ioe.getMessage() + "'");
		  					throw new RuntimeException(ioe);
		  				}
		  	        };
		  	       	ProxySelector.setDefault(proxySelector);  // added proxy selector
		  	        	
		  	        // setup default authenticator if proxy authentication is used
		  	       	if (pp.getWsProxyUser() != null && !pp.getWsProxyUser().isEmpty()) {
			  	    	AuthCacheValue.setAuthCache(new AuthCacheImpl());
			  			Authenticator.setDefault(new Authenticator() { 
			  				@Override
			  				protected PasswordAuthentication getPasswordAuthentication() {
			  					PasswordAuthentication pa = new PasswordAuthentication(pp.getWsProxyUser(), pp.getWsProxyPassword().toCharArray());
			  					if (log.isLoggable(Level.FINER)) {
			  						log.finer(String.format("getPasswordAuthentication method: user is '%s'; password is '%s'; class is '%s'", pa.getUserName(), new String(pa.getPassword()), pa.getClass().getCanonicalName()));
			  					}
			  						
			  					return pa;
			  				}
			  			});
		  	       	}
	  			}
	  	        	
	  			// define service
	  			QName operationQName = new QName(pp.getWsTargetNamespace(), pp.getWsOperationName());
	  			QName portQName = new QName(pp.getWsTargetNamespace(), pp.getWsPortName());
	  			javax.xml.ws.Service svc = javax.xml.ws.Service.create(operationQName);
	  			svc.addPort(portQName, pp.getWsBindingId(), pp.getWsLocation());
	  
	  			// create dispatch object from this service
	  			Dispatch<SOAPMessage> dispatch = svc.createDispatch(portQName, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
	  			// The soapActionUri is set here. otherwise we get an error on .net based services.
	  			if (pp.isDotNET()) {
		  		    String soapActionUri = new StringBuilder(pp.getWsTargetNamespace()).append("/").append(pp.getWsOperationName()).toString();
		  	        dispatch.getRequestContext().put(Dispatch.SOAPACTION_USE_PROPERTY, new Boolean(true));
		  	        dispatch.getRequestContext().put(Dispatch.SOAPACTION_URI_PROPERTY, soapActionUri);
		  		    if (log.isLoggable(Level.FINER)) {
		  		    	log.finer("execute method: soapActionUri is '" + soapActionUri + "'");
		  		    }
	  			}
	  			
	  			// add SOAP message
	  			MessageFactory factory = MessageFactory.newInstance();
	  
	  		    SOAPMessage request = factory.createMessage();
	  		    SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
	  		    
	  		    SOAPBody soapBody = request.getSOAPBody();
	  		    soapBody.addNamespaceDeclaration("q0", pp.getWsTargetNamespace());
	  		    SOAPBodyElement soapBodyElement = soapBody.addBodyElement(envelope.createName(pp.getWsOperationName(), "q0", pp.getWsTargetNamespace()));
	  		    // add parameters
		        for (Element p : pp.getWsOpParams().getArgList()) {
		        	addElement(p, getComplexTypeStructure(p.getName(), pp.getWsOpParams()), soapBodyElement, pp);
				}
		        if (log.isLoggable(Level.FINER)) {
			        ByteArrayOutputStream out = new ByteArrayOutputStream();
			        request.writeTo(out);
			        String msgString = new String(out.toByteArray(), DEFAULT_ENCODING);
			        log.finer("execute method: SOAP request is '" + msgString + "'");
		        }
		        // invoke web service
				SOAPMessage response = dispatch.invoke(request);
	  				
				org.w3c.dom.Node wrapper = response.getSOAPBody().getFirstChild() ;
			    while(wrapper.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
			    	wrapper = wrapper.getNextSibling() ;
			    }
			    org.w3c.dom.Node part = wrapper.getFirstChild() ;
			    while(part.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
			    	part = part.getNextSibling() ;
			    }
			    output = part.getFirstChild().getNodeValue() ;
	  
			    if (log.isLoggable(Level.FINER)) {
			    	log.finer("execute method: Web service returned '" + output + "'");
			    }
  			} catch (Exception e) {
  				String msg;
  				StringBuilder sb = new StringBuilder("execute method: exception is '").append(HelperUtils.getExceptionAsString(e)).append("'");
  				log.severe(msg = sb.toString());
  				return new Status(StatusCode.ErrorInfrastructure, msg, msg);
  			}
		}

  		if (env instanceof MonitorEnvironment) {
  			setMatchRuleSuccessMeasure((MonitorEnvironment)env, pp, output);
  			// set return code dynamic measure
  			if (obj != null) {
  				setReturnCodeMeasure((MonitorEnvironment)env, pp, obj);
  			}
  		}
  		} catch (Exception e) {
  			String msg = HelperUtils.getExceptionAsString(e);
  			log.severe("Execute method: exception is '" + msg + "'");
  			return new Status(StatusCode.ErrorInfrastructure, msg, msg, e);
  		}
  		
  		if (pp.isCapture()) {
  			return new Status(StatusCode.Success, "", output);
  		} else {
  			return STATUS_SUCCESS;
  		}
	}
	
	protected void teardown(PluginEnvironment env) throws Exception {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering teardown method");
		}
		if (pp.getConnectionMethod() != null) {
			pp.getConnectionMethod().teardown();
		}
	}
	
	protected void setReturnCodeMeasure(MonitorEnvironment env, GEPluginProperties props, GEReturnObject obj) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setReturnCodeMeasure method");
		}
		if (obj == null) {
			return;
		}
		// populate return code measure
		Collection<MonitorMeasure> monitorMeasures = env.getMonitorMeasures(METRIC_GROUP, MSR_RETURN_CODE);
		for (MonitorMeasure monitorMeasure : monitorMeasures) {
			monitorMeasure.setValue(obj.getRc() == null ? Double.NaN : obj.getRc());
					
			MonitorMeasure dynamicMeasure;
			String measure;
			if ((measure = props.getRcMeasureName()) != null && !measure.isEmpty()) {
				// setup dynamic measure from the based measure name (i.e add prefix and suffix fields)
				dynamicMeasure = env.createDynamicMeasure(monitorMeasure, SPLIT_QUEUE, props.getRcMeasureName());
				dynamicMeasure.setValue(obj.getRc() == null ? Double.NaN : obj.getRc());
			}
		}
	}
	
	protected void setReturnedMeasures(MonitorEnvironment env, GEPluginProperties props, String[] returnedValues) {
		Collection<MonitorMeasure> monitorMeasures = env.getMonitorMeasures(METRIC_GROUP, MSR_RETURNED_MEASURES);
		for (MonitorMeasure monitorMeasure : monitorMeasures) {
			int i = 0;
			Double d;
			String[] returnedMeasures = props.getReturnedMeasures();
			for (String returnedValue : returnedValues) {
				try {
					d = Double.valueOf(returnedValue);
				} catch (NumberFormatException e) {
					d = Double.NaN;
				}
						
				MonitorMeasure dynamicMeasure;
				String measure;
				if (i < returnedMeasures.length && (measure = returnedMeasures[i]) != null && !measure.isEmpty()) {
					// setup dynamic measure from the based measure name (i.e add prefix and suffix fields)
					dynamicMeasure = env.createDynamicMeasure(monitorMeasure, SPLIT_RETURNED_MEASURES, measure);
					dynamicMeasure.setValue(d);
					i++;
				}
			}
		}
	}

	private GEPluginProperties setConfiguration(PluginEnvironment env) {
		// Variable which holds reference to parameters values
		String value;
		String method;
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setConfiguration method");
		}
		
		// Create GEPluginParameters object
		GEPluginProperties props = new GEPluginProperties();
		
		// set method
		if ((method = env.getConfigString(CONFIG_METHOD)) != null && !(method = method.trim().toUpperCase()).isEmpty() && checkMethod(method)) {
			props.setMethod(method);
		} else {
			throw new RuntimeException(method == null ? METHOD_IS_NULL : String.format(METHOD_IS_INCORRECT, method));
		}
		
		if (method.equalsIgnoreCase(METHOD_SSH)) {
			// set auth method
			if ((value = env.getConfigString(CONFIG_AUTH_METHOD)) != null && !(value = value.trim().toUpperCase()).isEmpty() && checkAuthMethod(value)) {
				props.setAuthMethod(value);
			} else {
				throw new RuntimeException(value == null ? AUTH_METHOD_IS_NULL : String.format(AUTH_METHOD_IS_INCORRECT, value));
			}
			
			// set host
			if ((value = env.getConfigString(CONFIG_HOST)) != null && !(value = value.trim().toUpperCase()).isEmpty()) {
				props.setHost(value);
			} else {
				throw new RuntimeException(HOST_IS_NULL_OR_EMPTY);
			}
	
			// set port
			Long port = env.getConfigLong(CONFIG_PORT) != null && env.getConfigLong(CONFIG_PORT) > 0 ? env.getConfigLong(CONFIG_PORT) : DEFAULT_PORT;
			props.setPort(port.intValue());
				
			// set user
			props.setUser(env.getConfigString(CONFIG_USER).trim());
	
			// set password
			props.setPassword(props.getAuthMethod().equalsIgnoreCase(AUTH_METHOD_PUBLIC_KEY) ? env.getConfigPassword(CONFIG_PASSPHRASE) : env.getConfigPassword(CONFIG_PASSWORD));
				
			// set public key file
			if (props.getAuthMethod().equalsIgnoreCase(AUTH_METHOD_PUBLIC_KEY)) {
				if ((value = env.getConfigString(CONFIG_KEY_FILE)) != null && !(value = value.trim()).isEmpty()) {
					props.setKeyFile(value);
				} else {
					throw new RuntimeException(KEY_FILE_IS_NULL_OR_EMPTY);
				}
			}
		}
		
		// set host and port for monitor and task
		if (env instanceof MonitorEnvironment || env instanceof TaskEnvironment) {
			Long p = env.getConfigLong(CONFIG_PORT) != null && env.getConfigLong(CONFIG_PORT) > 0 ? env.getConfigLong(CONFIG_PORT) : DEFAULT_PORT;
			props.setPort(p.intValue());

			if (env instanceof MonitorEnvironment) {
				// monitor
				props.setHost(((MonitorEnvironment)env).getHost().getAddress());
			} else if (env instanceof TaskEnvironment) {
				// task
				props.setHost((value = env.getConfigString(CONFIG_HOST)) != null && !(value = value.trim().toUpperCase()).isEmpty() ? value : "");
			}
		}
		
		// set command
		if ((value = env.getConfigString(CONFIG_COMMAND)) != null && !(value = value.trim()).isEmpty()) {
			props.setCommand(value);
			// split command on tokens if method is local 
			if (props.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
				props.setTokenizedCommand(prepareCommand(props.getCommand()));
			}
		} else {
			throw new RuntimeException(COMMAND_IS_NULL_OR_EMPTY);
		}
		
		// set dynamic measure name which defines meaning of the return code 
		if ((value = env.getConfigString(CONFIG_RC_MEASURE_NAME)) != null && !(value = value.trim()).isEmpty()) {
			props.setRcMeasureName(value);
		} else {
			props.setRcMeasureName(null);
		}
		
		// set start/end incident date format
		if ((value = env.getConfigString(CONFIG_DATE_FORMAT)) != null && !(value = value.trim()).isEmpty()) {
			props.setDateFormat(value);
		}

		// set capture
		props.setCapture(env.getConfigBoolean(CONFIG_CAPTURE));
		
		// set returned names of measures
		props.setReturnedMeasures((value = env.getConfigString(CONFIG_RETURNED_MEASURES)) == null ? null : value.trim().split(BaseConstants.SCOLON));
		if (!props.isCapture() && value != null && !value.isEmpty()) {
			props.setCapture(true);
		}
		
		// set output buffer size
		props.setOutputBufferSize(env.getConfigLong(CONFIG_OUTPUT_BUFFER_SIZE));
		
		// set regex
		props.setRegex(env.getConfigString(CONFIG_REGEX));
		// set pattern if regex is present
		if (props.getRegex() != null && !props.getRegex().isEmpty()) {
			props.setPattern(Pattern.compile(props.getRegex(), Pattern.DOTALL));
		}
		
		// set success definition
		if ((value = env.getConfigString(CONFIG_SUCCESS_DEFINITION)) != null && !(value = value.trim()).isEmpty() && checkSuccessDefinition(value)) {
			props.setSuccessDefinition(value);
		} else {
			if (props.getRegex() != null && !props.getRegex().isEmpty()) {
				throw new RuntimeException(String.format(SUCCESS_DEFINITION_IS_INCORRECT, props.getRegex()));
			}
		}
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("GE Plugin Properties: method is '" + props.getMethod()
					+ "'," + LS + " authMethod is '" + props.getAuthMethod() + "'"
					+ "'," + LS + " host is '" + props.getHost() + "'"
					+ "'," + LS + " port is '" + props.getPort()
					+ "'," + LS + " user is '" + props.getUser() + "'"
					+ "'," + LS + " password is '" + props.getPassword() + "'"
					+ "'," + LS + " publicKeyPassphrase is '" + props.getPublicKeyPassphrase() + "'"
					+ "'," + LS + " keyFile is '" + props.getKeyFile()
					+ "'," + LS + " command is '" + props.getCommand()
					+ "'," + LS + " dateFormat is '" + props.getDateFormatString()
					+ "'," + LS + " capture is '" + props.isCapture()
					+ "'," + LS + " regex is '" + props.getRegex()
					+ "'," + LS + " successDefinition is '" + props.getSuccessDefinition());
		}

		return props;
	}
	
	// WS methods
  	private GEPluginProperties setConfigurationWs(PluginEnvironment env) {
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering setConfigurationWs method");
  		}
  
  		// Create GEPluginParameters object
  		GEPluginProperties props = new GEPluginProperties();
  		
  		// get wsdl
  		props.setWsWSDL(env.getConfigString(CONFIG_WSDL).trim());
  		
  		
  		// get ws operation
  		props.setWsOperationName(env.getConfigString(CONFIG_WS_OPERATION_NAME).trim());
  		
  		// get ws parameters
  		props.setWsParameters(env.getConfigString(CONFIG_WS_PARAMETERS));
  		Properties p = new Properties();
  	    try {
  	    	p.load(new StringReader(props.getWsParameters()));
  			props.setWsParmsSubstituter(p);
  		} catch (IOException e) {
  			throw new RuntimeException(e.getMessage());
  		}
  		
  		// get ws proxy host
  		props.setWsProxyHost(env.getConfigString(CONFIG_WS_PROXY_HOST).trim());
  		
  		// get ws proxy port
  		props.setWsProxyPort(env.getConfigLong(CONFIG_WS_PROXY_PORT));
  				
  		// get ws proxy user
  		props.setWsProxyUser(env.getConfigString(CONFIG_WS_PROXY_USER).trim());
  		
  		// get ws proxy password
  		props.setWsProxyPassword(env.getConfigPassword(CONFIG_WS_PROXY_PASSWORD).trim());
  		
  		// get usePrefix
  		props.setWsUsePrefix(env.getConfigBoolean(CONFIG_WS_USE_PREFIX));
  		
  		// get isDotNET
  		props.setDotNET(env.getConfigBoolean(CONFIG_WS_IS_DOT_NET));
  		
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("setConfigurationWs method: WSDL is '" + props.getWsWSDL() + "'");
  			log.finer("setConfigurationWs method: operation is '" + props.getWsOperationName() + "'");
      		int i = 0;
      		for (Entry<Object, Object> entry : props.getWsParmsSubstituter().entrySet()) {
      			log.finer("setConfigurationWs method: " + (++i) + ". '" + entry.getKey() + "' is '" + entry.getValue() + "'");
      		}
      		log.finer("setConfigurationWs method: proxy host is '" + (props.getWsProxyHost() == null ? "'" : props.getWsProxyHost() + "'"));
      		log.finer("setConfigurationWs method: proxy port is '" + props.getWsProxyPort() + "'");
      		log.finer("setConfigurationWs method: proxy user is '" + (props.getWsProxyUser() == null ? "'" : props.getWsProxyUser() + "'"));
      		log.finer("setConfigurationWs method: proxy password is '" + (props.getWsProxyPassword() == null ? "'" : props.getWsProxyPassword() + "'"));
  		}
  		
  		return props;		
  	}
  	
  	private static boolean isOperation(Definitions defs, String operationName) {
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering isOperation method");
  		}
  		for (PortType pt : defs.getPortTypes()) {
  			if (log.isLoggable(Level.FINER)) {
  				log.finer("isOperation method: Port name is '" + pt.getName() + "'");
  			}
  			for (Operation op : pt.getOperations()) {
  				if (log.isLoggable(Level.FINER)) {
  					log.finer("isOperation method: Operation name is '" + op.getName() + "'");
  				}
  				if (op.getName().equals(operationName)) {
  					return true;
  				}
  			}
  		}
  		
  		return false;
  	}
  	
  	private static Port getPort(Definitions defs, String portName) {
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering getPort method");
  		}
  		String msg;
  		List<Service> services;
  		Port port = null;
  		List<Port> ports = null;
  		if ((services = defs.getServices()) != null && !services.isEmpty()) {
  			if (portName != null && portName.isEmpty()) {
  				// check if port with portName exists
  				for (Service service : services) {
  					if ((ports = service.getPorts()) != null && !ports.isEmpty()) {
  						for (Port p : ports) {
  							if (p.getName().equals(portName)) {
  								port = p;
  							}
  						}
  					} else {
  						log.severe(msg = "getPort method: Check wsdl. Service '" + service.getName() + "' has no ports");
  						throw new RuntimeException(msg);
  					}
  				}
  			} else {
  				if ((ports = services.get(0).getPorts()) != null && !ports.isEmpty()) {
  					port = services.get(0).getPorts().get(0);
  				} else {
  					log.severe(msg = "getPort method: Check wsdl. Service '" + services.get(0).getName() + "' has no ports");
  					throw new RuntimeException(msg); 
  				}
  			}
  		} else {
  			log.severe(msg = "getPort method: Check wsdl. Array of services is null or empty");
  			throw new RuntimeException(msg);
  		}
  
  		return port;
  	}
  	
  	private static Binding getBinding(Definitions defs, String portName) {
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering getBinding method");
  		}
  		List<Binding> bindings;
  		if ((bindings = defs.getBindings()) != null) {
  			for (Binding binding : bindings) {
  				if (binding.getName().equalsIgnoreCase(portName)) {
  					return binding;
  				}
  			}
  		}
  		
  		return null;
  	}
  	
	private static WsOpParams getRequestParms(Definitions defs, String portName, String operation) {
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering getRequestParms method");
  		}
  		String msg;
  		WsOpParams wsOpParams = new WsOpParams(new ArrayList<Element>(), new LinkedHashMap<Element, List<Element>>());
  		Element e;
  		for (PortType pt : defs.getPortTypes()) {
  			for (Operation op : pt.getOperations()) {
  				String opName = op.getName();
  				if (log.isLoggable(Level.FINER)) {
  					log.finer("getRequestParms method: Operation name is '" + opName + "'");
  				}
  				if (opName.equals(operation)) {
  					//   get request parameters
  					if (log.isLoggable(Level.FINER)) {
  						log.finer("getRequestParms method: Request Parameters");
  					}
  					Message message;
  					List<Part> parts;
  					Element e1;
  					groovy.xml.QName qName;
  					if (op.getInput() != null && (message = op.getInput().getMessage()) != null && (parts = message.getParts()) != null && !parts.isEmpty()) {
  						// input parts list has always one element
  						// ET: added 07-07-2014
  						for (Part part : parts) {
	  						if ((e1 = part.getElement()) != null && (qName = e1.getQname()) != null) {
	  							e = defs.getElement(qName); 
	  							if (e != null) {
	  								listParameters(e, wsOpParams);
	  							} else {
	  								log.severe(msg = "getRequestParms method: Check wsdl. Element is null");
	  								throw new RuntimeException(msg);
	  							}
	  						} else if (e1 == null) {
	  							if (part.getType() != null && part.getType().getQname() != null && part.getType().getQname().getLocalPart() != null && !part.getType().getQname().getLocalPart().equals("complexType")) {
		  							// ET: 07-07-2014
		  							String name;
		  							TypeDefinition type;
		  							groovy.xml.QName qn;
		  							String s;
		  							if ((name = part.getName()) != null && !part.getName().isEmpty() && (type = part.getType()) != null && (qn = type.getQname()) != null && (s = qn.getLocalPart()) != null && !s.isEmpty()) {
		  								WSElement wsElement = new WSElement(name, qn);
		  								wsOpParams.getArgList().add(wsElement);
		  								log.finer("getRequestParms method: name is '" + name + "', type is '" + qn.getLocalPart() + "', namespaceURI is '" + qn.getNamespaceURI() + "'");
		  								
		  							} else {
			  							log.severe(msg = "getRequestParms method: Check wsdl. Element of Part is null and other available information is incomplete");
			  							throw new RuntimeException(msg);
		  							}
	  							} else {
	  								log.severe(msg = "getRequestParms method: Check wsdl. Element e1 of Part is null and other available information is incomplete");
	  		  						throw new RuntimeException(msg);
	  							}
	  						}
  						}
  					} else {
  						log.severe(msg = "getRequestParms method: Check wsdl. Input is null or input message is null or parts is null or parts array is empty");
  						throw new RuntimeException(msg);
  					}
  				}
  			}
  		}
  		
  		return wsOpParams;
  	}
  	
  	private static void listParameters(Element element, WsOpParams wsOpParams) {
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering listParameters method");
  		}
  		TypeDefinition tDef = element.getEmbeddedType();
//  		if (tDef instanceof ComplexType) {
	  		ComplexType ct = (ComplexType) tDef;
	  		if (ct != null && ct.getSequence() != null && ct.getSequence().getElements() != null && !ct.getSequence().getElements().isEmpty()) {
	  			for (Element e : ct.getSequence().getElements()) {
	  				log.finer(e.getName() + " " + e.getType());
	  				wsOpParams.getArgList().add(e);
	  				if (!e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) || (e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) && e.getType().getLocalPart().equals("complexType"))) {
	  					// for complex types drill into their structure
	  					if (!isParameterIn(wsOpParams.getComplexTypes(), e)) {
	  						wsOpParams.getComplexTypes().put(e, new ArrayList<Element>());
	  						listParameters1(e, wsOpParams);
	  					}
	  				}
	  			}
	  		} else if (element.getType() != null && element.getType().getLocalPart() != null && element.getType().getLocalPart().equals("complexType")) {
	  			ct = element.getSchema().getComplexType(element.getName());
				if (ct != null && ct.getSequence() != null && ct.getSequence().getElements() != null && !ct.getSequence().getElements().isEmpty()) {
					List<Element> list;
					wsOpParams.getComplexTypes().put(element, (list = new ArrayList<Element>()));
					for (Element e : ct.getSequence().getElements()) {
						list.add(e);
						if (e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) && e.getType().getLocalPart().equals("complexType")) {
			  				if (log.isLoggable(Level.FINER)) {
			  					log.finer("listParameters1 method: Element name is '" + e.getName() + "', element type is '" + e.getType() + "'");
			  				}
			  				listParameters1(e, wsOpParams);
						}
					}
				}
	  		} else {
	  			String msg = "listParameters method: Check wsdl. Complex type is null, or sequence is null, or elements list is null, or elements list is empty";
	  			log.severe(msg);
	  			throw new RuntimeException(msg);
	  		}
//  		}
  	}
  	
  	private static boolean isParameterIn(Map<Element, List<Element>> map, Element parameter) {
  		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering isParameterIn method");
		}
  		for (Element p : map.keySet()) {
  			if (p.getName().equals(parameter.getName())) {
  				return true;
  			}
  		}
  		
  		return false;
  	}
  
  	private static void listParameters1(Element element, WsOpParams wsOpParams) {
  		if (log.isLoggable(Level.FINER)) {
  			log.finer("Entering listParameters1 method");
  		}
  		ComplexType ct = (ComplexType) element.getEmbeddedType();
  		if (ct == null) {
//			ct = element.getSchema().getComplexType(element.getType().getLocalPart()); // if element is type from the Consts.SCHEMA_NS then ct is null
			if (element.getType().getLocalPart().equals("complexType")) {
				ct = element.getSchema().getComplexType(element.getName());
			} else {
				ct = element.getSchema().getComplexType(element.getType().getLocalPart());
			}
  		}
  		
  		if (ct == null) {
  			return;
  		}
  		
  		List<Element> list;
  		if ((list = getComplexTypeStructure(element.getName(), wsOpParams)) == null) {
  			wsOpParams.getComplexTypes().put(element, list = new ArrayList<Element>());
  		} 
  		for (Element e : ct.getSequence().getElements()) {
  			list.add(e);
  			// Fix for invalid schema
  			if (e.getType() == null) {
  				String msg = "listParameters1 method: Check wsdl. Element '" + element.getName() + "' has no type";
  				log.severe(msg);
  				throw new RuntimeException(msg);
  			}
  			if (!e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) || e.getType().getLocalPart().equals("complexType")) {
  				// element is a complex type: let's drill into it
  				if (!isParameterIn(wsOpParams.getComplexTypes(), e)) {
  					listParameters(e, wsOpParams);
				}
  			}
  		}
  	}
  	
  	private static List<Element> getComplexTypeStructure(String name, WsOpParams wsOpParams) {
  		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getComplexTypeStructure method");
		}
  		LinkedHashMap<Element, List<Element>> complexTypes = wsOpParams.getComplexTypes();
  		
  		for (Element p : complexTypes.keySet()) {
  			if (p.getName().equals(name)) {
  				return wsOpParams.getComplexTypes().get(p);
  			}
  		}
  		
  		return null;
  	}
  	
  	private static String getStringValue(Element element, GEPluginProperties props) {
  		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getStringValue method");
		}
  		String value;
  		String key = element.getName();
  		Properties customMap = props.getWsParmsSubstituter();
  		if ((value = customMap.getProperty(key)) != null) {
  			// process value string
  			String s = value;
  			if (props.getActionStrSubstituter() != null) {
  				s = props.getActionStrSubstituter().replace(value);
  			}  			
  			if (props.getNonActionStrSubstituter() != null) {
  				return(props.getNonActionStrSubstituter().replace(s));
  			}
  			return s;
  		} else {
   			return null;
  		}  		
  	}
  	
//  	private static String getDefaultValue (Element element) {
//  		if (log.isLoggable(Level.FINER)) {
//			log.finer("Entering getDefaultValue method");
//		}
//      	if (element.getType().getLocalPart().equals("string")) {
//      		return "_UNDEFINED_" + element.getName() + "_";
//      	} else if (element.getType().getLocalPart().equals("int")) {
//      		return String.valueOf(Integer.MIN_VALUE);
//      	} else if (element.getType().getLocalPart().equals("boolean")) {
//      		return "false";
//      	} else if (element.getType().getLocalPart().equals("dateTime")) {
//      		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //  "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
//      		return sdf.format(new Date());
//      	} else {
//      		log.warning("use string for the undefined datatype");
//      		return element.getName() + element.getType();
//      	}
//  	}
  	
  	private static void addElement(Element element, List<Element> list, SOAPElement soapElement, GEPluginProperties props) throws SOAPException {
  		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering addElement method, element name is '" + element.getName() + "'");
		}
  		String value;
  		if (log.isLoggable(Level.FINER)) {
	  		log.finer("addElement method: element name is '" + element.getName() 
	  				+ "', element.getType().getNamespaceURI() is '" + element.getType().getNamespaceURI() 
	  				+ "', element.getType().getLocalPart() is '" + element.getType().getLocalPart() + "'");
  		}
  		if (element.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) && !element.getType().getLocalPart().equals("complexType")) {
  			// add simple type
  			if (log.isLoggable(Level.FINER)) {
  				log.finer("addElement method: add simple type: element name is '" + element.getName() + "'");
  			}
  			if ((value = getStringValue(element, props)) == null || value.isEmpty()) {
  				// element is not present in the WS Parameters list or its value is empty
  				if (!element.getMinOccurs().equals("0")) {
  					// throw exception
  					String msg = "addElement method: element '" + element.getName() + "' should be present in the WS Parameters list as its minOccurs is '" + element.getMinOccurs() + "'";
  					log.severe(msg);
  					throw new RuntimeException(msg);
  				}
  			} else {
  				if (props.isWsUsePrefix()) {
  					soapElement.addChildElement(element.getName(), "q0").addTextNode(value);
  				} else {
  					soapElement.addChildElement(element.getName()).addTextNode(value);
  				}
  			}
		} else {
			// add complex type
			if (log.isLoggable(Level.FINER)) {
				log.finer("addElement method: add complex type: element name is '" + element.getName() + "'");
			}
			SOAPElement structureElement;
			if (props.isWsUsePrefix()) {
				structureElement = soapElement.addChildElement(element.getName(), "q0");
			} else {
				structureElement = soapElement.addChildElement(element.getName()).addTextNode(""); // added .addTextNode("") as workaround to avoid <... xmlns="">
			}
	        for (Element e : list) {
	        	if (log.isLoggable(Level.FINER)) {
		        	log.finer("addElement method: inside of for loop: element name is '" + e.getName() 
		      				+ "', e.getType().getNamespaceURI() is '" + e.getType().getNamespaceURI() 
		      				+ "', e.getType().getLocalPart() is '" + e.getType().getLocalPart() + "'");
	        	}
	        	if (e.getType().getNamespaceURI().equals(Consts.SCHEMA_NS) && !e.getType().getLocalPart().equals("complexType")) {
	        		// add simple type
	        		if (log.isLoggable(Level.FINER)) {
	        			log.finer("addElement method: inside of for loop: simple type: element name is '" + e.getName() + "'");
	        		}
	        		if ((value = getStringValue(e, props)) == null || value.isEmpty()) {
	        			// element is not present in the WS Parameters list or its value is empty
	      				if (!e.getMinOccurs().equals("0")) {
	      					// throw exception
	      					String msg = "addElement method: structure element '" + e.getName() + "' should be present in the WS Parameters list as its minOccurs is '" + e.getMinOccurs() + "'";
	      					log.severe(msg);
	      					throw new RuntimeException(msg);
	      				}
	      			} else {
	      				if (props.isWsUsePrefix()) {
	      					structureElement.addChildElement(e.getName(), "q0").addTextNode(value);
	      				} else {
	      					structureElement.addChildElement(e.getName()).addTextNode(value);
	      				}
	      			}
	        	} else {
	        		// add complex type
	        		if (log.isLoggable(Level.FINER)) {
	        			log.finer("addElement method: inside of for loop: complex type: element name is '" + e.getName() + "'");
	        		}
	        		addElement(e, getComplexTypeStructure(e.getName(), props.getWsOpParams()), structureElement, props);
	        	}
	        }
		}

  	}
	//   end of WS methods
	
	private ConnectionMethod getConnection(GEPluginProperties props) throws Exception {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getConnection method");
		}

		ConnectionMethod connMethod = ConnectionMethod.getConnectionMethod(props.getMethod());

		if (props.getMethod().equalsIgnoreCase(METHOD_SSH)) {
			if (props.getAuthMethod().equalsIgnoreCase(AUTH_METHOD_PUBLIC_KEY)) {
				// use key file
				((SSHConnectionMethod) connMethod).setup(props.getHost(), props.getUser(), props.getPassword(), props.getPort(), props.getKeyFile());
			} else {
				// Use password
				((SSHConnectionMethod) connMethod).setup(props.getHost(), props.getUser(), props.getPassword(), props.getPort());
			}
		}
		return connMethod;
	}

	protected boolean checkSuccessDefinition(String sd) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering checkSuccessDefinition method");
		}
		return sd.equalsIgnoreCase(SUCCESS_DEFINITION_ON_MATCH) || sd.equalsIgnoreCase(SUCCESS_DEFINITION_ON_NO_MATCH);
	}
	
	protected boolean checkMethod(String method) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering checkMethod method");
		}
		return method.equalsIgnoreCase(METHOD_SSH) || method.equalsIgnoreCase(METHOD_LOCAL);
	}
	
	protected boolean checkAuthMethod(String authMethod) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering checkAuthMethod method");
		}
		return authMethod.equalsIgnoreCase(AUTH_METHOD_PASSWORD) || authMethod.equalsIgnoreCase(AUTH_METHOD_PUBLIC_KEY);
	}
	
	private void setMatchRuleSuccessMeasure(MonitorEnvironment env, GEPluginProperties props, String output) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setMatchRuleSuccessMeasure method");
		}
		// match output if regex is set for monitors
		String s;
		boolean matchFound = false;
		if (props.getRegex() != null && !props.getRegex().isEmpty() && (s = props.getSuccessDefinition()) != null && !s.isEmpty()) {
			matchFound = props.getPattern().matcher(output).find();
			if (s.equalsIgnoreCase(SUCCESS_DEFINITION_ON_NO_MATCH)) {
				matchFound = !matchFound;
			}			
					
			Collection<MonitorMeasure> measures;
			if ((measures = ((MonitorEnvironment)env).getMonitorMeasures(METRIC_GROUP, MSR_EXEC_SUCCESS)) != null) {
				for (MonitorMeasure measure : measures) {
					measure.setValue(matchFound ? 1 : 0);
				}
			}
		}
	}
	
	public static Map<String, String> getNonActionVarsSubstituter(GEPluginProperties props) {
		String host;
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getNonActionVarsSubstituter method");
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put(NonActionFields.HOST.name(), (host = props.getHost()) == null || host.isEmpty() ? BaseConstants.DASH : host);
		map.put(NonActionFields.PORT.name(), Integer.toString(props.getPort()));
		if (log.isLoggable(Level.FINER)) {
			log.finer("getNonActionVarsSubstituter method: non-action substituter map is '"  + Arrays.toString(map.entrySet().toArray()));
		}
		return map;	
	}
	
	public static void escapeChars(Map<String, String> map, GEPluginProperties props) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering escapeChars method");
		}
		/*
		 * 03-06-2014 ET: 	commented and replaced by the next if statement. 
		 * 					For "Local" method no processing of the escape characters because we tokenized command before execution
		 * 
		 * 
		if (props.getMethod().equalsIgnoreCase(METHOD_LOCAL) && OS_NAME.startsWith("W")) {
			// escape Windows special chars
			HelperUtils.escapeCharsWin(map);
		} else {
			// escape Unix special chars
			HelperUtils.escapeCharsUnix(map);
		}
		*/
		if (!props.getMethod().equalsIgnoreCase(METHOD_LOCAL)) {
			// escape Unix special chars
			HelperUtils.escapeCharsUnix(map);
		}
	}
	
	private String[] prepareCommand(String subjectString) {
		ArrayList<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"']+|\"[^\"]*\"|'[^']*'");
		Matcher regexMatcher = regex.matcher(subjectString);
		while (regexMatcher.find()) {
			matchList.add(regexMatcher.group());
		}

		if (log.isLoggable(Level.FINE)) {
			StringBuilder sb = new StringBuilder("Prepared command to execute: ");
			for (String match : matchList) {
				sb.append(match).append(" ");
			}
			log.fine(sb.toString());
		}
		return matchList.toArray(new String[matchList.size()]);
	}
	
	private GEReturnObject executeLocalCommand(String[] command, long size) throws IOException, InterruptedException {
		// add threads to read out and err
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
				
		Process child = Runtime.getRuntime().exec(command); 
		Pipe.pipe(child, out, err, size);
		child.waitFor(); 
		Integer rc = child.exitValue();
			
		StringBuilder sb = new StringBuilder(out.size() + err.size() + 100);
		sb.append("\n").append("*** Output stream ***").append("\n").append(out.toString(DEFAULT_ENCODING))
			.append("\n").append("*** End of Output stream ***").append("\n").append("*** Error stream ***").append("\n")
			.append(err.toString(DEFAULT_ENCODING)).append("\n").append("*** End of Error stream ***").append("\n");
		GEReturnObject obj = new GEReturnObject();
		obj.setOutput(sb.toString());
		obj.setRc(rc);
		if (child != null) {
			child.destroy();
		}
		return obj;
	}

}
