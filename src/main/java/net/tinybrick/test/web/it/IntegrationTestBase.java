package net.tinybrick.test.web.it;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import net.tinybrick.utils.rest.RestClient;

//import com.htche.security.authentication.filter.EnhancedBasicAuthenticationFilter.IEncryptionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest({ "server.port:0", "authentication.filter.captcha:false",
		"authentication.filter.captcha.minAcceptedWordLength:1",
		"authentication.filter.captcha.maxAcceptedWordLength:1", "authentication.filter.captcha.randomWords:0" })
@WebAppConfiguration
@DirtiesContext
public abstract class IntegrationTestBase extends RestClient {
	public @Value("${local.server.port:0}") int port;
	public @Value("${authentication.filter.enhanced_basic:true}") boolean enhancedBasic;

	protected boolean getEnhancedBasic() {
		return enhancedBasic;
	}
}
