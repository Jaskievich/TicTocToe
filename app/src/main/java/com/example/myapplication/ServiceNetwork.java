package com.example.myapplication;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class ServiceNetwork  extends Thread {

    public static final int READ_CMD = 1;
    public static final int WRITE_CMD = 2;
    protected final static int SERVER_PORT = 6789; /* Сокет, который обрабатывает соединения на сервере */
    protected Handler handler;
    public static ServiceNetwork instance = null;
    protected boolean is_busy = false;
    protected Socket socket = null;
    protected byte [] data;


    protected ServiceNetwork( Handler handler){
        this.handler = handler;
    }

    public static ServiceNetwork getInstanceServer( Handler handler){
        if( instance == null ) {
            instance =  new ServerGame(handler) ;
        }
        return instance;
    }

    public static ServiceNetwork getInstanceClient( Handler handler , String server_name){
        if( instance == null ) {
            instance =  new ClientGame(handler, server_name) ;
        }
        return instance;
    }


    public boolean isBusy()
    {
        return is_busy;
    }

    protected synchronized void sendData(byte [] data){
       this.data = data;
       this.notify();
    }


    protected void _sendData(byte [] data) throws Exception
    {
        //  errText.setLength(0);
        if(socket == null && socket.isClosed()){
            throw new Exception("Невозможно отправить данные. Сокет не создан или закрыт");
        }
        try {
            socket.getOutputStream().write(data);
            socket.getOutputStream().flush();
        }catch (IOException e){
            throw new Exception("Невозможно отправить данные. Сокет не создан или закрыт");
        }
     }


    public void closeConnection()
    {
        if(socket != null && !socket.isClosed()){
            try{
                socket.close();
            }catch (Exception e){
                e.printStackTrace();
                //    Log.e(LOG_TAG, "Невозможно закрыть сокет: " + e.getMessage());
            }
            finally {
                socket = null;
            }
        }
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }


}

class ServerGame  extends ServiceNetwork {
    private ServerSocket serverSoket = null;

    public ServerGame(Handler handler) {

        super(handler);
        is_busy = true;
    }

    public synchronized void accept()
    {
        try {
            serverSoket = new ServerSocket(SERVER_PORT);
            while (true) {
                socket = serverSoket.accept();

                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];
                while (true) {
                    try { /* * получаем очередную порцию данных * в переменной count хранится реальное количество байт, которое получили */
                        int count = inputStream.read(buffer, 0, buffer.length); /* проверяем, какое количество байт к нам прийшло */
                        if (count > 0) {
                            Message readMsg = handler.obtainMessage(READ_CMD, count, -1, buffer);
                            readMsg.sendToTarget();
                            is_busy = false;
                            this.wait();
                            _sendData(data);
                            is_busy = true;
                        } else /* если мы получили -1, значит прервался наш поток с данными */ if (count == -1) {
                            socket.close();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                serverSoket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        accept();
    }
}

class ClientGame extends ServiceNetwork
{
    private String mNameServ;

    public ClientGame(Handler handler, String mNameServ) {
        super(handler);
        is_busy = false;
        this.mNameServ = mNameServ;
    }

    public synchronized void connection()
    {
        closeConnection();
        try { /* Создаем новый сокет. Указываем на каком компютере и порту запущен наш процесс, который будет принамать наше соединение. */
            socket = new Socket(mNameServ, SERVER_PORT);
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            while( true){
                this.wait();
                _sendData(data);
                is_busy = true;
                int count = inputStream.read(buffer, 0, buffer.length); /* проверяем, какое количество байт к нам прийшло */
                if (count > 0) {
                    Message readMsg = handler.obtainMessage(READ_CMD, count, -1, buffer);
                    readMsg.sendToTarget();
                }
                is_busy = false;
              }
        } catch (IOException e) {
            //     errText.append("Невозможно создать сокет: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() { /* Освобождаем ресурсы */
        connection();
    }


}