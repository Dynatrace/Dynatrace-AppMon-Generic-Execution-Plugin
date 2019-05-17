// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GEReturnObject.java

package com.dynatrace.diagnostics.remoteconnection;


public class GEReturnObject
{

    public GEReturnObject()
    {
        output = null;
        stdout = null;
        stderr = null;
        rc = Integer.valueOf(-1);
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public String getStdout()
    {
        return stdout;
    }

    public void setStdout(String stdout)
    {
        this.stdout = stdout;
    }

    public String getStderr()
    {
        return stderr;
    }

    public void setStderr(String stderr)
    {
        this.stderr = stderr;
    }

    public Integer getRc()
    {
        return rc;
    }

    public void setRc(Integer rc)
    {
        this.rc = rc;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("GEReturnObject [output=").append(output).append(", stdout=").append(stdout).append(", stderr=").append(stderr).append(", rc=").append(rc).append("]");
        return builder.toString();
    }

    private String output;
    private String stdout;
    private String stderr;
    private Integer rc;
}
