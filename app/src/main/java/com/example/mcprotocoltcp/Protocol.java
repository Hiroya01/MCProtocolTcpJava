package com.example.mcprotocoltcp;

import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class Protocol {
    //列挙体 Read　or Write

   ConnectionParameter connectionParameter;

    //受信デバイスバッファ
    private int[] recieve_buffer;
    //送信デバイスバッファ
    private int[] send_buffer;
    //socket
    private Socket socket;
    //入力ストリーム
    private InputStream in;
    //出力ストリーム
    private OutputStream out;
    //テキストビュー
    private TextView textView;
    //スレッド
    private Thread thread;
    //実行中フラグ
    private volatile boolean running = true;
    //コンストラクタ
    public Protocol(ConnectionParameter cp) {
        connectionParameter=cp;

    }
    //接続 接続先アドレスとポート番号を指定
    public abstract boolean connect(String address, int port);
    //接続 接続先とポート番号はコンストラクタで指定
    public abstract boolean connect();//
    //切断
    public abstract void disconnect();
    //送信
    public abstract void send(byte[] data);
    //受信
    public abstract byte[] receive();
    //受信するByte数を取得
    public abstract int recieve_available(ReadOrWrite readOrWrite, int deviceType, int deviceNumber);
    //送信するByte数を取得
    public abstract int send_available(ReadOrWrite readOrWrite, int deviceType, int deviceNumber);
    //デバイス名をByteデータに変換取
    public abstract byte[] deviceNameToByte(int deviceType);
    //int配列の送信データをByteデータに変換 引数　読込書込み、デバイスタイプ、開始要素、受信データ数、Int配列
    public abstract byte[] intArrayToByte(ReadOrWrite readOrWrite, int deviceType, int start, int count, int[] data);
    //Byteデータをint配列に変換　引数　読込書込み、デバイスタイプ,開始要素、受信データ数、Byte配列
     public abstract int[] byteToIntArray(ReadOrWrite readOrWrite, int deviceType, int start, int count, byte[] data);

    //指定したデバイスタイプ、開始要素、データ数のint配列を取得

    public abstract int[] ReadData(ReadOrWrite readOrWrite, int deviceType, int start, int count);
    //指定したデバイスタイプ、開始要素、データ数のint配列を設定
    public abstract boolean WriteData(ReadOrWrite readOrWrite, int deviceType, int start, int count, int[] data);

    //テキストビューの設定

    public abstract void setTextView(TextView textView);
    //スレッドの実行
    public abstract void run();
    //スレッドの停止
    public abstract void stop();
    //読み込み
    public abstract int read();
    //書き込み
    public abstract int write();
    //MCプロトコルのリクエストを作成
    public abstract byte[] createMCProtocolRequest();
    //MCプロトコルのレスポンスを処理
    public abstract int processResponse(byte[] response, int bytesRead);
    //UIスレッドでの処理
    public abstract void runOnUiThread(Runnable action);






}
