# Java Message Service (JMS) test

### This test project consists of 3 main parts:
- A REST API written in Spring Boot which receives some kind of data at an endpoint
- An ActiveMQ Java message broker server (not uploaded into this repository)
- A receiver service written in Spring Boot which receives the data from the message broker and just logs it out (to console in my case only)

### To run the application:
1) Download ActiveMQ (https://activemq.apache.org/components/classic/download/)
2) Download the contents of this repository
3) Make sure to have Java 11+ installed on the PC (for development AdoptOpenJDK11 was used)
4) Inside the ActiveMQ folder open a command line or bash and type "bin\activemq start"
5) Open the two Spring Boot projects in any compatible IDE and launch both of them as Java Applications (order does not matter)

Disclaimer: I know that for this demo it would be more than succificent to have only 1 project which containts the message broker as an embedded service, the sender and the receiver, however I wanted to test out what would happen if any of these services are offline and it is more likely that in a real-life solution these are seperate services working individually.

### Design decisions:
- For the REST API using Spring Boot was a no brainer. I added Spring Web (because the API receives HTTP requests) and the ActiveMQ packages (because I used Apache ActiveMQ as my message broker).
- For the message broker I used ActiveMQ. I chose this message broker as it is one of the most used message-oriented middleware with a long history and an active community so I if I would get stuck I would most probably find an answer to my problem. It is also open source (under Apache license) and could be used without any repercussions. I also chose this solution as setting up the middleware was very easy and took me basically no time.
- For the processor I also used Spring Boot. I was originally starting to do it in plain Java (and implement a listener there), but then I got thinking that Spring Boot provides a lot of very useful tools and annotations that would make my job very easy and no boilerplate code is required this way (writing the connectionfactory, loggers, etc.). If it would be a real business application, using Spring Boot would also make my life tremendously easier if I need to connect the received messages with any database through an ORM or deploying the app could be done in a few minutes when using a dedicated Tomcat Application Server. Using Spring Boot the setup of the processor/receiver became trivial and could be done very fast. I used the same additional packages as in the REST API.
- With JMS there are two different approaches: publisher-subscriber approach (where messages are put into a 'topic') and the point-to-point (where messages are put into a 'queue'). Based on the task description I used point-to-point, as the task clearly states that there is ALWAYS 1 processor and in certain cases that processor could be offline. The queue logic is also much more suitable for this task as the message broker in this case will forward the message first which is the oldest message in the queue and proceed from there (there is a reason it is called 'queue': first-in, first-out). This queueing logic of the messages inside ActiveMQ stays intact even if the processor is down for some time OR EVEN if the broker received some messages while the processor was down and then the broker was also taken offline for some time and then restarted.

### Assumptions:
- Hotels are sending the reservation data in a correct order from a datetime perspective (e.g reservation with id ABC012345 version 1 is sent first then version 2 to update it and then version 3 to finalize is: e.g no 2-3-1 order)
- A REST API and the message broker is always available to receive the initial data (otherwise we are occuring a data loss). The processor/receiver can be offline.
- Multiple REST APIs can receive data from hotels and put the data into the queue. Only a maximum of 1 processor is present at any time however.
- Reservation id from different hotels cannot be the same (if hotel 1 has a reservationid ABC012345, hotel 2 will not generate this same value)

### REST API (jms-restapi project):
- Endpoint- POST /jms-rest/simplemessage: The "simplemessage" endpoint accepts a simple String without any other payload or header and puts it to the ${app.jms.stringQueue} queue configurable in the application.properties. It is the baseline endpoint which remains simple as possible.
- Endpoint- POST /jms-rest/reservation: The "reservation" endpoint is a POST endpoint which accepts a Reservation object and puts it to the ${app.jms.reservationQueue} queue configurable in the application.properties. This endpoint also generates a UUID as correlation ID and returns a proper response to the costumer with the correlation ID, the received object and the datetime when the request was received.
- Obviously more endpoints could be done... (e.g. an endpoint for costumers where with a given correlation ID the status of the message processing is given back)
- Project follows the standard MVC model
- Special configuration bean is in place to convert incoming json object to be compatible with JMS

### Apache ActiveMQ:
- No additional configuration has been done with the messaging broker. (Baseline version is used)

### Processor/Receiver (jms-processor project):
- Listener 1- Listens at the configurable ${app.jms.stringQueue} queue from the message broker. Expecting a simple incoming String and listener logs it out.
- Listener 2- Listens at the configurable ${app.jms.reservationQueue} queue. Expecting a jmsrestapi Reservation model TOGETHER with a correlation id.
- I highly dislike that I am referring simply to a model where the package/namespace is for another package but due to time constraint this was easier to setup it like this. In a real-life application I would probably have the transportable class objects in a separate project and make a jar file from it which I would import into both Java applications and then this way it can be made sure both apps use the same models.
- The processor also has a configuration bean created to manage conversion between JMS - Jackson (same as in the REST API).

### Do you see any problems with this setup?:
- One processor: it could be overwhelmed if an enormous amount of messages is inbound
- Data loss if the broker is offline: REST API(s) could implement an in-memory queue themselves with the messages that couldn't be delivered to ActiveMQ and retry every 'X' minutes.
- Data loss if (all) REST API(s) are offline: In this case the requester has the responsibility to retry in a later time or try another REST API (if there are multiple accessible ones)
- Creating another processor inside the same queue could cause ordering problems as if there are multiple consumers in a point-to-point approach, the message broker does a load balance and only 1 consumers gets 1 message

### What kind of data does the sender’s message HAVE TO contain to ensure they are imported in the correct order?
- A 100% unique reservation id (if both hotels can have the same id, the REST API has to put a prefix into the ID: e.g Hotel is Hilton Vienna and it sent an ID of DRE943210 then the REST API would alter this to HILWIEN-DRE943210).
- A version of the reservation (1 being the initial) OR a definitive timestamp with timezone or in UTC to be able to identify the correct order, however from the ActiveMQ website: "ActiveMQ will preserve the order of messages sent by a single producer to all consumers on a topic. If there is a single consumer on a queue then the order of messages sent by a single producer will be preserved as well." As a result if my assumption is correct and the hotel won't sent version 3 of a reservation before version 2, then this is not strictly required.

### Optimizations that I haven't done but would be good in a real-world app:
- Not use hardcoded connection string, username and password (to activeMQ): use environment variables, hashed data, etc.
- Deploy the currently standalone activeMQ into JBoss/Wildfly
- The models used inside the REST API and the Processor would be a seperate jar file, to ensure that both applications are using the same model(s)
- Inside both Spring Boot projects the respective services (MessageConsumerService in the processor and MainService in the API) would first be an interface with the required methods and then implemented seperately (MessageConsumerServiceImpl and MainServiceImpl respectively)
- Better error handling (make sure the sent object is an instance of what we are expecting, meaningful error message response at REST API if hotels send something gibberish, etc.)