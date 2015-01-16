package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import groovy.xml.QName;

import com.predic8.schema.Element;

public class WSElement extends Element {
	
	String name;
	QName qName;
	
	public WSElement(String name, QName qName) {
		this.name = name;
		this.qName = qName;
	}
	
	public String getName() {
		return name;
	}

	public QName getType() {
		return qName;
	}

}
