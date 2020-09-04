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
        return "home";
    }

    @RequestMapping("/select-dates")
    public String RedirectToSelectDates() {
        return "SelectDates";
    }

    @RequestMapping("/view")
    public String ViewEvent() {
        return "View";
    }

//    @RequestMapping(value = "/addEvent", method = RequestMethod.GET)
//    public RedirectView AddEvent(HttpServletRequest request) {
//
//        EventDateTime eventDateTime = new EventDateTime();
//        String format = "yyyy-MM-dd hh:mm";
//
//        ModelAndView modelAndView = new ModelAndView();
//        try {
//            Event event = new Event();
////            String event_date = privateEvent.getDate();
//
////            System.out.println("date: " + event_date);
////            String start_time = privateEvent.getStart();
////            String end_time = privateEvent.getEnd();
////            String start_datetime = event_date + " " + start_time;
////            String end_datetime = event_date + " " + end_time;
////            Date startDate = new SimpleDateFormat(format).parse(start_datetime);
////            Date endDate = new SimpleDateFormat(format).parse(end_datetime);
////
////            //setting values
////            event.setSummary(privateEvent.getSummary());
////            event.setDescription(privateEvent.getDescription());
////            event.setStart(new EventDateTime().setDate(new DateTime(startDate)));
////            event.setEnd(new EventDateTime().setDate(new DateTime(endDate)));
//
//            //add the event
////            String response = CalendarLogic.addEvents(event);
////            String response = CalendarLogic.authorize();
////            System.out.println("response: " + response);
////
////            modelAndView.setViewName("test-add");
////            modelAndView.addObject("event", event);
//            return new RedirectView(CalendarLogic.authorize());
//        } catch (Exception ex) {
//            modelAndView.setViewName("400");
//            return null;
//        }
//    }

    @RequestMapping(value = "/viewEvents", method = RequestMethod.GET)
    public ModelAndView ViewEvents(HttpServletRequest request) {
        HttpSession session = request.getSession();


        //get the authorization code from the session storage
        String code = (String) session.getAttribute("code");

        //get the datetimes from the session storage
        DateTime fromDate = (DateTime) session.getAttribute("fromDate");
        DateTime toDate = (DateTime) session.getAttribute("toDate");

        GoogleAuthorizationCodeFlow flow = (GoogleAuthorizationCodeFlow) session.getAttribute("flow");

        //add the event information to the Google Calendar
        List<Event> result = CalendarLogic.getEvents(code, flow, fromDate, toDate);

//        System.out.println("Result: " + result);


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("View");
//        modelAndView.addObject("event", event);
        modelAndView.addObject("events", result);
        modelAndView.addObject("regex", "T");

        return modelAndView;
    }


}
