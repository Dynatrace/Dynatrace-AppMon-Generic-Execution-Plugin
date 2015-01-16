package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

public class Parameter {
	@Override
	public String toString() {
		return "Parameter [name=" + name + ", type=" + type + "]";
	}
	public Parameter(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}
	private String name;
	private String type;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
