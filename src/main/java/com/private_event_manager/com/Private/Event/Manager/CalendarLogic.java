package com.private_event_manager.com.Private.Event.Manager;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CalendarLogic {

    private static final String APPLICATION_NAME = "ssd-oauth-calendar";
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static com.google.api.services.calendar.Calendar client;

    private static GoogleClientSecrets clientSecrets;
    //    private static GoogleAuthorizationCodeFlow flow;
    private static Credential credential;

    @Value("${google.client.redirectUri}")
    private static final String redirectURI = "http://localhost:8000/callback";

    Events eventList;

    final DateTime date1 = new DateTime("2017-05-05T16:30:00.000+05:30");
    final DateTime date2 = new DateTime(new Date());


    //    this method will retrieve events from the user's Google Calendar
    public static List<Event> getEvents(String code, GoogleAuthorizationCodeFlow flow, DateTime fromDate, DateTime toDate) {

        Calendar.Events.Insert result = null;
        Events eventList;
        Event newEvent;
        List<Event> list;
        System.out.println("redirect uri: " + redirectURI);

        String message;
        try {
            //initiating a request to obtain the access token
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            System.out.println("before getting the token");
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
            credential = flow.createAndStoreCredential(response, "userID");
            client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

            //getting access to the calendar events
            Calendar.Events events = client.events();

            //retrieving the events within specified date range
            eventList = events.list("primary").setTimeMin(fromDate).setTimeMax(toDate).execute();
            list = eventList.getItems();

        } catch (Exception e) {
            System.out.println("warning: " + e.getMessage());
            return null;
        }
        //return the calendar events
        return list;
    }

}
