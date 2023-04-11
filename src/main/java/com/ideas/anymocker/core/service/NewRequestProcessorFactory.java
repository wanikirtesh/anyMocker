package com.ideas.anymocker.core.service;

import com.ideas.anymocker.core.components.ProcessorClosureWrapper;
import com.ideas.anymocker.core.components.Request;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class NewRequestProcessorFactory {
    @Value("${processors.path}")
    private String processorsPath;
    private final Map<String,ProcessorClosureWrapper> processors = new HashMap<>();
    public ProcessorClosureWrapper getProcessor(String processor) {
        if(processors.containsKey(processor)){
            return processors.get(processor);
        }
        try {
            File file = new File(processorsPath + "/" + processor + ".groovy");
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
            processors.put(processor,new ProcessorClosureWrapper(pre,process,post,init,download));
            return processors.get(processor);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        //return null;
    }
}
