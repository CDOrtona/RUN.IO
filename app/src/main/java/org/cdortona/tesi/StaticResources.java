package org.cdortona.tesi;

/**
 * Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

class StaticResources {

    private static final String PACKAGE_NAME = "com.cdortona.tesi";

    //ESP32 info
    static final String ESP32_ADDRESS = "30:AE:A4:F5:88:6E";
    static final String ESP32_SERVICE = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    static final String ESP32_TEMP_CHARACTERISTIC = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    static final String ESP32_HEARTH_CHARACTERISTIC = "00002a37-0000-1000-8000-00805f9b34fb";
    //client characteristic configuration which is used to by the client in order to configure the
    //Indicate or Notify property of that specified characteristic
    static final String ESP32_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    //Broadcaster Intent Filters
    static final String BROADCAST_CONNECTION_STATE = PACKAGE_NAME + ".connectionState";
    static final String BROADCAST_ESP32_INFO =  PACKAGE_NAME + ".serviceFound";
    static final String BROADCAST_CHARACTERISTIC_CHANGED = PACKAGE_NAME + ".characteristicChanged";
    static final String BROADCAST_CHARACTERISTIC_READ = PACKAGE_NAME + ".characteristicRead";

    //Keys for the putExtra method
    static final String EXTRA_STATE_CONNECTION = "keyConnection";
    static final String EXTRA_TEMP_VALUE = "tempValue";
    static final String EXTRA_HEART_VALUE = "heartValue";
    static final String EXTRA_TERMINAL_SERVICE = "terminalService";
    static final String EXTRA_TERMINAL_CHARACTERISTIC_TEMP = "terminalCharacteristicTemp";
    static final String EXTRA_TERMINAL_CHARACTERISTIC_HEART = "terminalCharacteristicHeart";

    //values for the putExtra method
    static final String STATE_CONNECTED = "Connected";
    static final String STATE_DISCONNECTED = "Disconnected";


}