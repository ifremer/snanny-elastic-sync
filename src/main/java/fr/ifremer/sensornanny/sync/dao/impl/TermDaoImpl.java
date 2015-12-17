package fr.ifremer.sensornanny.sync.dao.impl;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.skosapibinding.SKOSManager;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.dao.ITermDao;
import fr.ifremer.sensornanny.sync.dto.model.Term;
import uk.ac.manchester.cs.skos.SKOSDatasetImpl;

/**
 * Implementation of term DAO using tematres
 * 
 * @author athorel
 *
 */
public class TermDaoImpl implements ITermDao {

    private static final String SKOS_PARAMETER = "skosTema";
    private static final String NOTATION = "notation";
    private static final String PREF_LABEL = "prefLabel";

    private static final Logger LOGGER = Logger.getLogger(TermDaoImpl.class.getName());

    @Override
    public Term getTerm(String id) {

        try {
            Term term = null;
            SKOSManager manager = new SKOSManager();
            UriComponents uriComponent = UriComponentsBuilder.fromHttpUrl(Config.tematresEndpoint()).queryParam(
                    SKOS_PARAMETER, id).build();
            manager.loadDataset(uriComponent.toUri());

            Collection<SKOSDatasetImpl> skosDataSets = manager.getSKOSDataSets();
            for (SKOSDatasetImpl skosDatasetImpl : skosDataSets) {
                term = new Term();
                Set<OWLAxiom> axioms = skosDatasetImpl.getAsOWLOntology().getAxioms();

                for (OWLAxiom axiom : axioms) {
                    if (axiom instanceof OWLDataPropertyAssertionAxiom) {
                        OWLDataPropertyAssertionAxiom dataProperty = (OWLDataPropertyAssertionAxiom) axiom;
                        OWLDataPropertyExpression property = dataProperty.getProperty();
                        if (PREF_LABEL.equals(property.toString())) {
                            term.setLabel(dataProperty.getObject().getLiteral());
                        }
                        if (NOTATION.equals(property.toString())) {
                            term.setNotation(dataProperty.getObject().getLiteral());
                        }
                    }
                }
            }
            return term;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to retrieve term for id", e);
            return null;
        }

    }

}
