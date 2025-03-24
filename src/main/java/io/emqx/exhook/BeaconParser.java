package io.emqx.exhook;

import lombok.Data;

import java.util.Arrays;

public class BeaconParser {

    public static IBeacon parseIBeacon(byte[] rawData) {
        if (rawData.length < 25 || rawData[7] != 0x02 || rawData[8] != 0x15) {
            return null;
        }

        byte rssiByte = rawData[7];
        int rssi = (rssiByte & 0xFF) - 256;

        byte[] uuidBytes = Arrays.copyOfRange(rawData, 9, 25);
        String uuid = bytesToUuid(uuidBytes);

        int major = ((rawData[25] & 0xFF) << 8) | (rawData[26] & 0xFF);
        int minor = ((rawData[27] & 0xFF) << 8) | (rawData[28] & 0xFF);
        int txPower = rawData[29];

        return new IBeacon(uuid, major, minor, txPower, rssi);
    }

    public static EddystoneUID parseEddystoneUID(byte[] rawData) {
        if (rawData.length < 20 || rawData[0] != 0x00 || rawData[1] != 0xAA || rawData[2] != 0xFE) {
            return null;
        }

        int rssi = rawData[7]; // 假设 RSSI 在第8字节
        byte[] namespaceBytes = Arrays.copyOfRange(rawData, 3, 13);
        byte[] instanceBytes = Arrays.copyOfRange(rawData, 13, 19);
        String namespace = bytesToHex(namespaceBytes);
        String instance = bytesToHex(instanceBytes);
        int txPower = rawData[19];

        return new EddystoneUID(namespace, instance, txPower, rssi);
    }

    private static String bytesToUuid(byte[] bytes) {
        String hex = bytesToHex(bytes);
        return hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-" +
               hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    @Data
    public static class IBeacon {
        private final String uuid;
        private final int major;
        private final int minor;
        private final int txPower;
        private final int rssi;

        public double estimateDistance(double envFactor) {
            return Math.pow(10, (txPower - rssi) / (10 * envFactor));
        }

        @Override
        public String toString() {
            return String.format("IBeacon{uuid=%s, major=%d, minor=%d, txPower=%d, rssi=%d, distance=%.2f}",
                    uuid, major, minor, txPower, rssi, estimateDistance(2.0));
        }
    }

    @Data
    public static class EddystoneUID {
        private final String namespace;
        private final String instance;
        private final int txPower;
        private final int rssi;

        public double estimateDistance(double envFactor) {
            return Math.pow(10, (txPower - rssi) / (10 * envFactor));
        }

        @Override
        public String toString() {
            return String.format("EddystoneUID{namespace=%s, instance=%s, txPower=%d, rssi=%d, distance=%.2f}",
                    namespace, instance, txPower, rssi, estimateDistance(2.0));
        }
    }
}