package com.dynatrace.diagnostics.plugin.extendedexecutor.helper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.apache.commons.lang3.ArrayUtils;

import com.dynatrace.diagnostics.plugin.actionhelper.HelperUtils;
import com.dynatrace.diagnostics.plugin.extendedexecutor.GenericExecutor;

public class Pipe implements Runnable {
	private static final Logger log = Logger.getLogger(GenericExecutor.class.getName());
	private final InputStream in;
    private final OutputStream out;
    private final long size;

    private Pipe(InputStream in, OutputStream out, long size) {
        this.in = in;
        this.out = out;
        this.size = size;
        
    }

    public static void pipe(Process process, ByteArrayOutputStream out, ByteArrayOutputStream err, long size) {
        pipe(process.getInputStream(), out, size);
        pipe(process.getErrorStream(), err, size);
//        pipe(System.in, process.getOutputStream());
    }

    public static void pipe(InputStream in, OutputStream out, long size) {
        final Thread thread = new Thread(new Pipe(in, out, size));
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        try {
            int i = -1;
            byte[] buf = new byte[1024];

            int bufSize = 0;
            boolean isWriting = true;
            while (isWriting && (i = in.read(buf)) != -1) {
            	if ((bufSize += i) <= size) {
            		out.write(buf, 0, i);
            	} else {
            		byte[] bf1 = new byte[i];
            		System.arraycopy(buf,  0, bf1, 0, i);
            		out.write(ArrayUtils.addAll(bf1, " ...buffer size is exceeded".getBytes()), 0, i + " ...buffer size is exceeded".length());
            		isWriting = false;
            	}
            }
        } catch (Exception e) {
            try {
				log.severe(HelperUtils.getExceptionAsString(e));
			} catch (UnsupportedEncodingException e1) {
				// do nothing
			}
        }
        
        return;
    }
}
