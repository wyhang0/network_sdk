package com.wangyu.network.SocketConnect;

import android.os.Handler;
import android.os.Message;

import com.wangyu.network.Helper.NetworkInterfaceHelper;
import com.wangyu.network.NetworkState;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 管理upd报文在特定网卡的发送并发往目标机器指定的端口，接受特定端口的udp报文
 */
public class UdpSocketConnect {
    private DatagramSocket socket;
    private Timer timer;
    private String broadcastMsg;
    List<InetAddress> broadcastAddresses;
    private Handler networkStateChangedHandler;

    /**
     *
     * @param networkStateChangedHandler
     * 发送1.udp状态改变的message，2.upd报文的message;给创建handler的线程处理
     */
    public UdpSocketConnect(Handler networkStateChangedHandler){
        this.networkStateChangedHandler = networkStateChangedHandler;
    }

    public void setBroadcastMsg(String broadcastMsg) {
        this.broadcastMsg = broadcastMsg;
    }
    public synchronized void startBroadcast(final int receivePort, final int sendPort, long period, String networkInterfaceName){
        stopBroadcast(null);
        try {
            if(socket==null) {
                socket = new DatagramSocket(receivePort);
            }
        } catch (SocketException e) {
            e.printStackTrace();
            stateChange(NetworkState.UdbSocketConnectState.SOCKET_INIT_ERROR.getValue(), "create datagram socket in " + receivePort + " error");
            return;
        }

        broadcastAddresses = NetworkInterfaceHelper.getBroadcastAddresses(networkInterfaceName);

        stateChange(NetworkState.UdbSocketConnectState.BROADCAST_START.getValue(),
                "Schedule send broadcast start");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    if(broadcastAddresses.size()==0){
                        stopBroadcast("Broadcast address not found,stop schedule broadcast send");
                        return;
                    }

                    byte[] bytes = broadcastMsg.getBytes();
                    DatagramPacket outDP = new DatagramPacket(bytes, bytes.length);
                    outDP.setPort(sendPort);
                    for (InetAddress address:broadcastAddresses) {
                        outDP.setAddress(address);
                        socket.send(outDP);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    stopBroadcast("broadcast send error,stop schedule broadcast send");
                }
            }
        }, 0, period);


        stateChange(NetworkState.UdbSocketConnectState.RECEIVER_START.getValue(),
                "Schedule receive broadcast start");
        new Thread(){
            @Override
            public void run() {
                while (true){
                    try {
                        byte[] bytes = new byte[200];
                        DatagramPacket inDP = new DatagramPacket(bytes, bytes.length);
                        socket.receive(inDP);
                        stateChange(NetworkState.UdbSocketConnectState.DATAGRAM_PACKET_ARRIVAL.getValue(), inDP);
                    } catch (Exception e) {
                        e.printStackTrace();
                        stateChange(NetworkState.UdbSocketConnectState.RECEIVER_STOP.getValue(),
                                "Stop schedule broadcast receive");
                        return;
                    }
                }
            }
        }.start();
    }
    public synchronized void stopBroadcast(String msg){
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

        if(socket != null){
            socket.close();
            socket = null;
        }
        if(broadcastAddresses != null) {
            broadcastAddresses.clear();
            broadcastAddresses = null;
        }

        if(msg == null) {
            stateChange(NetworkState.UdbSocketConnectState.BROADCAST_STOP.getValue(),
                    "stop broadcast send");
        }else{
            stateChange(NetworkState.UdbSocketConnectState.BROADCAST_STOP.getValue(),
                    msg);
        }
    }

    private void stateChange(int state, Object obj){
        if(networkStateChangedHandler != null) {
            Message message = new Message();
            message.what = NetworkState.NetworkStateType.UDP_SOCKET_CONNECT_TYPE;
            message.arg1 = state;
            message.obj = obj;
            networkStateChangedHandler.sendMessage(message);
        }
    }
}
