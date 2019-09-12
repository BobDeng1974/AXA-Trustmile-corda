package com.sidis.eas.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import com.sidis.eas.flows.CarEventFlow;
import com.sidis.eas.flows.CarFlow;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CarEventFlowTests extends SidisBaseFlowTests {

    @Before
    public void setup() {
        this.setup(true,
            CarEventFlow.CreateResponder.class
            //ServiceFlow.UpdateResponder.class
        );
    }

    @After
    @Override
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


    @Test
    public void create_car() throws Exception {
        SignedTransaction tx = this.newCarEventCreateFlow("42", 15000000, 7000L, false, dataJSONString());
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.redCar.ledgerServices);
        CarEventState event = verifier
                .output()
                .one()
                .one(CarEventState.class)
                .object();

        Assert.assertEquals("vin must be 42", "42", String.valueOf(event.getVin()));
    }


}