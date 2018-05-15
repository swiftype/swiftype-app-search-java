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
      "{\"query\":\"cat\",\"api_key_name\":\"my-token-name\"}"
    );

    assertEquals(
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJxdWVyeSI6ImNhdCIsImFwaV9rZXlfbmFtZSI6Im15LXRva2VuLW5hbWUifQ.hhdpalMFuWwuhsVBpHr9piQpg9ISo7xkxp0vSe8Fb50",
      signedKey
    );

    Map<String, Object> decodedPayload = Jwt.verify("api-mu75psc5egt9ppzuycnc2mc3", signedKey);

    assertEquals(2, decodedPayload.size());
    assertEquals("my-token-name", decodedPayload.get("api_key_name"));
    assertEquals("cat", decodedPayload.get("query"));

    assertThrows(SignatureException.class, () -> {
      Jwt.verify("api-fakepsc5egt9ppzuycnc2mc3", signedKey);
    });
  }
}
