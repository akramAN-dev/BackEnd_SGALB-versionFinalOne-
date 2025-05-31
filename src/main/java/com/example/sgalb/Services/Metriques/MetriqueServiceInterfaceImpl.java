package com.example.sgalb.Services.Metriques;

import com.example.sgalb.Services.SshService.SshServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class MetriqueServiceInterfaceImpl implements MetriqueServiceInterface {
    private SshServiceInterface sshService;

    @Override
    public double getCpuUsage() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        CentralProcessor processor = hal.getProcessor();

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(1000); // petite pause pour calculer l'utilisation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
        return cpuLoad * 100; // pourcentage
    }
    @Override
    public double getRamUsage() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        long totalMemory = memory.getTotal();
        long usedMemory = totalMemory - memory.getAvailable();

        double ramUsage = (double) usedMemory / totalMemory;
        return ramUsage * 100; // en pourcentage
    }

    public Map<String, Long> getNetworkStats() {
        SystemInfo si = new SystemInfo();
        List<NetworkIF> networkIFs = si.getHardware().getNetworkIFs();

        long totalBytesSent = 0;
        long totalBytesRecv = 0;

        for (NetworkIF net : networkIFs) {
            net.updateAttributes(); // Important pour avoir les données à jour
            totalBytesSent += net.getBytesSent();
            totalBytesRecv += net.getBytesRecv();
        }

        Map<String, Long> stats = new HashMap<>();
        stats.put("bytesSent", totalBytesSent);
        stats.put("bytesRecv", totalBytesRecv);

        return stats;
    }
    @Override
    public double getStorageStatus() {
        File root = new File("/");
        long totalSpace = root.getTotalSpace(); // en octets
        long usableSpace = root.getUsableSpace(); // en octets
        long usedSpace = totalSpace - usableSpace;

        if (totalSpace == 0) return 0.0;
        return (double) usedSpace / totalSpace * 100;
    }
//    public String getRemoteCpuUsage() {
//        String host = "192.168.203.1"; // exemple : "192.168.56.101"
//        String user = "asus";
//        String password = "moadtaha";
//        String command = "top -bn1 | grep \"Cpu(s)\"";
//
//        return sshService.executeCommand(host, user, password, command);
//    }


}
