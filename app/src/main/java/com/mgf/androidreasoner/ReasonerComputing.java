package com.mgf.androidreasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import static com.mgf.androidreasoner.MainActivity.HERMIT;

import java.util.HashSet;
import java.util.Set;



/**
 * Created by Mattia on 14/06/2018.
 */

public class ReasonerComputing {

    private int reasonerSelected;
    private OWLOntology ontology;
    private Reasoner hermitReasoner;
    private PelletReasoner pelletReasoner;

    public ReasonerComputing(int reasonerType, OWLOntology o) {
        this.reasonerSelected = reasonerType;
        this.ontology = o;
        hermitReasoner = new Reasoner(o);
        pelletReasoner = new PelletReasonerFactory().createReasoner(ontology);
    }

    private OWLClass classFromName(String classSelected) {
        for (final OWLClass c : getClasses()) {
            if (c.getIRI().getFragment().equals(classSelected)){
                return c;
            }
        }
        return null;
    }

    private OWLObjectProperty propFromName(String propSelected) {
        for (final OWLObjectProperty o : getProperties()) {
            if (o.getIRI().getFragment().equals(propSelected)){
                return o;
            }
        }
        return null;
    }

    private Set<OWLClass> getClassSetFromNodeSet(NodeSet<OWLClass> nodeSet) {
        Set<OWLClass> returnSet = new HashSet<>();
        for(Node node : nodeSet) {
            Set<OWLClass> set = node.getEntities();
            for (OWLClass o : set) {
                returnSet.add(o);
            }
        }
        return returnSet;
    }

    private Set<OWLObjectProperty> getPropSetFromNode(Node<OWLObjectPropertyExpression> node) {
        Set<OWLObjectProperty> returnSet = new HashSet<>();
        Set<OWLObjectPropertyExpression> set = node.getEntities();
        for(OWLObject owlProp : set) {
            if(OWLObjectProperty.class.isInstance(owlProp)) {
                OWLObjectProperty p = (OWLObjectProperty)owlProp;
                if(!p.getIRI().getFragment().equals("bottomObjectProperty")){
                    returnSet.add(p);
                }
            }
        }
        return returnSet;
    }

    private Set<OWLObjectProperty> getPropSetFromNodeSet(NodeSet<OWLObjectPropertyExpression> nodeSet) {
        Set<OWLObjectProperty> returnSet = new HashSet<>();
        for(Node node : nodeSet) {
            Set<OWLObject> set = node.getEntities();
            for(OWLObject owlProp : set) {
                if(OWLObjectProperty.class.isInstance(owlProp)) {
                    OWLObjectProperty p = (OWLObjectProperty)owlProp;
                    if(!p.getIRI().getFragment().equals("bottomObjectProperty")){
                        returnSet.add(p);
                    }
                }
            }
        }
        return returnSet;
    }

    public Set<OWLClass> getClasses() {
        return ontology.getClassesInSignature();
    }

    public Set<OWLClass> getUnsatisfiableClasses() {
        Node<OWLClass> unClasses;
        unClasses = reasonerSelected == HERMIT ? hermitReasoner.getUnsatisfiableClasses() : pelletReasoner.getUnsatisfiableClasses();
        return unClasses.getEntities();
    }

    public Set<OWLClass> getEquivalentClasses(String classSelected) {
        OWLClass class_ = classFromName(classSelected);
        return reasonerSelected == HERMIT ? hermitReasoner.getEquivalentClasses(class_).getEntities() : pelletReasoner.getEquivalentClasses(class_).getEntities();
    }

    public Set<OWLClass> getSubClasses(String classSelected) {
        OWLClass class_ = classFromName(classSelected);
        NodeSet<OWLClass> nodeSet = reasonerSelected == HERMIT ? hermitReasoner.getSubClasses(class_,false) : pelletReasoner.getSubClasses(class_,false);
        return getClassSetFromNodeSet(nodeSet);
    }

    public Set<OWLClass> getSuperClasses(String classSelected) {
        OWLClass class_ = classFromName(classSelected);
        NodeSet<OWLClass> nodeSet = reasonerSelected == HERMIT ? hermitReasoner.getSuperClasses(class_,false) : pelletReasoner.getSuperClasses(class_,false);
        return getClassSetFromNodeSet(nodeSet);
    }

    public boolean isSubClassOf(String classDadName, String classSonName) {
        OWLClass classDad = classFromName(classDadName);
        OWLClass classSon = classFromName(classSonName);
        return hermitReasoner.isSubClassOf(classSon,classDad);
    }

    public boolean isSatisfiable(String classSelected) {
        OWLClass class_ = classFromName(classSelected);
        return reasonerSelected == HERMIT ? hermitReasoner.isSatisfiable(class_) : pelletReasoner.isSatisfiable(class_);
    }

    public Set<OWLObjectProperty> getProperties() {
        return ontology.getObjectPropertiesInSignature();
    }

    public Set<OWLObjectProperty> getSubProperties(String propSelected) {
        OWLObjectProperty obj = propFromName(propSelected);
        NodeSet<OWLObjectPropertyExpression> nodeSet = reasonerSelected == HERMIT ? hermitReasoner.getSubObjectProperties(obj,false) : pelletReasoner.getSubObjectProperties(obj,false);
        return getPropSetFromNodeSet(nodeSet);
    }

    public Set<OWLObjectProperty> getSuperProperties(String propSelected) {
        OWLObjectProperty obj = propFromName(propSelected);
        NodeSet<OWLObjectPropertyExpression> nodeSet = reasonerSelected == HERMIT ? hermitReasoner.getSuperObjectProperties(obj,false) : pelletReasoner.getSuperObjectProperties(obj,false);
        return getPropSetFromNodeSet(nodeSet);
    }

    public Set<OWLObjectProperty> getInverseProperties(String propSelected) {
        OWLObjectProperty obj = propFromName(propSelected);
        Node<OWLObjectPropertyExpression> node = reasonerSelected == HERMIT ? hermitReasoner.getInverseObjectProperties(obj) : pelletReasoner.getInverseObjectProperties(obj);
        return getPropSetFromNode(node);
    }

    public boolean isSubPropertyOf(String propDadName, String propSonName) {
        OWLObjectProperty propDad = null, propSon = null;
        for (final OWLObjectProperty prop : getProperties()) {
            if (prop.getIRI().getFragment().equals(propSonName)){
                propSon = prop;
            }
            if(prop.getIRI().getFragment().equals(propDadName)){
                propDad = prop;
            }
        }
        return hermitReasoner.isSubObjectPropertyExpressionOf(propSon,propDad);
    }

}
