package com.wangyu.network;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.Socket;

public class NetworkStateChangeHandler extends Handler {
    INetStateChangeBusiness iNetStateChangeBusiness;
    public NetworkStateChangeHandler(INetStateChangeBusiness iNetStateChangeBusiness, Looper looper){
        super(looper);
        this.iNetStateChangeBusiness = iNetStateChangeBusiness;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case NetworkState.NetworkStateType.UDP_SOCKET_CONNECT_TYPE:
                updSocketConnectType(msg);
                break;
            case NetworkState.NetworkStateType.BLUETOOTH_SOCKET_CONNECT_TYPE:
                bluetoothSocketConnectType(msg);
                break;
            case NetworkState.NetworkStateType.BW_MESSAGE_BUSINESS_THREAD_TYPE:
                netMessageBusinessThreadType(msg);
                break;
            case NetworkState.NetworkStateType.SOCKET_INPUT_STREAM_THREAD_TYPE:
                socketInputStreamThreadType(msg);
                break;
            case NetworkState.NetworkStateType.SOCKET_OUTPUT_STREAM_THREAD_TYPE:
                socketOutputStreamThreadType(msg);
                break;
            case NetworkState.NetworkStateType.TCP_SOCKET_CONNECT_TYPE:
                tcpSocketConnectType(msg);
                break;
            case NetworkState.NetworkStateType.USB_DEVICE_TYPE:
                usbDeviceType(msg);
                break;
        }
    }
    private void usbDeviceType(Message msg) {
        Log.w("usb", (String) msg.obj);
        iNetStateChangeBusiness.onUsbNetworkState(msg.arg1==1);
    }
    private void tcpSocketConnectType(Message msg){
        NetworkState.TcpSocketConnectState state = NetworkState.TcpSocketConnectState.valueOf(msg.arg1);
        if(state == null){
            Log.w("TCPSocketConnect", "未知的状态"+(String) msg.obj);
        }else {
            switch (state) {
                case SOCKET_ARRIVAL:
                    iNetStateChangeBusiness.onTcpSocketArrival((Socket) msg.obj);
                    break;
                case ACCEPT_SOCKET_STOP:
                    Log.w("TCPSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onTcpStartListen();
                    break;
                case ACCEPT_SOCKET_START:
                    Log.w("TCPSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onTcpStopListen();
                    break;
                case CONNECT_SOCKET_ERROR:
                    Log.w("TCPSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onTcpConnectError();
                    break;
                case CONNECT_SOCKET_START:
                    Log.w("TCPSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onTcpStartConnect();
                    break;
                case ACCEPT_SOCKET_INTI_ERROR:
                    Log.w("TCPSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onTcpListenInitError();
                    break;
                case ACCEPT_SOCKET_ALREADY_EXIST:
                    Log.w("TCPSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onTcpListenAlreadyRunByOther();
                    break;
                case CONNECT_SOCKET_ALREADY_EXIST:
                    Log.w("TCPSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onTcpConnectAlreadyRunByOther();
                    break;
            }
        }
    }
    private void socketOutputStreamThreadType(Message msg) {
        if (msg.arg1==1){
            Log.w("socket output stream", "output stream thread start");
        }else{
            Log.w("socket output stream", "output stream thread stop");
        }
    }
    private void socketInputStreamThreadType(Message msg) {
        if (msg.arg1==1){
            Log.w("socket input stream", "input stream thread start");
        }else{
            Log.w("socket input stream", "input stream thread stop");
        }
        this.iNetStateChangeBusiness.onCommunicateThreadStateChange(msg.arg1==1, msg.obj);
    }
    private void bluetoothSocketConnectType(Message msg) {
        NetworkState.BluetoothSocketConnectState state = NetworkState.BluetoothSocketConnectState.valueOf(msg.arg1);
        if(state == null){
            Log.w("BluetoothSocketConnect", "未知的状态"+(String) msg.obj);
        }else {
            switch (state) {
                case SOCKET_ARRIVAL:
                    iNetStateChangeBusiness.onBluetoothSocketArrival((BluetoothSocket) msg.obj);
                    break;
                case CONNECT_SOCKET_ALREADY_EXIST:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothConnectAlreadyRunByOther();
                    break;
                case ACCEPT_SOCKET_ALREADY_EXIST:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothListenAlreadyRunByOther();
                    break;
                case ACCEPT_SOCKET_INTI_ERROR:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothListenInitError();
                    break;
                case CONNECT_SOCKET_START:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothStartConnect();
                    break;
                case CONNECT_SOCKET_ERROR:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothConnectError();
                    break;
                case ACCEPT_SOCKET_START:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothStartListen();
                    break;
                case ACCEPT_SOCKET_STOP:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothStopListen();
                    break;
                case BLUETOOTH_DEVICE_IS_NULL:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothConnectDeviceIsNull();
                    break;
                case BLUETOOTH_ADAPTER_INIT_ERROR:
                    Log.w("BluetoothSocketConnect", (String) msg.obj);
                    iNetStateChangeBusiness.onBluetoothAdapterInitError();
                    break;
            }
        }
    }
    private void netMessageBusinessThreadType(Message msg){
        Log.w("message business thread", (String) msg.obj);
    }
    private void updSocketConnectType(Message msg){
        NetworkState.UdbSocketConnectState udbSocketConnectState = NetworkState.UdbSocketConnectState.valueOf(msg.arg1);
        if(udbSocketConnectState == null){
            Log.w("udpSocketConnectState", "未知的udp状态"+msg.obj);
        }else{
            switch (udbSocketConnectState){
                case RECEIVER_STOP:
                    Log.w("udpSocketConnectState", (String) msg.obj);
                    iNetStateChangeBusiness.onUdpStopReceiverBroadcast();
                    break;
                case BROADCAST_STOP:
                    Log.w("udpSocketConnectState", (String) msg.obj);
                    iNetStateChangeBusiness.onUdpStopBroadcast();
                    break;
                case RECEIVER_START:
                    Log.w("udpSocketConnectState", (String) msg.obj);
                    iNetStateChangeBusiness.onUdpStartReceiverBroadcast();
                    break;
                case BROADCAST_START:
                    Log.w("udpSocketConnectState", (String) msg.obj);
                    iNetStateChangeBusiness.onUdpStartBroadcast();
                    break;
                case SOCKET_INIT_ERROR:
                    Log.w("udpSocketConnectState", (String) msg.obj);
                    iNetStateChangeBusiness.onUdpInitError();
                    break;
                case DATAGRAM_PACKET_ARRIVAL:
                    iNetStateChangeBusiness.onUdpDatagramPacketArrival((DatagramPacket) msg.obj);
                    break;
            }
        }
    }
}
