package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.util.LinkedHashMap;
import java.util.List;

import com.predic8.schema.Element;

public class WsOpParams {
	private List<Element> argList;
	private LinkedHashMap<Element, List<Element>> complexTypes;
	
	public WsOpParams(List<Element> argList, LinkedHashMap<Element, List<Element>> complexTypes) {
		this.argList = argList;
		this.complexTypes = complexTypes;
	}
	public List<Element> getArgList() {
		return this.argList;
	}
	public LinkedHashMap<Element, List<Element>> getComplexTypes() {
		return complexTypes;
	}
}
