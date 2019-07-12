package com.wangyu.network;

import android.os.Handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NetMessage {
    //packageType
    public enum PackageType
    {
        BwPackage_Message,
        BwPackage_File;

        public int getValue(){
            return this.ordinal();
        }
        public static PackageType valueOf(int value) throws Exception {
            if(value >= 0 && value < values().length)
                return values()[value];
            else
                throw new Exception("Enum PackageType error");
        }
    };
    /// <summary>
    /// 类别
    /// </summary>
    public int headFlag=0;
    /// <summary>
    /// 命令
    /// </summary>
    public int command = -1;
    /// <summary>
    /// 处理结果
    /// </summary>
    public int resultFlag=-1;
    public int index=1;
    public int length=0;
    /// <summary>
    /// 消息内容
    /// </summary>
    public byte[] buffer;
    public Handler socketOutputStreamThreadHandler;

    public static NetMessage stream2BwMessage(DataInputStream in) throws Exception {
        NetMessage netMessage = new NetMessage();

        netMessage.headFlag = in.readInt();
        netMessage.command = in.readInt();
        netMessage.resultFlag = in.readInt();
        netMessage.index = in.readInt();
        netMessage.length = in.readInt();
        netMessage.buffer = new byte[netMessage.length];
        in.readFully(netMessage.buffer);

        return netMessage;
    }
    public static void netMessage2Stream(DataOutputStream dataOutputStream, NetMessage netMessage) throws IOException {
        dataOutputStream.writeInt(netMessage.headFlag);
        dataOutputStream.writeInt(netMessage.command);
        dataOutputStream.writeInt(netMessage.resultFlag);
        dataOutputStream.writeInt(netMessage.index);
        dataOutputStream.writeInt(netMessage.length);
        if(netMessage.length > 0)
            dataOutputStream.write(netMessage.buffer);
    }
}
