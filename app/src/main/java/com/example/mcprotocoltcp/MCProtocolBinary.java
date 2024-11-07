package com.example.mcprotocoltcp;

import java.io.IOException;

//CustomSocketクラスを継承したMCProtocolクラス
public class MCProtocolBinary extends CustomSocket {
    public static final int HEADDER_SIZE = 11;
    //コンストラクタ
    public MCProtocolBinary(TcpOrUdp tcpOrUdp, ConnectionParameter connectionParameter) {
        super(tcpOrUdp, connectionParameter);
    }
    public enum DeviceType {
        X,
        Y,
        B,
        D,
        W,
        Z,
        R,
        ZR,
        DM
    }


//    //データの受信
//    public byte[] receive() {
//        byte[] receiveData = new byte[connectionParameter.max_receive_length];
//        int length ;
//        try {
//            length=tcpin.read(receiveData);
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        byte[] data = new byte[length];
//        for (int i = 0; i < length; i++) {
//            data[i] = receiveData[i];
//        }
//        return data;
//    }
    //正常受信時のデータ数を取得
    @Override

    public int recieve_available(ReadOrWrite readOrWrite, int deviceType, int deviceNumber) {
       if (readOrWrite == ReadOrWrite.Read) {
            return HEADDER_SIZE + deviceNumber*2;
        } else {
            return HEADDER_SIZE;
        }
    }
    //送信時のデータ数を取得
    @Override
    public int send_available(ReadOrWrite readOrWrite, int deviceType, int deviceNumber) {

        if (readOrWrite == ReadOrWrite.Read) {
            return HEADDER_SIZE+10;
        } else {
            return HEADDER_SIZE+10 + deviceNumber*2;
        }
    }

    @Override
    /// <summary>
    /// デバイス名をバイトデータに変換
    /// </summary>
    /// <param name="deviceType"></param>
    /// <returns></returns>
    public byte deviceNameToByte(int devicetype) {
        byte memType;
        DeviceType devType = DeviceType.values()[devicetype];
        switch (devType) {
            case X:
                memType = (byte)0x9C;
                break;
            case Y:
                memType = (byte)0x9D;
                break;

            case B:
                memType = (byte)0xA0;
                break;
            case D:
                memType = (byte)0xA8;
                break;
            case W:
                memType = (byte)0xB4;
                break;
            case Z:
                memType = (byte)0xCC;
                break;
            case R:
                memType = (byte)0xAF;
                break;
            case ZR:
                memType = (byte)0xB0;
                break;

            default: //エラー
                memType = (byte)0;
                break;

        }

        return memType;
    }


    @Override
    /// <summary>
    /// 送信バイトデータを作成
    /// </summary>
    /// <param name="readOrWrite">読込or書込み</param>
    /// <param name="deviceType"></param>
    /// <param name="start"></param>
    /// <param name="count"></param>
    /// <param name="data"></param>
    /// <returns></returns>
    public byte[] makeSendByteData(ReadOrWrite readOrWrite, int deviceType, int start, int count, int[] data) {
        try{
            int index=0;
            int length=0;
            byte[] send_data;
            byte memType = deviceNameToByte(deviceType);
            length=send_available(readOrWrite,deviceType,count);//送信バイト数
            int reqsize= length-9;
            send_data = new byte[length];
            //サブヘッダ部
            send_data[index++] = (byte)0x50;
            send_data[index++] = (byte)0x00;
            //Qヘッダ部
            send_data[index++] = (byte)0x00;//ネットワークNo
            send_data[index++] = (byte)0xFF;//要求先局番
            send_data[index++] = (byte)0xFF;//要求先ユニットI/O番号
            send_data[index++] = (byte)0x03;//要求先ユニット局番
            send_data[index++] = (byte)0x00;//要求先ユニット局番
            //データ部
            send_data[index++] = (byte)(reqsize & 0x00ff);//要求データ長下位
            send_data[index++] = (byte)((reqsize & 0xff00) >> 8);//要求データ長上位
            send_data[index++] = (byte)0x10;//CPU監視タイマ下位
            send_data[index++] = (byte)0x00;//CPU監視タイマ上位
            if (readOrWrite == ReadOrWrite.Read) {
                send_data[index++] = (byte)0x01;//コマンド
                send_data[index++] = (byte)0x04;
                send_data[index++] = (byte)0x00;//サブコマンド
                send_data[index++] = (byte)0x00;
                send_data[index++] = (byte)(start & 0xff);//デバイス番号
                send_data[index++] = (byte)((start & 0xff00)>>8);//デバイス番号
                send_data[index++] = (byte)((start & 0xff0000)>>16);//デバイス番号
                send_data[index++]=memType;
                send_data[index++] = (byte)(count & 0xff);//データ数
                send_data[index++] = (byte)((count & 0xff00)>>8);//データ数

            } else {
                send_data[index++] = (byte)0x01;//コマンド
                send_data[index++] = (byte)0x14;
                send_data[index++] = (byte)0x00;//サブコマンド
                send_data[index++] = (byte)0x00;
                send_data[index++] = (byte)(start & 0xff);//デバイス番号
                send_data[index++] = (byte)((start & 0xff00)>>8);//デバイス番号
                send_data[index++] = (byte)((start & 0xff0000)>>16);//デバイス番号
                send_data[index++]=memType;//デバイスコード
                send_data[index++] = (byte)(count & 0xff);//データ数
                send_data[index++] = (byte)((count & 0xff00)>>8);//データ数
                for (int i = 0; i < count; i++) {
                    send_data[index++] = (byte)(data[i] & 0xff);
                    send_data[index++] = (byte)((data[i] & 0xff00)>>8);
                }

            }
            return send_data;

        }catch(Exception e){
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public int[] getRecieveData(ReadOrWrite readOrWrite, int deviceType, int start, int count, byte[] data) {
        try{

            if (data.length<HEADDER_SIZE){
                throw new Exception("受信データが不正です");
            }
            int response_count = (int)data[7] | (int)data[8]<<8;//応答データ長
            int endcode=(int)data[9] | (int)data[10]<<8;//終了コード
            if(endcode!=0){
                throw new Exception("PLCからエラーが返されました。EndCode＝" +  Integer.toHexString(endcode));
            }
            if (readOrWrite == ReadOrWrite.Read) {
                int[] receive_data = new int[count];
                int index=HEADDER_SIZE;
                for (int i = 0; i < count; i++) {
                    receive_data[i] = (int)data[index++] | (int)data[index++]<<8;
                }
                return receive_data;
            } else {
                return new int[0];
            }
        }catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int[] ReadData( int deviceType, int start, int count) {
        ReadOrWrite readOrWrite = ReadOrWrite.Read;
        byte[] send_data = makeSendByteData(readOrWrite, deviceType, start, count, new int[0]);
        try {
            send(send_data);
            byte[] receive_data = receive();
            return getRecieveData(readOrWrite, deviceType, start, count, receive_data);//エラーは例外
        }catch(Exception e){
            e.printStackTrace();
            return new int[0];
        }
    }

    @Override
    public boolean WriteData( int deviceType, int start, int count, int[] data) {
        ReadOrWrite readOrWrite = ReadOrWrite.Write;
        byte[] send_data = makeSendByteData(readOrWrite, deviceType, start, count, data);
        try {
            send(send_data);
            byte[] receive_data = receive();
            getRecieveData(readOrWrite, deviceType, start, count, receive_data);//エラーは例外
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
