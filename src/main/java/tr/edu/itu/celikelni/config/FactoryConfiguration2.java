package tr.edu.itu.celikelni.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import tr.edu.itu.celikelni.entity.Events;
import tr.edu.itu.celikelni.entity.States;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableStateMachineFactory(name = "factory_config_2")
public class FactoryConfiguration2 extends EnumStateMachineConfigurerAdapter<States, Events> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /** Default Constructor **/
    public FactoryConfiguration2() {
        logger.info(" ++++++++++ CONSTRUCTOR FOR factory_config_2 ++++++++++");
    }

    @PostConstruct
    public void init() {
        logger.info(" ++++++++++ POSTCONSTRUCTOR FOR factory_config_2 ++++++++++");
    }


    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states.withStates()
                .initial(States.UNPAID, initializationAction())
                .stateEntry(States.WAITING_FOR_RECEIVE,entryActionForWaiting())
                .stateExit(States.WAITING_FOR_RECEIVE, exitActionForWaiting())
                .stateEntry(States.DONE, entryActionForDone())
                .stateExit(States.DONE, exitActionForDone());
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
                .withConfiguration()
                .machineId("machine-without-zk");
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        /** Defines "EXTERNAL" type of transitions **/
        transitions
                .withExternal()
                .source(States.UNPAID).target(States.WAITING_FOR_RECEIVE)
                .event(Events.PAY)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.WAITING_FOR_RECEIVE).target(States.DONE)
                .event(Events.RECEIVE)
                .action(transitionAction())
                .and()
                .withExternal()
                .source(States.DONE).target(States.UNPAID)
                .event(Events.STARTFROMSCRATCH)
                .action(transitionAction());
    }

    @Bean
    public Action<States, Events> entryActionForWaiting() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                logger.info("FactoryConfiguration2::entryActionForWaitingState");
                Integer localVar = context.getExtendedState().get("localVarForWaiting", Integer.class);
                localVar = localVar + 20;
                context.getExtendedState().getVariables().put("localVarForWaiting", localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> exitActionForWaiting() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                logger.info("FactoryConfiguration2::exitActionForWaitingState");
                Integer localVar = context.getExtendedState().get("localVarForWaiting", Integer.class);
                logger.info("FactoryConfiguration2::exitActionForWaitingState::PrintLocalVar()" + localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> entryActionForDone() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                logger.info("FactoryConfiguration2::entryActionForDoneState");
                Integer localVar = context.getExtendedState().get("localVarForDone", Integer.class);
                localVar = localVar + 50;
                context.getExtendedState().getVariables().put("localVarForDone", localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> exitActionForDone() {
        return new Action<States, Events>() {

            @Override
            public void execute(StateContext<States, Events> context) {
                logger.info("FactoryConfiguration2::exitActionForDoneState");
                Integer localVar = context.getExtendedState().get("localVarForDone", Integer.class);
                logger.info("FactoryConfiguration2::exitActionForDoneState::PrintLocalVar()" + localVar);
            }
        };
    }

    @Bean
    public Action<States, Events> initializationAction() {
        return new Action<States, Events>() {
            @Override
            public void execute(StateContext<States, Events> context) {
                logger.info("FactoryConfiguration2::entryActionForInitalization");
                /** Define extended state variable as common variable used inside transition actions **/
                context.getExtendedState().getVariables().put("common", 0);
                /** Define extended state variable as private/local variable used inside state actions **/
                context.getExtendedState().getVariables().put("localVarForWaiting",10);
                context.getExtendedState().getVariables().put("localVarForDone",50);
            }
        };
    }

    @Bean
    public Action<States, Events> transitionAction() {
        return new Action<States, Events>() {
            @Override
            public void execute(StateContext<States, Events> context) {
                logger.info("FactoryConfiguration2::transitionAction");
                /* Get timeSleep from StateContext */
                Object sleep = context.getMessageHeaders().get("timeSleep");
                long longSleep = ((Number) sleep).longValue();
                /* Get processed event from StateContext */
                Object O_event = context.getMessageHeaders().get("processedEvent");
                String processedEvent = O_event.toString();
                /* Get UUID from StateContext and then print */
                Object O_UUID = context.getMessageHeaders().get("machineId");
                UUID uuid = UUID.fromString(O_UUID.toString());
                Map<Object, Object> variables = context.getExtendedState().getVariables();
                Integer commonVar = context.getExtendedState().get("common", Integer.class);

                if (commonVar == 0) {
                    logger.info("Switch common variable from 0 to 1");
                    variables.put("common", 1);
                    sleepForAWhile(longSleep);
                } else if (commonVar == 1) {
                    logger.info("Switch common variable from 1 to 2");
                    variables.put("common", 2);
                    sleepForAWhile(longSleep);
                } else if (commonVar == 2) {
                    logger.info("Switch common variable from 2 to 0");
                    variables.put("common", 0);
                    sleepForAWhile(longSleep);
                }

                try {
                    PerformCheckpoint(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void sleepForAWhile(Long sleepTime){
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException ex) {
            System.out.println("Exception during sleepForAWhile --> " + ex.toString());
        }

    }

    public void PerformCheckpoint(@NotNull StateContext<States, Events> context) throws Exception {
        //System.out.println("----- PERFORM CKPT -----");
        //Map<Object, Object> variables = context.getExtendedState().getVariables();
        //Map<String, ___Checkpoint> checkpoints = (Map<String, ___Checkpoint>) context.getExtendedState().getVariables().get("CKPT");
        /* Get state machine UUID from StateContext */
        Object O_UUID = context.getMessageHeaders().get("machineId");
        UUID uuid = UUID.fromString(O_UUID.toString());
        /* Get source and target states from StateContext */
        Object O_source = context.getMessageHeaders().get("source");
        String sourceState = O_source.toString();
        Object O_target = context.getMessageHeaders().get("target");
        String targetState = O_target.toString();
        /* Get processed event from StateContext */
        Object O_event = context.getMessageHeaders().get("processedEvent");
        String processedEvent = O_event.toString();
        /* Create a new CKPT db object */
        //CheckpointDbObject dbObject = new CheckpointDbObject(getTimeStamp(), uuid, sourceState,processedEvent, targetState);
        //dbObjectHandler.insertCheckpoint(dbObject);
    }


}

