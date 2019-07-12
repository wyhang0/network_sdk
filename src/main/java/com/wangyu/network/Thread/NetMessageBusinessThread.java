package com.wangyu.network.Thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.wangyu.network.NetMessage;
import com.wangyu.network.INetMessageBusinessLogic;
import com.wangyu.network.NetworkState;

public abstract class NetMessageBusinessThread extends Thread {
    private Handler netMessageBusinessThreadHandler=null;
    private Handler networkStateChangedHandler;
    private Looper looper=null;
    private INetMessageBusinessLogic netMessageBusinessLogic;

    public NetMessageBusinessThread(INetMessageBusinessLogic netMessageBusinessLogic,
                                    Handler networkStateChangedHandler){
        this.netMessageBusinessLogic = netMessageBusinessLogic;
        this.networkStateChangedHandler = networkStateChangedHandler;
    }

    public Handler getNetMessageBusinessThreadHandler() {
        return netMessageBusinessThreadHandler;
    }
    public abstract void netMessageBusinessThreadHandlerInitComplete();

    public void end(){
        if(looper != null){
            looper.quitSafely();
        }
    }
    @Override
    public void run() {
        Looper.prepare();
        looper = Looper.myLooper();

        netMessageBusinessThreadHandler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    NetMessage netMessage = (NetMessage) msg.obj;
                    if (netMessage.socketOutputStreamThreadHandler != null &&
                            netMessage.socketOutputStreamThreadHandler.getLooper().getThread().isAlive()) {
                        netMessageBusinessLogic.exec(netMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        netMessageBusinessThreadHandlerInitComplete();
        stateChange(true,"NetMessage business thread " + getId() + " start");
        looper.loop();
        stateChange(false,"NetMessage business thread " + getId() + " stop");
    }

    private void stateChange(Boolean isRunning, String msg){
        if(networkStateChangedHandler != null) {
            Message message = new Message();
            message.what = NetworkState.NetworkStateType.BW_MESSAGE_BUSINESS_THREAD_TYPE;
            message.arg1 = isRunning ? 1 : 0;
            message.obj = msg;
            networkStateChangedHandler.sendMessage(message);
        }
    }
}
