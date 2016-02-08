package eu.danman.zidostreamer.zidostreamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Allows to monitor a {@link Process} and resurrect it upon its untimely death.
 *
 * First instantiate the object of this class with full cmd line passed to
 * constructor and then {@link #start} the process as usual. Then, in regular
 * intervals, call {@link #monitor()} method. If process dies before
 * {@link #stop()} is requested it will be respawned.
 *
 * This allows to ensure restartable services, such as ffmpeg streaming, to live
 * on when the service process crashes stupidly, such as ffmpeg when ethernet
 * cable is disconnected.
 *
 * @author Zalewa
 */
public class MonitoredProcess {
    private Process process;
    private String cmd;
    private boolean stopped = true;

    private OutputStream outputStream;
    private InputStream inputStream;
    private InputStream errorStream;

    public MonitoredProcess(String cmd) {
        this.cmd = cmd;
    }

    public synchronized void start() throws IOException {
        if (!stopped) {
            return;
        }
        try {
            stopped = false;
        } catch (RuntimeException e) {
            stop();
            throw e;
        }
        process = Runtime.getRuntime().exec(cmd);
        outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                process.getOutputStream().write(b);
            }
        };
        inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return process.getInputStream().read();
            }
        };
        errorStream = new InputStream() {
            @Override
            public int read() throws IOException {
                return process.getErrorStream().read();
            }
        };
    }

    public synchronized void stop() {
        if (stopped || process == null) {
            return;
        }
        stopped = true;
        requestStop();
        waitForStop();
        closeStreams();
    }

    /**
     * Keeps process alive by resurrecting it in case of premature death.
     * @throws IOException
     */
    public synchronized void monitor() throws IOException {
        if (stopped) {
            return;
        }
        if (!isAlive()) {
            start();
        }
    }

    private void requestStop() {
        process.destroy();
    }

    private void waitForStop() {
        long timeout = System.currentTimeMillis() + (10 * 1000);
        try {
            while (System.currentTimeMillis() < timeout && isAlive()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeStreams() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            errorStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InputStream getErrorStream() {
        return errorStream;
    }

    public boolean isKeptAlive() {
        return !stopped;
    }

    private boolean isAlive() {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
