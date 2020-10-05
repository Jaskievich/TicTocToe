package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final  int [] ARR_BUTTON= new int[]{R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9};
    private Button arrButton[][];
    private Game m_game = new Game();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrButton = new Button[3][3];
        for(int l = 0, i = 0, j = 0; l < ARR_BUTTON.length; l++){
            i = l / 3;
            j = l % 3;
            arrButton[i][j] = (Button)findViewById(ARR_BUTTON[l]);
            arrButton[i][j].setWidth(160);
            arrButton[i][j].setHeight(160);
            arrButton[i][j].setTextSize(14);
            arrButton[i][j].setOnClickListener( new MyClickListener(i,j) );
        }

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
        public void onClick(View view) {
            final Game game = MainActivity.this.m_game;
            Game.State st = game.Step(i, j);
            if (st == Game.State.E_END_GAME) {
                ((Button)view).setText(game.GetSymbol());
                Toast.makeText(MainActivity.this, game.msg_game(), Toast.LENGTH_SHORT).show();
                return;
            }else if( st == Game.State.E_ERROR )
                Toast.makeText(MainActivity.this, game.msg_game(), Toast.LENGTH_SHORT).show();
            else ((Button)view).setText(game.GetSymbol());
        }
    }


}