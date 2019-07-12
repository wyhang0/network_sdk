package com.wangyu.network.Thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.wangyu.network.NetMessage;
import com.wangyu.network.NetworkState;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.wangyu.network.NetMessage.PackageType.BwPackage_Message;

public abstract class SocketOutputStreamThread extends Thread {
    private Object socketStream;
    private OutputStream outputStream;
    private Handler socketOutputStreamThreadHandler;
    private Handler networkStateChangedHandler;
    private Looper looper=null;

    public SocketOutputStreamThread(Object socketStream, OutputStream outputStream, Handler networkStateChangedHandler){
        this.socketStream = socketStream;
        this.outputStream = outputStream;
        this.networkStateChangedHandler = networkStateChangedHandler;
    }
    public void end(){
        looper.quit();
    }
    public Handler getSocketOutputStreamThreadHandler() {
        return socketOutputStreamThreadHandler;
    }
    public abstract void socketOutputStreamThreadHandlerInitComplete();

    @Override
    public void run() {
        Looper.prepare();
        looper = Looper.myLooper();

        socketOutputStreamThreadHandler = new Handler(looper){
            @Override
            public void handleMessage(Message msg) {
                try {
                    NetMessage netMessage = (NetMessage) msg.obj;
                    senedBwMessage(netMessage);
                }catch (Exception e){
                    e.printStackTrace();
                    end();
                }
            }
        };

        socketOutputStreamThreadHandlerInitComplete();
        stateChange(true,socketStream);
        looper.loop();
        stateChange(false,socketStream);
    }
    protected void senedBwMessage(NetMessage netMessage) throws IOException {
        netMessage.length = netMessage.buffer==null ? 0 : netMessage.buffer.length;

        OutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        long streamSize = Long.BYTES+Integer.BYTES*6+ netMessage.length;
        dataOutputStream.writeLong(streamSize);// placeholder for info about bytes for the binary data
        dataOutputStream.writeInt(BwPackage_Message.getValue());
        netMessage.netMessage2Stream(dataOutputStream, netMessage);
        outputStream.flush();
        byte[] data = ((ByteArrayOutputStream) outputStream).toByteArray();
        Log.w("send data size", String.valueOf(streamSize)+":"+String.valueOf(data.length));
        outputStream.close();
        this.outputStream.write(data);
        this.outputStream.flush();
    }
    private void stateChange(Boolean isRunning, Object obj){
        if(networkStateChangedHandler != null) {
            Message message = new Message();
            message.what = NetworkState.NetworkStateType.SOCKET_OUTPUT_STREAM_THREAD_TYPE;
            message.arg1 = isRunning ? 1 : 0;
            message.obj = obj;
            networkStateChangedHandler.sendMessage(message);
        }
    }
}
