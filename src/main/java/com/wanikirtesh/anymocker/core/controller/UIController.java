package com.wanikirtesh.anymocker.core.controller;

import com.wanikirtesh.anymocker.core.components.Request;
import com.wanikirtesh.anymocker.core.service.RequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class UIController {
    @Autowired
    private RequestFactory requestFactory;
    @RequestMapping(method = RequestMethod.GET, path = "/MOCKER/MANAGE")
    public String manager(final Model model)
    {
        model.addAttribute("requests", this.requestFactory.getRequestList());
        return "index";
    }
}
