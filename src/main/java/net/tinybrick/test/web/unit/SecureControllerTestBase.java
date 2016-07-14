package net.tinybrick.test.web.unit;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class SecureControllerTestBase extends ControllerTestBase {
	@Before
	public void setUp() throws Exception {
		super.setUp();
		httpHeaders.add("username", getUsername());
		httpHeaders.add("password", getPassword());

		MvcResult result = POST("/login", MediaType.ALL, MediaType.ALL).andReturn();
		//Cookie[] cookies = result.getResponse().getCookies();
	}
}
