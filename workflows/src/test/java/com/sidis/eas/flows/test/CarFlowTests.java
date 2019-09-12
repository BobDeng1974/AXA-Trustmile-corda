package com.sidis.eas.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import com.sidis.eas.flows.CarFlow;
import com.sidis.eas.flows.ServiceFlow;
import com.sidis.eas.states.CarState;
import com.sidis.eas.states.ServiceState;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CarFlowTests extends SidisBaseFlowTests {

    @Before
    public void setup() {
        this.setup(true,
            CarFlow.CreateResponder.class
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
        SignedTransaction tx = this.newCarCreateFlow("12.345.678", this.insurance1.party, "42", 7000, 1200, dataJSONString());
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.insurance1.ledgerServices);
        CarState service = verifier
                .output()
                .one()
                .one(CarState.class)
                .object();

        Assert.assertEquals("vin must be 42", "42", String.valueOf(service.getVin()));
    }


//    @Test
//    public void update_before_share_service() throws Exception {
//        SignedTransaction tx = this.newServiceCreateFlow("Exit", dataJSONString(), 7);
//        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.insurance1.ledgerServices);
//        ServiceState service = verifier
//                .output().one()
//                .one(ServiceState.class)
//                .object();
//        Assert.assertEquals("ZVP must be false", "false", service.getData("coverages.ZVP"));
//
//        StateVerifier verifier2 = StateVerifier.fromTransaction(
//                this.newServiceUpdateFlow(service.getId(), dataUpdateJSONString(), 42),
//                this.insurance1.ledgerServices);
//        ServiceState service2 = verifier2
//                .output().one()
//                .one(ServiceState.class)
//                .object();
//
//        Assert.assertEquals("ZVP must be true", "true", service2.getData("coverages.ZVP"));
//        Assert.assertEquals("price must be 42", "42", String.valueOf(service2.getPrice()));
//    }
}