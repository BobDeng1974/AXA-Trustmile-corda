package com.sidis.eas.client.webserver;

import com.sidis.eas.states.CarEventState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CarEventScheduler extends VaultChangeScheduler<CarEventState> {
    public CarEventScheduler(NodeRPCConnection rpc) {
        super(rpc, CarEventState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/vaultChanged/car-event");
    }
}
