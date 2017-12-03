package com.example.cong_jh.whatupfi;

/*
 * [Wifi]
 * wifi의 정보만을 가진 객체클래스
 */

public class WiFi {
    String SSID;
    String BSSID;
    int level;

    public WiFi(String SSID, String BSSID, int level) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.level = level;
    }
}
