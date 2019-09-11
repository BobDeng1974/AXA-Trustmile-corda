package com.sidis.eas.states;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.sidis.eas.SidisBaseTests;
import com.sidis.eas.contracts.StateMachine;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PolicyStateTests extends SidisBaseTests {

    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }


    public static String dataJSONString() {
        return "{ \"insurance-branch\" : \"health\", \"coverages\" : { \"OKP\" : true, \"ZVP\" : false } }";
    }
    public static String dataUpdateJSONString() {
        return "{ \"insurance-branch\" : \"health\", \"coverages\" : { \"OKP\" : true, \"ZVP\" : true, \"ADD-ON1\" : true } }";
    }
    public static String dataUpdateAfterShareJSONString() {
        return "{ \"insurance-branch\" : \"health\", \"coverages\" : { \"OKP\" : true, \"ZVP\" : true, \"ADD-ON1\" : true, \"UW\" : true } }";
    }

    /*

      public PolicyState(@NotNull UniqueIdentifier id, @NotNull String policyNumber, @NotNull Party insuredCar, @NotNull StateMachine.State state, Map<String, Object> eventData, Party insurer, @NotNull String vin) {
        this.id = id;
        this.state = state;
        this.policyNumber = policyNumber;
        this.insuredCar = insuredCar;
        this.eventData = eventData == null ? new LinkedHashMap<>() : eventData;
        this.insurer = insurer;
        this.vin = vin;
    }
     */

    @Test
    public void test_create() {
        PolicyState policy = PolicyState.create(
                new UniqueIdentifier(),
                "Policy123",
                this.insurance1.party,
                JsonHelper.convertStringToJson(dataJSONString()));
        Assert.assertEquals("state must be CREATED",
                StateMachine.State.CREATED, policy.getState());
    }

    @Test
    public void test_update_after_create() {
        PolicyState policy = PolicyState.create(
                new UniqueIdentifier(),
                "Policy123",
                this.insurance1.party,
                JsonHelper.convertStringToJson(dataJSONString()));
        PolicyState policyUpdated = policy.update(JsonHelper.convertStringToJson(dataUpdateJSONString()));
        Assert.assertEquals("state must be still CREATED",
                StateMachine.State.CREATED, policy.getState());
        Assert.assertEquals("old ZVP value must be false",
                "false", JsonHelper.getDataValue(policy.getEventData(), "coverages.ZVP"));
    }

}
