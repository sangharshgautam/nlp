package uk.co.sangharsh.nlp.resource;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class NlpApplication extends ResourceConfig{
//	private static final Logger LOGGER = Logger.getLogger(Gennie.class.getName());
	public NlpApplication() {
		super();
		packages(NlpResource.class.getPackage().getName());
		register(MoxyJsonFeature.class);
		register(LoggingFilter.class);
    }
}
