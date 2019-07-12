package com.wangyu.network;

import android.bluetooth.BluetoothSocket;

import java.net.DatagramPacket;
import java.net.Socket;

public interface INetStateChangeBusiness {
    //usb网络配置
    void onUsbNetworkState(boolean success);
    //tcp建立连接过程
    //for listen
    void onTcpStartListen();
    void onTcpStopListen();
    void onTcpListenAlreadyRunByOther();
    void onTcpListenInitError();
    //for connect
    void onTcpStartConnect();
    void onTcpConnectAlreadyRunByOther();
    void onTcpConnectError();
    //for listen and connect
    void onTcpSocketArrival(final Socket socket);
    //蓝牙建立连接过程
    //for listen
    void onBluetoothStartListen();
    void onBluetoothStopListen();
    void onBluetoothListenInitError();
    void onBluetoothListenAlreadyRunByOther();
    //for connect
    void onBluetoothStartConnect();
    void onBluetoothConnectAlreadyRunByOther();
    void onBluetoothConnectError();
    void onBluetoothConnectDeviceIsNull();
    //for listen and connect
    void onBluetoothAdapterInitError();
    void onBluetoothSocketArrival(final BluetoothSocket socket);
    //udp message
    void onUdpInitError();
    void onUdpStartBroadcast();
    void onUdpStopBroadcast();
    void onUdpStartReceiverBroadcast();
    void onUdpStopReceiverBroadcast();
    void onUdpDatagramPacketArrival(DatagramPacket dp);
    //for connected()
    void onCommunicateThreadStateChange(boolean threadRunning, Object socketStream);
}
