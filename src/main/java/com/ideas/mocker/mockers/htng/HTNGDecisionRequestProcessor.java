package com.ideas.mocker.mockers.htng;

import com.ideas.mocker.core.components.HTTPClient;
import com.ideas.mocker.core.components.Request;
import com.ideas.mocker.core.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import jakarta.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service("HTNG")
@Log
public class HTNGDecisionRequestProcessor implements RequestProcessor {
    @Autowired
    HTTPClient httpClient;


    @Override
    public void init() {

    }

    @Override
    public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type",req.getHeader("content-type"));
        return new ResponseEntity<>(processMessage(body),headers,HttpStatus.OK);
    }

    private String processMessage(String body){
        String wsaMessageId;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(body)));
            wsaMessageId  = document.getElementsByTagName("wsa:MessageID").item(0).getTextContent();
            log.info(wsaMessageId);
        } catch (Exception e) {
            log.severe(e.getMessage());
            throw new RuntimeException(e);
        }
        return "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\"><s:Header><a:Action s:mustUnderstand=\"1\">http://htng.org/PWSWG/2010/12/RatePlan_SubmitRequestResponse</a:Action><h:TimeStamp xmlns:h=\"http://htng.org/1.3/Header/\" xmlns=\"http://htng.org/1.3/Header/\">"+new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())+"</h:TimeStamp><a:RelatesTo>"+wsaMessageId+"</a:RelatesTo><a:MessageID>"+wsaMessageId+"</a:MessageID></s:Header><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"/></s:Envelope>";
    }

    @Override
    public void postProcess(Request match, String body, HttpServletRequest req){
        if(!match.getMeta("g3CallBack").isEmpty()){
            String contentType = req.getHeader("content-type");
            callBackHTNG(createCallBackResponse(body), contentType);
        }
    }

    @Override
    public void preProcess(Request match, String body, HttpServletRequest req) {

    }

    @Override
    public void downloadFixtures(Request match) {

    }

    private String[] createCallBackResponse(String body) {
        DocumentBuilder builder;
        String[] res = new String[2];
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(body)));
            String wsaMessageId  = document.getElementsByTagName("wsa:MessageID").item(0).getTextContent();
            String correlationID  = document.getElementsByTagName("htng:CorrelationID").item(0).getTextContent();
            String userName = document.getElementsByTagName("wsse:Username").item(0).getTextContent();
            String password = document.getElementsByTagName("wsse:Password").item(0).getTextContent();
            NodeList bodyNode = document.getElementsByTagName("env:Body");
            String typeOf;
            if(bodyNode.getLength()>0) {
                typeOf = bodyNode.item(0).getFirstChild().getNodeName().replace("RQ", "RS");
            }else{
                typeOf = document.getElementsByTagName("SOAP-ENV:Body").item(0).getFirstChild().getNodeName().replace("RQ", "RS");
            }
            String replyTo = document.getElementsByTagName("htng:ReplyTo").item(0).getFirstChild().getFirstChild().getTextContent();
            res[0] ="<Envelope xmlns=\"http://www.w3.org/2003/05/soap-envelope\"><soap2:Header xmlns:htng=\"http://htng.org/1.3/Header/\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:wss=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:htnga=\"http://htng.org/PWSWG/2007/02/AsyncHeaders\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap2=\"http://www.w3.org/2003/05/soap-envelope\"><wsa:Action>http://htng.org/PWSWG/2010/12/RatePlan_SubmitResult</wsa:Action><wsa:ReplyTo><wsa:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:Address></wsa:ReplyTo><wss:Security mustUnderstand=\"1\"><wss:UsernameToken><wss:Username>"+userName+"</wss:Username><wss:Password>"+password+"</wss:Password></wss:UsernameToken></wss:Security><wsa:MessageID>"+wsaMessageId+"</wsa:MessageID><htnga:RelatesToCorrelationID>"+correlationID+"</htnga:RelatesToCorrelationID><wsa:To>https://g3rms.ideas.com/pacman-platformsecurity/htng/asyncResponse</wsa:To></soap2:Header><Body><"+typeOf+" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" TimeStamp=\"2017-01-04T13:23:46.1201766+00:00\" Version=\"2.000\" xmlns=\"http://www.opentravel.org/OTA/2003/05\"><Success /></"+typeOf+"></Body></Envelope>";
            res[1] = replyTo;
        } catch (Exception e) {
            log.severe(e.getMessage());
            log.severe(body);
            throw new RuntimeException(e);
        }
        return res;
    }

    public void callBackHTNG(String[] callBack, String contentType){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String[] headers = new String[2];
        headers[0]="Content-Type";
        headers[1] = contentType ;
        HttpResponse<String> response = httpClient.makePostRequest(callBack[1], callBack[0], headers);
        if(response.statusCode()>300){
            log.severe(callBack[0]);
        }
    }

}
