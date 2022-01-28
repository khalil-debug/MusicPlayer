package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    String[] objets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Permission();
        listView = findViewById(R.id.lstSongs);

    }

    //on a installé une nouvelle dépendance "Dexter" pour assurer les permissions de l'utilisateur
    public void Permission(){
        //on affiche la bulle de permission
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        afficherChansons();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
    //cette fonction retourne une liste contenant toutes les chansons trouvées sur l'application
    public ArrayList<File> ChercherChansons(File f){
        ArrayList<File> lst = new ArrayList<>();
        File[] files= f.listFiles();
        //on parcoure les fichiers
        for (File singulier: files)
        {
            //si on trouve un fichier dans le fichier on le parcours aussi avec une itération jusqu'à ce qu'on trouve la musique
            if (singulier.isDirectory() && !singulier.isHidden()){
                lst.addAll(ChercherChansons(singulier));
            }
            //sinon on ajoute les fichiers .mp3, .wav ou .m4a(Huawei)
            else
            {
                if(singulier.getName().endsWith(".mp3") || singulier.getName().endsWith(".wav") || singulier.getName().endsWith(".m4a"))
                {
                    lst.add(singulier);
                }
            }
        }
        return lst;
    }

    void afficherChansons(){
        //une liste des chansons
        final ArrayList<File> musique = ChercherChansons(Environment.getExternalStorageDirectory());
        //un tableau de chansons
        this.objets = new String[musique.size()];
        //on efface les extensions .mp3 et les autres
        for (int i=0; i<musique.size(); i++){
            this.objets[i]=musique.get(i).getName().toString().replace(".mp3","").replace(".wav","").replace(".m4a","");
        }
        //on crée un adaptateur pour la liste
        Adaptateur a= new Adaptateur();
        listView.setAdapter(a);
        //lorsque l'utilisateur clique sur une chanson, la classe PlayActivity sera active
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nom= (String) listView.getItemAtPosition(position);
                startActivity(new Intent(getApplicationContext(), PlayActivity.class).putExtra("Chansons", musique)
                        .putExtra("NomChanson",nom).putExtra("pos",position));
            }
        });
    }

    //cette classe nous permet de lister les titres en listeview
    class Adaptateur extends BaseAdapter{

        @Override
        public int getCount() {
            return objets.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v= getLayoutInflater().inflate(R.layout.liste_objets,null);
            TextView chans = v.findViewById(R.id.txtsong);
            chans.setSelected(true);
            chans.setText(objets[position]);

            return v;
        }
    }
}