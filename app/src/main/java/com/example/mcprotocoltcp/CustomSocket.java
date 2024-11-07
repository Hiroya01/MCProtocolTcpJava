package com.example.mcprotocoltcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public abstract class CustomSocket {
    TcpOrUdp tcpOrUdp;
    ConnectionParameter connectionParameter;
    Socket tcpSocket;
    DatagramSocket udpSocket;
    InetAddress udpAddress;
    OutputStream tcpout;
    InputStream tcpin;

    public CustomSocket(TcpOrUdp tcpOrUdp, ConnectionParameter connectionParameter) {
        this.tcpOrUdp = tcpOrUdp;
        this.connectionParameter = connectionParameter;
    }

    public boolean connect()  {
        int port = ConnectionParameter.port;
        String remote_address = ConnectionParameter.remote_address;
        try {

            if (tcpOrUdp == TcpOrUdp.TCP) {

                tcpSocket = new Socket(remote_address, port);
                tcpout = tcpSocket.getOutputStream();
                tcpin = tcpSocket.getInputStream();

                return true;
            } else {
                udpSocket = new DatagramSocket();
                udpAddress = InetAddress.getByName(remote_address);
                return true;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        if (tcpOrUdp == TcpOrUdp.TCP) {
            try {
                tcpSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            udpSocket.close();
        }
    }

    public void send(byte[] data) throws IOException {
        if (tcpOrUdp == TcpOrUdp.TCP) {
          tcpout.write(data);
       } else {
            DatagramPacket packet = new DatagramPacket(data, data.length, udpAddress, ConnectionParameter.port);
            udpSocket.send(packet);

        }
    }

    public byte[] receive() throws IOException {
        int maxReceiveLength = ConnectionParameter.max_receive_length;
        byte[] buffer = new byte[maxReceiveLength];
        if (tcpOrUdp == TcpOrUdp.TCP) {
            int recieveCount = tcpin.read(buffer);
            return Arrays.copyOf(buffer, recieveCount);


//        int max_receive_length = ConnectionParameter.max_receive_length;
//        byte[] buffer = new byte[max_receive_length];
//        if (tcpOrUdp==TcpOrUdp.TCP) {
//
//            try {
//                 reciveCount=tcpin.read(buffer);
//                return buffer;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        } else {

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                udpSocket.receive(packet);
                return packet.getData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];//error
    }


    public abstract int recieve_available(ReadOrWrite readOrWrite, int deviceType, int deviceNumber);


    public abstract int send_available(ReadOrWrite readOrWrite, int deviceType, int deviceNumber);


    public abstract byte deviceNameToByte(int deviceType) ;

    //PLCに送信するデータを作成
    public abstract byte[] makeSendByteData(ReadOrWrite readOrWrite, int deviceType, int start, int count, int[] data);
    //PLCから受信したデータをINT配列に変換
    public abstract int[] getRecieveData(ReadOrWrite readOrWrite, int deviceType, int start, int count, byte[] data);



    public abstract  int[] ReadData(int deviceType, int start, int count);

    public abstract boolean WriteData( int deviceType, int start, int count, int[] data);
}
