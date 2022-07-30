<div align="center">
  <h1>BLE and MQTT based IoT System for Smart City Scenarios</h1>
  
  <p>
    End-to-End Open Source Proof-of-Concept IoT architecture for Smart Cities Scenarios
  </p>

  
<!-- Badges -->
<p>
  <a href="https://github.com/CDOrtona/RUN.IO/graphs/contributors">
    <img src="https://img.shields.io/github/contributors/Louis3797/awesome-readme-template" />
  </a>
  <a href="https://github.com/CDOrtona/RUN.IO/network/members">
    <img src="https://img.shields.io/github/forks/CDOrtona/RUN.IO" alt="forks" />
  </a>
  <a href="https://github.com/CDOrtona/RUN.IO/stargazers">
    <img src="https://img.shields.io/github/stars/CDOrtona/RUN.IO" alt="stars" />
  </a>
  <a href="https://github.com/CDOrtona/RUN.IO/blob/master/LICENSE">
    <img src="https://img.shields.io/github/license/CDOrtona/RUN.IO" alt="license" />
  </a>
</p>
   
<h4>
    <a href="https://www.mdpi.com/1999-5903/14/2/57">Published Paper</a>
</div>

<br />

<!-- Table of Contents -->
# Table of Contents

- [About the Project](#about-the-project)
  * [Sensing Layer](#sensing-layer)
  * [Android Gateway](#android-gateway)
  * [MQTT Broker](#mqtt-broker)
  * [Dashboard](#dashboard)
- [Paper](#paper)
- [License](#license)


# About The Project

<div align="center"> 
  <img src="https://github.com/CDOrtona/RUN.IO/blob/master/media/E2EArch.png" width="200" />
</div>

The MQTT protocol is supposed to be used at the application layer for transmitting
data.
The designed architecture is composed of four main functional layers:

* The Sensing Layer is responsible of implementing the cyber-physical interface,
enabling the possibility of sensing physical data from the users. The sensed data
can be human related data, (e.g., hearth monitor, position) as well vehicle related
data (e.g., battery charge, vehicle position) or environmental data (e.g., humidity,
air temperature, pressure). Wearable sensors can be considered as deployed in
sportswear without no need to use expensive vehicles.


* The Gateway is responsible to collect data from the sensing layer through any
proprietary/custom protocol embedded in an IP packet to be delivered through
the Internet. The gateway layer is implemented through two different nodes, one
enabling the interface towards the sensing layer while the other, acting as user
personal device, for transmitting data toward the final destination. In particular,
the gateway acts as the MQTT publisher and is implemented on a mobile device,
assumed to be hold by the users. This choice allows the appoach to be available
for any kind of road users, even in case of simple and cheap vehicles, like bikes or
simple pedestrian. The mobile device can be replaced in principles by other devices
already installed on more complex vehicles, like cars.


* The MQTT Broker acts as intermediate point of the Publisher/Subcriber communication architecture. 
It is responsible of receiving any MQTT input from the
Gateways and notify the subscriber about updated sensed data.


* The Dashboard acts as MQTT subscriber enabling the possibility of showing all the
data collected by the Gateway through internal sensors as well at the Sensing layer.

## Sensing Layer

The sensing layer allows the collection of data, such as health information of the
user with the wearable device, and the surrounding environmental information, through
the collection of the sensors data that have been implemented. Sensors and actuators, in
fact, are the primary source of information in an IoT system and provide the data that
will then be processed by the device, e.g., a micro-controller, they are connected to.
According to this view, we have implemented a general purpose embedded system
for wearable devices, through the use of the ultra-low power SoC Esp32 produced by
Espressif System.
The ESP32 works as a GATT Server, hence it defines the BLE services and characteristics which
are used in order to exchange the information gathered through the sensor with the Android Gateway.
The GATT server hierarchy which has been defined is illustrated below.
The full code can be found [here](https://github.com/CDOrtona/RUN.IO/tree/master/ESP32%20GATT_SERVER).

<div align="center"> 
  <img src="https://github.com/CDOrtona/RUN.IO/blob/master/media/gattHierarchy.png" width="200" />
</div>

## Android Gateway

These information streams, gathered through the sensors, will then be sent to
a smartphone –in this case an Android device– through a BLE connection. The role
of the Android device is to provide an edge gateway which allows to offload the
computational tasks directly to the mobile device rather than transmitting it to the cloud.
This solution has been already considered as a viable option when Android devices are
considered as an edge processing node.
The program which have been developed enables the Android device to both act as a BLE Client, as well
as a MQTT publisher and subscriber.
The BLE connection is used to comunicate with the microcontroller which gathers the information, whereas 
the MQTT messaging protocol is used to comunicate with other Android devices and a Dashboard.

The Android app developed is depicted below.

<div align="center"> 
  <img src="https://github.com/CDOrtona/RUN.IO/blob/master/media/app_main_activity.png" width="200" />
</div>


## MQTT Broker

The MQTT connection is
implemented thanks to the broker which is running on a Raspberry Pi 3B. For what
concerns the Broker software implementation we use Mosquitto, an open-source
broker developed by Oracle, while the Android device and the web-service act as MQTT
clients. The developed Android app also integrates the MQTT connection through the
use of the Paho library, developed by Oracle, and publishes on predefined topics
the information acquired by the sensors. 

## Dashboard

The control panel acts as a MQTT subscriber by
subscribing to the topics the Android devices are publishing; this capability has been
implemented through the use of the Paho JavaScript library. However, due to the HTTP protocol not being full-duplex,
it is impossible to have a direct link for data exchange between the broker and
the web-service, hence, we resorted to the use of MQTT over web-sockets.
The full code can be found [here](https://github.com/CDOrtona/MQTT_WebSocket_Javascript_Client).

<div align="center"> 
  <img src="https://github.com/CDOrtona/RUN.IO/blob/master/media/dashboard_mqtt.png" width="200" />
</div>

## Paper

* D’Ortona, C.; Tarchi, D.;
Raffaelli, C. Open-Source
MQTT-based End-to-End IoT System
for Smart City Scenarios. Future
Internet 2021, 1, 0. https://doi.org

## License
Distributed under the GPL-3.0 License. See LICENSE.txt for more information.
