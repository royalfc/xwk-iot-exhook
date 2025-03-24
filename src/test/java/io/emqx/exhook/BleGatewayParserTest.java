package io.emqx.exhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

public class BleGatewayParserTest {

    private BeaconParser parser;

    @BeforeEach
    void setUp() {
        parser = new BeaconParser();
    }

    @Test
    void testParseBinaryData_ValidData() {
        // 准备测试数据
        byte[] beaconData = createSampleBeaconData();

        // 执行测试
        BeaconParser.IBeacon beacon = parser.parseIBeacon(beaconData);

        System.out.println(beacon.estimateDistance());

        // 验证结果
        assertNotNull(beacon);
        System.out.println(beacon);
        // assertEquals("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6", beacon.getUuid());
        // assertEquals(1, beacon.getMajor());
        // assertEquals(1, beacon.getMinor());
    }

    private byte[] createSampleBeaconData() {
        return new byte[] {
            (byte) 0x00, // 数据类型
            (byte) 0x45, (byte) 0xC6, (byte) 0x6A, (byte) 0xF1, (byte) 0x73, (byte) 0x59, // BLE设备MAC地址
            (byte) 0xB6, // 第8个字节，RSSI 实际值减去 256 BA：rssi=0xBA-256=-68
            // 广播数据部分
            (byte) 0x02, (byte) 0x01, (byte) 0x06, (byte) 0x1A, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x02, (byte) 0x15, // 9个byte特定头部数据
            (byte) 0xFD, (byte) 0xA5, (byte) 0x06, (byte) 0x93, (byte) 0xA4, (byte) 0xE2, (byte) 0x4F, (byte) 0xB1, (byte) 0xAF, (byte) 0xCF, (byte) 0xC6, (byte) 0xEB, (byte) 0x07, (byte) 0x64, (byte) 0x78, (byte) 0x25, // uuid
            (byte) 0x74, (byte) 0xE5, //Major
            (byte) 0x9B, (byte) 0xC9, //Minor
            (byte) 0xC9 //RSSI@1m
        };

        // 创建一个示例iBeacon数据
        // return new byte[] {
        //         // 前导码
        //         (byte) 0x02, (byte) 0x01, (byte) 0x06,
        //         // AD Length
        //         (byte) 0x1A,
        //         // AD Type (Manufacturer Specific Data)
        //         (byte) 0xFF,
        //         // Company ID (Apple)
        //         (byte) 0x4C, (byte) 0x00,
        //         // iBeacon Proximity
        //         (byte) 0x02, (byte) 0x15,
        //         // UUID
        //         (byte) 0x2F, (byte) 0x23, (byte) 0x44, (byte) 0x54, (byte) 0xCF, (byte) 0x6D, (byte) 0x4A, (byte) 0x0F,
        //         (byte) 0xAD, (byte) 0xF2, (byte) 0xF4, (byte) 0x91, (byte) 0x1B, (byte) 0xA9, (byte) 0xFF, (byte) 0xA6,
        //         // Major
        //         (byte) 0x00, (byte) 0x01,
        //         // Minor
        //         (byte) 0x00, (byte) 0x01,
        //         // TX Power
        //         (byte) 0xC5
        // };
        
        // iBeacon 原始数据（示例）
        // byte[] rawIBeacon = new byte[] {
        //         (byte) 0x02, (byte) 0x01, (byte) 0x06, (byte) 0x1A, (byte) 0xFF, (byte) 0x4C, (byte) 0x00, // 广播头
        //         (byte) 0x02, (byte) 0x15, // iBeacon 前缀
        //         (byte) 0xE2, (byte) 0xC5, (byte) 0x6D, (byte) 0xB5, (byte) 0xDF, (byte) 0xFB, (byte) 0x48, (byte) 0xD2, // UUID（小端序）
        //         (byte) 0xB0, (byte) 0x60, (byte) 0xD0, (byte) 0xF5, (byte) 0xA7, (byte) 0x10, (byte) 0x96, (byte) 0xE0,
        //         (byte) 0x00, (byte) 0x01, // Major（大端序）
        //         (byte) 0x00, (byte) 0x02, // Minor（大端序）
        //         (byte) 0xC5 // TX Power
        // };
        // return rawIBeacon;
    }

}