package com.wang.web.it;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.MultiValueMap;

import com.wang.utils.crypto.DES3;

//import com.htche.security.authentication.filter.EnhancedBasicAuthenticationFilter.IEncryptionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@IntegrationTest({ "server.port:0", "authentication.filter.captcha:false",
		"authentication.filter.captcha.minAcceptedWordLength:1",
		"authentication.filter.captcha.maxAcceptedWordLength:1", "authentication.filter.captcha.randomWords:0" })
@WebAppConfiguration
@DirtiesContext
public abstract class IntegrationTestBase {
	@Value("${authentication.filter.enhanced_basic:true}") boolean enhancedBasic;

	//@Autowired(required = false) IEncryptionManager encryptionManager;

	public String encrypt(String str) throws Exception {
		return DES3.encrypt("default_key", str);
	}

	public abstract String getUsername();

	public abstract String getPassword();

	public boolean getEnhancedBasic() {
		return enhancedBasic;
	}

	/**
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	protected HttpClient getSslHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
		HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(getSSLSocketFactory()).build();

		return httpClient;
	}

	/**
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	protected SSLConnectionSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
		SSLConnectionSocketFactory sslsf = null;
		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			sslsf = new SSLConnectionSocketFactory(builder.build(),
					SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		}
		catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}

		return sslsf;
	}

	/**
	 * @return
	 */
	protected TestRestTemplate getRestTemplate() {
		TestRestTemplate testRestTemplate = null;
		if (getEnhancedBasic()) {
			testRestTemplate = new TestRestTemplate();
		}
		else {
			testRestTemplate = new TestRestTemplate(getUsername(), getPassword());

		}
		return testRestTemplate;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private HttpEntity<Void> getHttpEntity() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		if (getEnhancedBasic()) {
			headers.add("Authorization", "Basic " + encrypt(getUsername() + ":" + getPassword()));
		}
		return new HttpEntity<Void>(headers);
	}

	private HttpHeaders getHttpHeaders() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		if (getEnhancedBasic()) {
			headers.add("Authorization", "Basic " + encrypt(getUsername() + ":" + getPassword()));
		}
		return headers;
	}

	private HttpEntity<?> getHttpEntity(MultiValueMap<String, String> form) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		if (getEnhancedBasic()) {
			headers.add("Authorization", "Basic " + encrypt(getUsername() + ":" + getPassword()));
		}
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(form,
				headers);
		return httpEntity;
	}

	/**
	 * @return
	 */
	protected TestRestTemplate getRestTemplate(String username, String password) {
		TestRestTemplate testRestTemplate = new TestRestTemplate(username, password);

		return testRestTemplate;
	}

	/**
	 * @return
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	protected TestRestTemplate getSSLRestTemplate() throws KeyManagementException, NoSuchAlgorithmException {
		TestRestTemplate testRestTemplate = new TestRestTemplate(getUsername(), getPassword());
		((HttpComponentsClientHttpRequestFactory) testRestTemplate.getRequestFactory())
				.setHttpClient(getSslHttpClient());

		return testRestTemplate;
	}

	/**
	 * @param testRestTemplate
	 * @param url
	 * @param method
	 * @param requestEntity
	 * @param returnType
	 * @param redirect
	 * @return
	 */
	protected <T> ResponseEntity<T> request(TestRestTemplate testRestTemplate, String url, HttpMethod method,
			HttpEntity<?> requestEntity, Class<T> returnType, boolean redirect) {
		ResponseEntity<T> responseEntity = testRestTemplate.exchange(url, method, requestEntity, returnType);

		if (responseEntity.getStatusCode().equals(HttpStatus.FOUND)) {
			if (redirect) {
				String location = null;
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.putAll(requestEntity.getHeaders());//new HttpHeaders();
				HttpHeaders responseHeaders = responseEntity.getHeaders();
				Iterator<Entry<String, List<String>>> items = responseHeaders.entrySet().iterator();
				while (items.hasNext()) {
					Entry<String, List<String>> item = items.next();
					if (item.getKey().equals("Set-Cookie")) {
						requestHeaders.put("Cookie", item.getValue());
					}
					if (item.getKey().equals("Location")) {
						location = item.getValue().get(0);
					}
				}

				if (null != location) {
					requestEntity = new HttpEntity<Object>(requestEntity.getBody(), requestHeaders);
					responseEntity = request(testRestTemplate, location, HttpMethod.GET, requestEntity, returnType,
							redirect);
				}
			}
		}
		return responseEntity;
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> get(String url) throws Exception {
		return (ResponseEntity<T>) request(getRestTemplate(), url, HttpMethod.GET, getHttpEntity(), String.class,
				false);
	}

	protected <T> ResponseEntity<T> get(String url, Class<T> returnType) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.GET, new HttpEntity<Void>(getHttpHeaders()), returnType,
				false);
	}

	protected <T> ResponseEntity<T> get(String url, Class<T> returnType, boolean redirect) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.GET, getHttpEntity(), returnType, redirect);
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form) throws Exception {
		return (ResponseEntity<T>) request(getRestTemplate(), url, HttpMethod.POST, getHttpEntity(form), String.class,
				true);
	}

	protected <T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form, Class<T> returnType)
			throws Exception {
		return request(getRestTemplate(), url, HttpMethod.POST, getHttpEntity(form), returnType, true);
	}

	protected <T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form, Class<T> returnType,
			boolean redirect) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.POST, getHttpEntity(form), returnType, redirect);
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form) throws Exception {
		return (ResponseEntity<T>) request(getRestTemplate(), url, HttpMethod.DELETE, getHttpEntity(form), String.class,
				true);
	}

	protected <T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form, Class<T> returnType)
			throws Exception {
		return request(getRestTemplate(), url, HttpMethod.DELETE, getHttpEntity(form), returnType, true);
	}

	protected <T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form, Class<T> returnType,
			boolean redirect) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.DELETE, getHttpEntity(form), returnType, redirect);
	}

	@SuppressWarnings("unchecked")
	protected <T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form) throws Exception {
		return (ResponseEntity<T>) request(getRestTemplate(), url, HttpMethod.PUT, getHttpEntity(form), String.class,
				true);
	}

	protected <T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form, Class<T> returnType)
			throws Exception {
		return request(getRestTemplate(), url, HttpMethod.PUT, getHttpEntity(form), returnType, true);
	}

	protected <T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form, Class<T> returnType,
			boolean redirect) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.PUT, getHttpEntity(form), returnType, redirect);
	}

}
