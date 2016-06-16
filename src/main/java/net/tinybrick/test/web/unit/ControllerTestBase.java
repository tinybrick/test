package net.tinybrick.test.web.unit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class ControllerTestBase {

	@Autowired protected WebApplicationContext webApplicationContext;
	@Autowired protected MockHttpSession session;

	public String getUsername() {
		return "user";
	}

	public String getPassword() {
		return "user";
	}

	protected MockMvc mockMvc;
	protected HttpHeaders httpHeaders = new HttpHeaders();

	//protected Cookie[] cookies;

	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	protected ResultActions GET(String url, MediaType content, MediaType accept) throws Exception {
		MockHttpServletRequestBuilder requestBuilder = get(url).session(session).contentType(content).accept(accept);
		ResultActions result = mockMvc.perform(requestBuilder);
		return result;
	}

	protected ResultActions POST(String url, MediaType content, MediaType accept) throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post(url).session(session).contentType(content)
				.headers(httpHeaders).accept(accept);
		ResultActions result = mockMvc.perform(requestBuilder);
		return result;
	}

	protected ResultActions POST(String url, MediaType contentType, MediaType accept, Map<String, Object> content)
			throws Exception {
		String contentStr = JSONObject.toJSONString(content);
		MockHttpServletRequestBuilder requestBuilder = post(url).session(session).contentType(contentType)
				.content(contentStr).headers(httpHeaders).accept(accept);
		ResultActions result = mockMvc.perform(requestBuilder);
		return result;
	}

	protected ResultActions POSTByParams(String url, MediaType contentType, MediaType accept,
			Map<String, Object> params) throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post(url).session(session).contentType(contentType)
				.headers(httpHeaders).accept(accept);
		for (String key : params.keySet()) {
			requestBuilder.param(key, params.get(key).toString());
		}
		ResultActions result = mockMvc.perform(requestBuilder);
		return result;
	}
}
