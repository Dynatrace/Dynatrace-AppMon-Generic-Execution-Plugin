package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.util.List;

import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.TypeDefinition;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.PortType;

public class DebugStructure {
	public WsOpParams wsOpParams;
	public Definitions defs;
	public PortType pt;
	public Operation op;
	public String opName;
	public Message message;
	public List<Part> parts;
	public Element e1;
	public Part part;
	public Element e;
	public TypeDefinition tDef;
	public ComplexType ct;
	public Element eListParameters;
	public List<Element> list;
	public ComplexType ctListParameters1;
	public List<Element> listListParameters1;
	public Element eListParameters1;
}
