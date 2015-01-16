package com.dynatrace.diagnostics.plugin.extendedexecutor;

import com.dynatrace.diagnostics.pdk.Action;
import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.Status;

public class GEAction extends GenericExecutor implements Action {

	@Override
	public Status setup(ActionEnvironment env) throws Exception {
		return super.setup(env);
	}

	@Override
	public Status execute(ActionEnvironment env) throws Exception {
		return super.execute(env);
	}

	@Override
	public void teardown(ActionEnvironment env) throws Exception {
		super.teardown(env);
	}
}
