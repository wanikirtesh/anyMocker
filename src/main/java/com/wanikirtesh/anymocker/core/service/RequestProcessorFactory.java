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
    private static class ProcessorHolder {
        final ClosureProcessor processor;
        final GroovyClassLoader classLoader;
        ProcessorHolder(ClosureProcessor processor, GroovyClassLoader classLoader) {
            this.processor = processor;
            this.classLoader = classLoader;
        }
    }
    private static final Map<String, ProcessorHolder> processorMap = new HashMap<>();
    private static final int RELOAD_THRESHOLD = 50;

    @PostConstruct
    void init(){
        processors.clear();
    }
    public ClosureProcessor getProcessor(final String strProcessor) {
        ProcessorHolder holder = processorMap.get(strProcessor);
        if (holder != null) {
            return holder.processor;
        }
        this.updateProcessor(strProcessor);
        holder = processorMap.get(strProcessor);
        return holder != null ? holder.processor : null;
    }

     public void updateProcessor(final String strProcessor) {
        final File file = new File(this.processorsPath + "/" + strProcessor + ".groovy");
        log.info("adding / Resetting processor from file:" + file.getPath());
        if (!file.exists()) {
            log.error("No Processor file found with [" + strProcessor + ".groovy] ");
            return;
        }
        try {
            // Remove old processor and class loader
            processorMap.remove(strProcessor);
            // Create a new class loader for this processor
            GroovyClassLoader loader = new GroovyClassLoader(this.getClass().getClassLoader());
            final Class<?> groovyClass = loader.parseClass(file);
            final Object closureOwner = groovyClass.newInstance();
            final ClosureProcessor processor = getClosureProcessor(closureOwner);
            processorMap.put(strProcessor, new ProcessorHolder(processor, loader));
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
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
