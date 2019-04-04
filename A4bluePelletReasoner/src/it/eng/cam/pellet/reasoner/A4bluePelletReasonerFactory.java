package it.eng.cam.pellet.reasoner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory.QueryEngineType;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
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
public class A4bluePelletReasonerFactory {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(
			A4bluePelletReasonerFactory.class);

	/**
	 * static reference
	 */
	private static A4bluePelletReasonerFactory reasoner = null;
	
	/**
	 * @constructor for this calss
	 */
	private A4bluePelletReasonerFactory (){
	}
	
	/**
	 * get static instance of this class
	 * @return
	 */
	public static A4bluePelletReasonerFactory getInstance() {
		if (reasoner == null) {
			reasoner = new A4bluePelletReasonerFactory();
		}
		return reasoner;
	}
	
	/**
	 * To execute inference SPARQL Query on OWL Ontology. The level of reasoning is OWL_DL_MEM
	 * @param sparqlQuery - Query SPARQL to search results on inference
	 * @return JSON Object
	 * @throws Exception
	 */
	public String executeInferenceQuery(String documentURI, String sparqlQuery)
			throws Exception {
		log.info("Method executeInferenceQuery INIT");
		QueryExecution qe = null;
		ByteArrayOutputStream outputStream = null;
		String json = null;
		try {
			log.info("documentIRI --> " + documentURI);
			log.info("sparqlQuery --> " + sparqlQuery);
			
			// create Pellet reasoner
			Reasoner reasoner = PelletReasonerFactory.theInstance().create();
			
			OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
			model.read(documentURI);
			log.info("Ontology Model created with success!");
			model.prepare();
			
			log.info("Model Successfull prepared!");
			
			//bind the reasoner to the ontology model
			reasoner = reasoner.bindSchema(model);
			
			//Bind the reasoner to the data model into a new Inferred model
			Model infModel = ModelFactory.createInfModel(reasoner,model);
			
			Query q = QueryFactory.create(sparqlQuery);
			// Create a SPARQL-DL query execution for the given query and
			// ontology model
			qe = SparqlDLExecutionFactory.create(q, DatasetFactory.create(infModel), null, QueryEngineType.MIXED);
			//qe = QueryExecutionFactory.create(q, infModel);

			// We want to execute a SELECT query, do it, and return the result
			// set
			ResultSet rs = qe.execSelect();

			log.info("Inferred Statement execute with Success!");
			
			// write to a ByteArrayOutputStream
			outputStream = new ByteArrayOutputStream();

			ResultSetFormatter.outputAsJSON(outputStream, rs);

			// and turn that into a String
			json = new String(outputStream.toByteArray());
			
			
			if (json != null)
				log.info("Reasoner JSON Results returned! ");
			// log.info("Reasoner JSON Results --> " + json);
			
			outputStream.close();
			outputStream = null;
		} catch (RuntimeException e) {
			log.info("Unable to inferred statement", e);
		} catch (Exception e) {
			log.info("Unable to inferred statement", e);
			throw new Exception(e.getMessage());
		} finally {
			if (qe != null)
				qe.close();
			if (outputStream != null)
				try {
					outputStream.close();
				} catch (IOException e) {
					log.warn("Unable to close ByteArrayOutputStream: " + e.getMessage());
				}
		}
		
		return json;
		
	}
	
}
