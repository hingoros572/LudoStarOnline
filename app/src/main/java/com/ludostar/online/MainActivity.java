
package com.ludostar.online;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.os.Handler;
import java.util.Random;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setContentView(new LudoView());
    }

    class LudoView extends View {
        Paint p = new Paint(3);
        Random random = new Random();
        Handler handler = new Handler();

        int navy = Color.rgb(10, 18, 42);
        int gold = Color.rgb(245, 190, 65);
        int[] colors = {
            Color.rgb(235, 48, 55),
            Color.rgb(35, 180, 85),
            Color.rgb(45, 115, 235),
            Color.rgb(245, 205, 35)
        };

        boolean playing = false;
        int turn = 0;
        int dice = 0;
        boolean rolled = false;
        int[][] pawns = new int[4][4];

        float boardX, boardY, cell;
        float scaleX, scaleY;

        int[][] path = {
            {6,1},{6,2},{6,3},{6,4},{6,5},
            {5,6},{4,6},{3,6},{2,6},{1,6},{0,6},
            {0,7},{0,8},{1,8},{2,8},{3,8},{4,8},{5,8},
            {6,9},{6,10},{6,11},{6,12},{6,13},{6,14},
            {7,14},{8,14},{8,13},{8,12},{8,11},{8,10},{8,9},
            {9,8},{10,8},{11,8},{12,8},{13,8},{14,8},
            {14,7},{14,6},{13,6},{12,6},{11,6},{10,6},{9,6},
            {8,5},{8,4},{8,3},{8,2},{8,1},{8,0},
            {7,0},{6,0}
        };

        int[] starts = {0,13,26,39};

        public LudoView() {
            super(MainActivity.this);
            for (int a=0;a<4;a++)
                for (int b=0;b<4;b++)
                    pawns[a][b] = -1;
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void paint(int color) {
            p.setColor(color);
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(2);
        }

        void text(Canvas c, String s, float x, float y,
                  float size, int color) {
            paint(color);
            p.setTextSize(size);
            p.setTextAlign(Paint.Align.CENTER);
            p.setFakeBoldText(true);
            c.drawText(s,x,y,p);
            p.setFakeBoldText(false);
        }

        void rect(Canvas c,float l,float t,float r,float b,
                  int color,float radius) {
            paint(color);
            c.drawRoundRect(l,t,r,b,radius,radius,p);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(navy);

            float w=getWidth(), h=getHeight();

            if (!playing) {
                drawHome(canvas,w,h);
                return;
            }

            drawGame(canvas,w,h);
        }

        void drawHome(Canvas c,float w,float h) {
            text(c,"LUDO STAR",w/2,h*0.19f,42,gold);
            text(c,"PLAY  •  ENJOY  •  WIN",w/2,h*0.24f,15,Color.WHITE);

            float size=Math.min(w*0.78f,h*0.42f);
            float left=(w-size)/2;
            float top=h*0.32f;

            rect(c,left,top,left+size,top+size,
                 Color.rgb(25,35,65),22);

            float q=size/2;
            rect(c,left+8,top+8,left+q-5,top+q-5,colors[0],15);
            rect(c,left+q+5,top+8,left+size-8,top+q-5,colors[3],15);
            rect(c,left+8,top+q+5,left+q-5,top+size-8,colors[1],15);
            rect(c,left+q+5,top+q+5,left+size-8,top+size-8,colors[2],15);

            text(c,"●  ●",left+q/2,top+q/2+8,30,Color.WHITE);
            text(c,"●  ●",left+q+q/2,top+q/2+8,30,Color.WHITE);
            text(c,"●  ●",left+q/2,top+q+q/2+8,30,Color.WHITE);
            text(c,"●  ●",left+q+q/2,top+q+q/2+8,30,Color.WHITE);

            rect(c,w*0.18f,h*0.82f,w*0.82f,h*0.91f,gold,18);
            text(c,"START GAME",w/2,h*0.875f,23,navy);
            text(c,"1 PLAYER  VS  3 COMPUTERS",w/2,h*0.96f,13,Color.WHITE);
        }

        void drawGame(Canvas c,float w,float h) {
            text(c,"LUDO STAR ONLINE",w/2,32,22,gold);

            text(c,"YOU",w*0.18f,65,14,colors[0]);
            text(c,"COMPUTER 1",w*0.78f,65,13,colors[1]);
            text(c,"COMPUTER 2",w*0.18f,h-20,13,colors[2]);
            text(c,"COMPUTER 3",w*0.78f,h-20,13,colors[3]);

            float available=Math.min(w-24,h-190);
            cell=available/15f;
            boardX=(w-available)/2;
            boardY=(h-available)/2;

            drawBoard(c);

            // Dice button
            rect(c,w/2-37,72,w/2+37,132,gold,14);
            text(c,dice==0 ? "DICE" : ""+dice,
                 w/2,111,27,navy);

            text(c,"TURN: "+new String[]{"RED","GREEN","BLUE","YELLOW"}[turn],
                 w/2,h-48,15,Color.WHITE);
        }

        void drawBoard(Canvas c) {
            rect(c,boardX-3,boardY-3,
                 boardX+cell*15+3,boardY+cell*15+3,
                 Color.WHITE,5);

            for (int r=0;r<15;r++) {
                for (int col=0;col<15;col++) {
                    float x=boardX+col*cell;
                    float y=boardY+r*cell;
                    int color=Color.WHITE;

                    if (r<6 && col<6) color=colors[0];
                    if (r<6 && col>8) color=colors[3];
                    if (r>8 && col<6) color=colors[1];
                    if (r>8 && col>8) color=colors[2];

                    // Center cross lanes
                    if (col==7 && r>=0 && r<=5) color=colors[0];
                    if (r==7 && col>=9 && col<=14) color=colors[3];
                    if (col==7 && r>=9 && r<=14) color=colors[2];
                    if (r==7 && col>=0 && col<=5) color=colors[1];

                    paint(color);
                    p.setStyle(Paint.Style.FILL);
                    c.drawRect(x,y,x+cell,y+cell,p);

                    paint(Color.rgb(190,190,190));
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(0.7f);
                    c.drawRect(x,y,x+cell,y+cell,p);
                }
            }

            // White pawn yards
            drawYard(c,0,0,colors[0]);
            drawYard(c,9,0,colors[3]);
            drawYard(c,0,9,colors[1]);
            drawYard(c,9,9,colors[2]);

            // Center home
            float cx=boardX+7.5f*cell;
            float cy=boardY+7.5f*cell;
            paint(gold);
            c.drawCircle(cx,cy,cell*1.25f,p);
            text(c,"★",cx,cy+cell*.35f,cell*1.5f,navy);

            // Pawns
            for(int a=0;a<4;a++) {
                for(int b=0;b<4;b++) {
                    float[] pos=pawnPosition(a,b);
                    float x=boardX+(pos[0]+0.5f)*cell;
                    float y=boardY+(pos[1]+0.5f)*cell;
                    paint(colors[a]);
                    c.drawCircle(x,y,cell*.36f,p);
                    paint(Color.WHITE);
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(2);
                    c.drawCircle(x,y,cell*.36f,p);
                    paint(Color.WHITE);
                    c.drawCircle(x,y,cell*.10f,p);
                }
            }
        }

        void drawYard(Canvas c,int gx,int gy,int color) {
            float x=boardX+gx*cell;
            float y=boardY+gy*cell;
            rect(c,x+cell*.55f,y+cell*.55f,
                 x+cell*5.45f,y+cell*5.45f,
                 Color.WHITE,cell*.3f);

            for(int i=0;i<4;i++) {
                float px=x+cell*(i%2==0?2:4);
                float py=y+cell*(i<2?2:4);
                paint(color);
                c.drawCircle(px,py,cell*.48f,p);
                paint(Color.WHITE);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2);
                c.drawCircle(px,py,cell*.48f,p);
            }
        }

        float[] pawnPosition(int player,int pawn) {
            int value=pawns[player][pawn];

            if(value<0) {
                int[][] homes={
                    {{2,2},{4,2},{2,4},{4,4}},
                    {{10,2},{12,2},{10,4},{12,4}},
                    {{2,10},{4,10},{2,12},{4,12}},
                    {{10,10},{12,10},{10,12},{12,12}}
                };
                return new float[]{homes[player][pawn][0],homes[player][pawn][1]};
            }

            if(value>=52) {
                int lane=value-52;
                if(player==0) return new float[]{6,5-lane};
                if(player==1) return new float[]{9+lane,6};
                if(player==2) return new float[]{6,9+lane};
                return new float[]{5-lane,8};
            }

            int index=(starts[player]+value)%52;
            return new float[]{path[index][1],path[index][0]};
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;

            float x=e.getX(), y=e.getY();
            float w=getWidth(), h=getHeight();

            if(!playing) {
                if(y>h*.78f) {
                    playing=true;
                    turn=0;
                    dice=0;
                    invalidate();
                }
                return true;
            }

            if(turn!=0 || rolled) return true;

            if(y>65 && y<145 && x>w*.35f && x<w*.65f) {
                rollDice();
                return true;
            }

            if(rolled) {
                int selected=findPawn(x,y);
                if(selected>=0) movePawn(selected);
            }
            return true;
        }

        void rollDice() {
            dice=random.nextInt(6)+1;
            rolled=true;
            invalidate();

            boolean canMove=false;
            for(int i=0;i<4;i++) {
                if(pawns[0][i]<0 && dice==6) canMove=true;
                if(pawns[0][i]>=0 && pawns[0][i]<57) canMove=true;
            }

            if(!canMove) {
                rolled=false;
                if(dice!=6) nextTurn();
                invalidate();
            }
        }

        int findPawn(float x,float y) {
            for(int i=0;i<4;i++) {
                float[] pos=pawnPosition(0,i);
                float px=boardX+(pos[0]+.5f)*cell;
                float py=boardY+(pos[1]+.5f)*cell;
                if(Math.hypot(x-px,y-py)<cell*.75f) return i;
            }
            return -1;
        }

        void movePawn(int pawn) {
            if(pawns[0][pawn]<0) {
                if(dice!=6) return;
                pawns[0][pawn]=0;
            } else {
                pawns[0][pawn]+=dice;
                if(pawns[0][pawn]>57) pawns[0][pawn]=57;
            }

            rolled=false;
            boolean extra=(dice==6);
            dice=0;
            invalidate();

            if(!extra) nextTurn();
        }

        void nextTurn() {
            turn=(turn+1)%4;
            dice=0;
            rolled=false;
            invalidate();

            if(turn!=0) {
                handler.postDelayed(() -> computerTurn(),700);
            }
        }

        void computerTurn() {
            dice=random.nextInt(6)+1;
            int chosen=-1;

            for(int i=0;i<4;i++) {
                if(pawns[turn][i]<0 && dice==6) {
                    chosen=i;
                    break;
                }
                if(pawns[turn][i]>=0 && pawns[turn][i]<57) {
                    chosen=i;
                    break;
                }
            }

            if(chosen>=0) {
                if(pawns[turn][chosen]<0) pawns[turn][chosen]=0;
                else pawns[turn][chosen]+=dice;
                if(pawns[turn][chosen]>57) pawns[turn][chosen]=57;
            }

            boolean extra=(dice==6);
            dice=0;
            invalidate();

            if(extra) {
                handler.postDelayed(() -> computerTurn(),700);
            } else {
                nextTurn();
            }
        }
    }
}
