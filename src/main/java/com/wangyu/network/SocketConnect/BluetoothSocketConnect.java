package com.wangyu.network.SocketConnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.wangyu.network.NetworkState;

import java.io.IOException;
import java.util.UUID;

/**
 * 管理目标机器到本机的BluetoothSocket连接,本机到目标机器的bluetoothsocket连接
 */
public class BluetoothSocketConnect {
    private BluetoothServerSocket bluetoothServerSocket;//use to accept socket
    private BluetoothSocket bluetoothConnectSocket;//use to connect socket
    private BluetoothAdapter bluetoothAdapter;
    private Thread acceptThread;
    private Thread connetThread;
    private Handler networkStateChangedHandler;

    public BluetoothSocketConnect(BluetoothAdapter bluetoothAdapter, Handler networkStateChangedHandler){
        this.bluetoothAdapter = bluetoothAdapter;
        this.networkStateChangedHandler = networkStateChangedHandler;
    }

    public synchronized void startAcceptSocket(final String uuid){
        stopAcceptSocket();
        if(bluetoothAdapter == null) {
            stateChange(NetworkState.BluetoothSocketConnectState.BLUETOOTH_ADAPTER_INIT_ERROR.getValue(),"bluetooth adapter is null");
            return;
        }

        if(acceptThread!=null && acceptThread.isAlive()) {
            stateChange(NetworkState.BluetoothSocketConnectState.ACCEPT_SOCKET_ALREADY_EXIST.getValue(),"accept socket exist");
        }else{
            acceptThread = new Thread(){
                @Override
                public void run() {
                    try{
                        bluetoothServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BluetoothSocketConnect", UUID.fromString(uuid));
                        if(bluetoothServerSocket == null) {
                            stateChange(NetworkState.BluetoothSocketConnectState.ACCEPT_SOCKET_INTI_ERROR.getValue(),"listenUsingInsecureRfcommWithServiceRecord "+uuid+" error");
                            return;
                        }

                        BluetoothSocket socket;
                        while (true){
                            stateChange(NetworkState.BluetoothSocketConnectState.ACCEPT_SOCKET_START.getValue(), "start accept socket");
                            socket = bluetoothServerSocket.accept();
                            if(socket != null){
                                bluetoothServerSocket.close();
                                stateChange(NetworkState.BluetoothSocketConnectState.SOCKET_ARRIVAL.getValue(), socket);
                                break;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    stopAcceptSocket();
                    stateChange(NetworkState.BluetoothSocketConnectState.ACCEPT_SOCKET_STOP.getValue(), "stop accept socket");
                }
            };
            acceptThread.start();
        }
    }
    public synchronized void stopAcceptSocket(){
        if(null == bluetoothServerSocket)
            return;

        try {
            bluetoothServerSocket.close();
            bluetoothServerSocket = null;
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public synchronized void startConnectSocket(final BluetoothDevice device, final String uuid){
        if(bluetoothAdapter == null) {
            stateChange(NetworkState.BluetoothSocketConnectState.BLUETOOTH_ADAPTER_INIT_ERROR.getValue(),"bluetooth adapter is null");
            return;
        }
        if(device == null) {
            stateChange(NetworkState.BluetoothSocketConnectState.BLUETOOTH_DEVICE_IS_NULL.getValue(),"bluetooth device is null");
            return;
        }

        if(connetThread!=null && connetThread.isAlive()){
            stateChange(NetworkState.BluetoothSocketConnectState.CONNECT_SOCKET_ALREADY_EXIST.getValue(), "正在连接目标");
        }else{
            connetThread = new Thread() {
                @Override
                public void run() {
                    try {
                        stateChange(NetworkState.BluetoothSocketConnectState.CONNECT_SOCKET_START.getValue(), "start connect socket");
                        bluetoothConnectSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid));
                        bluetoothConnectSocket.connect();
                        stateChange(NetworkState.BluetoothSocketConnectState.SOCKET_ARRIVAL.getValue(), bluetoothConnectSocket);
                        bluetoothConnectSocket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        stateChange(NetworkState.BluetoothSocketConnectState.CONNECT_SOCKET_ERROR.getValue(), "stop accept socket");
                    }
                }
            };
            connetThread.start();
        }
    }
    public synchronized void stopConnectSocket(){
        if(null == bluetoothConnectSocket)
            return;

        try {
            bluetoothConnectSocket.close();
            bluetoothConnectSocket = null;
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void stateChange(int state, Object obj){
        if(networkStateChangedHandler != null) {
            Message message = new Message();
            message.what = NetworkState.NetworkStateType.BLUETOOTH_SOCKET_CONNECT_TYPE;
            message.arg1 = state;
            message.obj = obj;
            networkStateChangedHandler.sendMessage(message);
        }
    }
}
