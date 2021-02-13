package org.cdortona.tesi;

public class DevicesScannedModel {

    private String deviceName;
    private String address;
    private int bondState;
    private int rssi;

    //constructor
    DevicesScannedModel(String deviceName, String address, int rssi, int bondState) {
        if(deviceName == null){
            this.deviceName = "Unknown";
        }
        else{
            this.deviceName = deviceName;
        }
        this.address = address;
        this.rssi = rssi;
        this.bondState = bondState;
    }

    String getDeviceName(){
        return this.deviceName;
    }

    String getBleAddress(){
        return this.address;
    }

    int getBondState(){
        return this.bondState;
    }

    int getRssi(){
        return this.rssi;
    }

    void setDeviceName(String deviceName){
        if(deviceName == null){
            this.deviceName = "Unknown";
        }
        else{
            this.deviceName = deviceName;
        }
    }

    void setAddress(String address){
        this.address = address;
    }

    void setBondState(int bondState){
        this.bondState = bondState;
    }

    void setRssi(int rssi){
        this.rssi = rssi;
    }

    public String toString(){
        return  '\n' + "Address: " + this.getBleAddress() + '\n'
                + "Device name: " + this.getDeviceName() + '\n'
                + "Bound State: " + this.getBondState() + '\n'
                + "Rssi: " + this.getRssi();
    }


}