package ru.pavlenty.surfacegame2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private Player player;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private ArrayList<Star> stars = new ArrayList<Star>();
    private ArrayList<Enemy> enemies = new ArrayList<Enemy>();

    int screenX;
    int countMisses;

    boolean flag ;


    private boolean isGameOver;


    int score;

    ArrayList<Integer> highScore = new ArrayList<>();
    //int highScore[] = new int[4];

    SharedPreferences sharedPreferences;

    static MediaPlayer gameOnsound;
    final MediaPlayer killedEnemysound;
    final MediaPlayer gameOversound;

    Context context;

    public GameView(Context context, int screenX, int screenY) {
        super(context);
        player = new Player(context, screenX, screenY);

        surfaceHolder = getHolder();
        paint = new Paint();

        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s = new Star(screenX, screenY);
            stars.add(s);
        }

        int enemyNums = 4;
        for (int i = 0; i < enemyNums; i++) {
            Enemy enemy = new Enemy(context, screenX, screenY);
            enemies.add(enemy);
        }

        this.screenX = screenX;
        countMisses = 0;
        isGameOver = false;


        score = 0;
        sharedPreferences = context.getSharedPreferences("SHAR_PREF_NAME", Context.MODE_PRIVATE);


        highScore.add(sharedPreferences.getInt("score1", 0));
        highScore.add(sharedPreferences.getInt("score2", 0));
        highScore.add(sharedPreferences.getInt("score3", 0));
        highScore.add(sharedPreferences.getInt("score4", 0));
        this.context = context;


        gameOnsound = MediaPlayer.create(context,R.raw.gameon);
        killedEnemysound = MediaPlayer.create(context,R.raw.killedenemy);
        gameOversound = MediaPlayer.create(context,R.raw.gameover);

        gameOnsound.seekTo(18000);
        gameOnsound.start();
    }

    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                player.stopBoosting();
                break;
            case MotionEvent.ACTION_DOWN:
                player.setBoosting();
                break;

        }

        if(isGameOver){
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                context.startActivity(new Intent(context,MainActivity.class));

            }
        }
        return true;
    }

    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);


            paint.setColor(Color.WHITE);
            paint.setTextSize(20);

            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }

            for (Enemy e : enemies) {
                canvas.drawBitmap(
                        e.getBitmap(),
                        e.getX(),
                        e.getY(),
                        paint);
            }

            paint.setTextSize(30);
            canvas.drawText("Очки: "+score,100,50,paint);

            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);


            if(isGameOver){
                setHighScore();
                gameOversound.start();
                paint.setTextSize(150);
                paint.setTextAlign(Paint.Align.CENTER);

                int yPos=(int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
                canvas.drawText("Конец игры",canvas.getWidth()/2,yPos,paint);

            }

            surfaceHolder.unlockCanvasAndPost(canvas);

        }
    }


    public static void stopMusic(){
        gameOnsound.stop();
    }

    private void update() {
        score++;

        player.update();

        for (Enemy e : enemies) {
            e.update(player.getSpeed());
        }
        for (Star s : stars) {
            s.update(player.getSpeed());
        }
    }

    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        gameOnsound.pause();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        gameOnsound.start();
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void setHighScore() {
        highScore.add(score);
        Collections.sort(highScore);
        Collections.reverse(highScore);
        highScore.remove(4);
        sharedPreferences.edit()
                .putInt("score1", highScore.get(0))
                .putInt("score2", highScore.get(1))
                .putInt("score3", highScore.get(2))
                .putInt("score4", highScore.get(3)).apply();
    }
}