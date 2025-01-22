# Welcome to AnyMocker

**AnyMocker** is a versatile Spring Boot-based application designed to simplify and streamline HTTP request handling. It provides two core features to empower developers and testers:

1. **Mocking HTTP Responses**: Create and manage mock responses for any HTTP request.
2. **Request Validation**: Validate incoming HTTP requests against an OpenAPI specification to ensure compliance and correctness.

---

## Key Features

### 1. Mock HTTP Responses
AnyMocker enables you to easily mock responses for any HTTP request by configuring the details through a user-friendly UI. You can define the following parameters for each mocked API:

- **Name**: A descriptive identifier for the mock API.
- **Path**: The endpoint path of the API.
- **Path Parameters**: Variables in the path that the request can pass.
- **Query Parameters**: Key-value pairs to be passed as part of the request.
- **Request Type**: Specify the HTTP method (e.g., `GET`, `POST`, etc.).
- **Processor**: The name of a Groovy file used for custom request processing.

With these settings, you can create tailored mock responses to test various scenarios and behaviors.

### 2. Customizable Request Processing
The Processor field adds a powerful dimension to AnyMocker by enabling dynamic response creation. The processor refers to a Groovy file that includes hooks to handle various stages of request processing, such as:

- **Process**: Handle and manipulate the incoming request data.
- **Post Process**: Apply additional logic after the initial processing.
- **Download**: Manage file downloads, if required by the request.
- **Init**: Initialize any resources or data before processing.

These hooks give you access to details like:
- Path parameters
- Query parameters
- HTTP headers
- Request payloads

This allows you to create dynamic and context-aware responses tailored to the specifics of the incoming request.

### 3. Validate Requests with OpenAPI
Ensure that incoming requests adhere to your defined OpenAPI specifications. AnyMocker validates the structure, parameters, and payload of each request against the schema, helping you catch errors early and maintain API integrity.

---

## Benefits

- **Ease of Use**: Intuitive UI for configuring and managing mock APIs.
- **Flexibility**: Groovy-based processors provide unparalleled customization options.
- **Efficiency**: Rapidly mock and test APIs without the need for a live backend.
- **Accuracy**: Validate requests with OpenAPI to ensure consistency and correctness.
- **Dynamic Responses**: Leverage request details for highly customized responses.

---

## Getting Started
1. **Setup Your Mock API**: Use the UI to define the details of your mock API.
2. **Write a Groovy Processor**: Create a Groovy file with hooks to handle request processing as needed.
3. **Test and Validate**: Send HTTP requests to your configured endpoints and verify the mocked responses or validation results.

---

Experience the power and simplicity of AnyMocker for all your HTTP mocking and validation needs!

