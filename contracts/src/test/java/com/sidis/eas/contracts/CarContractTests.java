package com.sidis.eas.contracts;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.sidis.eas.SidisBaseTests;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import com.sidis.eas.states.CarStateTests;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class CarContractTests extends SidisBaseTests {

    @Before
    public void setup() {
        super.setup(false);
    }

    //TODO: fix json
    private String detailsJSONString() {
        return "{ \"some-data\" : \"some-value\" }";
    }

    private CarState newCar() {
        return CarState.create(
                new UniqueIdentifier(), "123-123131", this.redCar.party, this.insurance1.party, "42", 7000,
                CarState.MileageState.IN_RANGE, CarState.AccidentState.NO, 1200,
                JsonHelper.convertStringToJson(detailsJSONString()));
    }

    private CarState updateCar(CarState car){
         return car.update(CarState.State.VALID, CarState.MileageState.IN_RANGE, CarState.AccidentState.NO);
    }

    /*
    public static CarState create(@NotNull UniqueIdentifier id, @NotNull String policyNumber, @NotNull Party car,
                                  Party insurer, @NotNull String vin, Integer mileagePerYear,
                                  @NotNull MileageState mileageState, @NotNull AccidentState accidentState,
                                  Integer insuranceRate, Map<String, Object> details) {
        return new CarState(id, State.VALID, policyNumber, car, insurer, vin, mileagePerYear, mileageState,
                accidentState, insuranceRate, details);
    }

    *
     */

   @Test
   public void car_create(){
       transaction(this.redCar.ledgerServices, tx->{
           CarState carState = newCar();
           CarState carState2 = newCar();
           tx.input(CarContract.ID,carState2);
           tx.output(CarContract.ID,carState);
           tx.command(carState.getParticipantKeys(), new CarContract.Commands.Create());
           tx.failsWith(("Input must be empty"));
           return null;
       });
       transaction(this.redCar.ledgerServices, tx->{
           CarState carState = newCar();
           tx.output(CarContract.ID,carState);
           tx.command(carState.getParticipantKeys(), new CarContract.Commands.Create());
           tx.verifies();
           return null;
       });
   }


   @Test
   public void car_update_no_input(){
       transaction(this.redCar.ledgerServices ,tx ->{
           CarState car1 = newCar();
           CarState car2 = updateCar(car1);
           tx.output(CarContract.ID, car2);
           tx.command(car2.getParticipantKeys(), new CarContract.Commands.Update());
           tx.failsWith("List must have 2 entries");
           return null;
       });
   }





}
