<p align="center"><img src="https://github.com/swiftype/swiftype-app-search-java/blob/master/logo-app-search.png?raw=true" alt="Elastic App Search Logo"></p>

<p align="center"><a href="https://circleci.com/gh/swiftype/swiftype-app-search-java"><img src="https://circleci.com/gh/swiftype/swiftype-app-search-java.svg?style=svg" alt="CircleCI buidl"></a>
<a href="https://github.com/swiftype/swiftype-app-search-java/releases"><img src="https://img.shields.io/github/release/swiftype/swiftype-app-search-java/all.svg?style=flat-square" alt="GitHub release" /></a></p>

> A first-party Java client for building excellent, relevant search experiences with [Elastic App Search](https://www.elastic.co/cloud/app-search-service).

## Contents

- [Getting started](#getting-started-)
- [Usage](#usage)
- [Running tests](#running-tests)
- [FAQ](#faq-)
- [Contribute](#contribute-)
- [License](#license-)

---

## Getting started 🐣

This project is not currently published to any public repositories. You will need to install the JARs manually.

The latest builds can be found here: https://github.com/swiftype/swiftype-app-search-java/releases/latest

### Build locally

Run:
```bash
ST_APP_SEARCH_HOST_KEY="YOUR_HOST_KEY" ST_APP_SEARCH_API_KEY="YOUR_API_KEY" gradle build shadowjar
```
This will generate two jars:

1. `swiftype-app-search-<version>-all.jar`: includes all
dependencies.
2. `swiftype-app-search-<version>.jar` includes only client code.

## Usage

### Setup: Configuring the client and authentication

Create a new instance of the Swiftype App Search Client. This requires your `HOST_IDENTIFIER`, which
identifies the unique hostname of the Swiftype API that is associated with your Swiftype account.
It also requires a valid `API_KEY`, which authenticates requests to the API:

```java
import com.swiftype.appsearch.Client;

String hostIdentifier = "host-c5s2mj";
String apiKey = "private-mu75psc5egt9ppzuycnc2mc3";
Client client = new Client(hostIdentifier, apiKey);
```

### Using with App Search Managed Deploys

The client can be configured to use a managed deploy by using the
`baseUrl` parameter. Since managed deploys do not rely on a `hostIdentifier`.
, it can be omitted.

```java
import com.swiftype.appsearch.Client;

String apiKey = "private-mu75psc5egt9ppzuycnc2mc3";
String baseUrl = "http://localhost:3002/api/as/v1/";
Client client = new Client(null, apiKey, baseUrl);
```

### API Methods

This client is a thin interface to the Swiftype App Search Api. Additional details for requests and responses can be
found in the [documentation](https://swiftype.com/documentation/app-search).

#### Indexing: Creating and updating Documents

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

List<Map<String, Object>> documents = Arrays.asList(doc1, doc2);

try {
  List<Map<String, Object>> response = client.indexDocuments(engineName, documents);
  System.out.println(response);
} catch (ClientException e) {
  System.out.println(e);
}
```

#### Retrieving Documents

```java
String engineName = "favorite-videos";
List<String> documentIds = Arrays.asList("INscMGmhmX4", "JNDFojsd02");

try {
  List<Map<String, Object>> response = client.getDocuments(engineName, documentIds);
  System.out.println(response);
} catch (ClientException e) {
  System.out.println(e);
}
```

#### Destroying Documents

```java
String engineName = "favorite-videos";
List<String> documentIds = Arrays.asList("INscMGmhmX4", "JNDFojsd02");

try {
  List<Map<String, Object>> response = client.destroyDocuments(engineName, documentIds)
  System.out.println(response);
} catch (ClientException e) {
  System.out.println(e);
}
```

#### Listing Engines

```java
try {
  Map<String, Object> response = client.listEngines();
  System.out.println(response);
} catch (ClientException e) {
  System.out.println(e);
}
```

#### Retrieving Engines

```java
String engineName = "favorite-videos";

try {
  Map<String, Object> response = client.getEngine(engineName);
  System.out.println(response);
} catch (ClientException e) {
  System.out.println(e);
}
```

#### Creating Engines

```java
String engineName = "favorite-videos";

try {
  Map<String, Object> response = client.createEngine(engineName);
  System.out.println(response);
} catch (ClientException e) {
  System.out.println(e);
}
```

#### Destroying Engines

```java
String engineName = "favorite-videos";

try {
  Map<String, Boolean> response = client.destroyEngine(engineName);
  System.out.println(response);
} catch (ClientException e) {
  System.out.println(e);
}
```

#### Searching

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
  Map<String, Object> response = client.search(engineName, query, options);
  System.out.println(response);
} catch (ClientException e) {
  System.out.println(e);
}
```

## Running tests

```bash
ST_APP_SEARCH_HOST_IDENTIFIER="YOUR_HOST_IDENTIFIER" ST_APP_SEARCH_API_KEY="YOUR_API_KEY" ./gradlew test
```

## FAQ 🔮

### Where do I report issues with the client?

If something is not working as expected, please open an [issue](https://github.com/swiftype/swiftype-app-search-java/issues/new).

### Where can I learn more about App Search?

Your best bet is to read the [documentation](https://swiftype.com/documentation/app-search).

### Where else can I go to get help?

You can checkout the [Elastic App Search community discuss forums](https://discuss.elastic.co/c/app-search).

## Contribute 🚀

We welcome contributors to the project. Before you begin, a couple notes...

- Prior to opening a pull request, please create an issue to [discuss the scope of your proposal](https://github.com/swiftype/swiftype-app-search-java/issues).
- Please write simple code and concise documentation, when appropriate.

## License 📗

[MIT](https://github.com/swiftype/swiftype-app-search-java/blob/master/LICENSE) © [Elastic](https://github.com/elastic)

Thank you to all the [contributors](https://github.com/swiftype/swiftype-app-search-java/graphs/contributors)!
