package com.swiftype.appsearch;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

  private String hostKey;
  private String apiKey;
  private String engineName;
  private Client client;

  @BeforeEach
  public void setUp() {
    hostKey = System.getenv("ST_APP_SEARCH_HOST_KEY");
    apiKey = System.getenv("ST_APP_SEARCH_API_KEY");
    engineName = Optional.ofNullable(System.getenv("ST_APP_SEARCH_TEST_ENGINE_NAME"))
      .orElse("java-client-test-engine");

    assertNotNull(hostKey);
    assertNotNull(apiKey);

    client = new Client(hostKey, apiKey);

    try {
      client.destroyEngine(engineName);
    } catch(ClientException e) {
    }
  }

  @Test
  void testBaseUrl() {
    assertEquals(String.format("https://%s.api.swiftype.com/api/as/v1/", hostKey), client.baseUrl());
  }

  @Test
  void createSignedSearchKey() throws SignatureException, InvalidKeyException {
    Map<String, Object> options = new HashMap<>();
    options.put("query", "cat");

    String signedKey = Client.createSignedSearchKey("api-mu75psc5egt9ppzuycnc2mc3", "42", options);

    Map<String, Object> decodedPayload = Jwt.verify("api-mu75psc5egt9ppzuycnc2mc3", signedKey);

    assertEquals(2, decodedPayload.size());
    assertEquals("42", decodedPayload.get("api_key_id"));
    assertEquals("cat", decodedPayload.get("query"));
  }

  @Test
  void createAndDestroyEngine() throws ClientException {
    Map<String, Object> createResponse = client.createEngine(engineName);
    assertEquals(createResponse.get("name"), engineName);

    Map<String, Boolean> response = client.destroyEngine(engineName);
    assertTrue(response.get("deleted"));
  }

  @Test
  void listEngines() throws ClientException {
    List<Map<String, Object>> engines = client.listEngines();
    client.createEngine(engineName);
    List<Map<String, Object>> enginesAfterCreate = client.listEngines();

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
}
