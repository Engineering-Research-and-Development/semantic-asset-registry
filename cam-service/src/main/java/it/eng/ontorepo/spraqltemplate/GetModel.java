package it.eng.ontorepo.spraqltemplate;




public class GetModel {
	
	public GetModel() {
	}

	public String querystring(String Class, String SubClass) {
		
		String PrefixB2MML = "prefix b2mml: <http://www.mesa.org/xml/B2MML-V0600#> ";
		String PrefixVAR = "prefix var:  <http://a4blue/ontologies/var.owl#> ";
		String PrefixRDFS ="prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";
		String PrefixOWL ="PREFIX owl:  <http://www.w3.org/2002/07/owl#> ";
		String Select ="SELECT DISTINCT ?o ";
		String StartWhere ="WHERE { ";
		String arg1 ="{var:"+SubClass+" rdfs:subClassOf ?object . ?object owl:onProperty ?o .} ";
		String Union ="UNION ";
		String arg2 ="{var:"+Class+" rdfs:subClassOf ?object . ?object owl:onProperty ?o .} ";
		String EndWhere = "} ";
				
		return PrefixB2MML+PrefixVAR+PrefixRDFS+PrefixOWL+Select+StartWhere+arg1+Union+arg2+EndWhere;
	}
	
}
