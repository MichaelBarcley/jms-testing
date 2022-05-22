package com.example.jmsrestapi.controllers;

import com.example.jmsrestapi.models.Reservation;
import com.example.jmsrestapi.models.StandardResponse;
import com.example.jmsrestapi.services.MainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RequestMapping("jms-rest")
@RestController
public class MainController {

    private Logger log = LoggerFactory.getLogger(getClass());
    private final MainService service;

    @Autowired
    public MainController(MainService service) {
        this.service = service;
    }

    @PostMapping("simplemessage")
    public ResponseEntity<String> simpleMessage(@RequestBody String text) {
        log.info("Sending into JMS: " + text);
        service.jmsSendSimpleString(text);
        return new ResponseEntity<>("String sent into JMS: " + text, HttpStatus.CREATED);
    }

    @PostMapping("reservation")
    public ResponseEntity<StandardResponse> sendReservation(@RequestBody Reservation reservation) {
        String correlationId = UUID.randomUUID().toString();
        LocalDateTime currentTime = LocalDateTime.now();

        log.info("Sending into JMS: " + reservation + ", with corrId: " + correlationId);
        service.jmsSendReservation(reservation, correlationId);
        StandardResponse response = new StandardResponse(correlationId, reservation, currentTime);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
