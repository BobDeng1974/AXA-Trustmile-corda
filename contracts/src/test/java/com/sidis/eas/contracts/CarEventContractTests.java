package com.sidis.eas.contracts;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.sidis.eas.SidisBaseTests;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarEventStateTests;
import com.sidis.eas.states.CarState;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class CarEventContractTests extends SidisBaseTests {

    @Before
    public void setup() {
        super.setup(false);
    }

    private CarEventState newCarEvent() {
        return new CarEventState(
                new UniqueIdentifier(),
                this.redCar.party,
                "42",
                1568211122,
                27085L,
                false,
                JsonHelper.convertStringToJson(CarEventStateTests.dataJSONString())
        );
    }

    @Test
    public void car_event_create() {
        transaction(this.redCar.ledgerServices, tx -> {
            CarEventState carEventState = newCarEvent();
            CarEventState carEventState2 = newCarEvent();
            tx.input(CarEventContract.ID, carEventState2);
            tx.output(CarEventContract.ID, carEventState);
            tx.command(carEventState.getParticipantKeys(), new CarEventContract.Commands.Create());
            tx.failsWith(("Input must be empty"));
            return null;
        });
        transaction(this.redCar.ledgerServices, tx -> {
            CarEventState carEventState = newCarEvent();
            tx.output(CarEventContract.ID, carEventState);
            tx.command(carEventState.getParticipantKeys(), new CarEventContract.Commands.Create());
            tx.verifies();
            return null;
        });
    }
}