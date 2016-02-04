package com.wang.web.it;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public interface IRestClient {
	String getUsername();

	String getPassword();

	<T> ResponseEntity<T> get(String url) throws Exception;

	<T> ResponseEntity<T> get(String url, Class<T> returnType) throws Exception;

	<T> ResponseEntity<T> get(String url, Class<T> returnType, boolean redirect) throws Exception;

	<T> ResponseEntity<T> get(String url, List<MediaType> acceptableMediaTypes, Class<T> returnType) throws Exception;

	<T> ResponseEntity<T> get(String url, List<MediaType> acceptableMediaTypes, Class<T> returnType, boolean redirect)
			throws Exception;

	<T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form) throws Exception;

	<T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form, Class<T> returnType) throws Exception;

	<T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form, Class<T> returnType, boolean redirect)
			throws Exception;

	<T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form, List<MediaType> acceptableMediaTypes,
			Class<T> returnType) throws Exception;

	<T> ResponseEntity<T> post(String url, MultiValueMap<String, String> form, List<MediaType> acceptableMediaTypes,
			Class<T> returnType, boolean redirect) throws Exception;

	<T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form) throws Exception;

	<T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form, Class<T> returnType) throws Exception;

	<T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form, Class<T> returnType, boolean redirect)
			throws Exception;

	<T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form, List<MediaType> acceptableMediaTypes,
			Class<T> returnType) throws Exception;

	<T> ResponseEntity<T> delete(String url, MultiValueMap<String, String> form, List<MediaType> acceptableMediaTypes,
			Class<T> returnType, boolean redirect) throws Exception;

	<T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form) throws Exception;

	<T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form, Class<T> returnType) throws Exception;

	<T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form, Class<T> returnType, boolean redirect)
			throws Exception;

	<T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form, List<MediaType> acceptableMediaTypes,
			Class<T> returnType) throws Exception;

	<T> ResponseEntity<T> put(String url, MultiValueMap<String, String> form, List<MediaType> acceptableMediaTypes,
			Class<T> returnType, boolean redirect) throws Exception;

}