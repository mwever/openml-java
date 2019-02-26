package apiconnector;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xstream.XstreamXmlMapping;

import com.thoughtworks.xstream.XStream;

public class TestBase {
	
	protected static final String url_test = "https://test.openml.org/";
	protected static final String url_live = "https://www.openml.org/";
	protected static final OpenmlConnector client_admin_test = new OpenmlConnector(url_test,"d488d8afd93b32331cf6ea9d7003d4c3"); 
	protected static final OpenmlConnector client_write_test = new OpenmlConnector(url_test, "8baa83ecddfe44b561fd3d92442e3319");
	protected static final OpenmlConnector client_read_test = new OpenmlConnector(url_test, "c1994bdb7ecb3c6f3c8f3b35f4b47f1f"); 
	protected static final OpenmlConnector client_read_live = new OpenmlConnector(url_live, "c1994bdb7ecb3c6f3c8f3b35f4b47f1f"); 
	
	protected static final XStream xstream = XstreamXmlMapping.getInstance();
	
}
