package io.smppgateway.simulation;

import com.beust.jcommander.Parameter;

/**
 * Configuration for load testing via command-line arguments.
 */
public class LoadTestConfig {

    @Parameter(names = {"-h", "--host"}, description = "SMPP server host")
    private String host = "localhost";

    @Parameter(names = {"-p", "--port"}, description = "SMPP server port")
    private int port = 2775;

    @Parameter(names = {"-s", "--system-id"}, description = "SMPP system ID")
    private String systemId = "smppclient1";

    @Parameter(names = {"--password"}, description = "SMPP password")
    private String password = "password";

    @Parameter(names = {"-c", "--connections"}, description = "Number of concurrent connections")
    private int connections = 2;

    @Parameter(names = {"-r", "--rate"}, description = "Messages per second")
    private int rate = 100;

    @Parameter(names = {"-d", "--duration"}, description = "Test duration in seconds")
    private int duration = 60;

    @Parameter(names = {"--ramp-up"}, description = "Ramp-up time in seconds")
    private int rampUpSeconds = 0;

    @Parameter(names = {"--help"}, help = true, description = "Display help")
    private boolean help;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getRampUpSeconds() {
        return rampUpSeconds;
    }

    public void setRampUpSeconds(int rampUpSeconds) {
        this.rampUpSeconds = rampUpSeconds;
    }

    public boolean isHelp() {
        return help;
    }

    @Override
    public String toString() {
        return "LoadTestConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", systemId='" + systemId + '\'' +
                ", connections=" + connections +
                ", rate=" + rate +
                ", duration=" + duration +
                ", rampUpSeconds=" + rampUpSeconds +
                '}';
    }
}
