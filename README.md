# MqttTest
## 功能
---
了解一般Android 與 Mqtt的交互時所需要的過程。

## 安裝
---
1. git clone下載到指定路徑
```
git clone https://github.com/jo60913/MqttTest.git
```

2. android studio 中選擇Project from Version Control 輸入以下URL
```
https://github.com/jo60913/MqttTest.git
```
## 設置
---
Kotlin 版本 1.9.0
com.android.application 版本 8.6.0

## 操作
---
1. 進入後按下連線會連線至(tcp://broker.emqx.io:1883)網址(可使用MQTTX建立)
2. 輸入需要訂閱的主題後按下訂閱
3. 訂閱後就可以開始收到該主題發送出來的訊息
4. 亦可自己發送內容至該主題

## 打包
---
專案為了解android呼叫Mqtt間的互動，所以沒有打包。只是直接在Android Stduio上Build出app至手機上執行。

## 套件
* com.google.dagger:hilt-android:2.55 依賴注入
* com.jakewharton.timber 5.0.1 Log輸出
* org.eclipse.paho:org.eclipse.paho.client.mqttv3 1.2.5 mqtt溝通呼叫
* app/libs/serviceLibrary-release.aar Mqtt時MqttAndroidClient使用。

## 備註
目前org.eclipse.paho已經沒有對android12以後的版本做支援。所以需要呼叫MqttAndroidClient對象時需要使用serviceLibrary-release.aar檔案。
並import info.mqtt.android.service.MqttAndroidClient才可以成功呼叫。
