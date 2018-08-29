import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.junit.Test;

import com.bigdata.rdf.sail.webapp.client.DefaultHttpClientFactory;

public class SparqlEndpointTest {

	@Test
	public void test() throws Exception {
		
		final DefaultHttpClientFactory defaultFactory = new DefaultHttpClientFactory();
		final HttpClient client = defaultFactory.newInstance();
		
//		client.GET("https://ld.stadt-zuerich.ch/query");

		Request request = client.newRequest("https://ld.stadt-zuerich.ch/query");		
		new RequestLogger().listenTo(request).send();	
	}

}
