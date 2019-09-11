package com.sidis.eas.client.webserver;
import com.google.common.collect.ImmutableMap;
import com.sidis.eas.contracts.StateMachine;
import com.sidis.eas.client.pojo.CarPolicy;
import com.sidis.eas.client.pojo.CarEvent;
import com.google.common.collect.ImmutableList;
import com.sidis.eas.states.CarState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.util.stream.Collectors.toList;


/**
 * Define your API endpoints here.
 * supported by example for WebSockets
 * https://www.toptal.com/java/stomp-spring-boot-websocket
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/") // The paths for HTTP requests are relative to this base path.
public class Controller {

    Random random = new Random();


    public static final boolean DEBUG = true;

    private final CordaRPCOps proxy;
    private final CordaX500Name myLegalName;

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

    // PUBLIC METHODS
    @RequestMapping(
            value = "/car",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpStatus sendCarEvent(HttpServletRequest request, @RequestBody CarEvent carEvent)
    {
        try {
            logger.info(carEvent.toString());
            return HttpStatus.OK;

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return HttpStatus.BAD_REQUEST;

        }
    }

    @RequestMapping(
            value = "/policy",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpStatus sendCarPolicy(HttpServletRequest request, @RequestBody CarPolicy carPolicy)
    {
        try {
            logger.info(carPolicy.toString());
            return HttpStatus.OK;

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return HttpStatus.BAD_REQUEST;

        }
    }


    @RequestMapping(value = "/car/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CarEvent getCarEvent (HttpServletRequest request, @PathVariable("id") String carId)
    {
        //TODO: Setup real method from CORDA
        return randomCarEventGenerator(carId);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, CordaX500Name> whoami() {
        return ImmutableMap.of("me", myLegalName);
    }


    // NEW METHODS

    /**
     * returns the patient records that exist in the node's vault.
     */
    @GetMapping(value = "/car-records", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CarState> getPolicyRecords() {
        List<CarState> states = proxy.vaultQuery(CarState.class).getStates()
                .stream().map(state -> state.getState().getData()).collect(toList());
        return states;
    }
    /**
     * receives a mandate that exist with a given ID from the node's vault.
     * @param id unique identifier as UUID for mandate
     */
    @RequestMapping(
            value =  "/policy-records/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public CarState getPolicyRecords(@PathVariable("id") String id) {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Arrays.asList(uid),
                Vault.StateStatus.ALL,
                null);
        List<CarState> states = proxy.vaultQueryByCriteria(queryCriteria, CarState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        return states.isEmpty() ? null : states.get(states.size()-1);
    }




    // PRIVATE METHODS
    private CarEvent randomCarEventGenerator(String carId){
        CarEvent carEvent = new CarEvent();
        carEvent.setCar((random.nextInt()%2)==0?"Ferrari " + random.nextInt(50):"McLaren " + random.nextInt(50));
        carEvent.setVin(carId);
        carEvent.setMileage(random.nextInt(500000));
        carEvent.setAccident((random.nextInt()%2)==0);
        carEvent.setTimestamp(1500000000+ random.nextInt(1000000));
        return carEvent;

    }



}