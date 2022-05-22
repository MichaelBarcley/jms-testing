package com.example.jmsprocessor.services;

import com.example.jmsrestapi.models.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumerService {

    private Logger log = LoggerFactory.getLogger(getClass());

    @JmsListener(destination = "${app.jms.stringQueue}")
    public void stringListener(String receivedString) {
        log.info("Received info from JMS: " + receivedString);
    }

    @JmsListener(destination = "${app.jms.reservationQueue}")
    public void reservationListener(@Payload Reservation receivedReservation,
                                    @Header(JmsHeaders.CORRELATION_ID) String correlationId) {
        log.info("Received info from JMS: " + receivedReservation + ", with corrId: " + correlationId);

        // Do magic with the reservation (business logic + then save entity to db)

        /* With correlation id:
        A) Inside db have a table name called reservationJobs and save correlation id (as primary key), with timestamp and status so REST API can see what's up
        B) REST API sends the data to the message broker where a reply is required and waits for it, BUT that makes it sync and not async
         */
    }
}
