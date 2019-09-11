package com.sidis.eas.client.webserver;
import com.sidis.eas.contracts.StateMachine;
import com.sidis.eas.client.pojo.CarPolicy;
import com.sidis.eas.client.pojo.CarEvent;
import com.google.common.collect.ImmutableList;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * Define your API endpoints here.
 * supported by example for WebSockets
 * https://www.toptal.com/java/stomp-spring-boot-websocket
 */
@RestController
@CrossOrigin(origins = "http://localhost:63342")
@RequestMapping("/api/v1/") // The paths for HTTP requests are relative to this base path.
public class Controller {

    public static final boolean DEBUG = true;

    private final CordaRPCOps proxy;
    private final CordaX500Name myLegalName;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final List<String> serviceNames = ImmutableList.of("Notary");

    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    public Controller(NodeRPCConnection rpc) {
        StateMachine.State.values();
        StateMachine.StateTransition.values();
        if (DEBUG && rpc.proxy == null) {
            this.proxy = null;
            this.myLegalName = null;
            return;
        }
        this.proxy = rpc.proxy;
        this.myLegalName = rpc.proxy.nodeInfo().getLegalIdentities().get(0).getName();

    }

    @RequestMapping(
            value = "/car/{id}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpStatus sendCarEvent(HttpServletRequest request, @PathVariable("id") String carId, @RequestBody CarEvent carEvent)
    {
        try {
            if (!(carId.equals(carEvent.getVin()))){
                logger.error("Car ID " + carId +" not matching with " + carEvent.getVin() );
                return HttpStatus.EXPECTATION_FAILED;
            }
            return HttpStatus.OK;

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return HttpStatus.BAD_REQUEST;

        }
    }

    @RequestMapping(
            value = "/policy/{id}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpStatus sendCarPolicy(HttpServletRequest request, @PathVariable("id") String carId, @RequestBody CarPolicy carPolicy)
    {
        try {
            if (!(carId.equals(carPolicy.getVin()))){
                logger.error("Car ID " + carId +" not matching with " + carPolicy.getVin() );
                return HttpStatus.EXPECTATION_FAILED;
            }
            return HttpStatus.OK;

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return HttpStatus.BAD_REQUEST;

        }
    }


}