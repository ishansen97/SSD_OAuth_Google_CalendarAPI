package com.private_event_manager.com.Private.Event.Manager;

import com.google.api.services.calendar.model.EventDateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

//importing google calendar api
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

//this class will act as the RESTful API controller
@RestController
public class EventController {

    private final static Log logger = LogFactory.getLog(EventController.class);
    private static final String APPLICATION_NAME = "ssd-oauth-calendar";
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static com.google.api.services.calendar.Calendar client;

    GoogleClientSecrets clientSecrets;
    GoogleAuthorizationCodeFlow flow;
    Credential credential;

    @Value("${google.client.client-id}")
    private String clientId;
    @Value("${google.client.client-secret}")
    private String clientSecret;
    @Value("${google.client.redirectUri}")
    private String redirectURI;

    private Set<Event> events = new HashSet<>();

    final DateTime date1 = new DateTime("2017-05-05T16:30:00.000+05:30");
    final DateTime date2 = new DateTime(new Date());

    public void setEvents(Set<Event> events) {
        this.events = events;
    }


    @RequestMapping(method = RequestMethod.GET, path = "/home")
    public String getRequest() {
        return "Welcome to the home page";
    }

//    this method will handle getting the authorization code by using OAuth
    @RequestMapping(value = "/login/google", method = RequestMethod.POST)
    public RedirectView googleConnectionStatus(HttpServletRequest request) throws Exception {

//        retrieve parameters from the request
        String strFromDate = request.getParameter("from_date");
        String strToDate = request.getParameter("to_date");

        String format = "yyyy-MM-dd";

//        converting the parameters into Date variables
        Date fromDate = new SimpleDateFormat(format).parse(strFromDate);
        Date toDate = new SimpleDateFormat(format).parse(strToDate);

        DateTime fromDateTime = new DateTime(fromDate);
        DateTime toDateTime = new DateTime(toDate);


        //set the session variables
        HttpSession session = request.getSession(true);
        //set the datetime values
        session.setAttribute("fromDate", fromDateTime);
        session.setAttribute("toDate", toDateTime);


        return new RedirectView(authorize(request));
    }

    @RequestMapping(value = "/login/google", method = RequestMethod.GET, params = "code")
    public ResponseEntity<String> oauth2Callback(@RequestParam(value = "code") String code) {
        com.google.api.services.calendar.model.Events eventList;
        String message;
        try {
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
            System.out.println("token type: " + response.toPrettyString());
            credential = flow.createAndStoreCredential(response, "userID");
            client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
            Events events = client.events();
            eventList = events.list("primary").setTimeMin(date1).setTimeMax(date2).execute();
            message = eventList.getItems().get(0).toString();
            System.out.println("My:" + eventList.getItems().get(0));
        } catch (Exception e) {
            logger.warn("Exception while handling OAuth2 callback (" + e.getMessage() + ")."
                    + " Redirecting to google connection status page.");
            message = "Exception while handling OAuth2 callback (" + e.getMessage() + ")."
                    + " Redirecting to google connection status page.";
        }

        System.out.println("cal message:" + message);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }


    //this method will request the authorization server and retrieve the authorization code
    private String authorize(HttpServletRequest request) throws Exception {
        AuthorizationCodeRequestUrl authorizationUrl;
        if (flow == null) {
            Details web = new Details();

            //setting client ID and secret for the request
            web.setClientId(clientId);
            web.setClientSecret(clientSecret);

            clientSecrets = new GoogleClientSecrets().setWeb(web);
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
                    Collections.singleton(CalendarScopes.CALENDAR)).build();
        }
        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectURI);
        System.out.println("cal authorizationUrl->" + authorizationUrl);
        //store the authorization code in session storage
        HttpSession session = request.getSession(true);
        session.setAttribute("flow", flow);


        return authorizationUrl.build();
    }

    //this method will define the endpoint for the callback path specified by the client while registering its app
    @RequestMapping(method = RequestMethod.GET, path = "/callback")
    public RedirectView getCallBack(HttpServletRequest request) {

        //extracting the authorization code out of the request
        String query_string = request.getQueryString();
        String[] params = query_string.split("&");
        String[] auth_code_string_list = params[0].split("=");
        String code = auth_code_string_list[1];

        //store the code in session
        HttpSession session = request.getSession();
        session.setAttribute("code", code);

        //redirecting to the frontend for the user to view calendar events
        return new RedirectView("/viewEvents");
    }
}

