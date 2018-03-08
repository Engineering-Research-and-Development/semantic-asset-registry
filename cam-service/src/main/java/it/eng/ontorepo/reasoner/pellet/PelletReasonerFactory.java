package it.eng.ontorepo.reasoner.pellet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;

/**
 * 
 * @author nsvulcan
 *
 */
public class PelletReasonerFactory {

	/**logger for this class **/
	private static final Logger logger = LogManager.getLogger(
			PelletReasonerFactory.class.getName());

	public static ResourceBundle finder = null;
    private static String SESAME_REPO_URL = null;
    private static String SESAME_REPO_NAME = null;
    private static String SESAME_DOCUMENT_IRI = null;
    
    static {
        try {
            finder = ResourceBundle.getBundle("cam-service");
            SESAME_REPO_URL = finder.getString("sesame.url");
            SESAME_REPO_NAME = finder.getString("sesame.repository");
            SESAME_DOCUMENT_IRI = SESAME_REPO_URL + "repositories/" + SESAME_REPO_NAME + "/statements";
        } catch (MissingResourceException e) {
            logger.warn(e);
        }
    }

	/**
	 * static reference
	 */
	private static PelletReasonerFactory reasoner = null;
	
	/**
	 * @constructor for this calss
	 */
	private PelletReasonerFactory (){
	}
	
	/**
	 * get static instance of this class
	 * @return
	 */
	public static PelletReasonerFactory getInstance() {
		if (reasoner == null) {
			reasoner = new PelletReasonerFactory();
		}
		return reasoner;
	}
	
	/**
	 * To execute inference SPARQL Query on OWL Ontology. The level of reasoning is OWL_DL_MEM
	 * @param sparqlQuery - Query SPARQL to search results on inference
	 * @return JSON Object
	 * @throws Exception
	 */
	public String executeInferenceQuery(String sparqlQuery) throws Exception {
		logger.info("Method executeInferenceQuery INIT");
		
		if (logger.isDebugEnabled()) {
			logger.debug("documentIRI --> " + SESAME_DOCUMENT_IRI);
			logger.debug("sparqlQuery --> " + sparqlQuery);
		}
		
		QueryExecution qe = null;
		ByteArrayOutputStream outputStream = null;
		String json = null;
		try {
			// create Pellet reasoner
			Reasoner reasoner = org.mindswap.pellet.jena.PelletReasonerFactory.theInstance().create();
			
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
			model.read(SESAME_DOCUMENT_IRI);
			logger.info("Ontology Model READ from REPO");
			model.prepare();
			
			logger.info("Model Successfull prepared!");
			
			//bind the reasoner to the ontology model
			reasoner = reasoner.bindSchema(model);
			
			//Bind the reasoner to the data model into a new Inferred model
			Model infModel = ModelFactory.createInfModel(reasoner,model);
			
			Query q = QueryFactory.create(sparqlQuery);
			// Create a SPARQL-DL query execution for the given query and
			// ontology model
			qe = SparqlDLExecutionFactory.create(q, infModel);

			// We want to execute a SELECT query, do it, and return the result
			// set
			ResultSet rs = qe.execSelect();

			logger.info("Inferred Statement execute with Success!");
			
			// write to a ByteArrayOutputStream
			outputStream = new ByteArrayOutputStream();

			ResultSetFormatter.outputAsJSON(outputStream, rs);

			// and turn that into a String
			json = new String(outputStream.toByteArray());
			
			if (logger.isDebugEnabled())
				logger.debug("Reasoner JSON Results --> " + json);
			
			outputStream.close();
			outputStream = null;
			
		} catch (Exception e) {
			logger.error("Unable to inferred statement", e);
			throw new Exception(e.getMessage());
		} finally {
			if (qe != null)
				qe.close();
			if (outputStream != null)
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.warn("Unable to close ByteArrayOutputStream: " + e.getMessage());
				}
		}
		
		return json;
		
	}
	
}
