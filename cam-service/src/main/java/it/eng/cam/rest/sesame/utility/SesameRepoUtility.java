package it.eng.cam.rest.sesame.utility;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

public class SesameRepoUtility {

	private static final Logger logger = Logger
			.getLogger(SesameRepoUtility.class.getName());
	public static ResourceBundle finder = null;

	private static String SESAME_REPO_URL = null;
	private static String SESAME_REPO_NAME = null;

	private static SesameRepoUtility instance = null;

	static {
		try {
			finder = ResourceBundle.getBundle("cam-service");
			SESAME_REPO_URL = finder.getString("sesame.url");
			SESAME_REPO_NAME = finder.getString("sesame.repository");
		} catch (MissingResourceException e) {
			logger.warn(e);
		}
	}

	private SesameRepoUtility() {
	}

	public static SesameRepoUtility getInstance() {
		if (instance == null) {
			instance = new SesameRepoUtility();
		}
		return instance;
	}

	/**
	 * Add file RDF to REPO
	 * 
	 * @param rdfFile
	 * @param baseUri
	 * @param forceAdd
	 * @throws Exception 
	 */
	public void addRdfFileToRepo(File rdfFile, String baseUri, boolean forceAdd)
			throws Exception {
		logger.info("Method addRdfFileToRepo INIT");
		if (logger.isDebugEnabled()) {
			logger.debug("RDF File Name: " + rdfFile.getName());
			logger.debug("RDF File Path: " + rdfFile.getPath());
		}
		Repository repo = null;
		RepositoryConnection con = null;
		try {
			repo = new HTTPRepository(SESAME_REPO_URL, SESAME_REPO_NAME);
			repo.initialize();
			if (logger.isDebugEnabled())
				logger.debug("Repo Initialize");

			con = repo.getConnection();
			con.begin(IsolationLevels.SERIALIZABLE);
			if (logger.isDebugEnabled())
				logger.debug("Transaction in begin");
			
			if (null == baseUri || baseUri.isEmpty()) {
				baseUri = "file://" + rdfFile.getAbsolutePath();
				if (logger.isDebugEnabled())
					logger.debug("File baseUri: --> " + baseUri);
			}

			con.add(rdfFile, baseUri, RDFFormat.RDFXML);
			if (logger.isDebugEnabled())
				logger.debug("Add RDF File operation performed");
			
			con.commit();
			logger.info("Commit operation performed!");

		} catch (RepositoryException e) {	
			logger.error("Unable to commit SPARQL Update transaction", e);
			if (con !=  null) {
				con.rollback();
				if (logger.isDebugEnabled())
					logger.debug("Rollback transaction");
			}
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			logger.error("Unable to perform SPARQL Update operation", e);
			if (con !=  null && con.isActive()) {
				if (logger.isDebugEnabled())
					logger.debug("Rollback transaction");
				con.rollback();
			}
			throw new Exception(e.getMessage());
		} finally {
			try {
				if (con != null) {
					if (logger.isDebugEnabled())
						logger.debug("Connection closed");
					con.close();
				}
				if (repo != null) {
					if (logger.isDebugEnabled())
						logger.debug("Repo shutDown");
					repo.shutDown();
				}
			} catch (RepositoryException e) {
				logger.error("Unable to close rdf4j connection to the repo", e);
			}
		}

	}
	
	/**
	 * Execute a SPARQL Update on REPO
	 * @param sparqlUpdate
	 * @throws Exception
	 */
	public void sparqlUpdate(String sparqlUpdate)
			throws Exception {
		logger.info("Method sparqlUpdate INIT");
		if (logger.isDebugEnabled())
			logger.debug("SPARQL Update Statement: " + sparqlUpdate);
		Repository repo = null;
		RepositoryConnection con = null;
		try {
			repo = new HTTPRepository(SESAME_REPO_URL, SESAME_REPO_NAME);
			repo.initialize();
			if (logger.isDebugEnabled())
				logger.debug("Repo Initialize");

			con = repo.getConnection();
			con.begin(IsolationLevels.SERIALIZABLE);
			if (logger.isDebugEnabled())
				logger.debug("Transaction in begin");
			
			Update update = con.prepareUpdate(QueryLanguage.SPARQL, sparqlUpdate); 
			update.execute();
			if (logger.isDebugEnabled())
				logger.debug("Execute Update performed");
			
			con.commit();
			logger.info("Commit operation performed!");
			
		} catch (RepositoryException e) {	
			logger.error("Unable to commit SPARQL Update transaction", e);
			if (con !=  null) {
				con.rollback();
				if (logger.isDebugEnabled())
					logger.debug("Rollback transaction");
			}
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			logger.error("Unable to perform SPARQL Update operation", e);
			if (con !=  null && con.isActive()) {
				if (logger.isDebugEnabled())
					logger.debug("Rollback transaction");
				con.rollback();
			}
			throw new Exception(e.getMessage());
		} finally {
			try {
				if (con != null) {
					if (logger.isDebugEnabled())
						logger.debug("Connection closed");
					con.close();
				}
				if (repo != null) {
					if (logger.isDebugEnabled())
						logger.debug("Repo shutDown");
					repo.shutDown();
				}
			} catch (RepositoryException e) {
				logger.error("Unable to close rdf4j connection to the repo", e);
			}
		}

	}


}
