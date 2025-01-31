package com.wanikirtesh.anymocker.core.components;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.openapi4j.core.exception.DecodeException;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.model.OAIContext;
import org.openapi4j.core.model.reference.Reference;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.operation.validator.model.Request;
import org.openapi4j.operation.validator.model.impl.Body;
import org.openapi4j.operation.validator.model.impl.DefaultRequest;
import org.openapi4j.operation.validator.validation.RequestValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class OpenApiValidator {


    private static String specPath;
    private static final Faker faker = new Faker();

    @Autowired
    public OpenApiValidator(@Value("${specs.path}") String specPath) {
        this.specPath = specPath;
    }

    public static ResponseEntity<Object> validateRequest(com.wanikirtesh.anymocker.core.components.Request rq,HttpServletRequest request, String body)  {
        try {
            String specpath = specPath + File.separator + rq.getSpec();
            OpenApi3 openApi3 = new OpenApi3Parser().parse(new File(specpath), true);
            Map<String, Path> pathTemplates = new HashMap<>();
            pathTemplates.putAll(openApi3.getPaths());
            OAIContext context = openApi3.getContext();
            RequestValidator validator = new RequestValidator(openApi3);
            DefaultRequest.Builder builder = new DefaultRequest.Builder(request.getRequestURI(), Request.Method.getMethod(request.getMethod()));
            if(body != null) {
                builder.body(Body.from(body));
            }
            builder.headers(getHeadersAsMap(request));
            builder.query(request.getQueryString());
            validator.validate(builder.build());
            Path path = findMatchingPath(request.getRequestURI(),openApi3,pathTemplates);
            if (path == null) {
                return ResponseEntity.notFound().build();
            }
            Operation operation = getOperation(path, request.getMethod());
            if (operation == null) {
                return ResponseEntity.badRequest().body("Unsupported HTTP method for this path");
            }
            Map<String, String> pathParameters = extractPathParameters(path, request.getRequestURI(),pathTemplates);
            Response response = operation.getResponse("200");
            if (response == null) {
                return ResponseEntity.ok().build();
            }
            Schema schema = response.getContentMediaType("application/json").getSchema();
            Object responseBody = generateResponse(schema, pathParameters,context);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseBody);

        }catch (ValidationException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(e.toString());
        } catch (ResolutionException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        //return null;
    }

    private static Object generateResponse(Schema schema, Map<String, String> pathParameters, OAIContext context) {
        try {
            if (schema.isRef()) {
                schema = schema.getReference(context).getMappedContent(Schema.class);
            }
            if (schema.getType().equals("array")) {
                return generateArray(schema, pathParameters, context,"");
            } else if (schema.getType().equals("object")) {
                return generateObject(schema, pathParameters,context);
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static Object[] generateArray(Schema schema, Map<String, String> pathParameters, OAIContext context,String key) throws DecodeException {
        Schema itemSchema;
        if(schema.getItemsSchema().isRef()){
            itemSchema = schema.getItemsSchema().getReference(context).getMappedContent(Schema.class);
        }else{
            itemSchema = schema.getItemsSchema();
        }
        if(itemSchema.getType() == null){
            try {
                itemSchema = itemSchema.getReference(context).getMappedContent(Schema.class);
            } catch (DecodeException e) {
                throw new RuntimeException(e);
            }
        }
        if(itemSchema.getType().equals("object")) {
            return new Object[]{
                    generateObject(itemSchema, pathParameters, context),
                    generateObject(itemSchema, pathParameters, context)
            };
        }
        else{
                return new Object[]{
                        generateValue(itemSchema,key),
                        generateValue(itemSchema,key)
                };
            }
    }

    private static Map<String, Object> generateObject(Schema schema, Map<String, String> pathParameters, OAIContext context) {
        Map<String, Object> object = new HashMap<>();
        schema.getProperties().forEach((key, value) -> {
            String type = value.getType();
            if(type == null) {
                try {
                    object.put(key, generateObject(value.getReference(context).getMappedContent(Schema.class), pathParameters, context));
                }catch (DecodeException e) {
                    throw new RuntimeException(e);
                }
            }else if(type.equals("array")) {
                try {
                    object.put(key, generateArray(value, pathParameters, context,key));
                } catch (DecodeException e) {
                    throw new RuntimeException(e);
                }
            }else if(type.equals("object")) {
                object.put(key, generateObject(value, pathParameters, context));
            }else {
                object.put(key, generateValue(value,key));

            }
        });
        return object;
    }
    public static Object generateValue(Schema schema,String key){
        switch (schema.getType()) {
            case "integer" :
                if(key.toLowerCase().contains("id"))
                    return faker.number().numberBetween(1,2000);
                else
                    return faker.number().numberBetween(1,Integer.MAX_VALUE);
            case "string" :
                if(key.toLowerCase().contains("name")){
                    return faker.name().fullName();
                }
                if(key.toLowerCase().contains("url")){
                    return faker.internet().image();
                }
                if(key.toLowerCase().contains("date")){
                    return new Date();
                }
                if(key.toLowerCase().contains("password")){
                    return faker.lorem().word();
                }
                return faker.lorem().sentence(1);
            default :
                return null;
        }
    }
    public static Object generateValue(Schema schema){
         return switch (schema.getType()) {
            case "integer" -> faker.number().numberBetween(1, Integer.MAX_VALUE);
            case "string" -> faker.leagueOfLegends().champion();
            default -> "null";
        };
    }


    private static Map<String, String> extractPathParameters(Path path, String requestPath,Map<String,Path> pathTemplates) {
        Map<String, String> pathParameters = new HashMap<>();
        String pathTemplate = pathTemplates.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(path))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (pathTemplate != null) {
            // Convert OpenAPI path template to a regex pattern
            String regex = pathTemplate.replaceAll("\\{([^}]+)\\}", "([^/]+)");
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(requestPath);

            if (matcher.matches()) {
                // Extract parameter names from the path template
                Pattern paramPattern = Pattern.compile("\\{([^}]+)\\}");
                Matcher paramMatcher = paramPattern.matcher(pathTemplate);

                int index = 1;
                while (paramMatcher.find()) {
                    String paramName = paramMatcher.group(1);
                    String paramValue = matcher.group(index);
                    pathParameters.put(paramName, paramValue);
                    index++;
                }
            }
        }

        return pathParameters;
    }

    private static Operation getOperation(Path path, String httpMethod) {
        return switch (httpMethod.toUpperCase()) {
            case "GET" -> path.getGet();
            case "POST" -> path.getPost();
            case "PUT" -> path.getPut();
            case "DELETE" -> path.getDelete();
            default -> path.getOperation(httpMethod);
        };
    }

    private static Path findMatchingPath(String requestPath, OpenApi3 openApi3, Map<String, Path> pathTemplates) {
        for (String pathTemplate : pathTemplates.keySet()) {
            if (matchesPathTemplate(pathTemplate, requestPath)) {
                return openApi3.getPath(pathTemplate);
            }
        }
        return null;
    }

    private static boolean matchesPathTemplate(String pathTemplate, String requestPath) {
        String regex = pathTemplate.replaceAll("\\{([^}]+)\\}", "([^/]+)");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(requestPath);
        return matcher.matches();
    }


    public static Map<String, Collection<String>> getHeadersAsMap(HttpServletRequest request) {
        Map<String, Collection<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        return headers;
    }
}