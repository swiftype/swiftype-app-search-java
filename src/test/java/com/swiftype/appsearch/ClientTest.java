package com.swiftype.appsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ClientTest {
  @Test
  void testBaseUrl() {
    assertEquals("https://host-c5s2mj.api.swiftype.com/api/as/v1/", client().baseUrl());
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

  private Client client() {
    return new Client("host-c5s2mj", "api-mu75psc5egt9ppzuycnc2mc3");
  }
}
