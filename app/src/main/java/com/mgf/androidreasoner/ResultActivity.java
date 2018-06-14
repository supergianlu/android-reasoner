package com.mgf.androidreasoner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Mattia on 14/06/2018.
 */

public class ResultActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private ReasonerComputing reasonerComputing;
    private String type;
    private Spinner spinner, spinnerC1, spinnerC2;
    private ListView listView;
    private TextView title, result;
    private String[] classActions = {"Visualizza le classi","Classi insoddisfacibili","Classi equivalenti a...",
            "Sottoclassi di...", "Superclassi di...", "C1 sottoclasse C2?","C è soddisfacibile?"};
    private String[] propActions = {"Visualizza le proprietà","Sottoproprietà di...","Superproprietà di...",
            "Visualizza proprietà inverse di...", "P1 è sottoproprietà di P2?"};
    private List classes = null;
    private List prop = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        type = intent.getStringExtra("TYPE");

        spinner = (Spinner) findViewById(R.id.spinnerActions);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter aa;
        if(type.equals("classi")) {
            aa = new ArrayAdapter(this,android.R.layout.simple_list_item_1, classActions);
        } else {
            aa = new ArrayAdapter(this,android.R.layout.simple_list_item_1, propActions);
        }
        spinner.setAdapter(aa);

        listView = (ListView) findViewById(R.id.listView);
        title = (TextView) findViewById(R.id.textViewType);
        title.setText("Analizza "+type);
        result = (TextView) findViewById(R.id.textViewBoolean);

        spinnerC1 = (Spinner) findViewById(R.id.spinnerChoice1);
        spinnerC1.setOnItemSelectedListener(this);
        spinnerC2 = (Spinner) findViewById(R.id.spinnerChoice2);
        spinnerC2.setOnItemSelectedListener(this);

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

        reasonerComputing = MainActivity.getReasonerComputing();
/*
        //get all classes from ontology
        Set<OWLClass> classes = reasonerComputing.getClasses();
        //getUnsatistiableClass
        Set<OWLClass> classesUnsatisfiable = reasonerComputing.getUnsatisfiableClasses();
        //getEquivalentClass
        String classSelected = "Capricciosa";
        Set<OWLClass> classesEquivalent = reasonerComputing.getEquivalentClasses(classSelected);
        //getSubClasses
        Set<OWLClass> subClasses = reasonerComputing.getSubClasses(classSelected);
        //getSuperClasses
        Set<OWLClass> superClasses = reasonerComputing.getSuperClasses(classSelected);
        //check if c1 is subClass of c2
        String classDadName = "NamedPizza", classSonName = "Capricciosa";
        Boolean isSubClassOf = reasonerComputing.isSubClassOf(classDadName,classSonName);
        //class is Satisfiable ?
        Boolean satisfiable = reasonerComputing.isSatisfiable(classSelected);


        //get all property from ontology
        Set<OWLObjectProperty> properties = reasonerComputing.getProperties();
        //getSubProperties
        String propSelected = "isIngredientOf";
        Set<OWLObjectProperty> subProperties = reasonerComputing.getSubProperties(propSelected);
        //getSuperProperties
        Set<OWLObjectProperty> superProperties = reasonerComputing.getSuperProperties(propSelected);
        //getInverseProperties
        Set<OWLObjectProperty> inverseProperties = reasonerComputing.getInverseProperties(propSelected);
        //check if p1 is subProperty of p2
        String propDadName = "isIngredientOf", propSonName = "isBaseOf";
        Boolean isSubPropertyOf = reasonerComputing.isSubPropertyOf(propDadName,propSonName);
*/
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View arg1, int position, long id) {
        List<String> list = new ArrayList<>();
        Set<OWLClass> resultSet = null;
        Set<OWLObjectProperty> resultObjSet = null;
        switch(parent.getId()) {
            case R.id.spinnerActions:
                if (type.equals("classi")) {
                    switch (position) {
                        case 0: //GET CLASSES
                            resultSet = reasonerComputing.getClasses();
                            spinnerC1.setVisibility(View.GONE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        case 1: //GET CLASSES UNSATISFIABLE
                            resultSet = reasonerComputing.getUnsatisfiableClasses();
                            spinnerC1.setVisibility(View.GONE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        case 3:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        case 4:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        case 5:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.VISIBLE);
                            result.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                            result.setText("");
                            break;
                        case 6:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                            result.setText("");
                            break;
                    }
                    if(position < 2) {
                        for(OWLClass o : resultSet) {
                            list.add(o.getIRI().getFragment());
                        }
                        if(position == 0){
                            classes = list;
                            ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_list_item_1, classes.toArray());
                            spinnerC1.setAdapter(aa);
                            spinnerC2.setAdapter(aa);
                        }
                    }
                } else {
                    switch (position) {
                        case 0:
                            resultObjSet = reasonerComputing.getProperties();
                            spinnerC1.setVisibility(View.GONE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            for(OWLObjectProperty o : resultObjSet) {
                                list.add(o.getIRI().getFragment());
                            }
                            prop = list;
                            ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_list_item_1, prop.toArray());
                            spinnerC1.setAdapter(aa);
                            spinnerC2.setAdapter(aa);
                            break;
                        case 1:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        case 3:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.GONE);
                            result.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            break;
                        case 4:
                            spinnerC1.setVisibility(View.VISIBLE);
                            spinnerC2.setVisibility(View.VISIBLE);
                            result.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                            break;
                    }
                }
                break;
            case R.id.spinnerChoice1:
                if (type.equals("classi")) {
                    switch (spinner.getSelectedItemPosition()) {
                        case 2:
                            resultSet = reasonerComputing.getEquivalentClasses((String)classes.get(position));
                            break;
                        case 3:
                            resultSet = reasonerComputing.getSubClasses((String)classes.get(position));
                            break;
                        case 4:
                            resultSet = reasonerComputing.getSuperClasses((String)classes.get(position));
                            break;
                        case 5:
                            Boolean b = reasonerComputing.isSubClassOf((String)spinnerC2.getSelectedItem(),(String)classes.get(position));
                            result.setText((String)classes.get(position)+" è sottoclasse di "+(String)spinnerC2.getSelectedItem()+": "+b);
                            break;
                        case 6:
                            result.setText(""+reasonerComputing.isSatisfiable((String)classes.get(position)));
                            break;
                    }
                    if(spinner.getSelectedItemPosition() < 5){
                        for(OWLClass o : resultSet) {
                            list.add(o.getIRI().getFragment());
                        }
                    }

                } else {
                    switch (spinner.getSelectedItemPosition()) {
                        case 1:
                            resultObjSet = reasonerComputing.getSubProperties((String)prop.get(position));
                            break;
                        case 2:
                            resultObjSet = reasonerComputing.getSuperProperties((String)prop.get(position));
                            break;
                        case 3:
                            resultObjSet = reasonerComputing.getInverseProperties((String)prop.get(position));
                            break;
                        case 4:
                            Boolean b = reasonerComputing.isSubPropertyOf((String)spinnerC2.getSelectedItem(),(String)prop.get(position));
                            result.setText((String)prop.get(position)+" è sottoproprietà di "+(String)spinnerC2.getSelectedItem()+": "+b);
                            break;
                    }
                    if(spinner.getSelectedItemPosition() < 4){
                        for(OWLObjectProperty o : resultObjSet) {
                            list.add(o.getIRI().getFragment());
                        }
                    }
                }
                break;
            case R.id.spinnerChoice2:
                if (type.equals("classi")) {
                    Boolean b = reasonerComputing.isSubClassOf((String)classes.get(position),(String)spinnerC1.getSelectedItem());
                    result.setText((String)spinnerC1.getSelectedItem()+" è sottoclasse di "+(String)classes.get(position)+": "+b);
                } else {
                    Boolean b = reasonerComputing.isSubPropertyOf((String)prop.get(position),(String)spinnerC1.getSelectedItem());
                    result.setText((String)spinnerC1.getSelectedItem()+" è sottoproprietà di "+(String)prop.get(position)+": "+b);
                }
                break;
        }
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list.toArray());
        listView.setAdapter(aa);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
