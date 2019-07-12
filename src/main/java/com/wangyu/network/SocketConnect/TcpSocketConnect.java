package com.wangyu.network.SocketConnect;

import android.os.Handler;
import android.os.Message;

import com.wangyu.network.NetworkState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpSocketConnect {
    private ServerSocket serverSocket;//use to accept socket
    private Socket socket;//use to connect socket
    private Thread acceptThread;
    private Thread connectThread;
    private Handler networkStateChangedHandler;

    public TcpSocketConnect(Handler networkStateChangedHandler){
        this.networkStateChangedHandler = networkStateChangedHandler;
    }

    public synchronized void startAcceptSocket(final int listenPort){
        if(acceptThread!=null && acceptThread.isAlive()){
            stateChange(NetworkState.TcpSocketConnectState.ACCEPT_SOCKET_ALREADY_EXIST.getValue(),"tcp accept exist");
        }else{
            acceptThread = new Thread(){
                @Override
                public void run() {
                    try {
                        serverSocket = new ServerSocket(listenPort);
                        stateChange(NetworkState.TcpSocketConnectState.ACCEPT_SOCKET_START.getValue(), "start tcp accept");
                        Socket socket;
                        while (true) {
                            socket = serverSocket.accept();
                            serverSocket.close();
                            stateChange(NetworkState.TcpSocketConnectState.SOCKET_ARRIVAL.getValue(),socket);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stopAcceptSocket();
                    stateChange(NetworkState.TcpSocketConnectState.ACCEPT_SOCKET_STOP.getValue(), "stop tcp accept");
                }
            };
            acceptThread.start();
        }
    }
    public synchronized void stopAcceptSocket(){
        if(serverSocket == null){
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = null;
    }
    public synchronized void startConnectSocket(final InetAddress host, final int port){
        if(connectThread!=null && connectThread.isAlive()){
            stateChange(NetworkState.TcpSocketConnectState.CONNECT_SOCKET_ALREADY_EXIST.getValue(), "正在建立tcp连接");
        }else{
            connectThread = new Thread(){
                @Override
                public void run() {
                    try {
                        stateChange(NetworkState.TcpSocketConnectState.CONNECT_SOCKET_START.getValue(), "start tcp connect");
                        socket = new Socket(host, port);
                        stateChange(NetworkState.TcpSocketConnectState.SOCKET_ARRIVAL.getValue(), socket);
                        socket = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stopConnectSocket();
                    stateChange(NetworkState.TcpSocketConnectState.CONNECT_SOCKET_ERROR.getValue(), "stop tcp connect");
                }
            };
            connectThread.start();
        }
    }
    public synchronized void stopConnectSocket(){
        if(socket == null)
            return;

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
    }
    private void stateChange(int state, Object obj){
        if(networkStateChangedHandler != null) {
            Message message = new Message();
            message.what = NetworkState.NetworkStateType.TCP_SOCKET_CONNECT_TYPE;
            message.arg1 = state;
            message.obj = obj;
            networkStateChangedHandler.sendMessage(message);
        }
    }
}
