package com.wanikirtesh.anymocker.core.components;

import lombok.extern.slf4j.Slf4j;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.openapi4j.operation.validator.model.Request;
import org.openapi4j.operation.validator.model.impl.Body;
import org.openapi4j.operation.validator.model.impl.DefaultRequest;
import org.openapi4j.operation.validator.validation.RequestValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class OpenApiValidator {


    private static String specPath;

    @Autowired
    public OpenApiValidator(@Value("${specs.path}") String specPath) {
        this.specPath = specPath;
    }

    public static ValidationException validateRequest(com.wanikirtesh.anymocker.core.components.Request rq,HttpServletRequest request, String body) throws IOException, ValidationException, ResolutionException {
        String specpath = specPath + File.separator + rq.getSpec();
        OpenApi3 apiSpec = new OpenApi3Parser().parse(new File(specpath), true);
        RequestValidator validator = new RequestValidator(apiSpec);
        DefaultRequest.Builder builder = new DefaultRequest.Builder(request.getRequestURI(), Request.Method.getMethod(request.getMethod()));
        if(body != null)
            builder.body(Body.from(body));
        builder.headers(getHeadersAsMap(request));
        builder.query(request.getQueryString());
        try {
            validator.validate(builder.build());
        }catch (ValidationException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return e;
        }
        return null;
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