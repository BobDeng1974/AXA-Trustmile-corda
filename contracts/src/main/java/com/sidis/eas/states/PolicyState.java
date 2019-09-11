package com.sidis.eas.states;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sidis.eas.contracts.ServiceContract;
import com.sidis.eas.contracts.StateMachine;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@BelongsToContract(ServiceContract.class)
public class PolicyState implements LinearState {


    @NotNull
    private final UniqueIdentifier id;
    @NotNull
    private final StateMachine.State state;
    //POLICY NAME
    @NotNull
    private final String policyNumber;

    @JsonIgnore
    @NotNull
    private final Party insuredCar;

    @NotNull
    private final Map<String, Object> eventData;

    @JsonIgnore
    private final Party insurer;

    @NotNull
    private final String vin;

    @ConstructorForDeserialization
    public PolicyState(@NotNull UniqueIdentifier id, @NotNull String policyNumber, @NotNull Party insuredCar, @NotNull StateMachine.State state, Map<String, Object> eventData, Party insurer, @NotNull String vin) {
        this.id = id;
        this.state = state;
        this.policyNumber = policyNumber;
        this.insuredCar = insuredCar;
        this.eventData = eventData == null ? new LinkedHashMap<>() : eventData;
        this.insurer = insurer;
        this.vin = vin;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.id;
    }

    @NotNull
    @JsonIgnore
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> list = new ArrayList<>();
        list.add(this.insuredCar);
        if (this.insurer != null) list.add(this.insurer);
        return list;
    }

    @NotNull
    @JsonIgnore
    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    @NotNull
    public UniqueIdentifier getId() {
        return id;
    }
    @NotNull
    public String getPolicyNumber() {
        return policyNumber;
    }
    @NotNull
    public Party getInsuredCar() {
        return insuredCar;
    }
    @NotNull
    public StateMachine.State getState() { return state; }
    public Party getInsurer() {
        return insurer;
    }
    @NotNull
    public String getVin() {
        return vin;
    }



    @NotNull
    public String getInitiatorX500() {
        return insuredCar.getName().getX500Principal().getName();
    }
    public String getServiceProviderX500() {
        return insurer != null ? insurer.getName().getX500Principal().getName() : "";
    }

    // TODO : not sure the Event Data here makes sense...should it be something different?
    public Map<String, Object> getEventData() {
        return eventData;
    }

    public List<AbstractParty> getCounterParties(Party me) {
        if (me != null) {
            if (this.getInsurer() != null) {
                if (this.getInsuredCar().equals(me)) return Arrays.asList(this.insurer);
                if (this.getInsurer().equals(me)) return Arrays.asList(this.insuredCar);
            } else {
                if (this.getInsuredCar().equals(me)) return Collections.EMPTY_LIST;
            }
        }
        return Collections.EMPTY_LIST;
    }


    /* actions CREATE */
    public static PolicyState create(@NotNull UniqueIdentifier id, @NotNull String serviceName, @NotNull Party initiator, Map<String, Object> serviceData) {
        return new PolicyState(id, serviceName, initiator, StateMachine.StateTransition.CREATE.getInitialState(), serviceData, null, null);
    }
    /* actions UPDATE */
    public PolicyState update(Map<String, Object> newServiceData) {
        return this.update(newServiceData, this.vin);
    }

    public PolicyState update(Map<String, Object> newServiceData, String vin) {
        StateMachine.State newState = StateMachine.StateTransition.UPDATE.getNextStateFrom(this.state);
        return new PolicyState(this.id, this.policyNumber, this.insuredCar, newState, newServiceData, this.insurer, vin);
    }

    /* actions SHARE */
    public PolicyState share(@NotNull Party newServiceProvider) {
        StateMachine.State newState = StateMachine.StateTransition.SHARE.getNextStateFrom(this.state);
        return new PolicyState(this.id, this.policyNumber, this.insuredCar, newState, this.eventData, newServiceProvider, this.vin);
    }


    /* actions any */
    public PolicyState withAction(StateMachine.StateTransition transition) {
        StateMachine.State newState = transition.getNextStateFrom(this.state);
        return new PolicyState(this.id, this.policyNumber, this.insuredCar, newState, this.eventData, this.insurer, this.vin);
    }

    public String getData(String keys) {
        return JsonHelper.getDataValue(this.getEventData(), keys);
    }

}
