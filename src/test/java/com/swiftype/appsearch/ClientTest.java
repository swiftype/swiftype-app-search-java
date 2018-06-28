package com.swiftype.appsearch;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

  private String hostIdentifier;
  private String apiKey;
  private String engineName;
  private Client client;

  @BeforeEach
  public void setUp() {
    hostIdentifier = System.getenv("ST_APP_SEARCH_HOST_IDENTIFIER") != null
      ? System.getenv("ST_APP_SEARCH_HOST_IDENTIFIER")
      : System.getenv("ST_APP_SEARCH_HOST_KEY"); // Deprecated

    apiKey = System.getenv("ST_APP_SEARCH_API_KEY");
    engineName = Optional.ofNullable(System.getenv("ST_APP_SEARCH_TEST_ENGINE_NAME"))
        .orElse("java-client-test-engine");

    assertNotNull(hostIdentifier, "Missing required env variable: ST_APP_SEARCH_HOST_IDENTIFIER");
    assertNotNull(apiKey, "Missing required env variable: ST_APP_SEARCH_API_KEY");

    client = new Client(hostIdentifier, apiKey);

    try {
      client.destroyEngine(engineName);
    } catch(ClientException e) {
    }
  }

  @Test
  void testBaseUrl() {
    assertEquals(String.format("https://%s.api.swiftype.com/api/as/v1/", hostIdentifier), client.baseUrl());
  }

  @Test
  void createSignedSearchKey() throws SignatureException, InvalidKeyException {
    Map<String, Object> options = new HashMap<>();
    options.put("query", "cat");

    String signedKey = Client.createSignedSearchKey("api-mu75psc5egt9ppzuycnc2mc3", "my-token-name", options);

    Map<String, Object> decodedPayload = Jwt.verify("api-mu75psc5egt9ppzuycnc2mc3", signedKey);

    assertEquals(2, decodedPayload.size());
    assertEquals("my-token-name", decodedPayload.get("api_key_name"));
    assertEquals("cat", decodedPayload.get("query"));
  }

  @Test
  void createAndDestroyEngine() throws ClientException {
    Map<String, Object> createResponse = client.createEngine(engineName);
    assertEquals(createResponse.get("name"), engineName);

    Map<String, Boolean> response = client.destroyEngine(engineName);
    assertTrue(response.get("deleted"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void listEngines() throws ClientException {
    List<Map<String, Object>> engines = (List<Map<String, Object>>) client.listEngines().get("results");
    client.createEngine(engineName);
    List<Map<String, Object>> enginesAfterCreate = (List<Map<String, Object>>) client.listEngines().get("results");

    assertEquals(enginesAfterCreate.size() - 1, engines.size());
    List<Map<String, Object>> enginesWithEngineName = enginesAfterCreate
        .stream()
        .filter(e -> e.get("name").equals(engineName))
        .collect(Collectors.toList());
    assertEquals(enginesWithEngineName.size(), 1);
  }

  @SuppressWarnings("unchecked")
  @Test
  void listEnginesWithPagination() throws ClientException {
    List<Map<String, Object>> engines = (List<Map<String, Object>>) client.listEngines(1, 20).get("results");
    client.createEngine(engineName);
    List<Map<String, Object>> enginesAfterCreate = (List<Map<String, Object>>) client.listEngines(1, 20).get("results");

    assertEquals(enginesAfterCreate.size() - 1, engines.size());
    List<Map<String, Object>> enginesWithEngineName = enginesAfterCreate
        .stream()
        .filter(e -> e.get("name").equals(engineName))
        .collect(Collectors.toList());
    assertEquals(enginesWithEngineName.size(), 1);
  }

  @Test
  void getEngine() throws ClientException {
    assertThrows(ClientException.class,
        () -> {
          client.getEngine(engineName);
        }
        );
    client.createEngine(engineName);
    Map<String, Object> response = client.getEngine(engineName);
    assertEquals(response.get("name"), engineName);
  }

  @Test
  void indexDocumentsWithId() throws ClientException {
    String doc1_id = "INscMGmhmX4";
    Map<String, Object> doc1 = new HashMap<>();
    doc1.put("id", doc1_id);
    doc1.put("url", "https://www.youtube.com/watch?v=INscMGmhmX4");

    List<Map<String, Object>> documents = Arrays.asList(doc1);

    client.createEngine(engineName);
    List<Map<String, Object>> response = client.indexDocuments(engineName, documents);
    assertEquals(response.get(0).get("id"), doc1_id);
  }

  @Test
  void indexDocumentsWithoutId() throws ClientException {
    Map<String, Object> doc1 = new HashMap<>();
    doc1.put("url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");

    List<Map<String, Object>> documents = Arrays.asList(doc1);

    client.createEngine(engineName);
    List<Map<String, Object>> response = client.indexDocuments(engineName, documents);
    assertEquals(response.get(0).get("id").getClass(), String.class);
  }

  @Test
  void indexDocumentWithId() throws ClientException {
    String doc1_id = "INscMGmhmX4";
    Map<String, Object> document = new HashMap<>();
    document.put("id", doc1_id);
    document.put("url", "https://www.youtube.com/watch?v=INscMGmhmX4");


    client.createEngine(engineName);
    Map<String, Object> response = client.indexDocument(engineName, document);
    assertEquals(response.get("id"), doc1_id);
  }

  @Test
  void indexDocumentWithoutId() throws ClientException {
    Map<String, Object> document = new HashMap<>();
    document.put("url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");

    client.createEngine(engineName);
    Map<String, Object> response = client.indexDocument(engineName, document);
    assertEquals(response.get("id").getClass(), String.class);
  }

  @Test
  void invalidDocumentException() throws ClientException {
    Map<String, Object> document = new HashMap<>();
    document.put("bad_field_name_because_this_key_value_is_really_really_really_long_almost_too_long_to_where_it_makes_you_uncomfortable_and_you_want_to_stop_reading_but_you_cant_so_you_just_keep_going", "foo");

    client.createEngine(engineName);
    assertThrows(InvalidDocumentException.class,
        () -> {
          client.indexDocument(engineName, document);
        }
        );
  }
}
