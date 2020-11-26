package com.example.myapplication;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public abstract class ServiceNetwork  extends Thread {

    public static final int READ_CMD = 1;
    public static final int ERROR_MSG = 2;
    protected final static int SERVER_PORT = 6789; /* Сокет, который обрабатывает соединения на сервере */

    protected Handler handler;
    protected boolean is_busy = true;
    protected Socket socket = null;
    protected byte [] data_send;

    public static ServiceNetwork instance = null;

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
       this.data_send = data;
       this.notify();
    }


    protected void _sendData(byte [] data) throws Exception
    {
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
        close();
    }

    public void close(){
        closeConnection();
    }

    public void StopThread(){
        if( instance!= null ) {
            ServiceNetwork instance_curr = instance;
            instance = null;
            instance_curr.interrupt();
        }
    }

 }

class ServerGame  extends ServiceNetwork
{
    private ServerSocket serverSoket = null;

    public ServerGame(Handler handler)
    {
        super(handler);
    }

    @Override
    public void close()
    {
       super.close();
        if(serverSoket != null && !serverSoket.isClosed()) {
            try {
                serverSoket.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                serverSoket = null;
            }
        }
    }

    public synchronized void accept() throws Exception, InterruptedException, IOException
    {
        serverSoket = new ServerSocket(SERVER_PORT);
        while (true) {
            socket = serverSoket.accept();
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int count = inputStream.read(buffer, 0, buffer.length); /* проверяем, какое количество байт к нам прийшло */
                if (count > 0) {
                    Message readMsg = handler.obtainMessage(READ_CMD, count, -1, buffer);
                    readMsg.sendToTarget();
                    is_busy = false;
                    this.wait();
                    _sendData(data_send);
                    is_busy = true;
                } else /* если мы получили -1, значит прервался наш поток с данными */ if (count == -1) {
                    socket.close();
                    handler.sendEmptyMessage(ERROR_MSG);
                    break;
                }
            }
            serverSoket.close();
        }
    }

    public void run() {
        try {
            accept();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

class ClientGame extends ServiceNetwork
{
    private String mNameServ;

    public ClientGame(Handler handler, String mNameServ) {
        super(handler);
        this.mNameServ = mNameServ;
    }

    public synchronized void connection() throws UnknownHostException, IOException, Exception
    {
        /* Создаем новый сокет. Указываем на каком компютере и порту запущен наш процесс, который будет принамать наше соединение. */
        socket = new Socket();
        socket.connect(new InetSocketAddress(mNameServ, SERVER_PORT), 50000);
        is_busy = false;
        InputStream inputStream = socket.getInputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            this.wait();
            _sendData(data_send);
            is_busy = true;
            int count = inputStream.read(buffer, 0, buffer.length); /* проверяем, какое количество байт к нам прийшло */
            if (count > 0) {
                Message readMsg = handler.obtainMessage(READ_CMD, count, -1, buffer);
                readMsg.sendToTarget();
            }
            else{
                handler.sendEmptyMessage(ERROR_MSG);
            }
            is_busy = false;
        }
    }

    public void run() { /* Освобождаем ресурсы */
        closeConnection();
        try {
            connection();
        }catch(UnknownHostException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(ERROR_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}