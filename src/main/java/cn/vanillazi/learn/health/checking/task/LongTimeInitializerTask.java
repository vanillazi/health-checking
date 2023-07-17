package cn.vanillazi.learn.health.checking.task;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.TimeUnit;

@Component
public class LongTimeInitializerTask {

    private static final Logger logger= LoggerFactory.getLogger(LongTimeInitializerTask.class);
    @PostConstruct
    public void onInit() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        logger.info("initialized!");
    }
}
