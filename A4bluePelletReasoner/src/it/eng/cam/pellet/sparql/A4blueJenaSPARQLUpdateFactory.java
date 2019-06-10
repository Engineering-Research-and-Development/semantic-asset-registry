package it.eng.cam.pellet.sparql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

public class A4blueJenaSPARQLUpdateFactory {

	/** local logger for this class **/
	private static Log log = LogFactory.getLog(
			A4blueJenaSPARQLUpdateFactory.class);
	
	public static final String TURTLE = "TURTLE";

	public static final String CONTENT_TYPE = "Content-Type";
	
	public static final String ACCEPT_FORMAT = "application/rdf+xml;charset=UTF-8";
	
	/**
	 * Execute an an SPARQL Update on Inference Model 
	 * @param documentURI SPARQL endpoint for download RDF4J Model from Repository
	 * @param sparqlUpdate SPARQL Update Statement
	 * @return New model containing the new SPARQL Statement
	 * @throws Exception
	 */
	public String executeInferenceUpdate(String documentURI, String sparqlUpdate)
			throws Exception {
		log.info("Method executeInferenceUpdate INIT");
		
		ByteArrayOutputStream outputStream = null;
		String rdfModel = null;
		
		try {
			log.info("documentIRI --> " + documentURI);
			log.info("sparqlUpdate --> " + sparqlUpdate);
			
			// create Pellet reasoner
			Reasoner reasoner = PelletReasonerFactory.theInstance().create();

			OntModel model = ModelFactory
					.createOntologyModel(OntModelSpec.OWL_DL_MEM);
			model.read(documentURI, null, TURTLE);
			log.info("Ontology Model created with success!");
			model.prepare();

			// bind the reasoner to the ontology model
			reasoner = reasoner.bindSchema(model);

			// Bind the reasoner to the data model into a new Inferred model
			InfModel infModel = ModelFactory.createInfModel(reasoner, model);

			// SPARQL Update
			GraphStore graphStore = GraphStoreFactory.create(infModel);
			// Create a new update Request
			UpdateRequest request = UpdateFactory.create(sparqlUpdate);
			// execute update on graph
			UpdateAction.execute(request, graphStore);

			log.info("SPARQL Update performed on Inference Model");

			infModel.rebind();
			infModel.reset();

			outputStream = new ByteArrayOutputStream();
			RDFDataMgr.write(outputStream, model.getRawModel(), RDFFormat.RDFXML);
			log.info("RDF Model write with success: New Model contains new SPARQL Statement updated");

			rdfModel = new String(outputStream.toByteArray(), "UTF-8");
			
            if (log.isDebugEnabled())
            	log.debug("NEW RDF Model: " + rdfModel);
			
		} catch (RuntimeException e) {
			log.info("Unable to inferred statement", e);
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			log.info("Unable to inferred statement", e);
			throw new Exception(e.getMessage());
		} finally {
			if (outputStream != null)
				try {
					outputStream.close();
				} catch (IOException e) {
					log.warn("Unable to close ByteArrayOutputStream: " + e.getMessage());
				}
		}
		log.info("Method executeInferenceUpdate END");
		
		return rdfModel;
		
	}
	
	/**
	 * Updates data in the repository, replacing any existing data with the
	 * supplied data. The data supplied with this request is expected to contain
	 * an RDF document in one of the supported RDF formats.
	 * 
	 * @param documentURI SPARQL endpoint to perform operation
	 * @param rdfModel RDF Model to PUT
	 * @throws Exception
	 */
	public boolean putRDFData(String documentURI, String rdfModel) throws Exception {
		log.info("Method putRDFData INIT");
		
		boolean result = false;
		
		try {
			Client client = ClientBuilder.newClient();

			WebTarget webTarget = client.target(documentURI);

			Invocation.Builder invocationBuilder = webTarget.request();
			invocationBuilder.header(CONTENT_TYPE,
					ACCEPT_FORMAT);

			Response response = invocationBuilder.put(Entity.entity(rdfModel,
					ACCEPT_FORMAT));
			
			log.info("HTTP Response STATUS: " + response.getStatus());
			
			if (null == response || response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
	            log.error("Error PUT Data to RDF4J Repository: " + response.getStatus());
	        } else {
	        	log.info("Result PUT Data to RDF4J Repository: " + response.readEntity(String.class));
	        	result = true;
	        }
			
		} catch (RuntimeException e) {
			log.info("Unable to inferred statement", e);
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			log.info("Unable to inferred statement", e);
			throw new Exception(e.getMessage());
		} 

		log.info("Method putRDFData END");
		
		return result;
	}

}
