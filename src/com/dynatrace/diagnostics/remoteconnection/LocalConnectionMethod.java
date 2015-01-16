package com.dynatrace.diagnostics.remoteconnection;

import java.io.ByteArrayOutputStream;

import com.dynatrace.diagnostics.plugin.extendedexecutor.helper.Pipe;


public class LocalConnectionMethod extends
		com.dynatrace.diagnostics.remoteconnection.ConnectionMethod {

	public LocalConnectionMethod() {
	}

	public GEReturnObject executeCommand(String command, String env, long size) throws java.lang.Exception {
		Process child = Runtime.getRuntime().exec(command,
				env.equals("") ? (new java.lang.String[] { /* line 9 */
				"LANG=C" /* 11 */
				}) : (new java.lang.String[] { "LANG=C", env /* line 12 */
				}));
		// add threads to read out and err
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		Pipe.pipe(child, out, err, size);
		child.waitFor(); /* line 13 */
		Integer rc = null;
		if (child != null) {
			rc = child.exitValue();
		}
		
		StringBuilder sb = new StringBuilder(out.size() + err.size() + 100);
		sb.append("\n").append("*** Output stream ***").append("\n").append(out.toString(DEFAULT_ENCODING))
			.append("\n").append("*** End of Output stream ***").append("\n").append("*** Error stream ***").append("\n")
			.append(err.toString(DEFAULT_ENCODING)).append("\n").append("*** End of Error stream ***").append("\n");
		if (child != null) {
			child.destroy();
		}
		GEReturnObject obj = new GEReturnObject();
		obj.setOutput(sb.toString());
		obj.setRc(rc);
		return obj;
	}
}
