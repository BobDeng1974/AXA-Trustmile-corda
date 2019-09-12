package com.sidis.eas.contracts;

import ch.cordalo.corda.common.contracts.ReferenceContract;
import ch.cordalo.corda.common.contracts.StateVerifier;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.serialization.CordaSerializable;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CarContract implements Contract {
    public static final String ID = "com.sidis.eas.contracts.CarContract";

    public CarContract() {
    }

    public interface Commands extends CommandData {
        class Create implements CarContract.Commands {

        }
        class Update implements CarContract.Commands{

        }

        @CordaSerializable
        public class Reference extends ReferenceContract.Commands.Reference<CarState> implements CarContract.Commands {
            public Reference(CarState myState) {
                super(myState);
            }
        }
    }

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, CarContract.Commands.class);
        CommandData commandData = verifier.command();
        if (commandData instanceof CarContract.Commands.Create){
            verifyCreate(tx, verifier);
        }
        else if (commandData instanceof CarContract.Commands.Update){
            verifyUpdate(tx, verifier);

        }
    }

    private void verifyCreate(LedgerTransaction tx, StateVerifier verifier){
        requireThat(req ->{
            verifier.input().empty("input must be empty");
            CarState newCarState = verifier.output().one().one(CarState.class).object();
            this.verifyAllSigners(verifier);
            return null;
        });
    }


    private void verifyUpdate(LedgerTransaction tx, StateVerifier verifier){
        requireThat(req -> {
            verifier.output().notEmpty().count(2);
            CarState car = verifier.input().one(CarState.class).object();
            CarEventState carEvent = verifier.input().one(CarEventState.class).object();
            verifier.output().notEmpty().count(2);
            CarState newCar = verifier.output().one(CarState.class).object();
            CarEventState newCarEvent = verifier.output().one(CarEventState.class).object();
            verifyAllSigners(verifier);
            return null;
        });

    }

    private void verifyAllSigners(StateVerifier verifier) {
        requireThat(req -> {
            verifier
                    .output()
                    .participantsAreSigner("all participants must be signer");
            return null;
        });
    }
}
