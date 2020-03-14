package tr.edu.itu.celikelni;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tr.edu.itu.celikelni.service.StateMachineWorker;


@SpringBootApplication
@ComponentScan(basePackages = {"tr.edu.itu.celikelni"})
public class Application implements CommandLineRunner {

    @Autowired
    private StateMachineWorker worker;

    @Override
    public void run(String... args) throws Exception {

        /*Sending events to state machines */
        worker.sendPayEvent("Pay", 0,1000);
        worker.sendReceiveEvent("Receive", 0,1000);
        worker.sendStartFromScratchEvent("StartFromScratch", 0,1000);

        worker.sendPayEvent("Pay", 0,1000);
        worker.sendReceiveEvent("Receive", 0,1000);
        worker.sendStartFromScratchEvent("StartFromScratch", 0,1000);

        worker.sendPayEvent("Pay", 0,1000);
        worker.sendReceiveEvent("Receive", 0,1000);
        worker.sendStartFromScratchEvent("StartFromScratch", 0,1000);

        worker.stop();

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
