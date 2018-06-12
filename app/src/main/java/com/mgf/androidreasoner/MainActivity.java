package com.mgf.androidreasoner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
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
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.Permission;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String MY_PREFS_NAME = "my_prefs";
    private static final int HERMIT = 0;
    private static final int PELLET = 1;
    private String[] arraySpinner = new String[] {"Hermit", "Pellet"};
    private static final int PICKFILE_RESULT_CODE = 123;
    private TextView textView;
    private EditText editText;
    private int elementSelected;
    private ProgressBar progressBar;
    private Button consistencyButton;
    private Button classesButton;

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

        final Button button = findViewById(R.id.button);
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

        consistencyButton = findViewById(R.id.button2);
        classesButton = findViewById(R.id.button3);

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

        classesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard();
                new Thread(){
                    public void run(){
                        OWLOntology ontology = getOntology();
                        if(ontology != null){
                            Reasoner hermitReasoner = null;
                            PelletReasoner pelletReasoner = null;
                            if (elementSelected == HERMIT) {
                                    hermitReasoner = new Reasoner(ontology);
                            } else if (elementSelected == PELLET) {
                                pelletReasoner = new PelletReasonerFactory().createReasoner(ontology);
                            }

                            //get all individuals of a specific class using reasoner
                        /*for (OWLClass c : ontology.getClassesInSignature()) {
                            if (c.getIRI().getFragment().equals("Capricciosa")){
                                NodeSet<OWLNamedIndividual> instances;
                                if(pelletReasoner != null) {
                                    instances = pelletReasoner.getInstances(c, false);
                                }else {
                                    instances = hermitReasoner.getInstances(c, false);
                                }
                                System.out.println(c.getIRI().getFragment());
                                for (OWLNamedIndividual i : instances.getFlattened()) {
                                    System.out.println(i.getIRI().getFragment());
                                }
                            }
                        }*/

                            textView.post(new Runnable() {
                                public void run() {
                                    textView.setText("");
                                }
                            });
                            //get all classes from ontology
                            Set<OWLClass> classes = ontology.getClassesInSignature();
                            for (final OWLClass class_ : classes) {
                                //NodeSet<OWLClass> ciao = hermitReasoner.getSuperClasses(class_, false);
                                textView.post(new Runnable() {
                                    public void run() {
                                        textView.setText(textView.getText() + class_.getIRI().getFragment() + "\n");
                                    }
                                });
                            }

                            //get all property from ontology
                            Set<OWLObjectProperty> ciao3 = ontology.getObjectPropertiesInSignature();


                            Node<OWLClass> ciao5 = hermitReasoner.getUnsatisfiableClasses();


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
}
