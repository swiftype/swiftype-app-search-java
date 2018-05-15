# Java client for the Swiftype App Search Api

**Note: Swiftype App Search is currently in beta**

## Installation

To build locally run:

    gradle build shadowjar

This will generate two jars. `swiftype-app-search-0.1.0-all.jar` includes all necessary
dependencies and `swiftype-app-search-0.1.0.jar` includes only the client code.

## Usage

### Setup: Configuring the client and authentication

Create a new instance of the Swiftype App Search Client. This requires your `ACCOUNT_HOST_KEY`, which
identifies the unique hostname of the Swiftype API that is associated with your Swiftype account.
It also requires a valid `API_KEY`, which authenticates requests to the API:

```java
import com.swiftype.appsearch.Client;

String accountHostKey = "host-c5s2mj";
String apiKey = "api-mu75psc5egt9ppzuycnc2mc3";
Client client = new Client(accountHostKey, apiKey);
```

### Indexing: Creating and updating Documents

```java
String engineName = "favorite-videos";
Map<String, Object> doc1 = new HashMap<>();
doc1.put("id", "INscMGmhmX4");
doc1.put("url", "https://www.youtube.com/watch?v=INscMGmhmX4");
doc1.put("title", "The Original Grumpy Cat");
doc1.put("body", "A wonderful video of a magnificent cat.");

Map<String, Object> doc2 = new HashMap<>();
doc2.put("id", "JNDFojsd02");
doc2.put("url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ");
doc2.put("title", "Another Grumpy Cat");
doc2.put("body", "A great video of another cool cat.");

List<Map<String, Object>> documents = Arrays.asList(doc1, doc2)

try {
  client.indexDocuments(engineName, documents);
} catch (ClientException e) {
  // handle error
}
```

### Listing Documents

```java
String engineName = "favorite-videos";
List<String> documentIds = Arrays.asList("INscMGmhmX4", "JNDFojsd02");

try {
  List<Map<String, Object>> documentContents = client.getDocuments(engineName, documentIds);
  // handle document contents
} catch (ClientException e) {
  // handle error
}
```

### Destroying Documents

```java
String engineName = "favorite-videos";
List<String> documentIds = Arrays.asList("INscMGmhmX4", "JNDFojsd02");

try {
  List<Map<String, Object>> destroyDocumentResults = client.destroyDocuments(engineName, documentIds)
  // handle destroy document results
} catch (ClientException e) {
  // handle error
}
```

### Searching

```java
String engineName = "favorite-videos";
String query = "cat";

Map<String, Object> searchFields = new HashMap<>();
searchFields.put("title", Collections.emptyMap());

Map<String, Object> idResultField = new HashMap<>();
idResultField.put("raw", Collections.emptyMap());
Map<String, Object> resultFields = new HashMap<>();
resultFields.put("title", idResultField);

Map<String, Object> options = new HashMap<>();
options.put("search_fields", searchFields);
options.put("result_fields", resultFields);

try {
  Map<String, Object> searchResults = client.search(engineName, query, options)
  // handle search results
} catch (ClientException e) {
  // handle error
}
```


## Running Tests

```bash
ST_APP_SEARCH_HOST_KEY="YOUR_HOST_KEY" ST_APP_SEARCH_API_KEY="YOUR_API_KEY" gradle test
```

## Contributions

To contribute code to this gem, please fork the repository and submit a pull request.
