package com.wangyu.network;

public class NetworkState {
    public class NetworkStateType {
        public static final int USB_DEVICE_TYPE = 1;
        public static final int UDP_SOCKET_CONNECT_TYPE = USB_DEVICE_TYPE + 1;
        public static final int BLUETOOTH_SOCKET_CONNECT_TYPE = UDP_SOCKET_CONNECT_TYPE + 1;
        public static final int TCP_SOCKET_CONNECT_TYPE = BLUETOOTH_SOCKET_CONNECT_TYPE + 1;
        public static final int SOCKET_INPUT_STREAM_THREAD_TYPE = TCP_SOCKET_CONNECT_TYPE + 1;
        public static final int SOCKET_OUTPUT_STREAM_THREAD_TYPE = SOCKET_INPUT_STREAM_THREAD_TYPE + 1;
        public static final int BW_MESSAGE_BUSINESS_THREAD_TYPE = SOCKET_OUTPUT_STREAM_THREAD_TYPE + 1;
    }

    public enum UdbSocketConnectState {
        SOCKET_INIT_ERROR,//创建DatagramSocket失败
        BROADCAST_START,//开始发送报文
        BROADCAST_STOP,//停止发送报文
        RECEIVER_START,//开始接受报文
        RECEIVER_STOP,//停止接受报文
        DATAGRAM_PACKET_ARRIVAL;//接受一条报文

        public int getValue() {
            return this.ordinal();
        }
        public static UdbSocketConnectState valueOf(int value) {
            if (value >= 0 && value < values().length)
                return values()[value];
            else
                return null;
        }
    }
    public enum BluetoothSocketConnectState{
        BLUETOOTH_ADAPTER_INIT_ERROR,//蓝牙硬件不可用
        ACCEPT_SOCKET_INTI_ERROR,//创建接受目标蓝牙连接的server socket失败
        ACCEPT_SOCKET_ALREADY_EXIST,//已经存在一个蓝牙连接的监听
        ACCEPT_SOCKET_START,//本地开始监听目标的蓝牙连接
        ACCEPT_SOCKET_STOP,//本机结束监听目标的蓝牙连接
        SOCKET_ARRIVAL,//本机与目标之间的蓝牙连接
        BLUETOOTH_DEVICE_IS_NULL,//蓝牙设备为空
        CONNECT_SOCKET_START,//开始创建到目标的蓝牙连接过程
        CONNECT_SOCKET_ERROR,//到目标的蓝牙连接过程失败
        CONNECT_SOCKET_ALREADY_EXIST;//已经存在一个到目标的蓝牙连接

        public int getValue() {
            return this.ordinal();
        }
        public static BluetoothSocketConnectState valueOf(int value) {
            if (value >= 0 && value < values().length)
                return values()[value];
            else
                return null;
        }
    }
    public enum TcpSocketConnectState{
        ACCEPT_SOCKET_INTI_ERROR,//创建接受目标蓝牙连接的server socket失败
        ACCEPT_SOCKET_ALREADY_EXIST,//已经存在一个tcp连接的监听
        ACCEPT_SOCKET_START,//本地开始监听目标的tcp连接
        ACCEPT_SOCKET_STOP,//本机结束监听目标的tcp连接
        SOCKET_ARRIVAL,//本机与目标之间的tcp连接
        CONNECT_SOCKET_START,//开始创建到目标的tcp连接过程
        CONNECT_SOCKET_ERROR,//到目标的tcp连接过程失败
        CONNECT_SOCKET_ALREADY_EXIST,//已经存在一个到目标的tcp连接
        ;

        public int getValue() {
            return this.ordinal();
        }
        public static TcpSocketConnectState valueOf(int value) {
            if (value >= 0 && value < values().length)
                return values()[value];
            else
                return null;
        }
    }
    public enum socketInputStreamState{

    }
}
