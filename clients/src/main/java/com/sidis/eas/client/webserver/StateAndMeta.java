package com.sidis.eas.client.webserver;

import net.corda.core.contracts.LinearState;
import net.corda.core.node.services.Vault;

import java.time.Instant;

public class StateAndMeta<T extends LinearState> {

    private final T state;

    private final String contractStateClassName;
    private final Instant recordedTime;
    private final Instant consumedTime;
    private final Vault.StateStatus status;
    private final Vault.RelevancyStatus relevancyStatus;



    public StateAndMeta(T state, Vault.StateMetadata meta) {
        this.state = state;
        this.contractStateClassName = meta.getContractStateClassName();
        this.recordedTime = meta.getRecordedTime();
        this.consumedTime = meta.getConsumedTime();
        this.status = meta.getStatus();
        this.relevancyStatus = meta.getRelevancyStatus();
    }

    public T getState() {
        return state;
    }

    public String getContractStateClassName() {
        return contractStateClassName;
    }

    public Instant getRecordedTime() {
        return recordedTime;
    }

    public Instant getConsumedTime() {
        return consumedTime;
    }

    public Vault.StateStatus getStatus() {
        return status;
    }

    public Vault.RelevancyStatus getRelevancyStatus() {
        return relevancyStatus;
    }
}
