package com.sidis.eas.client.webserver;
import ch.cordalo.corda.common.contracts.JsonHelper;
import com.google.common.collect.ImmutableMap;
import com.sidis.eas.contracts.StateMachine;
import com.sidis.eas.client.pojo.CarPolicy;
import com.sidis.eas.client.pojo.CarEvent;
import com.google.common.collect.ImmutableList;
import com.sidis.eas.flows.CarFlow;
import com.sidis.eas.states.CarState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public static final boolean DEBUG = false;

    private final CordaRPCOps proxy;
    private final CordaX500Name myLegalName;

    @Autowired
    private  SimpMessagingTemplate messagingTemplate;

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
            value = "/car-event",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpStatus sendCarEvent(HttpServletRequest request, @RequestBody CarEvent carEvent)
    {
        try {
            logger.info(carEvent.toString());
            return HttpStatus.CREATED;

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return HttpStatus.BAD_REQUEST;

        }
    }

    @RequestMapping(
            value = "/car-policy",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpStatus sendCarPolicy(HttpServletRequest request, @RequestBody CarPolicy carPolicy)
    {
        try {
            UniqueIdentifier uid = new UniqueIdentifier(carPolicy.getVin());
            Party insurerParty = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(carPolicy.getInsurer()));
            if (insurerParty == null){
                logger.error("party not found "+carPolicy.getInsurer());
                return HttpStatus.BAD_REQUEST;
            }

            final SignedTransaction signedTx = proxy
                    .startTrackedFlowDynamic(
                            CarFlow.Create.class,
                            // String policyNumber, Party insurer, String vin, Integer mileagePerYear, Integer insuranceRate, String details
                            carPolicy.getPolicyNumber(),
                            insurerParty,
                            carPolicy.getVin(),
                            carPolicy.getMileagePerYear(),
                            carPolicy.getInsuranceRate(),
                            JsonHelper.convertJsonToString(carPolicy.getAdditionalProperties())
                        )
                    .getReturnValue()
                    .get();
            return HttpStatus.CREATED;
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return HttpStatus.BAD_REQUEST;

        }
    }

    @RequestMapping(value = "/car-policy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CarPolicy getCarPolicy (HttpServletRequest request)
    {
        //TODO: Setup real method from CORDA
        return randomCarGenerator("42");
    }

    @RequestMapping(value = "/car-event", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CarEvent getCarEvent (HttpServletRequest request)
    {
        //TODO: Setup real method from CORDA
        return randomCarEventGenerator();
    }





    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, CordaX500Name> whoami() {
        return ImmutableMap.of("me", myLegalName);
    }

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GetMapping(value =  "/peers", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, List<CordaX500Name>> getPeers() {
        List<NodeInfo> nodeInfoSnapshot = proxy.networkMapSnapshot();
        return ImmutableMap.of("peers", nodeInfoSnapshot
                .stream()
                .map(node -> node.getLegalIdentities().get(0).getName())
                .filter(name -> !name.equals(myLegalName) && !serviceNames.contains(name.getOrganisation()))
                .collect(toList()));
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
    private CarEvent randomCarEventGenerator(){
        // O=AXA Versicherungen AG,L=Winterthur,ST=ZH,C=CH
        String vin = myLegalName.getOrganisation().startsWith("AXA") ? "1FMHK7B80CGA07773" : "JTEBU5JR7A5006904";
        CarEvent carEvent = new CarEvent();
        carEvent.setCar((random.nextInt()%2)==0?"Ferrari " + random.nextInt(50):"McLaren " + random.nextInt(50));
        carEvent.setVin(vin);
        carEvent.setMileage(random.nextInt(500000));
        carEvent.setAccident((random.nextInt()%2)==0);
        carEvent.setTimestamp(1500000000+ random.nextInt(1000000));
        return carEvent;
    }

    /*
    *       "policyNumber",
            "vin",
            "car",
            "insurer",
            "mileagePerYear",
            "mileageState",
            "accidentState",
            "insuranceRate",
            "data"
     */

    // PRIVATE METHODS
    private CarPolicy randomCarGenerator(String carId){
        CarPolicy car = new CarPolicy();
        car.setPolicyNumber("18.123.121");
        car.setVin("WAURFAFR4EA012488");
        car.setCar("Ferrari Modena");
        car.setInsurer("AXA Versicherungen AG");
        car.setMileagePerYear(7000);
        car.setMileageState("IN_RANGE");
        car.setAccidentState("NO");
        car.setInsuranceRate(1500);
        car.setData("trustIssuer", "AXA Versicherungen AG");
        car.setData("originalPrice", 152000);
        car.setData("image", "car-black.jpg");
        car.setData("color", "RED");
        car.setData("numberOfPreviousOwners", 1);
        car.setData("model", "Modena");
        return car;

    }



}