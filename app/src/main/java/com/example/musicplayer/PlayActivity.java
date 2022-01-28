package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayActivity extends AppCompatActivity {

    TextView txtsong2, txtdemarrage, txtarret;
    SeekBar seekBar;
    BarVisualizer visualizer;
    Button btnplay, btnNext, btnRetour, btnff, btnfr;
    ImageView IV;

    String nom;
    public static final String EXTRA_NAME= "NomChanson";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> chansons;
    //la variable pour la seekbar pour synchroniser avec le temps de la chanson
    Thread update;


    //l'affichage du audiovisualizer
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer!=null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        getSupportActionBar().setTitle("ça Joue!");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //on initialise nos variables avec les id des boutons et les textViews.
        btnplay= findViewById(R.id.btnplay);
        btnNext= findViewById(R.id.btnNext);
        btnRetour=findViewById(R.id.btnRetour);
        btnff=findViewById(R.id.btnff);
        btnfr=findViewById(R.id.btnfr);
        txtsong2=findViewById(R.id.txtsong2);
        txtdemarrage=findViewById(R.id.txtdemarrage);
        txtarret=findViewById(R.id.txtarret);
        this.seekBar=findViewById(R.id.seekBar);
        visualizer=findViewById(R.id.blast);
        IV=findViewById(R.id.iv);


        //si le diffuseur du media a une variable on arrête la diffusion
        if(mediaPlayer!= null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        //ceci nous permet d'ouvrir la page de la musique pour le music play
        Intent i=getIntent();
        Bundle bundle=i.getExtras();

        chansons=(ArrayList) bundle.getParcelableArrayList("Chansons");
        final String[] nom = {i.getStringExtra("NomChanson")};
        position=bundle.getInt("pos",0);
        txtsong2.setSelected(true);
        Uri u= Uri.parse(chansons.get(position).toString());
        this.nom = chansons.get(position).getName();
        txtsong2.setText(this.nom);

        mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
        mediaPlayer.start();

        update= new Thread(){
            @Override
            public void run() {
                int temps=mediaPlayer.getDuration();
                int position=0;
                while(position<temps){
                    try {
                        sleep(500);
                        position=mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(position);
                    }catch(InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        this.seekBar.setMax(mediaPlayer.getDuration());
        update.start();
        this.seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_on_primary), PorterDuff.Mode.MULTIPLY);
        this.seekBar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_on_primary),PorterDuff.Mode.MULTIPLY);

        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seek) {
                mediaPlayer.seekTo(seek.getProgress());
            }
        });

        String fin=TimeConverter(mediaPlayer.getDuration());
        txtarret.setText(fin);

        final Handler h=new Handler();
        final int retard=1000;
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                String wakt= TimeConverter(mediaPlayer.getCurrentPosition());
                txtdemarrage.setText(wakt);
                h.postDelayed(this,retard);
            }
        }, retard);

        //on verifie si la chanson est en train d'être jouée
        btnplay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying())
            {
                btnplay.setBackgroundResource(R.drawable.play);
                mediaPlayer.pause();
            }else{
                btnplay.setBackgroundResource(R.drawable.pause);
                mediaPlayer.start();
            }
        });
        //l'action du bouton next
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position= ((position+1)%chansons.size());
                Uri u =Uri.parse(chansons.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                PlayActivity.this.nom =chansons.get(position).getName();
                txtsong2.setText(PlayActivity.this.nom);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.pause);
                String endTime = TimeConverter(mediaPlayer.getDuration());
                txtarret.setText(endTime);
                Animation(IV);
                int session= mediaPlayer.getAudioSessionId();
                if (session!= -1){
                    visualizer.setAudioSessionId(session);
                }
            }
        });
        //lorsque la chanson se termine
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.performClick();
            }
        });

        int session= mediaPlayer.getAudioSessionId();
        if (session!= -1){
            visualizer.setAudioSessionId(session);
        }

        //l'action du bouton retour
        btnRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position= ((position-1)<0)?(chansons.size()-1):(position-1);
                Uri u =Uri.parse(chansons.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),u);
                PlayActivity.this.nom =chansons.get(position).getName();
                txtsong2.setText(PlayActivity.this.nom);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.pause);
                String endTime = TimeConverter(mediaPlayer.getDuration());
                txtarret.setText(endTime);
                Animation(IV);
                int session= mediaPlayer.getAudioSessionId();
                if (session!= -1){
                    visualizer.setAudioSessionId(session);
                }
            }
        });
        //les actions de sauter 10 sec ou retourner 10 sec
        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });
        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

    }

    //cette fonction permet la rotation de l'image de la chanson
    public void Animation(View view){
        ObjectAnimator oa= ObjectAnimator.ofFloat(IV,"rotation",0f,360f);
        oa.setDuration(1000);
        AnimatorSet set=new AnimatorSet();
        set.playTogether(oa);
        set.start();
    }

    //cette fonction nous permet le display du temps à coté de la seekBar
    public String TimeConverter(int T){
        String wakt="";
        int minute=T/1000/60;
        int seconde= T/1000%60;

        wakt+=minute+":";
        if (seconde<10){
            wakt+="0";
        }
        wakt+=seconde;

        return wakt;
    }
}