package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.ClosureProcessor;
import com.wanikirtesh.anymocker.core.components.Request;
import groovy.lang.GroovyClassLoader;
import lombok.extern.java.Log;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@Log
public class RequestProcessorFactory {
    @Value("${processors.path}")
    private String processorsPath;
    private final Map<String, ClosureProcessor> processors = new HashMap<>();
    public ClosureProcessor getProcessor(String strProcessor) {
        if(processors.containsKey(strProcessor)){
            return processors.get(strProcessor);
        }
        try {
            File file = new File(processorsPath + "/" + strProcessor + ".groovy");
            log.info("adding processor from file:" + file.getPath());
            if(!file.exists())
                return null;
            ClassLoader parent = getClass().getClassLoader();
            Class<?> groovyClass;
            try (GroovyClassLoader loader = new GroovyClassLoader(parent)) {
                groovyClass = loader.parseClass(file);
            }
            Object closureOwner = groovyClass.newInstance();
            Request myObject = new Request();
            myObject.setMethod("POST");
            MethodClosure process = new MethodClosure(closureOwner, "process");
            MethodClosure pre = new MethodClosure(closureOwner, "pre");
            MethodClosure post = new MethodClosure(closureOwner, "post");
            MethodClosure init = new MethodClosure(closureOwner, "init");
            MethodClosure download = new MethodClosure(closureOwner, "download");
            ClosureProcessor processor = new ClosureProcessor(log, pre, process, post, init, download);
            processors.put(strProcessor, processor);
            return processor;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
