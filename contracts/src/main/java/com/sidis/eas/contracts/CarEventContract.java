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

public class CarEventContract implements Contract {
    public static final String ID = "com.sidis.eas.contracts.CarEventContract";

    public CarEventContract() {
    }

    public interface Commands extends CommandData {
        class Create implements CarEventContract.Commands {

        }
        class Update implements CarEventContract.Commands{

        }
    }

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, CarEventContract.Commands.class);
        CommandData commandData = verifier.command();
        if (commandData instanceof CarEventContract.Commands.Create){
            verifyCreate(tx,verifier);
        } else if (commandData instanceof CarEventContract.Commands.Update){
            verifyUpdate(tx,verifier);
        } else {
            throw new IllegalArgumentException("missing command contract " + commandData.getClass().toString());
        }
    }

    private void verifyCreate(LedgerTransaction tx, StateVerifier verifier){
        requireThat(req ->{
            verifier.input().empty("input must be empty");
            CarEventState newCarEventState = verifier
                    .output().one()
                    .one(CarEventState.class)
                    .object();
            verifyAllSigners(verifier);
            return null;
        });
    }


    private void verifyUpdate(LedgerTransaction tx, StateVerifier verifier){
        requireThat(req -> {
            CarEventState carEvent = verifier.input().one().one(CarEventState.class).object();
            CarEventState newCarEvent = verifier.output().one().one(CarEventState.class).object();
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
