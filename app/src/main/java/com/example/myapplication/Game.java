package com.example.myapplication;

public class Game
{
    public final int N = 3;
    private int [][] game_board = new int[N][N];
    private int step = 0;
    private String msg ;
    private Playr [] playrs;

    public enum State
    {
        E_END_GAME,
        E_ERROR,
        E_OK
    }

    class Playr{
        public String name;
        public String symbol;
    }

    public Game()
    {
        Reset();
        playrs = new Playr[2];
        playrs[0] = new Playr();
        playrs[0].name = "Игрок 2";
        playrs[0].symbol = "x";
        playrs[1] = new Playr();
        playrs[1].name = "Игрок 1";
        playrs[1].symbol = "0";
    }

    public String GetCurrNamePlayer(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(playrs[(step + 1)  % 2 ].name);
        stringBuilder.append(" - ");
        stringBuilder.append(playrs[(step + 1) % 2 ].symbol);
        return stringBuilder.toString();
    }

    public void Reset()
    {
        for(int i = 0; i < N; i++)
            for(int j = 0; j < N; j++) game_board[i][j] = -N;
        step = 0;
    }

    private int CheckPlay()
    {
        int s = 0;
        // Суммирование по строкам
        for( int i = 0; i < N; ++i ) {
            s = 0;
            for (int j = 0; j < N; j++) s += game_board[i][j];
            if( s == 0 || s == N ) return s;
        }
        // Суммирование постолбцам
        for( int i = 0; i < N; ++i ) {
            s = 0;
            for (int j = 0; j < N; j++) s += game_board[j][i];
            if( s == 0 || s == N ) return s;
        }
        // Суммирование по главной дагонали
        s = 0;
        for( int i = 0; i < N; ++i ) s += game_board[i][i];
        if( s == 0 || s == N ) return s;
        // Суммирование по побочной дагонали
        s = 0;
        for( int i = 0; i < N; ++i ) s += game_board[N - i - 1][i];
        if( s == 0 || s == N ) return s;
        return -1;
    }

    /*
        Шаг в игре. true - игра окончена
     */
    public State Step(int i, int j)
    {
        if(  game_board[i][j] > -1 ){
            msg = "Ошибка эта клетка занята";
            return State.E_ERROR;
        }
        step++;
        game_board[i][j] = step % 2;
        if( step > 4  ){
            if( CheckPlay() > -1 ){
                msg = playrs[step % 2].name + " выиграл";
                return State.E_END_GAME; // игра окончена
            } if( step == 9 ){
                msg = "Ничья";
                return State.E_END_GAME; // игра окончена
            }
        }
        return State.E_OK;
    }

    public String msg_game() {
        return msg.toString();
    }

    public String GetSymbol() {
        return  playrs[step % 2].symbol;
    }
}
