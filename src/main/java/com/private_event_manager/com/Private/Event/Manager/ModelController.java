package com.private_event_manager.com.Private.Event.Manager;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class ModelController {

    //    this method will retrieve the home page
    @RequestMapping("/")
    public String Home() {
        return "Home";
    }

//    this method will retrieve the select-dates page
    @RequestMapping("/select-dates")
    public String RedirectToSelectDates() {
        return "SelectDates";
    }


    @RequestMapping("/view")
    public String ViewEvent() {
        return "View";
    }

    //this method will redirect user to the view their calendar events
    @RequestMapping(value = "/viewEvents", method = RequestMethod.GET)
    public ModelAndView ViewEvents(HttpServletRequest request) {
        HttpSession session = request.getSession();

        //get the authorization code from the session storage
        String code = (String) session.getAttribute("code");

        //get the datetimes from the session storage
        DateTime fromDate = (DateTime) session.getAttribute("fromDate");
        DateTime toDate = (DateTime) session.getAttribute("toDate");

        //get the 'GoogleAuthorizationCodeFlow' object from the session storage
        GoogleAuthorizationCodeFlow flow = (GoogleAuthorizationCodeFlow) session.getAttribute("flow");

        //retrieve the Google Calendar events
        List<Event> result = CalendarLogic.getEvents(code, flow, fromDate, toDate);


        //setting up the data to be displayed in the frontend page
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("View");
        modelAndView.addObject("events", result);
        modelAndView.addObject("regex", "T");

        return modelAndView;
    }


}
