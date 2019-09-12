package com.sidis.eas.client.webserver;

import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.ServiceState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CarEventScheduler extends VaultChangeScheduler<CarEventState> {
    public CarEventScheduler(NodeRPCConnection rpc) {
        super(rpc, CarEventState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/sidis/eas/vaultChanged");
    }
}
