package com.sidis.eas.flows.test;

import com.sidis.eas.SidisBaseTests;
import com.sidis.eas.flows.CarEventFlow;
import com.sidis.eas.flows.CarFlow;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.StartedMockNode;

import java.util.concurrent.ExecutionException;

public class SidisBaseFlowTests extends SidisBaseTests {

    protected SignedTransaction newCarCreateFlow(String policyNumber, Party insurer, String vin, Integer mileagePerYear, Integer insuranceRate, String data) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new CarFlow.Create(policyNumber, insurer, vin, mileagePerYear, insuranceRate, data);
        CordaFuture<SignedTransaction> future = redCar.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }

    protected SignedTransaction newCarUpdateFlow(String policyNumber, Party insurer, Integer mileagePerYear,
                                                 Integer insuranceRate, String data) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new CarFlow.Update(policyNumber, insurer, mileagePerYear, insuranceRate, data);
        CordaFuture<SignedTransaction> future = redCar.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }

    protected SignedTransaction newCarEventCreateFlow(String vin, Integer timestamp, Long mileage, Boolean accident, String data) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new CarEventFlow.Create(vin, timestamp, mileage, accident, data);
        CordaFuture<SignedTransaction> future = redCar.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }

    protected SignedTransaction newCarEventUpdateFlow(String vin, Integer timestamp, Long mileage, Boolean accident, String data) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new CarEventFlow.Update(vin, timestamp, mileage, accident, data);
        CordaFuture<SignedTransaction> future = redCar.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }


}
