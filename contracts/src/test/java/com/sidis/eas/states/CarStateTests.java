package com.sidis.eas.states;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.sidis.eas.SidisBaseTests;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CarStateTests extends SidisBaseTests {

    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }


    public static String detailsJSONString() {
        return "{ \"some-field\" : \"some-value\" }";
    }

//    public static CarState create(@NotNull UniqueIdentifier id, @NotNull String policyNumber, @NotNull Party car,
//                                  Party insurer, @NotNull String vin, Map<String, Object> details) {

    @Test
    public void test_create() {
        CarState carState = CarState.create(
                new UniqueIdentifier("42"),
                "12.345.678",
                this.redCar.party,
                this.insurance1.party,
                "42",
                7000,
                CarState.MileageState.IN_RANGE,
                CarState.AccidentState.NO,
                2500,
                JsonHelper.convertStringToJson(detailsJSONString()));
        Assert.assertEquals("state must be CREATED",
                CarState.State.VALID, carState.getState());
    }

//    @Test
//    public void test_update_after_create() {
//        CarState policy = CarState.create(
//                new UniqueIdentifier(),
//                "Policy123",
//                this.insurance1.party,
//                JsonHelper.convertStringToJson(dataJSONString()));
//        CarState policyUpdated = policy.update(JsonHelper.convertStringToJson(dataUpdateJSONString()));
//        Assert.assertEquals("state must be still CREATED",
//                StateMachine.State.CREATED, policy.getState());
//        Assert.assertEquals("old ZVP value must be false",
//                "false", JsonHelper.getDataValue(policy.getEventData(), "coverages.ZVP"));
//    }

}
