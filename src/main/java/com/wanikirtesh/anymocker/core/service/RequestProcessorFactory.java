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
    public ClosureProcessor getProcessor(final String strProcessor) {
        if(RequestProcessorFactory.processors.containsKey(strProcessor)){
            return RequestProcessorFactory.processors.get(strProcessor);
        }
        this.updateProcessor(strProcessor);
        return RequestProcessorFactory.processors.get(strProcessor);
    }

    public void updateProcessor(final String strProcessor) {
        final File file = new File(this.processorsPath + "/" + strProcessor + ".groovy");
        RequestProcessorFactory.log.info("adding / Resetting processor from file:" + file.getPath());
        if(!file.exists())
            RequestProcessorFactory.log.severe("No Processor file found with ["+strProcessor+".groovy] ");
        else
            try {
                final ClassLoader parent = this.getClass().getClassLoader();
                final Class<?> groovyClass;
                try (final GroovyClassLoader loader = new GroovyClassLoader(parent)) {
                    groovyClass = loader.parseClass(file);
                }
                final Object closureOwner = groovyClass.newInstance();
                final Request myObject = new Request();
                myObject.setMethod("POST");
                final MethodClosure process = new MethodClosure(closureOwner, "process");
                final MethodClosure pre = new MethodClosure(closureOwner, "pre");
                final MethodClosure post = new MethodClosure(closureOwner, "post");
                final MethodClosure init = new MethodClosure(closureOwner, "init");
                final MethodClosure download = new MethodClosure(closureOwner, "download");
                final MethodClosure stats = new MethodClosure(closureOwner, "stats");
                final ClosureProcessor processor = new ClosureProcessor(pre, process, post, init, download,stats);
                RequestProcessorFactory.processors.put(strProcessor, processor);
            }catch (final Exception e){
                e.printStackTrace();
                RequestProcessorFactory.log.severe(e.getMessage());
                throw new RuntimeException(e);
            }
    }
}
