package com.example.lab8_threads_asynctask_handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Activité principale démontrant la gestion du multi-threading (Thread et AsyncTask).
 * Le code a été personnalisé pour une structure unique et des noms de variables originaux.
 */
public class MainActivity extends AppCompatActivity {

    // Références aux vues personnalisées
    private TextView etiquetteEtat;
    private ProgressBar barreProgression;
    private ImageView visualiseurImage;

    // Handler pour les mises à jour sur le fil principal (UI)
    private Handler aiguilleurInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Activation du mode bord à bord
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Application des marges système pour éviter le chevauchement avec les barres d'état/navigation
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_root), (vue, insets) -> {
            Insets barresSysteme = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vue.setPadding(barresSysteme.left, barresSysteme.top, barresSysteme.right, barresSysteme.bottom);
            return insets;
        });

        // Liaison des composants XML avec le code Java
        etiquetteEtat = findViewById(R.id.tv_status_display);
        barreProgression = findViewById(R.id.progress_bar_task);
        visualiseurImage = findViewById(R.id.image_viewer_main);

        Button btnActionThread = findViewById(R.id.btn_thread_process);
        Button btnActionAsync = findViewById(R.id.btn_async_process);
        Button btnVerifierUI = findViewById(R.id.btn_check_ui);

        // Initialisation du gestionnaire de messages pour l'UI
        aiguilleurInterface = new Handler(Looper.getMainLooper());

        // Bouton de test : l'interface doit rester réactive
        btnVerifierUI.setOnClickListener(view -> 
            Toast.makeText(getApplicationContext(), "L'interface répond instantanément !", Toast.LENGTH_SHORT).show()
        );

        // Chargement d'image via un fil d'exécution séparé
        btnActionThread.setOnClickListener(view -> demarrerTraitementThread());

        // Lancement du calcul lourd par une tâche asynchrone
        btnActionAsync.setOnClickListener(view -> new TaskCalculDeFond().execute());
    }

    /**
     * Méthode gérant le chargement d'une ressource dans un Thread secondaire.
     */
    private void demarrerTraitementThread() {
        // Phase de préparation (Thread UI)
        barreProgression.setVisibility(View.VISIBLE);
        barreProgression.setProgress(0);
        etiquetteEtat.setText("Action : Chargement par fil secondaire...");

        // Création et lancement du thread de travail
        new Thread(() -> {
            try {
                // Simulation d'une tâche de 1.2 seconde (ex: réseau)
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Génération d'un Bitmap en arrière-plan
            final Bitmap imageBmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            // Mise à jour de l'interface via l'aiguilleur (Handler)
            aiguilleurInterface.post(() -> {
                visualiseurImage.setImageBitmap(imageBmp);
                barreProgression.setVisibility(View.INVISIBLE);
                etiquetteEtat.setText("Résultat : Image chargée via Thread.");
            });
        }).start();
    }

    /**
     * Classe interne personnalisée pour le traitement asynchrone.
     */
    private class TaskCalculDeFond extends AsyncTask<Void, Integer, Long> {

        // Avant l'exécution : s'exécute sur le Thread principal
        @Override
        protected void onPreExecute() {
            barreProgression.setVisibility(View.VISIBLE);
            barreProgression.setProgress(0);
            etiquetteEtat.setText("Action : Calcul AsyncTask en cours...");
        }

        // Traitement principal : s'exécute sur un thread de travail (Worker Thread)
        @Override
        protected Long doInBackground(Void... params) {
            long accumulateur = 0;

            for (int cycle = 1; cycle <= 100; cycle++) {
                // Simulation d'une boucle gourmande en ressources
                for (int i = 0; i < 280000; i++) {
                    accumulateur += (cycle * i) % 9;
                }

                // Publication de l'avancement vers le fil principal
                publishProgress(cycle);
            }
            return accumulateur;
        }

        // Mise à jour de la progression : s'exécute sur le Thread principal
        @Override
        protected void onProgressUpdate(Integer... progressions) {
            barreProgression.setProgress(progressions[0]);
        }

        // Fin du traitement : s'exécute sur le Thread principal
        @Override
        protected void onPostExecute(Long resultat) {
            barreProgression.setVisibility(View.INVISIBLE);
            etiquetteEtat.setText("Calcul terminé. Score final : " + resultat);
        }
    }
}
