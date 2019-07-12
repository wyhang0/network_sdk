package com.wangyu.network.Device;

import android.os.Handler;
import android.os.Message;

import com.wangyu.network.Helper.NetworkInterfaceHelper;
import com.wangyu.network.NetworkState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;

/**
 * 打开或关闭USB共享网络
 */
public class UsbDeviceManager {
    private Handler networkStateChangedHandler;
    private Thread usbDetectorThread;
    private long millis;
    private final int period=2000;

    public UsbDeviceManager(Handler networkStateChangedHandler){
        this.networkStateChangedHandler = networkStateChangedHandler;
        usbDetectorThread = new Thread(){
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                millis = period;
                while (System.currentTimeMillis()-currentTime<=millis){
                    if(usbConfigureResult()){
                        stateChange(true, "usb network success");
                        return;
                    }
                    try {
                        sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                stateChange(false, "usb network fail");
            }
        };
    }

    public synchronized void closeUSB(){
        try {
            millis = 0;
            usbDetectorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String str = runCmd("/system/bin/getprop sys.usb.config");
        if(str!=null && str.contains("rndis")) {
            if(str.contains("adb")) {
                runCmd("/system/bin/setprop sys.usb.config mtp,adb");
            }else{
                runCmd("/system/bin/setprop sys.usb.config mtp");
            }
        }
    }
    public synchronized void useUSB(){
        //先关闭再打开确保系统设置ip地址为“192.168.7.2”
        closeUSB();
        String str = runCmd("/system/bin/getprop sys.usb.config");
        if(str!=null) {
            if(str.contains("adb")) {
                runCmd("/system/bin/setprop sys.usb.config rndis,adb");
            }else{
                runCmd("/system/bin/setprop sys.usb.config rndis");
            }
            usbDetectorThread.start();
        }

    }
    private boolean usbConfigureResult(){
        List<InetAddress> addresses = NetworkInterfaceHelper.getBroadcastAddresses("rndis");
        if(addresses==null || addresses.size()==0){
            return false;
        }else {
            return true;
        }
    }
    /**
     *
     * @param cmd
     * mtp mtp,adb
     * rndis rndis,adb
     * ptp ptp,adb
     * diag diag,adb
     * midi midi adb
     * @return
     * mtp mtp,adb
     * rndis rndis,adb
     * ptp ptp,adb
     * diag diag,adb
     * midi midi adb
     */
    private String runCmd(String cmd){
        Runtime mRuntime = Runtime.getRuntime();
        String str="";
        try {
            //Process中封装了返回的结果和执行错误的结果
            Process mProcess = mRuntime.exec(cmd);
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            StringBuffer mRespBuff = new StringBuffer();
            char[] buff = new char[1024];
            int ch = 0;
            while ((ch = mReader.read(buff)) != -1) {
                mRespBuff.append(buff, 0, ch);
            }
            str = mRespBuff.toString();
            mReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
    private void stateChange(boolean isSuccess, Object obj){
        if(networkStateChangedHandler != null) {
            Message message = new Message();
            message.what = NetworkState.NetworkStateType.USB_DEVICE_TYPE;
            message.arg1 = isSuccess ? 1 : 0;
            message.obj = obj;
            networkStateChangedHandler.sendMessage(message);
        }
    }
}
