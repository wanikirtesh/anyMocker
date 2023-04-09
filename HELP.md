# *Any Mocker*

---
This is server application which helps to mock responses for http/REST requests. All requests are need to be entered in **requests.json**. Every incoming http requests will get matched with URL patterns present in file, below is sample **requests.json**
```json
[{
"name": "request1",
"url":  "/statisticsCorrelation/{clientCode}/{propertyCode}/{statisticsCorrelationId}",
"method": "GET",
"queryParam": [],
"pathParam": ["clientCode","propertyCode","statisticsCorrelationId"],
"processor": "TEST",
"download": false,
"meta": {
    "pages": false
    }
},{
"name": "request2",
"url": "/api/v1/activities/hotel/{clientCode}/{propertyCode}/{correlationId}",
"method": "POST",
"queryParam": ["size","page"],
"pathParam": ["clientCode","propertyCode","correlationId"],
"processor": "TEST",
"download":true,
    "meta": {
    "pages": true,
    "correlation": true,
    "size": 500
    }
}]
```
### Parameter Description
| Key        | Description                                                                       |
|------------|-----------------------------------------------------------------------------------|
| name       | It is a place holder to identify any request                                      |
| url        | It is a ant matcher pattern for the the inbound requests                          |
| method     | http method for inbound request                                                   |
| queryParam | List of query parameters needs to be extract from the inbound URL                 |
| pathParam  | List of path parameter needs to be extract from the inbound URL                   |
| processor  | Name of bean which is responsible to process the respective request               |
| download   | its a boolean which shows the response fixture is downloadable                    |
| meta       | This is additional key value pairs which can be pass to help to mock the response |


### URL Pattern Example
| Pattern / Path Separator | Pattern Results                                                                                                                                                                                                                                                                                                                                                                                                |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| /views/products/**/*.cfm | **Matches:** <br/>/views/products/index.cfm<br>/views/products/SE10/index.cfm<br>/views/products/SE10/details.cfm<br>/views/products/ST80/index.cfm<br>/views/products/ST80/details.cfm<br><br>**Does Not Match:**<br>/views/index.cfm<br>/views/aboutUs/index.cfm<br>/views/aboutUs/managementTeam.cfm                                                                                                        |
| /views/**/*.cfm          | **Matches:**<br>/views/index.cfm<br>/views/aboutUs/index.cfm<br>/views/aboutUs/managementTeam.cfm<br>/views/products/index.cfm<br>/views/products/SE10/index.cfm<br>/views/products/SE10/details.cfm<br>/views/products/ST80/index.cfm<br>/views/products/ST80/details.cfm<br><br>**Does Not Match:**<br>/views/index.htm<br>/views/readme.txt                                                                 |
| /views/index??.cfm       | **Matches:**<br>/views/index01.cfm<br>/views/index02.cfm<br>/views/indexAA.cfm<br><br>**Does Not Match:**<br>/views/index01.htm<br>/views/index1.cfm<br>/views/indexA.cfm<br>/views/indexOther.cfm<br>/views/anotherDir/index01.cfm<br><br>(Remember that ? matches a single character, so the above example matches only files that start with "index", followed by two wildcard characters and then ".cfm".) |

The pattern uses three different wildcards.

| **Wildcard** | **Description**                   |
|--------------|-----------------------------------|
| `*`          | Matches zero or more characters.  |
| `?`          | Matches exactly one character.    |
| `**`         | Matches zero or more directories. |

---
### Processor Bean
- This should be a JAVA SpringBoot Bean having same name which is mentioned **requests.html** as `processor`.
- Bean should Implement the Interface `com.ideas.anymocker.core.service.RequestProcessor`
- To implement the interface below three methods needs to be overridden
  - `public void init()`
  - `public ResponseEntity<String> process(Request matchRequest, String body, HttpServletRequest actualReq)`
  - `public void postProcess(Request match, String body, HttpServletRequest req)`
  - `public void preProcess(Request match, String body, HttpServletRequest req)`
  - `public void downloadFixtures(Request match)`
#### Example Bean

```java
import com.ideas.anymocker.core.components.Request;
import com.ideas.anymocker.core.service.RequestProcessor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

@Service("TEST") // name of bean matching to processor property in requests.json
@Log
public class TestProcessor implements RequestProcessor {
  @Override
  @PostConstruct
  public void init() {
    // you can write your logic here to initialise this bean with fixtures and there path
  }

  @Override
  public ResponseEntity<String> process(Request match, String body, HttpServletRequest req) {
    // write your logic to process the request
    // Object match will have all the information regarding path and query params 
    // match will also contain meta object, which will have all details passed from request.json
    return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
  }

  @Override
  public void postProcess(Request match, String body, HttpServletRequest req) {
    // code to handle any task after giving the response
    log.info("post");
  }

  @Override
  public void preProcess(Request match, String body, HttpServletRequest req) {
    // code to handle any task needs to be take care before giving response
    log.info("pree");
  }

  @Override
  public void downloadFixtures(Request match) {
    // code to download response fixtures and save
    log.info("Downloading fixture");
  }
}
```

---