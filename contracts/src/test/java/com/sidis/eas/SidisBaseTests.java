package com.sidis.eas;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaTestNetwork;
import ch.cordalo.corda.common.test.CordaloBaseTests;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;

import java.util.List;

public abstract class SidisBaseTests extends CordaloBaseTests {

    public SidisBaseTests() {
    }

    protected CordaTestNetwork network;
    protected CordaNodeEnvironment insurance1;
    protected CordaNodeEnvironment insurance2;
    protected CordaNodeEnvironment redCar;
    protected CordaNodeEnvironment blackCar;

    public List<String> getCordappPackageNames() {
        return ImmutableList.of(
                "com.sidis.eas.contracts",
                "ch.cordalo.corda.common.contracts"
        );
    }


    public void setup(boolean withNodes, Class<? extends FlowLogic> ...responderClasses) {
        this.network = new CordaTestNetwork(
                withNodes,
            this.getCordappPackageNames(),
            responderClasses
        );
        this.insurance1 = network.startEnv("InsureMe", "O=InsureMe,L=Schaffhausen,ST=SH,C=CH");
        this.insurance2 = network.startEnv("AXA", "O=AXA Versicherungen AG,L=Winterthur,ST=ZH,C=CH");
        this.redCar = network.startEnv("Red", "O=RED,L=Maranello,ST=MO,C=IT");
        this.blackCar = network.startEnv("Black", "O=BLACK,L=Stuttgart,ST=BW,C=DE");
        this.network.startNodes();
    }

    public void tearDown() {
        if (network != null) network.stopNodes();
    };
}
