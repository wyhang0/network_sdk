package com.wangyu.network;

import android.os.Handler;

import com.wangyu.network.Thread.SocketInputStreamThread;
import com.wangyu.network.Thread.SocketOutputStreamThread;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SocketStream {
    protected Closeable socket;
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected SocketInputStreamThread socketInputStreamThread;
    protected SocketOutputStreamThread socketOutputStreamThread;
    protected Handler networkStateChangedHandler;

    public SocketStream(Closeable socket, InputStream inputStream, OutputStream outputStream, final Handler netMessageBusinessThreadHandler, Handler networkStateChangedHandler){
        this.networkStateChangedHandler = networkStateChangedHandler;

        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        //发送命令线程
        socketOutputStreamThread = new SocketOutputStreamThread(SocketStream.this, outputStream, networkStateChangedHandler) {
            @Override
            public void socketOutputStreamThreadHandlerInitComplete() {
                //接受命令线程
                socketInputStreamThread = new SocketInputStreamThread(SocketStream.this, SocketStream.this.inputStream,
                        netMessageBusinessThreadHandler,
                        this.getSocketOutputStreamThreadHandler(),
                        SocketStream.this.networkStateChangedHandler);
                socketInputStreamThread.start();
            }
        };
        socketOutputStreamThread.start();
    }

    public Closeable getSocket() {
        return socket;
    }

    synchronized public void close(){
        if(inputStream != null){
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
        if(outputStream != null){
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }
        if(socketInputStreamThread != null){
            socketInputStreamThread = null;
        }
        if(socketOutputStreamThread != null){
            socketOutputStreamThread.end();
            socketOutputStreamThread = null;
        }
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }
}
