package it.eng.ontorepo.sesame2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.base.AbstractRepository;

public class LoadNamespaceSingleton {

	private static LoadNamespaceSingleton instance;
	private Map<String,String> uriNamespacesMap = null;
	
	private LoadNamespaceSingleton(AbstractRepository repo) {
		super();

		RepositoryConnection con = null;
		con = repo.getConnection();

		RepositoryResult<Namespace> rr = con.getNamespaces();

		this.uriNamespacesMap = new HashMap<String,String>();
	
		while (rr.hasNext()) {
			Namespace n = rr.next();
			uriNamespacesMap.put(n.getName(), n.getPrefix());
		}
	}

	public static LoadNamespaceSingleton getInstance(AbstractRepository repo) {
		if(instance == null) {
			instance = new LoadNamespaceSingleton(repo);
		}
		return instance;
	}
	
	public boolean existURI(String uri) {
		return uriNamespacesMap.containsKey(uri);
	}
	
	public boolean existNamespace(String namespace) {
		return uriNamespacesMap.containsValue(namespace);
	}
	
	public String getNamespace(String uri) {
		return uriNamespacesMap.get(uri);
	}
	
	public String getUri(String namespace) {
		Set<Entry<String, String>> es = uriNamespacesMap.entrySet();
		for (Entry<String, String> entry : es) {
			if(entry.getValue().equals(namespace)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
}
