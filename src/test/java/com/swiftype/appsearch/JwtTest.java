package com.swiftype.appsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JwtTest {
  @Test
  void testSign() throws InvalidKeyException, SignatureException {
    String signedKey = Jwt.sign(
      "api-mu75psc5egt9ppzuycnc2mc3",
      "{\"typ\":\"JWT\",\"alg\":\"HS256\"}",
      "{\"query\":\"cat\",\"api_key_id\":\"42\"}"
    );

    assertEquals(
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJxdWVyeSI6ImNhdCIsImFwaV9rZXlfaWQiOiI0MiJ9.MSSucKMyjKrqXQeEMeVzCyjHLm32Z66wr_dQ3IITYgY",
      signedKey
    );

    Map<String, Object> decodedPayload = Jwt.verify("api-mu75psc5egt9ppzuycnc2mc3", signedKey);

    assertEquals(2, decodedPayload.size());
    assertEquals("42", decodedPayload.get("api_key_id"));
    assertEquals("cat", decodedPayload.get("query"));

    assertThrows(SignatureException.class, () -> {
      Jwt.verify("api-fakepsc5egt9ppzuycnc2mc3", signedKey);
    });
  }
}
