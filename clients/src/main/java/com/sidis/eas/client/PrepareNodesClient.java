package com.sidis.eas.client;

import ch.cordalo.corda.common.contracts.JsonHelper;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static net.corda.core.utilities.NetworkHostAndPort.parse;

public class PrepareNodesClient {
    private static final Logger logger = LoggerFactory.getLogger(PrepareNodesClient.class);
    private final URL server;
    private Map<String, Object> env;

    public static void main(String[] args) throws MalformedURLException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: PrepareNodesClient <URL: protocol//host:port>");
        new PrepareNodesClient(args[0]);
    }

    public PrepareNodesClient(String server) throws MalformedURLException {
        this(new URL(server));
    }
    public PrepareNodesClient(URL server) {
        this.server = server;
    }

    public void runJobs() {
        try {
            this.env = jsonFromRessource("00-env.json");
            List<String> jobs = (List<String>) env.get("jobs");
            for (String job: jobs) {
                Map<String, Object> jobData = jsonFromRessource(job);
                this.runJob(jobData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        {
          "nodes" : [
            "O=RED,L=Maranello,ST=MO,C=IT"
          ],
          "action": "POST",
          "URL": "/api/v1/car-policy",
          "headers": {
            "Content-Type": "application/json"
          },
          "body": {
            "policyNumber": "18.111.222",
            "vin": "42",
            "car": "Ferrari Modena",
            "insurer": "O=InsureMe,L=Schaffhausen,ST=SH,C=CH",
            "mileagePerYear": 10000,
            "mileageState": "IN_RANGE",
            "accidentState": "NO",
            "insuranceRate": 1800,
            "details": {
              "originalPrice": 152000,
              "valueReductionPerMile": 0.5,
              "color": "RED",
              "numberOfPreviousOwners": 2,
              "model": "Ferrari Modena"
            }
          }
        }
     */
    public void runJob(Map<String, Object> job) throws IOException {
        List<String> nodes = (List<String>)job.get("nodes");
        String action = (String)job.get("action");
        String url = (String)job.get("URL");
        Map<String, Object> headers = (Map<String, Object>)job.get("headers");
        Map<String, Object> body = (Map<String, Object>)job.get("body");
        for (String x500: nodes) {
            Http.ajax(action, endPointByX500(x500, url).toExternalForm(), JsonHelper.convertJsonToString(body), "");
            logger.info(x500+": "+action+" "+url);
        }
    }

    /*
        "O=AXA Versicherungen AG,L=Winterthur,ST=ZH,C=CH": {
          "web-port": 10801,
        },
     */
    private URL endPointByX500(String x500, String file) throws MalformedURLException {
        Map<String, Object> endpointInfo = getNodeFromEnv(x500);
        int port = (Integer)endpointInfo.get("web-port");
        return new URL(this.server.getProtocol(), this.server.getHost(), port, file);
    }

    private Map<String, Object> getNodeFromEnv(String x500) {
        Map<String, Object> nodes = (Map<String, Object>)this.env.get("nodes");
        return (Map<String, Object>)nodes.get(x500);
    }

    private Map<String, Object> jsonFromRessource(String ressourceName) throws IOException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/_preload/" + ressourceName);
        if (resourceAsStream != null) {
            try {
                StringBuffer strinBuffer = new StringBuffer();
                byte[] bytes = new byte[256];
                int len = resourceAsStream.read(bytes);
                while (len >= 0) {
                    strinBuffer.append(new String(bytes, 0, len, Charset.forName("UTF-8")));
                    len = resourceAsStream.read(bytes);
                }
                return JsonHelper.convertStringToJson(strinBuffer.toString());
            } finally {
                resourceAsStream.close();
            }
        } else {
            throw new IOException("file named "+ressourceName+" not found");
        }
    }

}
