package com.ericsson.cifwk.taf.executor.node;

public class ProcessWithTimeOut extends Thread
{
    private Process process;
    private int exitCode = Integer.MIN_VALUE;

    public ProcessWithTimeOut(Process process)
    {
        this.process = process;
    }

    public int waitForProcess(int timeoutMilliseconds) {
        this.start();

        try {
            this.join(timeoutMilliseconds);
        }
        catch (InterruptedException e) {
        }

        return exitCode;
    }

    @Override
    public void run() {
        try {
            exitCode = process.waitFor();
        }
       catch (Exception ex) {
            // Unexpected exception
        }
    }
}