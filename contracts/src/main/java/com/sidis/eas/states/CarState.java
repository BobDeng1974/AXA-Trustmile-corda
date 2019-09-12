package com.sidis.eas.states;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sidis.eas.contracts.CarContract;
import com.sidis.eas.contracts.ServiceContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@BelongsToContract(CarContract.class)
public class CarState implements LinearState {

    @CordaSerializable
    public enum State {
        VALID,
        FRAUD
    }
    @CordaSerializable
    public enum MileageState {
        IN_RANGE,
        ABOVE_LIMIT
    }
    @CordaSerializable
    public enum AccidentState {
        NO,
        ONE,
        MORE_THAN_ONE
    }

    @NotNull
    private final UniqueIdentifier id;

    @NotNull
    private final State state;

    @NotNull
    private final String policyNumber;

    @JsonIgnore
    @NotNull
    private final Party car;

    @JsonIgnore
    private final Party insurer;

    @NotNull
    private final String vin;

    private final Integer mileagePerYear;

    @NotNull
    private final MileageState mileageState;

    @NotNull
    private final AccidentState accidentState;

    private final Integer insuranceRate;

    private final Map<String, Object> details;

    @ConstructorForDeserialization
    public CarState(@NotNull UniqueIdentifier id, @NotNull State state, @NotNull String policyNumber,
                    @NotNull Party car, Party insurer, @NotNull String vin, Integer mileagePerYear,
                    @NotNull MileageState mileageState, @NotNull AccidentState accidentState, Integer insuranceRate,
                    Map<String, Object> details) {
        this.id = id;
        this.state = state;
        this.policyNumber = policyNumber;
        this.car = car;
        this.insurer = insurer;
        this.vin = vin;
        this.mileagePerYear = mileagePerYear;
        this.mileageState = mileageState;
        this.accidentState = accidentState;
        this.insuranceRate = insuranceRate;
        this.details = new LinkedHashMap<>(details);
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
        list.add(this.car);
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

    @JsonIgnore
    @NotNull
    public Party getCar() {
        return car;
    }

    @NotNull
    public State getState() { return state; }

    @JsonIgnore
    public Party getInsurer() {
        return insurer;
    }

    @NotNull
    public String getVin() {
        return vin;
    }

    public Integer getMileagePerYear() {
        return mileagePerYear;
    }

    @NotNull
    public MileageState getMileageState() {
        return mileageState;
    }

    @NotNull
    public AccidentState getAccidentState() {
        return accidentState;
    }

    public Integer getInsuranceRate() {
        return insuranceRate;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getCarX500() {
        return car.getName().getX500Principal().getName();
    }
    public String getInsurereX500() {
        return insurer != null ? insurer.getName().getX500Principal().getName() : "";
    }

    public List<AbstractParty> getCounterParties(Party me) {
        if (me != null) {
            if (this.getInsurer() != null) {
                if (this.getCar().equals(me)) return Arrays.asList(this.insurer);
                if (this.getInsurer().equals(me)) return Arrays.asList(this.car);
            } else {
                if (this.getCar().equals(me)) return Collections.EMPTY_LIST;
            }
        }
        return Collections.EMPTY_LIST;
    }


    /* actions CREATE */
    public static CarState create(@NotNull UniqueIdentifier id, @NotNull String policyNumber, @NotNull Party car,
                                  Party insurer, @NotNull String vin, Integer mileagePerYear,
                                  @NotNull MileageState mileageState, @NotNull AccidentState accidentState,
                                  Integer insuranceRate, Map<String, Object> details) {
        return new CarState(id, State.VALID, policyNumber, car, insurer, vin, mileagePerYear, mileageState,
                accidentState, insuranceRate, details);
    }

// this.id = id;
//        this.state = state;
//        this.policyNumber = policyNumber;
//        this.car = insuredCar;
//        this.insurer = insurer;
//        this.vin = vin;
//        this.mileagePerYear = mileagePerYear;
//        this.mileageState = mileageState;
//        this.accidentState = accidentState;
//        this.insuranceRate = insuranceRate;
//        this.details = details;

    /* actions UPDATE */
    public CarState update(State state, MileageState mileageState, AccidentState accidentState) {
        return this.update(state, this.policyNumber, this.insurer, this.mileagePerYear, mileageState, accidentState,
                this.insuranceRate, this.details);
    }

    public CarState update(State state, Integer mileagePerYear, MileageState mileageState, AccidentState accidentState,
                           Integer insuranceRate) {
        return this.update(state, this.policyNumber, this.insurer, mileagePerYear, mileageState, accidentState,
                insuranceRate, this.details);
    }

    public CarState update(State state, String policyNumber, Party insurer, Integer mileagePerYear, MileageState mileageState,
                           AccidentState accidentState, Integer insuranceRate, Map<String, Object> newDetails) {
        return new CarState(this.id, state, policyNumber, this.car, insurer, this.vin, mileagePerYear,
                mileageState, accidentState, this.insuranceRate, newDetails);
    }
//
//    /* actions SHARE */
//    public CarState share(@NotNull Party newServiceProvider) {
//        StateMachine.State newState = StateMachine.StateTransition.SHARE.getNextStateFrom(this.state);
//        return new CarState(this.id, this.policyNumber, this.car, newState, this.eventData, newServiceProvider, this.vin);
//    }
//
//
//    /* actions any */
//    public CarState withAction(StateMachine.StateTransition transition) {
//        StateMachine.State newState = transition.getNextStateFrom(this.state);
//        return new CarState(this.id, this.policyNumber, this.car, newState, this.eventData, this.insurer, this.vin);
//    }
//
//    public String getData(String keys) {
//        return JsonHelper.getDataValue(this.getEventData(), keys);
//    }

}
