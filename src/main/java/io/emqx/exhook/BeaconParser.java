package io.emqx.exhook;

import lombok.Data;

import java.util.Arrays;

/**
 * Beacon 数据解析器
 */
public class BeaconParser {
 /**
     * 解析 iBeacon 数据
     * 
     * @param rawData 原始字节数组（需包含完整 iBeacon 广播包）
     * @return 解析后的结果，若格式不匹配返回 null
     */
    public static IBeacon parseIBeacon(byte[] rawData) {
        rawData.toString();
        // iBeacon 格式要求：数据长度 >= 25，且第16-17字节为 0x02 0x15
        if (rawData.length < 25 || rawData[15] != 0x02 || rawData[16] != 0x15) {
            return null;
        }

        // 提取 RSSI（网关设备的第8个字节是 RSSI，索引为7）
        byte rssiByte = rawData[7];
        int rssi = (rssiByte & 0xFF) - 256; // 转为有符号整数（-256即 0xFFFFFF00 的补码操作）

        // 提取 UUID
        byte[] uuidBytes = Arrays.copyOfRange(rawData, 17, 33);
        String uuid = bytesToUuid(uuidBytes);

        // 提取 Major 和 Minor（大端序）
        int major = ((rawData[33] & 0xFF) << 8) | (rawData[34] & 0xFF);
        int minor = ((rawData[35] & 0xFF) << 8) | (rawData[36] & 0xFF);

        // 提取 TX Power（校准 RSSI）
        int txPower = rawData[37];

        return new IBeacon(uuid, major, minor, txPower, rssi);
    }

    public static boolean isByteEqualTo(byte[] data, int index, int unsignedValue) {
        // 检查数组边界
        if (data.length <= index) {
            return false;
        }
        // 将字节值转换为无符号整数并进行比较
        return (data[index] & 0xFF) == unsignedValue;
    }

    /**
     * 解析 Eddystone-UID 数据
     * 
     * @param rawData 原始字节数组
     * @return 解析后的结果，若格式不匹配返回 null
     */
    public static EddystoneUID parseEddystoneUID(byte[] rawData, int rssi) {
        // Eddystone 前缀：0x00, 0xAA, 0xFE
        if (rawData.length < 20 || rawData[0] != 0x00 || rawData[1] != 0xAA || rawData[2] != 0xFE) {
            return null;
        }

        // 提取 Namespace 和 Instance（直接拼接十六进制）
        byte[] namespaceBytes = Arrays.copyOfRange(rawData, 3, 13);
        byte[] instanceBytes = Arrays.copyOfRange(rawData, 13, 19);
        String namespace = bytesToHex(namespaceBytes);
        String instance = bytesToHex(instanceBytes);

        // 提取 TX Power
        int txPower = rawData[19];

        return new EddystoneUID(namespace, instance, txPower, rssi);
    }

    // 解析 iBeacon 的 UUID（修正为大端序）
    private static String bytesToUuid(byte[] bytes) {
        String hex = bytesToHex(bytes);
        return hex.substring(0, 8) + "-" +
                hex.substring(8, 12) + "-" +
                hex.substring(12, 16) + "-" +
                hex.substring(16, 20) + "-" +
                hex.substring(20);
    }

    // 字节数组转十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    /**
     * 字节转十六进制字符串
     * @param b
     * @return
     */
    public static String byteToHex(byte b) {
        return String.format("%02X", b);
    }

    // iBeacon 数据模型
    @Data
    public static class IBeacon {
        private final String uuid;
        private final int major;
        private final int minor;
        private final int txPower;
        private final int rssi;

        public IBeacon(String uuid, int major, int minor, int txPower,int rssi) {
            this.uuid = uuid;
            this.major = major;
            this.minor = minor;
            this.txPower = txPower;
            this.rssi = rssi;
        }

        /**
         * 估算距离（单位：米）
         * 
         * @param environmentFactor 环境衰减因子（默认 2.0，空旷环境为 2，复杂环境可调至 3-4）
         * @return 估算距离
         */
        public double estimateDistance(double environmentFactor) {
            // 使用对数路径损耗模型: distance = 10^((TxPower - RSSI)/(10 * n))
            return Math.pow(10, (txPower - rssi) / (10 * environmentFactor));
        }

        public double estimateDistance() {
            return estimateDistance(2.0); // 默认环境因子为 2.0
        }
    }

    // Eddystone-UID 数据模型
    @Data
    public static class EddystoneUID {
        private final String namespace;
        private final String instance;
        private final int txPower;
        private final int rssi;

        public EddystoneUID(String namespace, String instance, int txPower, int rssi) {
            this.namespace = namespace;
            this.instance = instance;
            this.txPower = txPower;
            this.rssi = rssi;
        }

        /**
         * 估算距离（单位：米）
         * 
         * @param environmentFactor 环境衰减因子
         * @return 估算距离
         */
        public double estimateDistance(double environmentFactor) {
            return Math.pow(10, (txPower - rssi) / (10 * environmentFactor));
        }

        public double estimateDistance() {
            return estimateDistance(2.0);
        }
    }

}