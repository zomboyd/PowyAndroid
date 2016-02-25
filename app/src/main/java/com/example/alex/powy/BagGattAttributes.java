package com.example.alex.powy;

import java.util.HashMap;

/**
 * Created by alex on 23/02/16.
 */
public class BagGattAttributes {
    /**
     * Gatt attributes we want to recover with the connection
     */
    private static HashMap<String, String> attributes = new HashMap();
    public static String BATTERY_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Battery Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(BATTERY_MEASUREMENT, "battery Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
