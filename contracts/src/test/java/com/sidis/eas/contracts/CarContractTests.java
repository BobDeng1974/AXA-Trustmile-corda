package com.sidis.eas.contracts;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.sidis.eas.SidisBaseTests;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarEventStateTests;
import com.sidis.eas.states.CarState;
import com.sidis.eas.states.CarStateTests;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class CarContractTests extends SidisBaseTests {

    @Before
    public void setup() {
        super.setup(false);
    }

    private CarState newCar() {
        return CarState.create(
                new UniqueIdentifier(), "12.345.678", this.redCar.party, this.insurance1.party, "42", 7000,
                CarState.MileageState.IN_RANGE, CarState.AccidentState.NO, 1200,
                JsonHelper.convertStringToJson(CarStateTests.detailsJSONString()));
    }

    private CarState updateCar(CarState car){
         return car.update(CarState.State.VALID, CarState.MileageState.IN_RANGE, CarState.AccidentState.NO);
    }

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
           tx.failsWith("List must contain only 1 entry");
           return null;
       });
   }

    @Test
    public void car_update(){
        transaction(this.redCar.ledgerServices ,tx ->{
            CarState car1 = newCar();
            CarState car2 = updateCar(car1);
            tx.input(CarContract.ID, car1);
            tx.output(CarContract.ID, car2);
            tx.command(car2.getParticipantKeys(), new CarContract.Commands.Update());
            tx.verifies();
            return null;
        });
    }



}
