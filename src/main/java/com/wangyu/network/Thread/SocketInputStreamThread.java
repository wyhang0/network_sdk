package com.wangyu.network.Thread;

import android.os.Handler;
import android.os.Message;

import com.wangyu.network.NetMessage;
import com.wangyu.network.NetworkState;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SocketInputStreamThread extends Thread {
    private Object socketStream;
    private InputStream inputStream;
    private Handler netMessageBusinessThreadHandler;
    private Handler socketOutStreamThreadHandler;
    private Handler networkStateChangedHandler;
    public SocketInputStreamThread(Object socketStream,
                                   InputStream inputStream,
                                   Handler netMessageBusinessThreadHandler,
                                   Handler socketOutStreamThreadHandler,
                                   Handler networkStateChangedHandler){
        this.socketStream = socketStream;
        this.inputStream = inputStream;
        this.netMessageBusinessThreadHandler = netMessageBusinessThreadHandler;
        this.socketOutStreamThreadHandler = socketOutStreamThreadHandler;
        this.networkStateChangedHandler = networkStateChangedHandler;
    }
    @Override
    public void run() {
        try{
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            long dataSize;
            int packType;

            stateChange(true, socketStream);

            while (true){
                dataSize = dataInputStream.readLong();
                packType = dataInputStream.readInt();
                switch (NetMessage.PackageType.valueOf(packType)){
                    case BwPackage_File:
                        break;
                    case BwPackage_Message:
                        NetMessage netMessage = NetMessage.stream2BwMessage(dataInputStream);
                        netMessage.socketOutputStreamThreadHandler = socketOutStreamThreadHandler;
                        Message message = new Message();
                        message.obj = netMessage;
                        netMessageBusinessThreadHandler.sendMessage(message);
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            try {
                inputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            socketOutStreamThreadHandler.getLooper().quit();
        }
        stateChange(false,socketStream);
    }

    private void stateChange(Boolean isRunning, Object obj){
        if(networkStateChangedHandler != null) {
            Message message = new Message();
            message.what = NetworkState.NetworkStateType.SOCKET_INPUT_STREAM_THREAD_TYPE;
            message.arg1 = isRunning ? 1 : 0;
            message.obj = obj;
            networkStateChangedHandler.sendMessage(message);
        }
    }
}
