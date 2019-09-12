package com.sidis.eas.contracts;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.sidis.eas.SidisBaseTests;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarEventStateTests;
import com.sidis.eas.states.CarState;
import net.corda.core.contracts.StateRef;
import net.corda.core.contracts.UniqueIdentifier;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

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

    private CarEventState updateEvent(CarEventState event, @NotNull Integer timestamp, @NotNull Long mileage, @NotNull Boolean accident, Map<String, Object> data) {
        return event.udpate(timestamp, mileage, accident, data);
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


    @Test
    public void car_event_update() {
        transaction(this.redCar.ledgerServices, tx -> {
            CarEventState carEventState = newCarEvent();
            CarEventState carEventUpdate = carEventState.udpate(19999, carEventState.getMileage() + 10, carEventState.getAccident(), carEventState.getData());

            tx.input(CarEventContract.ID, carEventState);
            tx.output(CarEventContract.ID, carEventUpdate);
            tx.command(carEventUpdate.getParticipantKeys(), new CarEventContract.Commands.Update());
            tx.verifies();
            return null;
        });
    }


}