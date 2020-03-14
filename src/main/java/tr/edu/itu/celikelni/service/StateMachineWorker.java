package tr.edu.itu.celikelni.service;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.stereotype.Service;
import tr.edu.itu.celikelni.entity.Events;
import tr.edu.itu.celikelni.entity.States;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Service
public class StateMachineWorker {

    static class ExitHook extends Thread {
        @Autowired
        private StateMachine<States, Events> stateMachine;

        private Scanner scanner;

        static final Logger logger = LoggerFactory.getLogger(ExitHook.class);

        public ExitHook(StateMachine<States,Events> sm, Scanner sc){
            this.stateMachine = sm;
            this.scanner = sc;
        }

        @Override
        public void run(){
            logger.info("**********************************");
            logger.info("*****Gracefully stopping SMOC*****");
            logger.info("**********************************");
            this.scanner.close();
            this.stateMachine.stop();
        }
    }

    /*
    @Autowired
    @Qualifier("factory_config_1")
    private StateMachineFactory<States, Events> factory_config_1;
    */

    @Autowired
    @Qualifier("factory_config_2")
    private StateMachineFactory<States, Events> factory_config_2;


    private StateMachine<States, Events> stateMachine;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public StateMachineWorker(){
        logger.info("+++++StateMachineWorker::Constructor+++++");
    }

    @PostConstruct
    public void init() {
        logger.info("+++++StateMachineWorker::PostConstruct+++++");

        /*
        stateMachine = factory_config_1.getStateMachine();
        logger.info("UUID from factory_with_zk is {}",stateMachine.getUuid());
        stateMachine.start();
        */

        stateMachine = factory_config_2.getStateMachine();
        logger.info("UUID from factory_with_zk is {}",stateMachine.getUuid());
        stateMachine.start();

        logger.info("SMOC __{}__ is started. From now on, events can be processed.",stateMachine.getUuid().toString());
        logger.info("Registers an exit hook which runs when the JVM is shut down.");
        InputStream stream = System.in;
        Scanner scanner = new Scanner(stream);
        Runtime.getRuntime().addShutdownHook(new ExitHook(this.stateMachine,scanner));

    }

    public void ProcessEvent(String event, Integer eventNumber, int timeSleep) throws Exception {
        switch(event){
            case "Pay": case "pay": case "PAY":
                System.out.print("\n\n\n\n\n");
                sendPayEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            case "Receive": case "receive": case "RECEIVE":
                System.out.print("\n\n\n\n\n");
                sendReceiveEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            case "StartFromScratch": case "startfromscratch": case"STARTFROMSCRATCH":
                System.out.print("\n\n\n\n\n");
                sendStartFromScratchEvent(event, eventNumber,timeSleep);
                System.out.print("\n\n\n\n\n");
                break;
            default:
                System.out.println("Please send one of the events below.");
                System.out.println("Pay/Receive/StartFromScratch");
                break;
        }

    }

    public void sendPayEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
        logger.info("{}.event will be processed",eventNumber);

        Message<Events> messagePay = MessageBuilder
                .withPayload(Events.PAY)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "UNPAID")
                .setHeader("processedEvent", event)
                .setHeader("target","WAITING_FOR_RECEIVE")
                .build();
        stateMachine.sendEvent(messagePay);


    }

    public void sendReceiveEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
        logger.info("{}.event will be processed",eventNumber);

        Message<Events> messageReceive = MessageBuilder
                .withPayload(Events.RECEIVE)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "WAITING_FOR_RECEIVE")
                .setHeader("processedEvent", event)
                .setHeader("target", "DONE")
                .build();
        stateMachine.sendEvent(messageReceive);
    }

    public void sendStartFromScratchEvent(@NotNull String event, Integer eventNumber, int timeSleep) throws Exception {
        logger.info("{}.event will be processed",eventNumber);

        Message<Events> messageStartFromScratch = MessageBuilder
                .withPayload(Events.STARTFROMSCRATCH)
                .setHeader("timeSleep", timeSleep)
                .setHeader("machineId", stateMachine.getUuid())
                .setHeader("source", "DONE")
                .setHeader("processedEvent", event)
                .setHeader("target","UNPAID")
                .build();
        stateMachine.sendEvent(messageStartFromScratch);

    }

    public void stop(){
        logger.info("... Stopping state machine ...");
        stateMachine.stop();
    }

}

