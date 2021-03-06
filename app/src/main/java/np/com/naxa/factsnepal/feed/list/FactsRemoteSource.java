package np.com.naxa.factsnepal.feed.list;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import np.com.naxa.factsnepal.feed.Fact;
import np.com.naxa.factsnepal.feed.FactsLocalSource;
import np.com.naxa.factsnepal.network.NetworkApiClient;
import np.com.naxa.factsnepal.network.NetworkApiInterface;
import np.com.naxa.factsnepal.network.facts.Category;
import np.com.naxa.factsnepal.network.facts.FactsResponse;

public class FactsRemoteSource {

    private static FactsRemoteSource INSTANCE = null;
    private HashMap<String, String> categoriesIdsMap = new HashMap<>();

    public static FactsRemoteSource getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new FactsRemoteSource();
        }
        return INSTANCE;
    }

    public Observable<List<Fact>> getFactsByCategoryId(List<Integer> categoryIds) {
        return NetworkApiClient.getAPIClient().create(NetworkApiInterface.class)
                .getFactsDetailsResponse(categoryIds)
                .subscribeOn(Schedulers.io())
                .map((Function<List<FactsResponse>, List<Fact>>) factsResponses -> {
                    categoriesIdsMap.clear();

                    for (FactsResponse factsResponse : factsResponses) {
                        for (Category category : factsResponse.getCategory()) {
                            categoriesIdsMap.put(String.valueOf(category.getId()), category.getTitle());
                        }
                    }

                    ArrayList<Fact> facts = new ArrayList<>();
                    for (FactsResponse factsResponse : factsResponses) {
                        for (Fact fact : factsResponse.getHome()) {
                            fact.setCategoryName(categoriesIdsMap.get(fact.getCatgoryId()));
                        }

                        facts.addAll(factsResponse.getHome());
                    }
                    return facts;
                }).map(facts -> {
                    FactsLocalSource.getINSTANCE().save(facts);
                    return facts;
                });
    }

    public Observable<List<Fact>> getAllFacts() {
        return getFactsByCategoryId(null);
    }

    public Observable<List<Category>> getCategories() {
        return NetworkApiClient.createCacheService(NetworkApiInterface.class)
                .getFactsDetailsResponse(null)
                .subscribeOn(Schedulers.io())
                .flatMapIterable(new Function<List<FactsResponse>, Iterable<FactsResponse>>() {
                    @Override
                    public Iterable<FactsResponse> apply(List<FactsResponse> factsResponses) throws Exception {
                        return factsResponses;
                    }
                })
                .map(FactsResponse::getCategory);

    }

}
