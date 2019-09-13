package com.sidis.eas.flows;

import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.FlowHelper;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import co.paralleluniverse.fibers.Suspendable;
import com.sidis.eas.contracts.CarContract;
import com.sidis.eas.contracts.CarEventContract;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import kotlin.Unit;
import net.corda.core.contracts.AlwaysAcceptAttachmentConstraint;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.LinkedHashMap;
import java.util.List;
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
            this.insurer = insurer;
            this.vin = vin;
            this.mileagePerYear = mileagePerYear;
            this.insuranceRate = insuranceRate;
            this.details = details;
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
            if (this.details != null) {
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
            transactionBuilder.addOutputState(car, CarContract.ID);
            /* ============================================================================
             *          TODO 2 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signSyncCollectAndFinalize(car.getInsurer(), transactionBuilder);
        }

    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Update extends BaseFlow {
        private final String policyNumber;
        private final Party insurer;
        private final Integer mileagePerYear;
        private final Integer insuranceRate;
        private final String details;

        private static final String INSURE_ME = "O=InsureMe,L=Schaffhausen,ST=SH,C=CH";

        public Update(String policyNumber, Party insurer, Integer mileagePerYear, Integer insuranceRate, String details) {
            this.policyNumber = policyNumber;
            this.insurer = insurer;
            this.mileagePerYear = mileagePerYear;
            this.insuranceRate = insuranceRate;
            this.details = details;
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
            StateAndRef<CarState> carRef = new FlowHelper<CarState>(this.getServiceHub()).getLastState(CarState.class);

            CarState car = this.getStateByRef(carRef);

            StateAndRef<CarEventState> carEventRef = new FlowHelper<CarEventState>(this.getServiceHub())
                    .getLastState(CarEventState.class);

            CarEventState carEvent = this.getStateByRef(carEventRef);

            List<StateAndRef<CarEventState>> carEventsRef = new FlowHelper<CarEventState>(this.getServiceHub())
                    .getAllStatesByLinearId(CarEventState.class, carEvent.getId());

            StateAndRef<CarEventState> carEventLastConsumed = null;
            if (carEventsRef.size() == 2) {
                carEventLastConsumed = carEventsRef.get(0);
            } else if (carEventsRef.size() > 2) {
                carEventLastConsumed = carEventsRef.get(carEventsRef.size() - 2);
            }

            Map<String,Object> detailsMap;
            if (this.details != null) {
                detailsMap = JsonHelper.convertStringToJson(details);
            } else {
                detailsMap = car.getDetails();
            }

            CarState.MileageState newMileageState = this.getNewMileageState(car.getMileageState(),
                    car.getMileagePerYear(), carEvent.getTimestamp(), carEvent.getMileage());;
            CarState.State newState;
            CarState.AccidentState newAccidentState = this.getNewAccidentState(car.getAccidentState(), carEvent.getAccident());

            if (car.getInsurereX500().equals(INSURE_ME)){
                newState = CarState.State.VALID;
            } else {
                newState = this.getNewState(car.getState(), carEvent, carEventLastConsumed);
            }
            CarState updatedCar = car.update(newState, newMileageState, newAccidentState, detailsMap);

            /* ============================================================================
             *      TODO 3 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder;
            if (!car.equals(updatedCar)) {
                transactionBuilder = getTransactionBuilderSignedByParticipants(
                        updatedCar,
                        new CarContract.Commands.Update());
            } else {
                transactionBuilder = getTransactionBuilderSignedByParticipants(
                        updatedCar,
                        new CarContract.Commands.NoUpdate());
            }
            transactionBuilder.addInputState(carRef);
            transactionBuilder.addOutputState(updatedCar, CarContract.ID);

            /* ============================================================================
             *          TODO 2 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signSyncCollectAndFinalize(updatedCar.getParticipants(), transactionBuilder);
        }

        private CarState.MileageState getNewMileageState(CarState.MileageState previousMileageState,
                                                         Integer mileagePerYear, Integer timestamp, Long mileage) {
            return CarState.MileageState.IN_RANGE;
        }

        private CarState.State getNewState(CarState.State previousState,
                                           CarEventState carEvent, StateAndRef<CarEventState> lastConsumedCarEventRef) {
            if (CarState.State.FRAUD.equals(previousState)) {
                return previousState;
            } else if (lastConsumedCarEventRef != null) {
                CarEventState lastConsumedCarEvent = this.getStateByRef(lastConsumedCarEventRef);
                if (lastConsumedCarEvent.getMileage() >= carEvent.getMileage()) {
                    return CarState.State.FRAUD;
                }
            }
            return CarState.State.VALID;
        }

        private CarState.AccidentState getNewAccidentState(CarState.AccidentState currentAccidentState, boolean isAccident) {
            if (isAccident) {
                if(CarState.AccidentState.NO.equals(currentAccidentState)) {
                    return CarState.AccidentState.ONE;
                } else {
                    return CarState.AccidentState.MORE_THAN_ONE;
                }
            }
            return currentAccidentState;
        }
    }

    @InitiatedBy(CarFlow.Create.class)
    public static class CreateResponder extends ResponderBaseFlow<CarState> {

        public CreateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(CarFlow.Update.class)
    public static class UpdateResponder extends ResponderBaseFlow<CarState> {

        public UpdateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

}