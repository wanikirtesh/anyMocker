package com.wanikirtesh.anymocker.core.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class OpenApiImporter {

    public static List<Request> importFromOpenApiSpec(String openApiYamlFile) {
        List<Request> requests = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            Map<String, Object> openApiSpec = mapper.readValue(openApiYamlFile, Map.class);
            Map<String, Object> paths = (Map<String, Object>) openApiSpec.get("paths");
            if (paths != null) {
                for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
                    String path = pathEntry.getKey().trim();
                    Map<String, Object> methods = (Map<String, Object>) pathEntry.getValue();
                    for (Map.Entry<String, Object> methodEntry : methods.entrySet()) {
                        String httpMethod = methodEntry.getKey().toUpperCase().trim();
                        Map<String, Object> methodDetails = (Map<String, Object>) methodEntry.getValue();
                        Request request = new Request();
                        request.setUrl(path);
                        request.setMethod(httpMethod);
                        if (methodDetails.containsKey("summary")) {
                            request.setName((String) methodDetails.get("summary"));
                        } else {
                            request.setName(httpMethod + " " + path.replace("/","_").replace("{","").replace("}",""));
                        }
                        if (methodDetails.containsKey("parameters")) {
                            List<Map<String, Object>> parameters = (List<Map<String, Object>>) methodDetails.get("parameters");
                            for (Map<String, Object> parameter : parameters) {
                                String paramIn = (String) parameter.get("in");
                                String paramName = (String) parameter.get("name");
                                if ("query".equalsIgnoreCase(paramIn)) {
                                    request.getQueryParam().add(paramName);
                                } else if ("path".equalsIgnoreCase(paramIn)) {
                                    request.getPathParam().add(paramName);
                                }
                            }
                        }
                        requests.add(request);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing OpenAPI spec file", e);
        }
        return requests;
    }
}
