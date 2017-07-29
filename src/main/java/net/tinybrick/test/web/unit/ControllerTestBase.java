package net.tinybrick.test.web.unit;

import org.json.JSONObject;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class ControllerTestBase {
	public static enum POST_DATA_POSITION{
		HEADER, BODY
	}

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

	// GET
	public ResultActions GET(String url, MediaType content, MediaType accept) throws Exception {
		MockHttpServletRequestBuilder requestBuilder = get(url).session(session).contentType(content)
				.headers(this.httpHeaders).accept(accept);
		ResultActions result = mockMvc.perform(requestBuilder);
		return result;
	}

	// DELETE
	public ResultActions DELETE(String url, MediaType content, MediaType accept) throws Exception {
		MockHttpServletRequestBuilder requestBuilder = get(url).session(session).contentType(content)
				.headers(this.httpHeaders).accept(accept);
		ResultActions result = mockMvc.perform(requestBuilder);
		return result;
	}

	// POST
	public ResultActions POST(String url, MediaType contentType, MediaType accept) throws Exception {
		return POST(url, contentType, accept, null);
	}

	public ResultActions POST(String url,
								 MediaType contentType,
								 MediaType accept,
								 Map<String, Object> content) throws Exception {
		return POST(url, contentType, accept, content, POST_DATA_POSITION.BODY);
	}

	public ResultActions POST(String url,
										 MediaType contentType,
										 MediaType accept,
										 Map<String, Object> content,
										 POST_DATA_POSITION position)
			throws Exception {
		MockHttpServletRequestBuilder requestBuilder = post(url).session(session).contentType(contentType)
				.headers(httpHeaders).accept(accept);
		if(null != content) {
			if(position == POST_DATA_POSITION.BODY) {
				String contentStr = JSONObject.valueToString(content);
				requestBuilder.content(contentStr);
			}
			else if(position == POST_DATA_POSITION.HEADER){
				for (String key : content.keySet()) {
					requestBuilder.param(key, content.get(key).toString());
				}
			}
			else
				throw new UnsupportedOperationException("You can't put data at here");
		}
		ResultActions result = mockMvc.perform(requestBuilder);
		return result;
	}

	// PUT
	public ResultActions PUT(String url, MediaType contentType, MediaType accept) throws Exception {
		return PUT(url, contentType, accept, null);
	}

	public ResultActions PUT(String url,
								 MediaType contentType,
								 MediaType accept,
								 Map<String, Object> content) throws Exception {
		return PUT(url, contentType, accept, content, POST_DATA_POSITION.BODY);
	}

	public ResultActions PUT(String url,
								 MediaType contentType,
								 MediaType accept,
								 Map<String, Object> content,
								 POST_DATA_POSITION position)
			throws Exception {
		MockHttpServletRequestBuilder requestBuilder = put(url).session(session).contentType(contentType)
				.headers(httpHeaders).accept(accept);
		if(null != content) {
			if(position == POST_DATA_POSITION.BODY) {
				String contentStr = JSONObject.valueToString(content);
				requestBuilder.content(contentStr);
			}
			else if(position == POST_DATA_POSITION.HEADER){
				for (String key : content.keySet()) {
					requestBuilder.param(key, content.get(key).toString());
				}
			}
			else
				throw new UnsupportedOperationException("You can't put data at here");
		}
		ResultActions result = mockMvc.perform(requestBuilder);
		return result;
	}
}
