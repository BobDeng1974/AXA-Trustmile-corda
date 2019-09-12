package com.sidis.eas.flows;

import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.FlowHelper;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import co.paralleluniverse.fibers.Suspendable;
import com.sidis.eas.contracts.CarContract;
import com.sidis.eas.contracts.CarEventContract;
import com.sidis.eas.contracts.ServiceContract;
import com.sidis.eas.contracts.StateMachine;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import com.sidis.eas.states.ServiceState;
import kotlin.Unit;
import net.corda.core.contracts.AlwaysAcceptAttachmentConstraint;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import javax.swing.event.CaretEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class CarFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends BaseFlow {


        private final String policyNumber;
        private final Party insurer;
        private final String vin;
        private final Integer mileagePerYear;
        private final Integer insuranceRate;
        private final String details;



        public Create(String policyNumber, Party insurer, String vin, Integer mileagePerYear, Integer insuranceRate, String details) {
            this.policyNumber = policyNumber;
            this.insurer=insurer;
            this.vin=vin;
            this.mileagePerYear=mileagePerYear;
            this.insuranceRate=insuranceRate;
            this.details=details;
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
            // We create our new TokenState.
            Map<String,Object> detailsMap = new LinkedHashMap<>();
            if (this.details!=null) {
                detailsMap = JsonHelper.convertStringToJson(details);
            }

            CarState car = new CarState(
                    new UniqueIdentifier(this.vin),
                    CarState.State.VALID,
                    this.policyNumber,
                    me,
                    this.insurer,
                    this.vin,
                    this.mileagePerYear,
                    CarState.MileageState.IN_RANGE,
                    CarState.AccidentState.NO,
                    this.insuranceRate,
                    detailsMap);

            /* ============================================================================
             *      TODO 3 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    car,
                    new CarContract.Commands.Create());
            transactionBuilder.addOutputState(car, CarContract.ID, AlwaysAcceptAttachmentConstraint.INSTANCE);
            /* ============================================================================
             *          TODO 2 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signSyncCollectAndFinalize(car.getParticipants(), transactionBuilder);
        }

    }



//    @InitiatingFlow(version = 2)
//    @StartableByRPC
//            getProgressTracker().setCurrentStep(PREPARATION);
//            // We get a reference to our own identity.
//            Party me = getOurIdentity();
//
//
//            /* ============================================================================
//             *         TODO 1 - Create our object !
//             * ===========================================================================*/
//            // We create our new TokenState.
//            StateAndRef<CarEventState> carEvent = new FlowHelper<CarEventState>(
//                    this.getServiceHub()).getLastState(CarEventState.class);
//            Map<String,Object> detailsMap = new LinkedHashMap<>();
//            if (this.details!=null) {
//                detailsMap = JsonHelper.convertStringToJson(details);
//            }
//
//            CarState car = new CarState(
//
//                    new UniqueIdentifier(carEvent.getState().getData().getId().getExternalId()),
//                    CarState.State.VALID,
//                    this.policyNumber,
//                    me,
//                    this.insurer,
//                    carEvent.getState().getData().getId().getExternalId(),
//                    this.mileagePerYear,
//                    CarState.MileageState.IN_RANGE,
//                    CarState.AccidentState.NO,
//                    this.insuranceRate,
//                    detailsMap);
//
//            /* ============================================================================
//             *      TODO 3 - Build our issuance transaction to update the ledger!
//             * ===========================================================================*/
//            // We build our transaction.
//            getProgressTracker().setCurrentStep(BUILDING);
//            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
//                    car,
//                    new CarContract.Commands.Create());
//            transactionBuilder.addCommand(new CarEventContract.Commands.Reference(carEvent.getState().getData()), car.getParticipantKeys());
//            transactionBuilder.addOutputState(car, CarContract.ID);
//            transactionBuilder.addInputState(carEvent);
//            transactionBuilder.addOutputState(carEvent.getState().getData(), CarEventContract.ID, AlwaysAcceptAttachmentConstraint.INSTANCE);
//
//            /* ============================================================================
//             *          TODO 2 - Write our contract to control issuance!
//             * ===========================================================================*/
//            // We check our transaction is valid based on its contracts.
//            return signCollectAndFinalize(car.getParticipants(), transactionBuilder);
//        }
//
//    }

    @InitiatedBy(CarFlow.Create.class)
    public static class CreateResponder extends ResponderBaseFlow<ServiceState> {

        public CreateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

//    @InitiatedBy(CarFlow.Update.class)
//    public static class UpdateResponder extends ResponderBaseFlow<ServiceState> {
//
//        public UpdateResponder(FlowSession otherFlow) {
//            super(otherFlow);
//        }
//
//        @Suspendable
//        @Override
//        public Unit call() throws FlowException {
//            return this.receiveIdentitiesCounterpartiesNoTxChecking();
//        }
//    }

}