package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final  int [] ARR_BUTTON= new int[]{R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9};
    private TextView text;
    private Button arrButton[][];
    private Game m_game = new Game();
    private  ServiceNetwork mServiceNetwork = null;
    private  ParserCMD parse = new ParserCMD();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView)findViewById(R.id.textView) ;
        text.setText(m_game.GetCurrNamePlayer());
        arrButton = new Button[3][3];
        for(int l = 0, i = 0, j = 0; l < ARR_BUTTON.length; l++){
            i = l / 3;
            j = l % 3;
            arrButton[i][j] = (Button)findViewById(ARR_BUTTON[l]);
            arrButton[i][j].setWidth(160);
            arrButton[i][j].setHeight(160);
            arrButton[i][j].setTextSize(18);
            arrButton[i][j].setOnClickListener( new MyClickListener(i,j) );
        }

        final Button btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 MainActivity.this.m_game.Reset();
                 for (int i = 0; i < 3; i++)
                     for(int j = 0; j < 3 ; j++)  arrButton[i][j].setText("");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
// Add the buttons
        builder.setPositiveButton("Server", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                mServiceNetwork = ServiceNetwork.getInstanceServer(handler);
                mServiceNetwork.start();
            }
        });
        builder.setNegativeButton("Client", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                mServiceNetwork = ServiceNetwork.getInstanceClient(handler, "192.168.1.2");
                mServiceNetwork.start();
            }
        });

// Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    //    if (mServiceNetwork!=null ) mServiceNetwork.start();
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    // Парсить команду
                    if( parse.extractIJ((byte[]) msg.obj, msg.arg1)) {
                        // выполнить нажатие кнопки
                        if( parse.i < 3 && parse.j < 3)
                            ClickButton(parse.i, parse.j, arrButton[parse.i][parse.j]);
                    }
                    break;
                case 2:
                    break;
            }

            return false;
        }
    });

    private void ClickButton(int i, int j, Button btn)
    {
        Game.State st = m_game.Step(i, j);
        text.setText(m_game.GetCurrNamePlayer());
        if (st == Game.State.E_END_GAME) {
            btn.setText(m_game.GetSymbol());
            Toast.makeText(MainActivity.this, m_game.msg_game(), Toast.LENGTH_SHORT).show();
            return;
        }else if( st == Game.State.E_ERROR )
            Toast.makeText(MainActivity.this, m_game.msg_game(), Toast.LENGTH_SHORT).show();
        else btn.setText(m_game.GetSymbol());
    }

    /*
        Внутренний класс обработчик для кнопки i,j
     */
    class MyClickListener implements View.OnClickListener
    {
        private int i, j;
        public MyClickListener(int i, int j){
            this.i = i;
            this.j = j;
        }

        @Override
        public void onClick(View view)
        {
            if(  mServiceNetwork.isBusy()) return;
            ClickButton(i, j, ((Button)view));
            parse.i = i;
            parse.j = j;
            try {
                mServiceNetwork.sendData(parse.getByte());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class ParserCMD {
        public int i, j;
        public ParserCMD() {
            i = j = 0;
        }
        public boolean extractIJ(byte [] buff, int len){
            for(int i = 0; i < len; i++){

            }
            return true;
        }

        byte[] getByte(){
            StringBuilder strB = new StringBuilder();
            strB.append(i);
            strB.append(" ");
            strB.append(j);
            return strB.toString().getBytes();
        }
    }


}