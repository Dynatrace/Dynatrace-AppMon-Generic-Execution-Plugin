package com.dynatrace.diagnostics.plugin.extendedexecutor;

import com.dynatrace.diagnostics.pdk.*;


public class GETask extends GenericExecutor implements Task {

	@Override
	public Status setup(TaskEnvironment env) throws Exception {
		return super.setup(env);
	}
	@Override
	public Status execute(TaskEnvironment env) throws Exception {
		return super.execute(env);
	}
	@Override
	public void teardown(TaskEnvironment env) throws Exception {
		super.teardown(env);
	}
}
