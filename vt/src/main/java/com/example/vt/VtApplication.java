package com.example.vt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
public class VtApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtApplication.class, args);
    }

}

@Controller
@ResponseBody
class DemoController {

    private final Object monitor = new Object();

//    final native void callNativeCPP ();

    // Java 24+
    // thread pinning happens with synchronizatijon + IO (21-23), native code (24+)
    void weird() throws InterruptedException {

        Executor executors = Executors.newVirtualThreadPerTaskExecutor();
        executors.execute(() -> {
            IO.println("im executing on a virtual thread");
        }) ;
        // 10 * 1_000_000


        Map<Thread, Object> map;


        // 1:1 one Java Thread = one OS Thread
        java.lang.Thread t = new Thread(() -> {
            // uses OS threads
        });
        java.lang.Thread osThread = Thread.ofPlatform().unstarted(new Runnable() {
            @Override
            public void run() {

            }
        });
        java.lang.Thread virtualThread = Thread.ofVirtual().name("").unstarted(new Runnable() {
            @Override
            public void run() {

            }
        });

        // print thread befire
        Thread.sleep(5000);
        // print thread after
        // Project Panama
//        Class.forName("") <- uses native code!
//        callNativeCPP();

        this.http.get().uri("http://localhost/delay/5").retrieve().body(String.class); // wait 5s!
        synchronized (this.monitor) {
            // "carrier threads"
            // virtual threads ...
        }
    }

    private final RestClient http;

    DemoController(RestClient.Builder http) {
        this.http = http.build();
    }

    @GetMapping("/demo")
    String get() {
        var st = "" + Thread.currentThread() + ":";
        var response = this.http.get().uri("http://localhost/delay/5").retrieve().body(String.class); // wait 5s!
        st += Thread.currentThread();
        IO.println(st);
        return response;
    }
}
