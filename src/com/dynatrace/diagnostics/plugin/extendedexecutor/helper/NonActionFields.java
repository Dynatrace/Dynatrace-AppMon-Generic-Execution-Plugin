package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

public enum NonActionFields {
	HOST,
	PORT,
	UNKNOWN;
	
	public static NonActionFields getValue(String value) {
        try {
            return valueOf(value.replaceAll("-", "_").toUpperCase());
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

}
