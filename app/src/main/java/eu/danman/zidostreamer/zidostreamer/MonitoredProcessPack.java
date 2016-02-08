package eu.danman.zidostreamer.zidostreamer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonitoredProcessPack {
    private List<MonitoredProcess> processes;

    public MonitoredProcessPack() {
        processes = new ArrayList<MonitoredProcess>();
    }

    public void add(MonitoredProcess process) {
        processes.add(process);
    }

    public void monitor() throws IOException {
        for (MonitoredProcess process : processes) {
            process.monitor();
        }
    }

    public void stop() {
        for (MonitoredProcess process : processes) {
            process.stop();
        }
    }

    public void writeToOutputStream(byte[] buffer, int offset, int len) throws IOException {
        for (MonitoredProcess process : processes) {
            process.getOutputStream().write(buffer, offset, len);
        }
    }
}
