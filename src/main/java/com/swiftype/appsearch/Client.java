package com.swiftype.appsearch;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Api client for Swiftype App Search.
 *
 * @see <a href="https://swiftype.com/documentation/app-search/">https://swiftype.com/documentation/app-search/</a>
 */
public class Client {
  // Remember to also update version in build.gradle!
  private final String VERSION = "0.1.0";
  private final List<String> REQUIRED_TOP_LEVEL_DOCUMENT_KEYS = Arrays.asList("id");

  private final String baseUrl;
  private final String apiKey;

  /**
   * @param accountHostKey account host key to use for base url
   * @param apiKey api key to use for authentication
   */
  public Client(String accountHostKey, String apiKey) {
    this(accountHostKey, apiKey, "https://%s.api.swiftype.com/api/as/v1/");
  }

  /**
   * Dev only constructor for hitting dev/private endpoints.
   *
   * @param accountHostKey account host key to use for base url
   * @param apiKey api key to use for authentication
   * @param baseUrlFormatString format string to build a custom base url using host key
   */
  public Client(String accountHostKey, String apiKey, String baseUrlFormatString) {
    this.baseUrl = String.format(baseUrlFormatString, accountHostKey);
    this.apiKey = apiKey;
  }

  /**
   * Search for documents.
   *
   * @param engineName unique engine name
   * @param query search query string
   * @return search results
   * @throws ClientException if the api request fails
   */
  public Map<String, Object> search(String engineName, String query) throws ClientException {
    return search(engineName, query, Collections.emptyMap());
  }

  /**
   * Search for documents.
   *
   * @param engineName unique engine name
   * @param query search query string
   * @param options see the <a href="https://swiftype.com/documentation/app-search/">App Search API</a> for supported search options.
   * @return search results
   * @throws ClientException if the api request fails
   */
  public Map<String, Object> search(String engineName, String query, Map<String, Object> options) throws ClientException {
    Map<String, Object> reqBody = new HashMap<>();
    reqBody.putAll(options);
    reqBody.put("query", query);

    return makeJsonRequest("GET", String.format("engines/%s/search", engineName), reqBody, JsonTypes.OBJECT);
  }

  /**
   * Index a batch of documents.
   *
   * @param engineName unique engine name
   * @param documents collection of document objects to index
   * @return list of document creation statuses
   * @throws ClientException if the api request fails
   */
  public List<Map<String, Object>> indexDocuments(String engineName, List<Map<String, Object>> documents) throws ClientException {
    for (Map<String, Object> d : documents) {
      validateDocument(d);
    }
    return makeJsonRequest("POST", String.format("engines/%s/documents", engineName), documents, JsonTypes.ARRAY_OF_OBJECTS);
  }

  /**
   * Retrieve a batch of documents.
   *
   * @param engineName unique engine name
   * @param ids batch of document ids to retrieve
   * @return list of document details
   * @throws ClientException if the api request fails
   */
  public List<Map<String, Object>> getDocuments(String engineName, List<String> ids) throws ClientException {
    return makeJsonRequest("GET", String.format("engines/%s/documents", engineName), ids, JsonTypes.ARRAY_OF_OBJECTS);
  }

  /**
   * Destroy a batch of documents.
   *
   * @param engineName unique engine name
   * @param ids batch of document ids to destroy
   * @return  list of document deletion statuses
   * @throws ClientException if the api request fails
   */
  public List<Map<String, Object>> destroyDocuments(String engineName, List<String> ids) throws ClientException {
    return makeJsonRequest("DELETE", String.format("engines/%s/documents", engineName), ids, JsonTypes.ARRAY_OF_OBJECTS);
  }

  /**
   * Creates a jwt search key that can be used for authentication to enforce a set of required search options.
   *
   * @param apiKeyId the unique API Key identifier
   * @param options see the <a href="https://swiftype.com/documentation/app-search/">App Search API</a> for supported search options
   * @return jwt search key
   * @throws InvalidKeyException if the api key is invalid
   */
  public static String createSignedSearchKey(String apiKey, String apiKeyId, Map<String, Object> options) throws InvalidKeyException {
    Map<String, Object> payload = new HashMap<>();
    payload.putAll(options);
    payload.put("api_key_id", apiKeyId);
    return Jwt.sign(apiKey, payload);
  }

  <T> T makeJsonRequest(String httpMethod, String path, Object body, TypeToken<T> resultType) throws ClientException {
    try {
      String reqBody = null;
      if (body != null) {
        reqBody = new Gson().toJson(body);
      }

      try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpDynamicRequestWithBody request = new HttpDynamicRequestWithBody(httpMethod, baseUrl + path);
        request.setHeader(HttpHeaders.USER_AGENT, String.format("swiftype-app-search-java/%s", VERSION));
        request.setHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", apiKey));
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        if (reqBody != null) {
          request.setEntity(new StringEntity(reqBody));
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
          int statusCode = response.getStatusLine().getStatusCode();
          if (statusCode < 200 || statusCode > 299) {
            throw new ClientException(String.format("Error: %d", statusCode));
          }
          String respBody = EntityUtils.toString(response.getEntity());
          return new Gson().fromJson(respBody, resultType.getType());
        }
      }
    } catch (IOException e) {
      throw new ClientException("Error making http request", e);
    }
  }

  String baseUrl() {
    return this.baseUrl;
  }

  private void validateDocument(Map<String, Object> document) throws ClientException {
    List<String> missingRequiredKeys = REQUIRED_TOP_LEVEL_DOCUMENT_KEYS.stream()
      .filter(k -> !document.containsKey(k))
      .collect(Collectors.toList());

    if (!missingRequiredKeys.isEmpty()) {
      throw new ClientException(String.format("Missing required fields: %s", missingRequiredKeys));
    }
  }

  private static class HttpDynamicRequestWithBody extends HttpEntityEnclosingRequestBase {
    private final String method;

    public HttpDynamicRequestWithBody(String method, String uri) {
      this.method = method;
      setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
      return this.method;
    }
  }
}
