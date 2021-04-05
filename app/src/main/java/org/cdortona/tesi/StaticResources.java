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
    static final String ESP32_HEARTH_CHARACTERISTIC = "5ebad8b8-8128-11eb-8dcd-0242ac130003";
    static final String ESP32_BRIGHTNESS_CHARACTERISTIC = "3935a44c-81c3-11eb-8dcd-0242ac130003";
    //client characteristic configuration which is used to by the client in order to configure the
    //Indicate or Notify property of that specified characteristic
    static final String ESP32_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    //Broadcaster Intent Actions what will be received by the broadcast receiver
    static final String ACTION_CONNECTION_STATE = PACKAGE_NAME + ".connectionState";
    static final String ACTION_CHARACTERISTIC_CHANGED = PACKAGE_NAME + ".characteristicChanged";
    static final String ACTION_CHARACTERISTIC_CHANGED_READ = PACKAGE_NAME + "characteristicChangedRead";

    //Keys for the putExtra method
    static final String EXTRA_STATE_CONNECTION = PACKAGE_NAME + "keyConnection";
    static final String EXTRA_TEMP_VALUE = PACKAGE_NAME + "tempValue";
    static final String EXTRA_HEART_VALUE = PACKAGE_NAME + "heartValue";
    static final String EXTRA_BRIGHTNESS_VALUE = PACKAGE_NAME + "brightnessValue";
    static final String EXTRA_CHARACTERISTIC_CHANGED = PACKAGE_NAME + "characteristicToBeNotified";

    //values for the putExtra method
    static final String STATE_CONNECTED = "Connected";
    static final String STATE_DISCONNECTED = "Disconnected";

    //General static Resources
    static final String WEB_PAGE = PACKAGE_NAME + ".webUrl";
    static final String EXTRA_CHOOSEN_ADDRESS = PACKAGE_NAME + ".address";
    static final String EXTRA_CHOOSEN_NAME = PACKAGE_NAME + ".name";
}