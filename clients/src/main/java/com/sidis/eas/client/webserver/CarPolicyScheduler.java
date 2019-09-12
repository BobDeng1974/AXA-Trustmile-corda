package com.sidis.eas.client.webserver;

import com.sidis.eas.client.pojo.CarEvent;
import com.sidis.eas.states.CarEventState;
import com.sidis.eas.states.CarState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class CarPolicyScheduler extends VaultChangeScheduler<CarState> {
    public CarPolicyScheduler(NodeRPCConnection rpc) {
        super(rpc, CarState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/sidis/eas/vaultChanged");
    }
}
