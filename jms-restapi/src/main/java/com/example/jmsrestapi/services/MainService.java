package com.example.jmsrestapi.services;

import com.example.jmsrestapi.models.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.TextMessage;
import java.util.UUID;

@Service
public class MainService {

    private Logger log = LoggerFactory.getLogger(getClass());
    private JmsTemplate jmsTemplate;

    @Value("${app.jms.stringQueue}")
    private String stringQueue;

    @Value("${app.jms.reservationQueue}")
    private String reservationQueue;

    @Autowired
    public MainService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void jmsSendSimpleString(String text) {
        jmsTemplate.convertAndSend(stringQueue, text);
    }

    public void jmsSendReservation(Reservation reservation, String correlationId) {
        jmsTemplate.convertAndSend(reservationQueue, reservation, item -> {
            item.setJMSCorrelationID(correlationId);
            return item;
                });
    }
}
