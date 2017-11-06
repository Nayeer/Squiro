package com.example.nayeer.squiro.GameEngine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.hardware.SensorEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import com.example.nayeer.squiro.GameOver.GameOverActivity;
import com.example.nayeer.squiro.Graphics.Obstacle;
import com.example.nayeer.squiro.Graphics.Squiro;
import com.example.nayeer.squiro.R;
import com.example.nayeer.squiro.Sounds.SoundManager;

import java.util.ArrayList;

public class GameView extends SurfaceView implements Runnable {


    SurfaceHolder holder;
    private Paint mPaint = null;
    private float x;
    private float y;
    private int viewWidth;
    private int viewHeight;
    private boolean beginning;
    private ArrayList<Obstacle> allObstacles;
    private ArrayList<Obstacle> obstaclesRestants;
    //bornes à ne pas dépasser pour Squiro
    private int borne_haute;
    private int borne_basse;
    //position du "trou" dans lequel Squiro doit tomber
    private int actualGapPosition;
    private int gapSize;
    private static final int STAY_MODE = 1;
    private static final int RUNNING_LEFT_MODE = 2;
    private static final int RUNNING_RIGHT_MODE = 3;

    private int count;
    private int periodicCount;
    private int defilSpeed;
    private boolean playing;
    Thread renderThread = null;
    private int obstacleHeight;
    private int spaceBetweenObstacles;

    private Bitmap platformBmp;


    private long fps;
    private long timeThisFrame;


    private Squiro squiro = null;
    private static final boolean RIGHT = true;
    private static final boolean LEFT = false;

    private boolean obstaclesSynchronization;
    private boolean canAddObstacles;
    private long timerStayMode;
    private boolean fallInGap;

    private int actual_score ;
    private boolean gameOver = false;
    private boolean lockAddObstacles ;
    private int increaseSpeed ;
    private SoundManager sm;
    private boolean canLoadGame = false;
    private RelativeLayout layout;
    private View progressBarView;
    private Context context;
    private boolean firstTime = true;




    public GameView(Context context) {
        super(context);
        this.context = context;
        holder = getHolder();

        //Sert à diminuer légèrement la qualité des bitmaps afin d'améliorer les performances
        holder.setFormat(0x00000004); //RGB_565
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //______________________________________________________________________________________________

        //Recupération de la progressbar afin de l'afficher lors du chargement du jeu
        layout = ((Activity) context).findViewById(R.id.relative_layout);
        progressBarView = ((Activity) context).findViewById(R.id.progressBarView);
        progressBarView.setVisibility(View.GONE);
        progressBarView.bringToFront(); //On la met au dessus de tous les élements
        layout.invalidate();

        sm= new SoundManager(getContext());
        init();
    }

    protected void loadGame() {
        if (canLoadGame) {
            if (firstTime) { //la toute première fois où MainActivity est instanciée.

                //Lancement du son d'ambiance
                sm.playAmbiance();

                //Chargement du bitmap de la platform une seule fois lors de la création de l'activité.
                platformBmp = BitmapFactory.decodeResource(getResources(), R.drawable.platform);

                //On utilisera paint seulement pour écrire le score.
                mPaint = new Paint();
                mPaint.setColor(Color.WHITE);
                mPaint.setTextSize((int) (0.1 * viewHeight));
                Typeface custom_font = Typeface.createFromAsset(getContext().getAssets(), "fonts/orange_juice_2_0.ttf");
                mPaint.setTypeface(custom_font);

                squiro = new Squiro(getContext(), spaceBetweenObstacles);

                firstTime = false;
            }
            //Les variables chargées ci-dessus ne le sont qu'une seule fois.
            //Par ex Squiro est assez lourd à être chargé (plusieurs bitmaps),
            //il n'est chargé que lors du lancement de l'application.
            //Cela a pu améliorer grandement les temps de chargement lors d'un restart du jeu par exemple.

            sm.setGameOver(false);
            gapSize = (int) (0.8 * squiro.getFrontModeFrameWidth());
            //Au début on à la moitié de l'écran rempli d'obstacles
            int nb_obstacles = (int) ((viewHeight / 2) / (obstacleHeight + spaceBetweenObstacles)) + 1;
            for (int i = 0; i < nb_obstacles; i++) {
                int y = (viewHeight / 2) + i * (obstacleHeight + spaceBetweenObstacles);
                Obstacle newObs = generateObstacle(y);
                allObstacles.add(newObs);
                obstaclesRestants.add(newObs);
            }
            //On cherche les bornes qui délmiteront les x et y de Squiro
            borne_basse = obstaclesRestants.get(0).getY() + (int) (0.3 * obstacleHeight);
            //sa borne haute est tout simplement égale à sa taille
            borne_haute = borne_basse + squiro.getFrameWidth();
            actualGapPosition = getActualGapPosition();
        }
    }

    protected void init() { //appelée au tout début mais aussi lors des restarts.
        gameOver = false;
        beginning = true;
        allObstacles = new ArrayList<Obstacle>();
        obstaclesRestants = new ArrayList<Obstacle>();
        borne_haute = actualGapPosition = 0;
        borne_basse = 999999;
        x = y = 0;
        count = 0;
        periodicCount = count;
        defilSpeed = 3;
        actual_score = 0;
        timerStayMode = -1;
        fallInGap = false;
        obstaclesSynchronization = true;
        canAddObstacles = true;
        playing = false;
        increaseSpeed = 0;
        lockAddObstacles = false;
    }

    protected void setDimensions() {
        obstacleHeight = (int) (0.10 * viewHeight);
        spaceBetweenObstacles = (int) (1.0 * obstacleHeight);
    }

    protected Obstacle generateObstacle(int posY) {
        return new Obstacle(this, obstacleHeight, posY);
    }

    protected void addNewObstacle() {
        if (!lockAddObstacles) { //Verouille la méthode pour éviter l'ajout de deux obstacles en même temps
            lockAddObstacles = true;
            Obstacle newObs = generateObstacle(getLastY() + obstacleHeight + spaceBetweenObstacles);
            allObstacles.add(newObs);
            obstaclesRestants.add(newObs);
            lockAddObstacles = false;
        }
    }

    protected int getLastY() { //utile à la création de nouveaux obstacles
        return allObstacles.get(allObstacles.size() -1 ).getY();
    }


    protected int getActualGapPosition() {
            return obstaclesRestants.get(0).getGapPosition();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        setDimensions();
        canLoadGame = true; //les dimensions sont disponibles
    }

    public void resume() { //Appelée manuellement lors du OnResume() parent
        if (!gameOver) {
            sm.playAmbiance();
            playing = true;
            renderThread = new Thread(this);
            renderThread.start();
        }
    }

    public void pause() { //Appelée manuellement lors du OnPause() parent
        playing = false;
        sm.stopAmbiance();
        threadJoin();
    }

    public void threadJoin() { //Bloque le thread tant qu'il n'est pas "mort"/terminé
        while(true) {
            try {
                renderThread.join();
                return;
            } catch (InterruptedException e) {}
        }
    }

    public void gameOver() { //appelée lorsque le joueur a perdu.
        gameOver = true;
        playing = false;
        //Lancement du son game over
        sm.playGameOver();
        sm.stopAmbiance();

        Intent intent = new Intent(getContext(), GameOverActivity.class);
        intent.putExtra("actualScore", actual_score);
        getContext().startActivity(intent);
    }

    public void restart() {
        //Grâce au fait que MainActivity soit en singleInstance, cela nous permet de réutiliser
        //des variables qui auraient pris beaucoup de temps à être rechargées, comme les bitmaps.
        //Le restart se fait donc simplement en réinitialisant quelques variables comme le score et
        //quelques booléens...
        init();
        resume();
    }

    //Capture les évènements de l'acceleromètre transmis par MainActivity,
    //gère Squiro et toute la logique lié au positionnement en général.
    public void onSensorEvent (SensorEvent event) {
        if (squiro != null && !gameOver) {

            float new_x = x - (event.values[0]*10);
            float new_y = y + (event.values[1]*10);

            if (!fallInGap) { //on bloque le mode STAY lors du fallInGap
                //Selection du mode de l'écureuil (courir vers la droite/gauche ou rester sur place)
                if (new_x > x + 8) {
                    timerStayMode = -1;
                    if (squiro.getMode() != RUNNING_RIGHT_MODE) {
                        squiro.runningMode(RIGHT);
                    }
                } else if (new_x < x - 8) {
                    timerStayMode = -1;
                    if (squiro.getMode() != RUNNING_LEFT_MODE) {
                        squiro.runningMode(LEFT);
                    }
                } else if (Math.abs(new_x - x) <= 8) {

                    if (timerStayMode == -1) {
                        timerStayMode = System.currentTimeMillis();
                    }
                    //Le joueur doit rester 100ms sur place pour revenir en position frontale (stayMode).
                    if (timerStayMode != -1 && System.currentTimeMillis() > timerStayMode + 100 && squiro.getMode() != STAY_MODE) {
                        squiro.stayMode();
                        timerStayMode = -1;
                    }
                }
            }
            //On bloque les mouvements en x lors du fallInGap et en STAY MODE
            if (!fallInGap && squiro.getMode() != STAY_MODE) {
                x = new_x;
            }
            y = new_y;

            int squiroWidth = squiro.getRunningModeFrameWidth();
            int squiroHeight = squiro.getFrameHeight();

            //Delimitation de bornes
            if (x <= -squiroWidth/2f) {
                x = -squiroWidth/2f;
            }
            if (x >= viewWidth - squiroWidth/2f) {
                x = viewWidth - squiroWidth/2f;
            }
            if (y <= borne_haute) {
                y = borne_haute;
            }
            if (y >= borne_basse - squiroHeight) {
                y = borne_basse - squiroHeight;
            }

            squiro.setPos((int) x, (int) y); //Changement des coordonnées de Squiro

            //Jeu
            //Is game over ?
            if (squiro.getY() <= -(squiro.getFrameWidth()/2)) {
                    gameOver();
            }
            //Si on se trouve devant le trou à 5 pixels près en STAY MODE
            int squiroCenter = (int) (squiro.getX() + squiro.getFrameWidth()/2f);
            int marge = (int) ((gapSize - 0.3* squiro.getFrameWidth())/2);

            if (squiro.getMode() == STAY_MODE && squiroCenter >= (actualGapPosition - marge) && squiroCenter <= (actualGapPosition + marge)) {
                //On active la partie fallInGap
                fallInGap = true;
                //On joue un petit son
                sm.playFall();
                //La nouvelle borne haute est égale à l'ancienne borne basse.
                borne_haute = borne_basse;
                obstaclesRestants.remove(0); //il nous reste un obstacle de moins à passer
                actual_score++; //On augmente le score du joueur
                borne_basse = obstaclesRestants.get(0).getY() + (int) (0.3*obstacleHeight);
                actualGapPosition = getActualGapPosition();
                //Si on se rend compte qu'il ne reste plus beaucoup d'obstacle on en ajoute un.
                if (obstaclesRestants.size() <= 1) {
                    addNewObstacle();
                }
            }
            //si Squiro se trouve dans la partie basse de l'écran
            //On accelere un peu le jeu pour s'adapter à la rapidité du joueur
            if (squiro.getY()>= 0.8*viewHeight) {
                increaseSpeed++;
            } else {
                increaseSpeed = 0;
            }

            //Si Squiro est arrivé sur la plateforme du bas, on peut dire qu'il a terminé son fallInGap
            if (fallInGap && squiro.getY() >= borne_basse - squiroHeight) {
                fallInGap = false;
            }
        }
    }

    protected void setProgressBarVisible(final boolean show) {
        //Doit être executé dans l'UI-Thread car les autres thread n'y ont pas le droit.
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(show) {
                    progressBarView.setVisibility(View.VISIBLE);
                } else {
                    progressBarView.setVisibility(View.GONE);
                }
            }
        });
    }



    @Override
    public void run() {
        while(playing) {

            if(!holder.getSurface().isValid()) {
                continue;
            }
            //-- Définition d'un canvas, et verrouillage le temps que l'on dessine dessus
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //Efface le canvas

            if (beginning) {
                setProgressBarVisible(true);
                loadGame(); //Chargement de certains élements importants du jeu.
                setProgressBarVisible(false);
                beginning = false;
            } //end beginning
            else {
                //on calcule la vitesse grâce à notre équation et on y ajoute un coefficient additif
                //qui augmentera la vitesse si Squiro se trouve en bas de l'écran
                int new_speed = computeSpeed(actual_score) + increaseSpeed;
                if (new_speed != defilSpeed) {
                    defilSpeed = new_speed;
                }
                //Etape de synchronisation du nombre d'obstacles.
                // (sert à atteindre le nombre optimal d'obstacles sur l'écran)
                //On ajoute un nouvel obstacle tous les x temps
                //A chaque fois qu'on a "défilé" de la hauteur d'un obstacle
                if (obstaclesSynchronization) {
                    count++;
                    if (count >= (periodicCount + (int) (obstacleHeight / defilSpeed))) {
                        periodicCount = count;
                        addNewObstacle();
                    }
                }
                //Une fois que l'obstacle le plus haut est suffisamment haut , l'étape de synchronisation
                //se termine et à partir de maintenant on ajoute un nouvel obstacle uniquement quand
                //il y en a un qui sort de l'écran. (on est donc en mode synchronisé)
                if (allObstacles.get(0).getY() <= (spaceBetweenObstacles + obstacleHeight)) {
                    obstaclesSynchronization = false;
                }
                //On ajoute un nouvel obstacle en bas lorsque l'obstacle le plus haut touche le bord haut de l'écran
                if (canAddObstacles && allObstacles.get(0).getY() <= 0) {
                    addNewObstacle();
                    canAddObstacles = false;
                }
                //Quelques instants après:
                //On supprime l'obstacle le plus haut si il est complétement sorti de l'écran
                if (allObstacles.get(0).getY() <= -obstacleHeight) {
                    allObstacles.remove(0);
                    canAddObstacles = true;
                }
                /* Affichage du nombre d'obstacles dans les tableaux en temps réel
                    Log.d("allObstaclesSize", String.valueOf(allObstacles.size()));
                    Log.d("obsRestantsSize", String.valueOf(obstaclesRestants.size()));
                */
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //Efface le canvas
                squiro.draw(canvas); //dessine Squiro a la bonne position et appelle ses fonctions d'animation.
                borne_basse = borne_basse - defilSpeed;
                //On dessine tous les obstacles et on les fait défiler.
                for (int i = 0; i < allObstacles.size(); i++) {
                    allObstacles.get(i).setY(allObstacles.get(i).getY() - defilSpeed);
                    allObstacles.get(i).draw(canvas);
                }
                //affichage du score
                drawTextCentered(String.valueOf(actual_score), (int) (0.5f * viewWidth), (int) (0.1f * viewHeight), canvas);
            }
            holder.unlockCanvasAndPost(canvas); //-- Libération du dessin
        }
    }

    protected int computeSpeed(int x) {
        //equation obtenue à l'aide de logiciel de curve fitting (CurveExpert Basic Version)
        //Difficile -> (int) (9.057444073319850*(1.651806470719324 - Math.exp(-0.01410675274317831*x)));
        //Plutot facile -> (int) (5.373320092809225*(1.899092077993690 - Math.exp(-0.01715817542773009*x)));

        //Cette valeur a été calculée sur un smartphone de résolution 1440x2560.
        int highResValue = (int) (8.200287431445810*(1.729569006879296 - Math.exp(-0.01247969452079778*x)));

        //Cependant cette valeur varie en fonction de la résolution du smartphone de l'utilisateur
        //on retourne donc une valeur proportionnelle.
        return (int) ((highResValue*viewHeight)/2560f);
    }


    public int getViewHeight() {
        return this.viewHeight;
    }

    public int getViewWidth() {
        return this.viewWidth;
    }

    public Bitmap getPlatformBmp() {
        return this.platformBmp;
    }

    public int getGapSize() {
        return this.gapSize;
    }

    private void drawTextCentered(String text, int x, int y, Canvas canvas) {
        int xPos = x - (int)(mPaint.measureText(text)/2);
        int yPos = (int) (y - ((mPaint.descent() + mPaint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, mPaint);
    }

}
