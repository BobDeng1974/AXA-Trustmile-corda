package com.sidis.eas.client.test;

import com.sidis.eas.client.PrepareNodesClient;
import org.junit.Test;

import java.net.MalformedURLException;

public class PrepareClientTest {

    @Test
    public void testLoadEnv() throws MalformedURLException {
        String server = "http://65.52.142.219:10803";
        PrepareNodesClient client = new PrepareNodesClient(server);
        client.runJobs();
    }
}
