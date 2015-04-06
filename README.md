# Using JMS Queue to audit JPA entity reads with Spring!  #
Recently a colleague of mine (Rudy De Busscher) posted an [interesting blog item](http://c4j.be/blog/using-jms-queue-audit-jpa-entity-reads) about using a JMS queue to audit JPA entity reads. After reading this post, I was wondering how I would solve it using Spring.

## Problem description ##
We have a quite simple problem: every time a JPA entity read occurs, this "read" is published to a JMS Queue. 

 1.  An entity Read happens
 2.  Push read message on JMS Queue
 3.  Read JMS message and output to stdout
 
 ![problem description](https://davyvanroy.be/wp-content/uploads/2015/04/jms_audit_problem.png)

## Solution ##
There are of course multiple solutions to this problem. In this solution, we will still be using the EntityListener that was shown in the blogpost of Rudy De Busscher. This entity listener will publish a read event. An event listener will pick up the event and publish it to a JMS Queue using a JMS Template. A JMS Message listener will pick up the message and log it to stdout.

A quick overview:

 1.  An entity read happens
 2.  This entity read triggers an [EntityListener](https://docs.jboss.org/hibernate/entitymanager/3.5/reference/en/html/listeners.html)
 3.  This EntityListener (Audit) publishes a "ReadEvent" using spring events
 4.  The (spring) event listener "ReadEventListener" picks up this event and publishes it to a JMS Queue
 5.  JMS Queue listener handles this JMS Message and logs it to stdout (normally a different application would handle it)
 
  ![solution description](https://davyvanroy.be/wp-content/uploads/2015/04/jpa_audit_solution.png)

As you may notice step 3 and 4 are actually not necessary, we could immediately let the entity listener publish its event to the JMS queue but I wanted to show the [improved application events in Spring](http://spring.io/blog/2015/02/11/better-application-events-in-spring-framework-4-2). And if we ever need to log a different kind of ReadEvent (ex. a NoSql read event), we simple have to fire the necessary event for it and we do not have to care anymore about or how it needs to be logged. Imagine we ever want to change the way ReadEvents are published (JMS), we only have to change it in one place ([SoC](http://en.wikipedia.org/wiki/Separation_of_concerns)). 

For this, we will be using **Spring Boot** with the latest Spring version 4.2.0-SNAPSHOT.

### Basic settings ###
We will use a basic Spring Boot application for setup. So the standard "spring-boot-starter-parent" is used with some extra dependencies: 

 - spring-boot-starter-test (for basic testing)
 - spring-boot-starter-data-jpa (spring data for performing reads)
 - spring-jms (read/write JMS Queue)
 - activemq-broker (JMS broker)
 - h2 (database)

The only special feature we will add is that we will use Spring 4.2 as Spring version. For this we add the spring-snapshots repository and set the spring.version maven property to: 4.2.0-SNAPSHOT.

Full POM is available on [github](https://bitbucket.org/spring-squad/jpa-audit-jms/src/f92cc87f77618952307ea2a0adefa09bcc576d43/pom.xml?at=master).

Standard configuration:

```
#!java
    @SpringBootApplication
    public class AppConfig {
    }
```


### Entity read ###
We have a basic entity (Employee) which we can annotate with an EntityListener.


```
#!java
    @Entity
    @EntityListeners({Audit.class})
    public class Employee implements Serializable {
    ...
```


Implementation of the entity listener Audit:

```
#!java
    public class Audit {
        @PostLoad
        public void auditUsage(Object entity) {
            ContextHelper.getPublisher().publishEvent(new ReadEvent(entity));
        }
    }
```

We will publish a "ReadEvent" object that will carry information about the entity being read:


```
#!java
    public class ReadEvent {
        private Object entity;
    
        public ReadEvent(Object entity) {
            this.entity = entity;
        }
    
        public Object getEntity() {
            return entity;
        }
    }
```


Because this Audit class is not a Spring component, a small [ContextHelper](https://bitbucket.org/spring-squad/jpa-audit-jms/src/f92cc87f77618952307ea2a0adefa09bcc576d43/src/main/java/be/c4j/springsquad/infrastructure/ContextHelper.java?at=master) class was created to get access to some Spring beans (like the application event publisher).

### Publish read event ###
In the previous chapter we got the publisher from the ContextHelper class. This is the Spring [ApplicationEventPublisher](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/ApplicationEventPublisher.html). So we will use basic event handling from Spring (which might not be known by a lot of people).

### Consume read event ###
Now we can use Spring's 4.2 improved application event listeners:


```
#!java
    @Component
    public class ReadEventListener {
        @Autowired
        private JmsTemplate jmsTemplate;
    
        @Value("${spring.jms.queue.destination}")
        private String destination;
    
        @EventListener
        public void onRead(ReadEvent readEvent) {
            jmsTemplate.convertAndSend(destination, readEvent.getEntity());
        }
    }
```


We have one method here "onRead" with our "ReadEvent" object as parameter. We simply need to annotate it with @EventListener and Spring will listen to all application events and when a "ReadEvent" is triggered, this method is executed.
This way of event handling is new in Spring 4.2 (that's why I am using a snapshot repository). If you need an older version of Spring, you can still use the standard [ApplicationListener](http://docs.spring.io/autorepo/docs/spring/current/javadoc-api/org/springframework/context/ApplicationListener.html).

### Publish entity to JMS queue ###

In the method of the entity listener, we use a [JmsTemplate](http://docs.spring.io/spring-framework/docs/2.5.x/api/org/springframework/jms/core/JmsTemplate.html) to send our object being read to a JMS Queue. You may notice that we didn't configure anything for this, thank you Spring Boot! Spring Boot notices that I have JMS on the classpath and ActiveMQ, it will start an in memory active MQ and create a JMS template bean which you can autowire. Pretty sweet that we will be having a running application without any confiugration. 
We can see this when starting the application:


```
#!text
    2015-03-29 20:19:32.981  INFO 21950 --- [           main] o.apache.activemq.broker.BrokerService   : Using Persistence Adapter: MemoryPersistenceAdapter
    2015-03-29 20:19:33.148  INFO 21950 --- [           main] o.apache.activemq.broker.BrokerService   : Apache ActiveMQ 5.10.0 (localhost, ID:mcpro.local-60314-1427653172999-0:1) is starting
    2015-03-29 20:19:33.154  INFO 21950 --- [           main] o.apache.activemq.broker.BrokerService   : Apache ActiveMQ 5.10.0 (localhost, ID:mcpro.local-60314-1427653172999-0:1) started
    2015-03-29 20:19:33.154  INFO 21950 --- [           main] o.apache.activemq.broker.BrokerService   : For help or more information please see: http://activemq.apache.org
    2015-03-29 20:19:33.188  INFO 21950 --- [           main] o.a.activemq.broker.TransportConnector   : Connector vm://localhost started
```


I added one configuration myself, the destination:

```

#!properties
    spring.jms.queue.destination=audit-queue
    
```

**At this point, every entity being read by JPA, will be sent to a JMS queue.**

### Consume JMS queue ###

For consuming the JMS queue, we will use a standard JMS listener container provided by Spring. We will create a Message Listener Adapter and log every read to stdout.

Configuration for the JMS listener container:

```
#!java

    @Configuration
    public class JmsConfig {
        @Value("${spring.jms.queue.destination}")
        private String destination;
    
        @Bean
        public MessageListenerAdapter adapter(AuditLogger auditLogger) {
            MessageListenerAdapter messageListener
                    = new MessageListenerAdapter(auditLogger);
            messageListener.setDefaultListenerMethod("log");
            return messageListener;
        }
    
        @Bean
        public SimpleMessageListenerContainer container(MessageListenerAdapter messageListener,
                                                 ConnectionFactory connectionFactory) {
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setMessageListener(messageListener);
            container.setConnectionFactory(connectionFactory);
            container.setDestinationName(destination);
            return container;
        }
    }
```


And finally the implementation of the object responsible for the actual logging:


```
#!java

    @Component
    public class AuditLogger {
    
        private final Logger log = LoggerFactory.getLogger(this.getClass());
    
        public void log(Object entity) {
            log.debug(entity.toString());
        }
    }
```

Logger is configured by spring boot, you can simply change the log level in your application.properties:

```

#!properties
    logging.level.be.c4j.springsquad=DEBUG
    
```
    
    
### Run application ###
Since we are using Spring Boot, we can simply start the application, create some users and read them:
    
```
#!java

public class App {
    
        private static List<String> names = Arrays.asList(
                "Davy Van Roy",
                "Stefanie Jacobs",
                "Amélie Van Roy",
                "Lucas Van Roy"
        );
    
        public static void main(String[] args) {
            ConfigurableApplicationContext context = createContext(args);
            auditUsers(context);
        }
    
        private static ConfigurableApplicationContext createContext(String[] args) {
            ConfigurableApplicationContext context = new SpringApplicationBuilder()
                    .sources(AppConfig.class)
                    .run(args);
            context.registerShutdownHook();
            return context;
        }
    
        private static void auditUsers(ConfigurableApplicationContext context) {
            EmployeeRepository repository = context.getBean(EmployeeRepository.class);
            createUsers(repository);
            readUsers(repository);
        }
    
        private static void createUsers(EmployeeRepository repository) {
            names.stream()
                    .map(Employee::new)
                    .forEach(repository::save);
        }
    
        private static void readUsers(EmployeeRepository repository) {
            names.stream()
                    .forEach(repository::findByName);
        }
    }
```


And we have success according to the output:

```
#!text

    2015-03-29 21:44:41.486 DEBUG 22169 --- [ Session Task-1] b.c.s.infrastructure.audit.AuditLogger   : Employee{id=1, name='Davy Van Roy'}
    2015-03-29 21:44:41.493 DEBUG 22169 --- [ Session Task-1] b.c.s.infrastructure.audit.AuditLogger   : Employee{id=2, name='Stefanie Jacobs'}
    2015-03-29 21:44:41.498 DEBUG 22169 --- [ Session Task-1] b.c.s.infrastructure.audit.AuditLogger   : Employee{id=3, name='Amélie Van Roy'}
    2015-03-29 21:44:41.504 DEBUG 22169 --- [ Session Task-1] b.c.s.infrastructure.audit.AuditLogger   : Employee{id=4, name='Lucas Van Roy'}
     

```

## Conclusion ##
We were able to use standard functionality from Spring to solve the problem, and it was pretty straight forward as well. The pieces of the puzzle fit nicely together. Using Spring Boot we were able to set up the environment in a matter of minutes, which is pretty sweet. 

If you really want to know more details, please take a look at the [source code](https://bitbucket.org/spring-squad/jpa-audit-jms/src/f92cc87f7761?at=master). Only parts of the code were shown in this blogpost to avoid clutter (but actually, there is not a lot of code).

## References ##

 - [Using JMS Queue to audit JPA entity reads](http://c4j.be/blog/using-jms-queue-audit-jpa-entity-reads) by Rudy De Busscher
 - [Better application events in Spring Framework 4.2](http://spring.io/blog/2015/02/11/better-application-events-in-spring-framework-4-2)