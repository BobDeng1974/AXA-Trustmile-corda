package com.sidis.eas.client.webserver;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sidis.eas.client.pojo.CarEvent;
import com.sidis.eas.client.pojo.CarPolicy;
import com.sidis.eas.flows.CarEventFlow;
import com.sidis.eas.flows.CarFlow;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
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
import static net.corda.core.node.services.vault.QueryCriteriaUtils.DEFAULT_PAGE_NUM;


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
            logger.info("car event="+carEvent.toString());

            if(hasAtLeastOneEvent()) {
                final SignedTransaction signedTx = proxy
                        .startTrackedFlowDynamic(
                                CarEventFlow.Update.class,
                                //  String vin, Integer timestamp, Long mileage, Boolean accident, String data
                                carEvent.getVin(),
                                carEvent.getTimestamp(),
                                carEvent.getMileage(),
                                carEvent.getAccident(),
                                JsonHelper.convertJsonToString(carEvent.getAdditionalProperties())
                        )
                        .getReturnValue()
                        .get();
            }else {

                final SignedTransaction signedTx = proxy
                        .startTrackedFlowDynamic(
                                CarEventFlow.Create.class,
                                //  String vin, Integer timestamp, Long mileage, Boolean accident, String data
                                carEvent.getVin(),
                                carEvent.getTimestamp(),
                                carEvent.getMileage(),
                                carEvent.getAccident(),
                                JsonHelper.convertJsonToString(carEvent.getAdditionalProperties())
                        )
                        .getReturnValue()
                        .get();

            }
            CarState car = getCarStateFromExternalId(carEvent.getVin());

            final SignedTransaction signedTx2 = proxy
                    .startTrackedFlowDynamic(
                            CarFlow.Update.class,
                            car.getPolicyNumber(),
                            car.getInsurer(),
                            car.getMileagePerYear(),
                            car.getInsuranceRate(),
                            JsonHelper.convertJsonToString(car.getDetails())
                    )
                    .getReturnValue()
                    .get();
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
            Party insurerParty = null;
            if (carPolicy.getInsurer() != null && !carPolicy.getInsurer().equals("")) {
                insurerParty = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(carPolicy.getInsurer()));
                if (insurerParty == null) {
                    logger.error("party not found " + carPolicy.getInsurer());
                    return HttpStatus.BAD_REQUEST;
                }
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
                            JsonHelper.convertJsonToString(carPolicy.getDetails())
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
        CarState car = getUnconsumedCar();
        if (car != null) {
            CarPolicy carPolicy = new CarPolicy();
            carPolicy.setAccidentState(car.getAccidentState().toString());
            carPolicy.setCar(car.getCarX500());
            carPolicy.setDetailsMap(car.getDetails());
            carPolicy.setInsuranceRate(car.getInsuranceRate());
            carPolicy.setInsurer(car.getInsurereX500());
            carPolicy.setState(car.getState().toString());
            carPolicy.setMileageState(car.getMileageState().toString());
            carPolicy.setAccidentState(car.getAccidentState().toString());
            carPolicy.setMileagePerYear(car.getMileagePerYear());
            carPolicy.setPolicyNumber(car.getPolicyNumber());
            return carPolicy;
        } else {
            return randomCarGenerator("42");
        }
    }

    @RequestMapping(value = "/car-event", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CarEvent getCarEvent (HttpServletRequest request)
    {
        //TODO: Setup real method from CORDA
        CarEventState carEventState = getUnconsumedCarEvent();
        if (carEventState != null) {
            CarEvent carEvent = new CarEvent();
            carEvent.setCar(carEventState.getInsuredCarX500());
            carEvent.setAccident(carEventState.getAccident());
            carEvent.setDataMap(carEventState.getData());
            carEvent.setMileage(carEventState.getMileage());
            carEvent.setTimestamp(carEventState.getTimestamp());
            carEvent.setVin(carEventState.getVin());
            return carEvent;
        }else{
            return randomCarEventGenerator();
        }
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
    private CarState getUnconsumedCar() {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                null,
                null,
                Vault.StateStatus.UNCONSUMED,
                null);
        List<CarState> carStates = proxy.vaultQueryByCriteria(queryCriteria, CarState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        return carStates.isEmpty() ? null : carStates.get(carStates.size() - 1);
    }

    private CarState getCarStateFromExternalId(String id) {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                null,
                Arrays.asList(id),
                Vault.StateStatus.UNCONSUMED,
                null);
        List<CarState> carStates = proxy.vaultQueryByCriteria(queryCriteria, CarState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        return carStates.isEmpty() ? null : carStates.get(carStates.size() - 1);
    }

    private CarEventState getUnconsumedCarEvent() {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                null,
                null,
                Vault.StateStatus.UNCONSUMED,
                null);
        List<CarEventState> carEventStates = proxy.vaultQueryByCriteria(queryCriteria, CarEventState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        return carEventStates.size() == 0 ? null : carEventStates.get(carEventStates.size() - 1);
    }
    private boolean hasAtLeastOneEvent() {
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                null,
                null,
                Vault.StateStatus.ALL,
                null);
        PageSpecification paging = new PageSpecification(DEFAULT_PAGE_NUM,1);
        return proxy.vaultQueryByWithPagingSpec(CarEventState.class, queryCriteria, paging).getTotalStatesAvailable() > 0;
    }


    private CarEvent randomCarEventGenerator(){
        // O=AXA Versicherungen AG,L=Winterthur,ST=ZH,C=CH
        String vin = myLegalName.getOrganisation().startsWith("AXA") ? "1FMHK7B80CGA07773" : "JTEBU5JR7A5006904";
        CarEvent carEvent = new CarEvent();
        carEvent.setCar((random.nextInt()%2)==0?"Ferrari " + random.nextInt(50):"McLaren " + random.nextInt(50));
        carEvent.setVin(vin);
        carEvent.setMileage(new Long(random.nextInt(50000)));
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
        car.setVin("42");
        car.setCar("Ferrari Modena");
        car.setInsurer("AXA Versicherungen AG");
        car.setMileagePerYear(7000);
        car.setMileageState("IN_RANGE");
        car.setAccidentState("NO");
        car.setInsuranceRate(1500);
        car.setDetails("originalPrice", 152000);
        car.setDetails("color", "RED");
        car.setDetails("numberOfPreviousOwners", 1);
        car.setDetails("model", "Ferrari Modena");
        return car;

    }



}