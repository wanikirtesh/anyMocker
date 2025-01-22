# Introduction to Anymocker

Anymocker is a Spring Boot-based application designed to revolutionize how you mock and validate HTTP requests. Think of it as your go-to API wizard, capable of spinning up dynamic responses and enforcing strict validation rules with ease. With Anymocker, developers and testers are armed with a tool that combines flexibility, power, and simplicity.

---

## Key Features

### 1. Mocking HTTP Requests
Anymocker offers an intuitive interface to mock HTTP responses on the fly. Here’s what you can do:

- **Define APIs with Precision:** Specify attributes like:
  - Name
  - Path
  - Path parameters
  - Query parameters
  - HTTP method (e.g., POST, GET)
  - Request type

- **Processors with Superpowers:** Attach a custom Groovy script to each API, which dynamically processes incoming requests and tailors the responses.

- **Dynamic Responses:** Generate responses that adapt based on the request’s body, path, and query parameters.

### 2. Request Validation
- **OpenAPI Spec Integration:** Upload an OpenAPI spec file, and Anymocker will transform it into actionable request objects.
- **Validation Like a Pro:** Match incoming requests against the spec file to ensure consistency with defined API structures.

---

## The Processor

At the heart of Anymocker lies the **Processor**—a Groovy script that breathes life into your mocked APIs. With its hooks, you can:

- **Initialize:** `init` – Prepare for action.
- **Process Requests:** `process` – The main event where requests are handled and responses are born.
- **Post-Process:** `post` – Tidy up after the response is sent.
- **Handle Files:** `download` – Manage file-related operations effortlessly.

### Example Processor
```groovy
import com.wanikirtesh.anymocker.core.components.GroovyHelper

def init(log, requests) {
    log.info("Initializing processor with ${requests.size()} requests.")
}

def process(log, match, body, req) {
    log.info("Processing request for API: ${match.name}, Path: ${match.url}")
    log.debug("Request details: Body=${body}, Params=${match.requestQueryParams}")

    return GroovyHelper.getResponseEntity("ok", 200)
}

def post(log, match, body, req) {
    log.info("Post-processing request for API: ${match.name}, Path: ${match.url}")
}

def download(log, match) {
    log.info("Downloading data for API: ${match.name}, Path: ${match.url}")
}
```

---

## Example: Dynamic Response Based on Parameters

### API Configuration
- **Path:** `/api/example`
- **Method:** `GET`
- **Query Parameters:** `name`, `age`
- **Processor:** `ExampleProcessor`

### Example Processor Script
```groovy
import com.wanikirtesh.anymocker.core.components.GroovyHelper

def process(log, match, body, req) {
    log.info("Incoming request for API: ${match.name}, Path: ${match.url}")

    def name = req.getQueryParam("name")
    def age = req.getQueryParam("age")

    if (!name || !age) {
        log.warn("Missing parameters: name=${name}, age=${age}")
        return GroovyHelper.getResponseEntity("Missing parameters", 400)
    }

    log.info("Parameters received: name=${name}, age=${age}")
    def responseMessage = "Hello, ${name}. You are ${age} years old."
    log.debug("Response message: ${responseMessage}")

    return GroovyHelper.getResponseEntity(responseMessage, 200)
}
```

### Request Example
**Request:**
```http
GET /api/example?name=John&age=30
```

**Response:**
```json
{
  "message": "Hello, John. You are 30 years old."
}
```

---

## Leveraging GroovyHelper

Anymocker’s **GroovyHelper** component is packed with utility methods that make dynamic API responses effortless. Here’s a snapshot of its capabilities:

- **Data Storage and Retrieval:**
  - `putObject(String key, Object o)` – Store objects for reuse across requests.
  - `Object getDataObject(String key)` – Retrieve stored objects.

- **Response Handling:**
  - `ResponseEntity<String> getResponseEntity(String body, Map<String, String> headers, int statusCode)`
  - `ResponseEntity<String> getResponseEntity(String body, int statusCode)`
  - `ResponseEntity<String> getResponseEntity(int statusCode)`

- **HTTP Requests:**
  - `HttpResponse<String> makePostRequest(String uri, String data, String[] headers)` – Make POST requests to external services.
  - `String makeGetRequest(String uri)` – Fetch data via GET requests.
  - `String makeRequest(String uri, String method, HttpRequest.BodyPublisher body)` – Customize HTTP calls.

- **File Management:**
  - `void writeFile(String content, String basePath, String... paths)` – Write content to a file.
  - `Map<String, Path> collectFiles(Path path)` – Collect files from a directory.
  - `Map<String, Map<String, Path>> collectNestedFiles(Path path)` – Collect nested files.

- **JSON Parsing:**
  - `JSONObject parseJsonObject(String str)` – Parse strings into JSON objects.
  - `JSONArray parseJsonArray(String str)` – Parse strings into JSON arrays.

### Example: External Service Call
```groovy
def process(log, match, body, req) {
    log.info("Calling external service for API: ${match.name}")

    def uri = "https://api.example.com/data"
    def response = GroovyHelper.makeGetRequest(uri)

    log.debug("External service response: ${response}")

    return GroovyHelper.getResponseEntity(response, 200)
}
```

---

## Example: OpenAPI Spec File

Below is a simple OpenAPI specification for the `/api/example` endpoint:

```yaml
openapi: 3.0.0
info:
  title: Example API
  version: 1.0.0
paths:
  /api/example:
    get:
      summary: Example API endpoint
      parameters:
        - name: name
          in: query
          required: true
          schema:
            type: string
        - name: age
          in: query
          required: true
          schema:
            type: integer
      responses:
        "200":
          description: Successful response
          content:
            application/json:
              schema:
                type: object
                properties:
                  message:
                    type: string
        "400":
          description: Missing parameters
```

---

## Logging and Monitoring

With Anymocker, you’re always in the loop:

- **Real-Time Insights:** The `log` object captures everything from info-level messages to detailed debug logs. These logs are prominently displayed in the application’s UI.
- **Detailed Activity Logs:** Every interaction, parameter, and decision point is logged for transparency and troubleshooting.

### Logging in Action
Here’s a snippet showcasing dramatic logging:
```groovy
log.info("🚀 Processing request for ${match.name} at ${match.url}")
log.debug("🛠 Extracted parameters: ${match.requestQueryParams}")
log.warn("⚠️ Missing critical parameters! Fallback response initiated.")
log.info("✅ Successfully processed request. Response dispatched.")
```

---

## Conclusion

Anymocker isn’t just a tool—it’s your API ally. Whether you need to mock intricate responses, validate against OpenAPI specs, or log every detail with flair, Anymocker delivers. Embrace the power of dynamic, responsive, and efficient API handling with Anymocker today.

