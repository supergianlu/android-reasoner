package com.mgf.androidreasoner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import org.semanticweb.HermiT.Reasoner;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.Set;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "my_prefs";
    public static final int HERMIT = 0;
    public static final int PELLET = 1;
    private String[] arraySpinner = new String[] {"Hermit", "Pellet"};
    private static final int PICKFILE_RESULT_CODE = 123;
    private TextView textView;
    private EditText editText;
    private int elementSelected;
    private ProgressBar progressBar;
    private Button consistencyButton, classesButton, propButton;
    private static ReasonerComputing reasonerComputing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        elementSelected = prefs.getInt("spinner", HERMIT);

        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        editText = findViewById(R.id.editText);
        progressBar = findViewById(R.id.progressBar);

        final Button button = findViewById(R.id.buttonDevice);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICKFILE_RESULT_CODE);
            }
        });

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(elementSelected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                elementSelected = position;
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("spinner", position);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // nothing
            }

        });

        consistencyButton = findViewById(R.id.buttonConsistency);
        consistencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();
                new Thread() {
                    public void run() {
                        OWLOntology ontology = getOntology();
                        if(ontology != null) {
                            boolean isConsistent = false;
                            final long t0 = System.currentTimeMillis();
                            if (elementSelected == HERMIT) {
                                final Reasoner hermitReasoner = new Reasoner(ontology);
                                isConsistent = hermitReasoner.isConsistent();
                            } else if (elementSelected == PELLET) {
                                final PelletReasoner pelletReasoner = new PelletReasonerFactory().createReasoner(ontology);
                                isConsistent = pelletReasoner.isConsistent();
                            }
                            final long t1 = System.currentTimeMillis();
                            final boolean _isConsistent = isConsistent;
                            textView.post(new Runnable() {
                                public void run() {
                                    textView.scrollTo(0,0);
                                    textView.setText("Ontologia consistente: " + _isConsistent + "\nTempo impiegato: " + (t1 - t0) + " millisecondi");
                                }
                            });
                        } else {
                            textView.post(new Runnable() {
                                public void run() {
                                    textView.setText("Il file non corrisponde ad un'ontologia");
                                }
                            });
                        }
                        restoreGui();
                    }
                }.start();
            }
        });

        classesButton = findViewById(R.id.buttonClasses);
        classesButton.setOnClickListener(View -> {
            prepareReasonerComputing("classi");
        });

        propButton = findViewById(R.id.buttonProp);
        propButton.setOnClickListener(View -> {
            prepareReasonerComputing("proprietà");
        });

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICKFILE_RESULT_CODE) {
            if(resultCode==RESULT_OK){
                String fileUri = data.getData().getPath();
                if(fileUri.contains(".owl")){
                    String filePath = fileUri.substring(fileUri.indexOf(":") + 1);
                    editText.setText(filePath);
                } else {
                    textView.setText("File non valido");
                }
            }
        }
    }

    public OWLOntology getOntology() {
        consistencyButton.setClickable(false);
        classesButton.setClickable(false);
        progressBar.post(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
        OWLOntology ontology = null;
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        try {
            try {
                ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(editText.getText().toString()));
            } catch (OWLOntologyInputSourceException e) {
                ontology = ontologyManager.loadOntologyFromOntologyDocument(IRI.create(editText.getText().toString()));
            }
        } catch (OWLOntologyCreationIOException e2){
            e2.printStackTrace();
            textView.post(new Runnable() {
                public void run() {
                    textView.setText("Connessione internet assente");
                }
            });
            consistencyButton.setClickable(true);
        }catch (Exception error) {
            error.printStackTrace();
            textView.post(new Runnable() {
                public void run() {
                    textView.setText("File non valido");
                }
            });
            consistencyButton.setClickable(true);
            classesButton.setClickable(true);
        }
        return ontology;
    }

    private void restoreGui() {
        progressBar.post(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
        consistencyButton.setClickable(true);
        classesButton.setClickable(true);
    }

    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != this.getCurrentFocus())
            imm.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getApplicationWindowToken(), 0);
    }

    private void prepareReasonerComputing(String s) {
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                OWLOntology ontology = getOntology();
                if(ontology != null){
                    reasonerComputing = null;
                    if (elementSelected == HERMIT) {
                        reasonerComputing = new ReasonerComputing(HERMIT,ontology);
                    } else if (elementSelected == PELLET) {
                        reasonerComputing = new ReasonerComputing(PELLET,ontology);
                    }
                }
                startNextActivity(s);
            }
        };
        t.start();
    }

    private void startNextActivity(String s) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("TYPE",s);
        startActivity(intent);
    }

    public static ReasonerComputing getReasonerComputing() {
        return reasonerComputing;
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
