package com.sidis.eas.flows;

import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import co.paralleluniverse.fibers.Suspendable;
import com.sidis.eas.contracts.CarContract;
import com.sidis.eas.contracts.CarEventContract;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import kotlin.Unit;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.LinkedHashMap;
import java.util.Map;


public class CarEventFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends BaseFlow {

        private final String vin;
        private final Integer timestamp;
        private final Long mileage;
        private final Boolean accident;
        private final String data;

        public Create(String vin, Integer timestamp, Long mileage, Boolean accident, String data) {
            this.vin = vin;
            this.timestamp = timestamp;
            this.mileage = mileage;
            this.accident = accident;
            this.data = data;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return this.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            getProgressTracker().setCurrentStep(PREPARATION);
            // We get a reference to our own identity.
            Party me = getOurIdentity();
            /* ============================================================================
             *         TODO 1 - Create our object !
             * ===========================================================================*/
            Map<String,Object> detailsMap = new LinkedHashMap<>();
            if (this.data != null && !this.data.equals("")) {
                detailsMap = JsonHelper.convertStringToJson(data);
            }
            CarEventState carEvent = new CarEventState(
                    new UniqueIdentifier(),
                    me,
                    this.vin,
                    this.timestamp,
                    this.mileage,
                    this.accident,
                    detailsMap);

            /* ============================================================================
             *      TODO 3 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    carEvent,
                    new CarEventContract.Commands.Create());
            transactionBuilder.addOutputState(carEvent);
            /* ============================================================================
             *          TODO 2 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signSyncCollectAndFinalize(carEvent.getParticipants(), transactionBuilder);
        }

    }


    @InitiatedBy(CarFlow.Create.class)
    public static class CreateResponder extends ResponderBaseFlow<CarEventState> {

        public CreateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

}