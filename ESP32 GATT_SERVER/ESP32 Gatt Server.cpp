//
// Created by Cristian on 21/03/2021.
//
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

BLEServer* pServer = NULL;
BLECharacteristic* tempCharacteristic = NULL;
BLECharacteristic* heartCharacteristic = NULL;
BLECharacteristic* brightnessCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint32_t value = 0;

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_TEMP "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHARACTERISTIC_HEART "5ebad8b8-8128-11eb-8dcd-0242ac130003"
#define CHARACTERISTIC_BRIGHTNESS "3935a44c-81c3-11eb-8dcd-0242ac130003"


class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("Connected");
      /*int i=0;
      while(i<3){
        digitalWrite(1, LOW);
        delay(500);
        digitalWrite(0, HIGH);
        delay(500);
        i++;
      }*/
    };
    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("Disconnected");
    }
};



void setup() {
  Serial.begin(115200);

  // Create the BLE Device
  BLEDevice::init("ESP32");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE temp Characteristic
  tempCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_TEMP,
                      BLECharacteristic::PROPERTY_READ |
                      BLECharacteristic::PROPERTY_NOTIFY
                    );


  // Create a BLE Descriptor for the temp characteristic
  tempCharacteristic->addDescriptor(new BLE2902());

  //crete BLE heart Characteristic
  heartCharacteristic = pService->createCharacteristic(
                        CHARACTERISTIC_HEART,
                        BLECharacteristic::PROPERTY_READ |
                        BLECharacteristic::PROPERTY_NOTIFY
                        );

  //create a BLE descriptor for the heart characteristic
  //0x2902 is the standard for Client Characteristic configuration Descriptor (CCCD)
  heartCharacteristic->addDescriptor(new BLE2902());

  //create a BLE brightness characteristic
  brightnessCharacteristic = pService->createCharacteristic(
                            CHARACTERISTIC_BRIGHTNESS,
                            BLECharacteristic::PROPERTY_READ |
                            BLECharacteristic::PROPERTY_NOTIFY
                            );

  brightnessCharacteristic->addDescriptor(new BLE2902());

  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();
  Serial.println("Waiting a client connection to notify...");
}

void loop() {
    // notify changed value
    if (deviceConnected) {
        if(Serial.available()>0){
          //pCharacteristic->setValue((uint8_t*)&value, 4);

          std::string message = Serial.readString().c_str();
          tempCharacteristic->setValue(message);
          tempCharacteristic->notify();
          delay(1000);
          heartCharacteristic->setValue(message);
          heartCharacteristic->notify();
          delay(1000);
          brightnessCharacteristic->setValue(message);
          brightnessCharacteristic->notify();

          value++;
          delay(1000); // bluetooth stack will go into congestion, if too many packets are sent
        }
    }

    //this is used in order to make the ESP32 advertise again once it disconnects from the Central
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
        delay(500);
        pServer->startAdvertising(); // restart advertising
        Serial.println("start advertising");
        oldDeviceConnected = deviceConnected;
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
        oldDeviceConnected = deviceConnected;
    }
}
