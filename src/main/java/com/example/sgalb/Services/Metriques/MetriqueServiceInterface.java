package com.example.sgalb.Services.Metriques;


import java.util.Map;

public interface MetriqueServiceInterface {
    double getCpuUsage();
    double getRamUsage();
    Map<String, Long> getNetworkStats();
    double getStorageStatus();
//    String getRemoteCpuUsage();

}
