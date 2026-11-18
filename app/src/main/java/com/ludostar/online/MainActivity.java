
package com.ludostar.online;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.MotionEvent;
import android.view.WindowManager;
import java.util.Random;

public class MainActivity extends Activity {

    private LudoView game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        hideSystemBars();

        game = new LudoView();
        setContentView(game);
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    @Override
    public void onBackPressed() {
        if (game != null && game.playing) {
            game.playing = false;
            game.invalidate();
        } else {
            super.onBackPressed();
        }
    }

    class LudoView extends View {

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Handler handler = new Handler();
        Random random = new Random();

        final int NAVY = Color.rgb(9, 17, 40);
        final int GOLD = Color.rgb(245, 190, 65);
        final int WHITE = Color.WHITE;
        final int BLACK = Color.BLACK;

        final int[] colors = {
            Color.rgb(230, 45, 55),
            Color.rgb(35, 175, 85),
            Color.rgb(45, 110, 235),
            Color.rgb(245, 200, 35)
        };

        final int[] starts = {0, 13, 26, 39};

        final int[][] path = {
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

        final int[][][] homes = {
            {{2,2},{4,2},{2,4},{4,4}},
            {{10,2},{12,2},{10,4},{12,4}},
            {{2,10},{4,10},{2,12},{4,12}},
            {{10,10},{12,10},{10,12},{12,12}}
        };

        final int[][] lanes = {
            {7,1},{7,2},{7,3},{7,4},{7,5},{7,6},
            {13,7},{12,7},{11,7},{10,7},{9,7},{8,7},
            {7,13},{7,12},{7,11},{7,10},{7,9},{7,8},
            {1,7},{2,7},{3,7},{4,7},{5,7},{6,7}
        };

        final int[] safe = {0,8,13,21,26,34,39,47};

        int[][] pawns = new int[4][4];

        boolean playing = false;
        boolean rolled = false;
        boolean busy = false;
        boolean gameOver = false;

        int turn = 0;
        int dice = 0;
        int selectedPawn = -1;
        int winner = -1;

        float boardX, boardY, cell;
        float screenW, screenH;

        public LudoView() {
            super(MainActivity.this);

            for (int a = 0; a < 4; a++) {
                for (int b = 0; b < 4; b++) {
                    pawns[a][b] = -1;
                }
            }

            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void fill(int color) {
            p.setColor(color);
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(1);
        }

        void stroke(int color, float width) {
            p.setColor(color);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(width);
        }

        void rect(Canvas c, float l, float t, float r, float b,
                  int color, float radius) {
            fill(color);
            c.drawRoundRect(l, t, r, b, radius, radius, p);
        }

        void text(Canvas c, String value, float x, float y,
                  float size, int color) {
            fill(color);
            p.setTextSize(size);
            p.setTextAlign(Paint.Align.CENTER);
            p.setFakeBoldText(true);
            c.drawText(value, x, y, p);
            p.setFakeBoldText(false);
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);

            screenW = getWidth();
            screenH = getHeight();

            c.drawColor(NAVY);

            if (!playing) {
                drawHome(c);
            } else {
                drawGame(c);
            }
        }

        void drawHome(Canvas c) {
            text(c, "LUDO STAR", screenW/2, screenH*.17f,
                 38, GOLD);

            text(c, "PLAY  •  ENJOY  •  WIN",
                 screenW/2, screenH*.22f, 15, WHITE);

            float size = Math.min(screenW*.76f, screenH*.40f);
            float x = (screenW-size)/2;
            float y = screenH*.29f;
            float q = size/2;

            rect(c,x,y,x+size,y+size,WHITE,18);

            rect(c,x+5,y+5,x+q-2,y+q-2,colors[0],12);
            rect(c,x+q+2,y+5,x+size-5,y+q-2,colors[3],12);
            rect(c,x+5,y+q+2,x+q-2,y+size-5,colors[1],12);
            rect(c,x+q+2,y+q+2,x+size-5,y+size-5,colors[2],12);

            float[][] points = {
                {x+q*.5f,y+q*.5f},
                {x+q*1.5f,y+q*.5f},
                {x+q*.5f,y+q*1.5f},
                {x+q*1.5f,y+q*1.5f}
            };

            for (int i=0;i<4;i++) {
                for (int j=0;j<4;j++) {
                    float px = points[i][0] + ((j%2)*q*.35f)-q*.17f;
                    float py = points[i][1] + ((j/2)*q*.35f)-q*.17f;
                    fill(WHITE);
                    c.drawCircle(px,py,size*.045f,p);
                }
            }

            rect(c,screenW*.14f,screenH*.79f,
                 screenW*.86f,screenH*.89f,GOLD,18);

            text(c,"START GAME",screenW/2,
                 screenH*.845f,22,NAVY);

            text(c,"1 PLAYER  VS  3 COMPUTERS",
                 screenW/2,screenH*.94f,13,WHITE);
        }

        void drawGame(Canvas c) {
            text(c,"LUDO STAR",screenW/2,27,20,GOLD);

            float available = Math.min(screenW-18,screenH-165);
            cell = available/15f;
            boardX = (screenW-available)/2;
            boardY = (screenH-available)/2+8;

            drawProfiles(c);
            drawBoard(c);
            drawPawns(c);
            drawDice(c);

            text(c,"TURN: "+playerName(turn),
                 screenW/2,screenH-17,13,WHITE);

            if (gameOver) {
                rect(c,screenW*.12f,screenH*.36f,
                     screenW*.88f,screenH*.62f,GOLD,18);
                text(c,playerName(winner)+" WINS!",
                     screenW/2,screenH*.46f,27,NAVY);
                text(c,"TAP TO PLAY AGAIN",
                     screenW/2,screenH*.54f,15,NAVY);
            }
        }

        String playerName(int player) {
            return new String[]{"RED","GREEN","BLUE","YELLOW"}[player];
        }

        void drawProfiles(Canvas c) {
            float top = Math.max(35,boardY-37);
            text(c,"🔴 YOU",screenW*.19f,top,13,colors[0]);
            text(c,"GREEN CPU",screenW*.80f,top,12,colors[1]);

            float bottom = boardY+cell*15+20;
            text(c,"BLUE CPU",screenW*.19f,bottom,12,colors[2]);
            text(c,"YELLOW CPU",screenW*.80f,bottom,12,colors[3]);
        }

        void drawBoard(Canvas c) {
            rect(c,boardX-3,boardY-3,
                 boardX+cell*15+3,boardY+cell*15+3,
                 WHITE,4);

            for (int r=0;r<15;r++) {
                for (int col=0;col<15;col++) {
                    int color=WHITE;

                    if (r<6 && col<6) color=colors[0];
                    if (r<6 && col>8) color=colors[3];
                    if (r>8 && col<6) color=colors[1];
                    if (r>8 && col>8) color=colors[2];

                    if (col==7 && r<=5) color=colors[0];
                    if (r==7 && col>=9) color=colors[3];
                    if (col==7 && r>=9) color=colors[2];
                    if (r==7 && col<=5) color=colors[1];

                    float x=boardX+col*cell;
                    float y=boardY+r*cell;

                    fill(color);
                    c.drawRect(x,y,x+cell,y+cell,p);

                    stroke(Color.rgb(180,180,180),.7f);
                    c.drawRect(x,y,x+cell,y+cell,p);
                }
            }

            drawYard(c,0,0,colors[0]);
            drawYard(c,9,0,colors[3]);
            drawYard(c,0,9,colors[1]);
            drawYard(c,9,9,colors[2]);

            drawHomeLanes(c);

            float cx=boardX+7.5f*cell;
            float cy=boardY+7.5f*cell;

            fill(GOLD);
            c.drawCircle(cx,cy,cell*1.4f,p);
            text(c,"★",cx,cy+cell*.42f,cell*1.7f,NAVY);

            for (int i=0;i<safe.length;i++) {
                int index=safe[i];
                float px=boardX+(path[index][1]+.5f)*cell;
                float py=boardY+(path[index][0]+.5f)*cell;
                fill(Color.rgb(235,220,150));
                c.drawCircle(px,py,cell*.18f,p);
            }
        }

        void drawYard(Canvas c,int gx,int gy,int color) {
            float x=boardX+gx*cell;
            float y=boardY+gy*cell;

            rect(c,x+cell*.55f,y+cell*.55f,
                 x+cell*5.45f,y+cell*5.45f,
                 WHITE,cell*.25f);

            for(int i=0;i<4;i++) {
                float px=x+cell*(i%2==0?2:4);
                float py=y+cell*(i<2?2:4);

                fill(color);
                c.drawCircle(px,py,cell*.48f,p);

                stroke(Color.WHITE,2);
                c.drawCircle(px,py,cell*.48f,p);
            }
        }

        void drawHomeLanes(Canvas c) {
            for(int i=0;i<6;i++) {
                drawCell(c,7,1+i,colors[0]);
                drawCell(c,13-i,7,colors[3]);
                drawCell(c,7,13-i,colors[2]);
                drawCell(c,1+i,7,colors[1]);
            }
        }

        void drawCell(Canvas c,int row,int col,int color) {
            float x=boardX+col*cell;
            float y=boardY+row*cell;
            fill(color);
            c.drawRect(x,y,x+cell,y+cell,p);
            stroke(Color.WHITE,1);
            c.drawRect(x,y,x+cell,y+cell,p);
        }

        float[] pawnPosition(int player,int pawn) {
            int value=pawns[player][pawn];

            if(value<0) {
                return new float[]{
                    homes[player][pawn][1],
                    homes[player][pawn][0]
                };
            }

            if(value>=52) {
                int lane=value-52;
                int[][] positions={
                    {7,5},{9,7},{7,9},{5,7}
                };
                int[][] dirs={
                    {0,-1},{-1,0},{0,1},{1,0}
                };
                return new float[]{
                    positions[player][1]+dirs[player][1]*lane,
                    positions[player][0]+dirs[player][0]*lane
                };
            }

            int index=(starts[player]+value)%52;

            return new float[]{
                path[index][1],
                path[index][0]
            };
        }

        void drawPawns(Canvas c) {
            for(int a=0;a<4;a++) {
                for(int b=0;b<4;b++) {
                    float[] pos=pawnPosition(a,b);

                    float x=boardX+(pos[0]+.5f)*cell;
                    float y=boardY+(pos[1]+.5f)*cell;

                    fill(colors[a]);
                    c.drawCircle(x,y,cell*.34f,p);

                    stroke(WHITE,2);
                    c.drawCircle(x,y,cell*.34f,p);

                    fill(WHITE);
                    c.drawCircle(x,y,cell*.10f,p);
                }
            }
        }

        void drawDice(Canvas c) {
            float x=screenW/2;
            float y=boardY-12;

            rect(c,x-25,y-24,x+25,y+24,GOLD,9);

            text(c,dice==0?"⚄":""+dice,
                 x,y+9,25,NAVY);
        }

        boolean isSafe(int position) {
            for(int s:safe) {
                if(position==s) return true;
            }
            return false;
        }

        boolean canMove(int player,int pawn,int number) {
            int value=pawns[player][pawn];

            if(value<0) return number==6;
            return value+number<=57;
        }

        boolean hasMove(int player,int number) {
            for(int i=0;i<4;i++) {
                if(canMove(player,i,number)) return true;
            }
            return false;
        }

        void rollDice() {
            if(busy || rolled || gameOver) return;

            dice=random.nextInt(6)+1;
            rolled=true;

            invalidate();

            if(!hasMove(turn,dice)) {
                final boolean extra=dice==6;

                handler.postDelayed(() -> {
                    rolled=false;
                    dice=0;
                    if(!extra) nextTurn();
                    else {
                        invalidate();
                        if(turn!=0) computerTurn();
                    }
                },600);
            }
        }

        void movePawn(int player,int pawn) {
            if(!canMove(player,pawn,dice)) return;

            if(pawns[player][pawn]<0) {
                pawns[player][pawn]=0;
            } else {
                pawns[player][pawn]+=dice;
            }

            boolean captured=false;

            int position=(starts[player]+pawns[player][pawn])%52;

            if(pawns[player][pawn]<52 && !isSafe(position)) {
                for(int other=0;other<4;other++) {
                    if(other==player) continue;

                    for(int j=0;j<4;j++) {
                        int value=pawns[other][j];

                        if(value>=0 && value<52) {
                            int otherPosition=(starts[other]+value)%52;

                            if(position==otherPosition) {
                                pawns[other][j]=-1;
                                captured=true;
                            }
                        }
                    }
                }
            }

            boolean won=true;
            for(int i=0;i<4;i++) {
                if(pawns[player][i]!=57) won=false;
            }

            if(won) {
                gameOver=true;
                winner=player;
                invalidate();
                return;
            }

            boolean extra=dice==6 || captured;

            dice=0;
            rolled=false;
            selectedPawn=-1;

            invalidate();

            if(!extra) {
                nextTurn();
            } else if(player!=0) {
                handler.postDelayed(() -> computerTurn(),700);
            }
        }

        void nextTurn() {
            if(gameOver) return;

            turn=(turn+1)%4;
            dice=0;
            rolled=false;
            busy=false;

            invalidate();

            if(turn!=0) {
                handler.postDelayed(() -> computerTurn(),700);
            }
        }

        void computerTurn() {
            if(gameOver || turn==0) return;

            busy=true;
            dice=random.nextInt(6)+1;

            if(!hasMove(turn,dice)) {
                boolean extra=dice==6;
                dice=0;
                busy=false;
                invalidate();

                if(extra) {
                    handler.postDelayed(() -> computerTurn(),700);
                } else {
                    nextTurn();
                }
                return;
            }

            int chosen=-1;

            for(int i=0;i<4;i++) {
                if(canMove(turn,i,dice)) {
                    chosen=i;
                    break;
                }
            }

            if(chosen>=0) {
                movePawn(turn,chosen);
            } else {
                nextTurn();
            }

            busy=false;
        }

        void resetGame() {
            for(int a=0;a<4;a++) {
                for(int b=0;b<4;b++) {
                    pawns[a][b]=-1;
                }
            }

            turn=0;
            dice=0;
            rolled=false;
            busy=false;
            gameOver=false;
            winner=-1;
            playing=true;

            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(event.getAction()!=MotionEvent.ACTION_UP) {
                return true;
            }

            float x=event.getX();
            float y=event.getY();

            if(!playing) {
                if(y>screenH*.74f) {
                    resetGame();
                }
                return true;
            }

            if(gameOver) {
                resetGame();
                return true;
            }

            if(turn!=0 || busy) return true;

            if(!rolled) {
                if(y>boardY-45 && y<boardY+15) {
                    rollDice();
                } else if(y>boardY+cell*15) {
                    rollDice();
                }
                return true;
            }

            int chosen=findTouchedPawn(x,y);

            if(chosen>=0) {
                movePawn(0,chosen);
            }

            return true;
        }

        int findTouchedPawn(float x,float y) {
            for(int i=0;i<4;i++) {
                if(!canMove(0,i,dice)) continue;

                float[] pos=pawnPosition(0,i);
                float px=boardX+(pos[0]+.5f)*cell;
                float py=boardY+(pos[1]+.5f)*cell;

                if(Math.hypot(x-px,y-py)<cell*.65f) {
                    return i;
                }
            }
            return -1;
        }
    }
}
