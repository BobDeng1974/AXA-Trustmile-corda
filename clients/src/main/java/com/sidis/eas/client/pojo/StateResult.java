package com.sidis.eas.client.pojo;

import com.sidis.eas.states.CarState;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.PageSpecification;
import org.springframework.http.HttpRequest;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StateResult<T extends ContractState> {

    private final int page;
    private final int pageSize;
    private final List<StateAndMeta<T>> data;
    private final Map<String, String> links;

    public StateResult(Vault.Page<T> pageResult, PageSpecification paging ) throws MalformedURLException, URISyntaxException {
        this.data = initResults(pageResult, paging);
        this.links = initLinks(pageResult, paging);
        // page number always starts with 1 and not 0
        this.page = paging.getPageNumber();
        this.pageSize = paging.getPageSize();
    }

    protected Map<String, String> initLinks(Vault.Page<T> pageResult, PageSpecification paging) throws URISyntaxException, MalformedURLException {
        Map<String, String> linksList = new LinkedHashMap<>();
        int lastSkip = (int) (Math.floor(new Double(pageResult.getTotalStatesAvailable()) / new Double(this.pageSize)) * this.pageSize);
        if (lastSkip == pageResult.getTotalStatesAvailable()) { lastSkip = Math.max(0, lastSkip - pageSize); }
        int prevPage = this.page - 1;
        int nextPage = this.page + 1;
        URI uri = new URI("");
        linksList.put("cur", linkURI(uri, page, pageSize, true));
        linksList.put("first", linkURI(uri, 0, pageSize, true));
        linksList.put("prev", linkURI(uri, prevPage, pageSize, false));
        linksList.put("next", linkURI(uri, nextPage, pageSize, false));
        linksList.put("last", linkURI(uri, lastSkip, pageSize, true));
        return linksList;
    }

    protected List<StateAndMeta<T>> initResults(Vault.Page<T> pageResult, PageSpecification paging) {
        int size = pageResult.getStates().size();
        List<StateAndMeta<T>> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new StateAndMeta<>(
                    pageResult.getStates().get(i).getState().getData(),
                    pageResult.getStatesMetadata().get(i)));
        }
        return list;
    }


    private String linkURI(URI uri, int linkPage, int max, boolean overwrite) throws MalformedURLException, URISyntaxException {
        int skip = linkPage * pageSize;
        if (!overwrite) {
            if (skip < 0) {
                return null;
            }
            if (skip + pageSize > max) {
                return null;
            }
            if (skip >= max) {
                return null;
            }
        }
        return appendUri(uri, "page="+linkPage+"&pageSize="+pageSize).toString();
    }

    private URI appendUri(URI uri, String appendQuery) throws URISyntaxException {
        String newQuery = uri.getQuery();
        if (newQuery == null) {
            newQuery = appendQuery;
        } else {
            newQuery += "&" + appendQuery;
        }
        URI newUri = new URI(uri.getScheme(), uri.getAuthority(),
                uri.getPath(), newQuery, uri.getFragment());
        return newUri;
    }

}
