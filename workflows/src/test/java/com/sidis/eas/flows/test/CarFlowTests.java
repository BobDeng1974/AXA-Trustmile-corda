package com.sidis.eas.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import com.sidis.eas.flows.CarEventFlow;
import com.sidis.eas.flows.CarFlow;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import net.corda.core.transactions.SignedTransaction;
import org.junit.*;

public class CarFlowTests extends SidisBaseFlowTests {

    @Before
    public void setup() {
        this.setup(true,
            CarFlow.CreateResponder.class,
            CarEventFlow.CreateResponder.class,
            CarFlow.UpdateResponder.class
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
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.redCar.ledgerServices);
        CarState service = verifier
                .output()
                .one()
                .one(CarState.class)
                .object();

        Assert.assertEquals("vin must be 42", "42", String.valueOf(service.getVin()));
    }

    @Test
    @Ignore
    public void update_car() throws Exception {
        SignedTransaction carCreateTx = this.newCarCreateFlow("12.345.678", this.insurance2.party,
                "42", 7000, 1200, dataJSONString());
        StateVerifier verifier1 = StateVerifier.fromTransaction(carCreateTx, this.redCar.ledgerServices);
        CarState car = verifier1
                .output().one()
                .one(CarState.class)
                .object();

        SignedTransaction carEventCreate1Tx = this.newCarEventCreateFlow("42", 15000000, 100L,
                false, dataJSONString());
        StateVerifier verifier2 = StateVerifier.fromTransaction(carEventCreate1Tx, this.redCar.ledgerServices);
        CarEventState carEvent1 = verifier2.output().one().one(CarEventState.class).object();

        SignedTransaction carEventCreate2Tx = this.newCarEventCreateFlow("42", 15000000, 90L,
                false, dataJSONString());
        StateVerifier verifier3 = StateVerifier.fromTransaction(carEventCreate2Tx, this.redCar.ledgerServices);
        CarEventState carEvent2 = verifier3.output().one().one(CarEventState.class).object();

        SignedTransaction carUpdateTx = this.newCarUpdateFlow("12.345.678", this.insurance2.party,
                7000, 1200, dataJSONString());
        StateVerifier verifier4 = StateVerifier.fromTransaction(carUpdateTx, this.redCar.ledgerServices);
        CarState updatedCar = verifier4
                .output().one()
                .one(CarState.class)
                .object();

        Assert.assertEquals("State to be FRAUD", CarState.State.FRAUD, updatedCar.getState());
    }
}