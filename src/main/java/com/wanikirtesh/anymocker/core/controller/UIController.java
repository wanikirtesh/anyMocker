package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.service.RequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class UIController {
    @Value("${requests.grouping}")
    Boolean groupRequests;
    @Value("${processors.path}")
    private String processorsPath;

    @Autowired
    private RequestFactory requestFactory;
    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE")
    public String manager(final Model model)
    {
        model.addAttribute("groupedUrls", groupRequests(this.requestFactory.getRequestList()));
        return "index";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/NEW")
    public String manager_new(final Model model)
    {
        model.addAttribute("groupedUrls", groupRequests(this.requestFactory.getRequestList()));
        return "index_new";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/REQUEST/GET/V2/{request}")
    public String getRequestV2(final Model model,@PathVariable final String request) throws IOException {
        Request r = requestFactory.getRequest(request);
        model.addAttribute("req", r);
        model.addAttribute("gScript", Files.readString(Paths.get(this.processorsPath, r.getProcessor() + ".groovy")));
        return "fragment/request";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE/REQUEST/GET/ALL")
    public String getTree(final Model model) throws IOException {
        model.addAttribute("groupedUrls", groupRequests(this.requestFactory.getRequestList()));
        return "fragment/tree";
    }


    private Map<String, Map<String, Map<String,List<Request>>>> groupRequests(List<Request> requestList) {
        Map<String, Map<String, Map<String,List<Request>>>> resultMap = new HashMap<>();
            for (Request request : requestList) {
                String[] parts = request.getUrl().split("/");
                if (4 <= parts.length && request.isGrouping() && groupRequests) {
                    String product = parts[1];
                    String module = parts[2];
                    String persona = parts[3];
                    resultMap
                            .computeIfAbsent(product, k -> new HashMap<>())
                            .computeIfAbsent(module, k -> new HashMap<>())
                            .computeIfAbsent(persona, k -> new ArrayList<>())
                            .add(request);
                } else {
                    resultMap
                            .computeIfAbsent("PRODUCT", k -> new HashMap<>())
                            .computeIfAbsent("MODULE", k -> new HashMap<>())
                            .computeIfAbsent("PERSONA", k -> new ArrayList<>())
                            .add(request);
                }
            }
        return resultMap;
    }

}
