package eu.fbk.das.adaptation.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class CustomPrefixMapper extends NamespacePrefixMapper {

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion,
	    boolean requirePrefix) {

	if (namespaceUri
		.equals("http://www.allow-project.eu/bpel/APFextension"))
	    return "apf";
	if (namespaceUri.equals("http://www.allow-project.eu/Object"))
	    return "obj";
	if (namespaceUri.equals("http://www.allow-project.eu/Fragment"))
	    return "frgm";
	if (namespaceUri
		.equals("http://docs.oasis-open.org/wsbpel/2.0/process/executable"))
	    return "bpel";
	if (namespaceUri.equals("eu.fbk.soa.domain.ProcessDiagram"))
	    return "bpel";

	return "";

    }

}
