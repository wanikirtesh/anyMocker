package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.ClosureProcessor;
import com.wanikirtesh.anymocker.core.components.Request;
import groovy.lang.GroovyClassLoader;
import lombok.extern.java.Log;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Log
public class RequestProcessorFactory {
    @Value("${processors.path}")
    private String processorsPath;
    private static final Map<String, ClosureProcessor> processors = new HashMap<>();
    public ClosureProcessor getProcessor(String strProcessor) {
        if(processors.containsKey(strProcessor)){
            return processors.get(strProcessor);
        }
        updateProcessor(strProcessor);
        return processors.get(strProcessor);
    }

    public void updateProcessor(String strProcessor) {
        File file = new File(processorsPath + "/" + strProcessor + ".groovy");
        log.info("adding / Resetting processor from file:" + file.getPath());
        if(!file.exists())
            log.severe("No Processor file found with ["+strProcessor+".groovy] ");
        else
            try {
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
                MethodClosure stats = new MethodClosure(closureOwner, "stats");
                ClosureProcessor processor = new ClosureProcessor(log, pre, process, post, init, download,stats);
                processors.put(strProcessor, processor);
            }catch (Exception e){
                e.printStackTrace();
                log.severe(e.getMessage());
                throw new RuntimeException(e);
            }
    }
}
