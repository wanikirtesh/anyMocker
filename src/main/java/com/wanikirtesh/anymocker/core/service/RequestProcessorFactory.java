package com.wanikirtesh.anymocker.core.service;

import com.wanikirtesh.anymocker.core.components.ClosureProcessor;
import com.wanikirtesh.anymocker.core.components.Request;
import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RequestProcessorFactory {
    @Value("${processors.path}")
    private String processorsPath;
    private static Map<String, ClosureProcessor> processors= new HashMap<>();;
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader(this.getClass().getClassLoader());

    @PostConstruct
    void init(){
        processors.clear();
    }
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
            RequestProcessorFactory.log.error("No Processor file found with ["+strProcessor+".groovy] ");
        else
            try {
                final Class<?> groovyClass = groovyClassLoader.parseClass(file);
                final Object closureOwner = groovyClass.newInstance();
                final ClosureProcessor processor = getClosureProcessor(closureOwner);
                RequestProcessorFactory.processors.remove(strProcessor);
                RequestProcessorFactory.processors.put(strProcessor, processor);
            }catch (final Exception e){
                e.printStackTrace();
                log.error(e.getMessage(),e);
                throw new RuntimeException(e);
            }
    }

    private static ClosureProcessor getClosureProcessor(Object closureOwner) {
        final MethodClosure process = new MethodClosure(closureOwner, "process");
        final MethodClosure pre = new MethodClosure(closureOwner, "pre");
        final MethodClosure post = new MethodClosure(closureOwner, "post");
        final MethodClosure init = new MethodClosure(closureOwner, "init");
        final MethodClosure download = new MethodClosure(closureOwner, "download");
        final MethodClosure stats = new MethodClosure(closureOwner, "stats");
        return new ClosureProcessor(pre, process, post, init, download,stats);
    }
}
