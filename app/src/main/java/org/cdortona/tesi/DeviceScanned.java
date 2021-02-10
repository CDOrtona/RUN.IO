package org.cdortona.tesi;

public class DeviceScanned {

    private String deviceName;
    private String alias;
    private String address;
    private int bondState;
    private int rssi;

    String getDeviceName(){
        return this.deviceName;
    }

    String getAlias(){
        return this.alias;
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
            this.deviceName = "no name";
        }
        else{
            this.deviceName = deviceName;
        }
    }

    void setAlias(String alias){
        this.alias = alias;
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
                + "Alias: " + this.getAlias() + '\n'
                + "Bound State: " + this.getBondState() + '\n'
                + "Rssi: " + this.getRssi();
    }


}
