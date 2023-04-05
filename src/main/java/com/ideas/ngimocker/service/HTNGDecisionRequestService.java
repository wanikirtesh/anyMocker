package com.ideas.ngimocker.service;

import com.ideas.ngimocker.components.MockRequest;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Log
public class HTNGDecisionRequestService implements RequestProcessor {

    @Override
    public ResponseEntity<String> process(MockRequest match, String body, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        return new ResponseEntity<String>(processMessage(body,req),headers,HttpStatus.OK);
    }

    private String processMessage(String body, HttpServletRequest req){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(body)));
            String wsaAddress  = document.getElementsByTagName("wsa:MessageID").item(0).getTextContent();
            log.info(wsaAddress);
        } catch (Exception e) {
            log.severe(e.getMessage());
            throw new RuntimeException(e);
        }
        return "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\"><s:Header><a:Action s:mustUnderstand=\"1\">http://htng.org/PWSWG/2010/12/RatePlan_SubmitRequestResponse</a:Action><h:TimeStamp xmlns:h=\"http://htng.org/1.3/Header/\" xmlns=\"http://htng.org/1.3/Header/\">"+new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())+"</h:TimeStamp><a:RelatesTo>${arrData[0]}</a:RelatesTo><a:MessageID>${arrData[0]}</a:MessageID></s:Header><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"/></s:Envelope>";
    }

}
