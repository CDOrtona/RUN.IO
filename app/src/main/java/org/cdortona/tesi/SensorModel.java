package org.cdortona.tesi;

/**
 * Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class SensorModel {

    byte[] temp;
    byte[] humidity;
    byte[] pressure;
    byte[] altitude;
    byte[] gps;
    byte[]heart;
    byte[] sos;

    public void setTemp(byte[] temp) {
        this.temp = temp;
    }

    public void setHumidity(byte[] humidity) {
        this.humidity = humidity;
    }

    public void setPressure(byte[] pressure) {
        this.pressure = pressure;
    }

    public void setAltitude(byte[] altitude) {
        this.altitude = altitude;
    }

    public void setGps(byte[] gps) {
        this.gps = gps;
    }

    public void setHeart(byte[] heart) {
        this.heart = heart;
    }

    public void setSos(byte[] sos) {
        this.sos = sos;
    }
}
