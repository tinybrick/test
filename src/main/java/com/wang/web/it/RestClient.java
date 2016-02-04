package com.wang.web.it;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.wang.utils.crypto.Codec;

public abstract class RestClient implements IRestClient {
	protected String encrypt(String str) throws Exception {
		return Codec.stringToBase64(str);
	}

	protected abstract boolean getEnhancedBasic();

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

	protected RestTemplate getRestTemplate() {
		class UnhandleErrorRestTemplate extends RestTemplate {
			public UnhandleErrorRestTemplate() {
				setErrorHandler(new DefaultResponseErrorHandler() {
					@Override
					public void handleError(ClientHttpResponse response) throws IOException {
					}
				});
			}
		}

		return new UnhandleErrorRestTemplate();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private HttpEntity<Void> getHttpEntity() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		if (getEnhancedBasic() && null != getUsername() && null != getPassword()) {
			headers.add("Authorization", "Basic " + encrypt(getUsername() + ":" + getPassword()));
		}
		return new HttpEntity<Void>(headers);
	}

	private HttpEntity<?> getHttpEntity(MultiValueMap<String, String> form) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		if (getEnhancedBasic() && null != getUsername() && null != getPassword()) {
			headers.add("Authorization", "Basic " + encrypt(getUsername() + ":" + getPassword()));
		}
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(form,
				headers);
		return httpEntity;
	}

	/**
	 * @return
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 */
	protected RestTemplate getSSLRestTemplate() throws KeyManagementException, NoSuchAlgorithmException {
		RestTemplate testRestTemplate = new RestTemplate();
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
	protected <T> ResponseEntity<T> request(RestTemplate testRestTemplate, String url, HttpMethod method,
			HttpEntity<?> requestEntity, List<MediaType> acceptableMediaTypes, Class<T> returnType, boolean redirect) {
		HttpHeaders requestHeaders = new HttpHeaders();

		if (null != requestEntity)
			requestHeaders.putAll(requestEntity.getHeaders());

		if (0 == requestHeaders.getAccept().size()) {
			requestHeaders.setAccept(acceptableMediaTypes);
		}

		requestEntity = new HttpEntity<Object>(null == requestEntity ? null : requestEntity.getBody(), requestHeaders);

		ResponseEntity<T> responseEntity = testRestTemplate.exchange(url, method, requestEntity, returnType);

		if (responseEntity.getStatusCode().equals(HttpStatus.FOUND)) {
			if (redirect) {
				String location = null;
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
					responseEntity = request(testRestTemplate, location, HttpMethod.GET, requestEntity,
							acceptableMediaTypes, returnType, redirect);
				}
			}
		}
		return responseEntity;
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#get(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<T> get(String url) throws Exception {
		return (ResponseEntity<T>) get(url, String.class);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#get(java.lang.String, java.lang.Class)
	 */
	@Override
	public <T> ResponseEntity<T> get(String url, Class<T> returnType) throws Exception {
		return get(url, returnType, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#get(java.lang.String, java.lang.Class, boolean)
	 */
	@Override
	public <T> ResponseEntity<T> get(String url, Class<T> returnType, boolean redirect) throws Exception {
		return get(url, Arrays.asList(MediaType.ALL), returnType, redirect);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#get(java.lang.String, java.util.List, java.lang.Class)
	 */
	@Override
	public <T> ResponseEntity<T> get(String url, List<MediaType> acceptableMediaTypes, Class<T> returnType)
			throws Exception {
		return get(url, acceptableMediaTypes, returnType, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#get(java.lang.String, java.util.List, java.lang.Class, boolean)
	 */
	@Override
	public <T> ResponseEntity<T> get(String url, List<MediaType> acceptableMediaTypes, Class<T> returnType,
			boolean redirect) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.GET, getHttpEntity(), acceptableMediaTypes, returnType,
				redirect);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#post(java.lang.String, org.springframework.util.MultiValueMap)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form) throws Exception {
		return (ResponseEntity<T>) post(url, form, String.class, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#post(java.lang.String, org.springframework.util.MultiValueMap, java.lang.Class)
	 */
	@Override
	public <T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form, Class<T> returnType)
			throws Exception {
		return post(url, form, returnType, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#post(java.lang.String, org.springframework.util.MultiValueMap, java.lang.Class, boolean)
	 */
	@Override
	public <T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form, Class<T> returnType,
			boolean redirect) throws Exception {
		return post(url, form, Arrays.asList(MediaType.ALL), returnType, redirect);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#post(java.lang.String, org.springframework.util.MultiValueMap, java.util.List, java.lang.Class)
	 */
	@Override
	public <T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form,
			List<MediaType> acceptableMediaTypes, Class<T> returnType) throws Exception {
		return post(url, form, acceptableMediaTypes, returnType, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#post(java.lang.String, org.springframework.util.MultiValueMap, java.util.List, java.lang.Class, boolean)
	 */
	@Override
	public <T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form,
			List<MediaType> acceptableMediaTypes, Class<T> returnType, boolean redirect) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.POST, getHttpEntity(form), acceptableMediaTypes, returnType,
				redirect);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#delete(java.lang.String, org.springframework.util.MultiValueMap)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form) throws Exception {
		return (ResponseEntity<T>) delete(url, form, String.class, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#delete(java.lang.String, org.springframework.util.MultiValueMap, java.lang.Class)
	 */
	@Override
	public <T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form, Class<T> returnType)
			throws Exception {
		return delete(url, form, returnType, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#delete(java.lang.String, org.springframework.util.MultiValueMap, java.lang.Class, boolean)
	 */
	@Override
	public <T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form, Class<T> returnType,
			boolean redirect) throws Exception {
		return delete(url, form, Arrays.asList(MediaType.ALL), returnType, redirect);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#delete(java.lang.String, org.springframework.util.MultiValueMap, java.util.List, java.lang.Class)
	 */
	@Override
	public <T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form,
			List<MediaType> acceptableMediaTypes, Class<T> returnType) throws Exception {
		return delete(url, form, acceptableMediaTypes, returnType, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#delete(java.lang.String, org.springframework.util.MultiValueMap, java.util.List, java.lang.Class, boolean)
	 */
	@Override
	public <T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form,
			List<MediaType> acceptableMediaTypes, Class<T> returnType, boolean redirect) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.DELETE, getHttpEntity(form), acceptableMediaTypes, returnType,
				redirect);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#put(java.lang.String, org.springframework.util.MultiValueMap)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form) throws Exception {
		return (ResponseEntity<T>) put(url, form, String.class, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#put(java.lang.String, org.springframework.util.MultiValueMap, java.lang.Class)
	 */
	@Override
	public <T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form, Class<T> returnType)
			throws Exception {
		return put(url, form, returnType, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#put(java.lang.String, org.springframework.util.MultiValueMap, java.lang.Class, boolean)
	 */
	@Override
	public <T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form, Class<T> returnType,
			boolean redirect) throws Exception {
		return put(url, form, Arrays.asList(MediaType.ALL), returnType, redirect);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#put(java.lang.String, org.springframework.util.MultiValueMap, java.util.List, java.lang.Class)
	 */
	@Override
	public <T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form,
			List<MediaType> acceptableMediaTypes, Class<T> returnType) throws Exception {
		return put(url, form, acceptableMediaTypes, returnType, true);
	}

	/* (non-Javadoc)
	 * @see com.wang.web.it.IRestClient#put(java.lang.String, org.springframework.util.MultiValueMap, java.util.List, java.lang.Class, boolean)
	 */
	@Override
	public <T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form,
			List<MediaType> acceptableMediaTypes, Class<T> returnType, boolean redirect) throws Exception {
		return request(getRestTemplate(), url, HttpMethod.PUT, getHttpEntity(form), acceptableMediaTypes, returnType,
				redirect);
	}
}
