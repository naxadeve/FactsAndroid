package np.com.naxa.factsnepal.feed;

import androidx.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Single;
import np.com.naxa.factsnepal.FactsNepal;
import np.com.naxa.factsnepal.common.Constant;
import np.com.naxa.factsnepal.database.FactsDAO;
import np.com.naxa.factsnepal.database.FactsNepalDatabase;
import np.com.naxa.factsnepal.database.base.BaseLocalDataSourceRX;
import np.com.naxa.factsnepal.utils.SharedPreferenceUtils;

public class FactsLocalSource implements BaseLocalDataSourceRX<Fact> {

    private static FactsLocalSource INSTANCE = null;
    private FactsDAO factsDAO;

    public static FactsLocalSource getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new FactsLocalSource();
        }
        return INSTANCE;
    }

    private FactsLocalSource() {
        this.factsDAO = FactsNepalDatabase.getDatabase(FactsNepal.getInstance()).getFactsDAO();
    }


    public Completable toggleBookMark(Fact fact) {
        if (fact.isBookmarked()) {
            return markFactAsNotBookmarked(fact);
        } else {
            return markFactAsBookmarked(fact);
        }

    }

    private Completable markFactAsBookmarked(Fact fact) {
        return Completable.fromAction(() -> {
            fact.setBookmarked(true);
            factsDAO.update(fact);
        });

    }

    private Completable markFactAsNotBookmarked(Fact fact) {
        return Completable.fromAction(() -> {
            fact.setBookmarked(false);
            factsDAO.update(fact);
        });
    }

    @Override
    public Completable saveCompletable(Fact... items) {
        return Completable.fromAction(() -> factsDAO.insert(items));
    }

    @Override
    public Completable saveCompletable(List<Fact> items) {
        return Completable.fromAction(() -> factsDAO.insert(items));
    }

    @Override
    public void saveAsync(List<Fact> items) {
        AsyncTask.execute(() -> {
            factsDAO.insert(items);
        });
    }

    @Override
    public void save(List<Fact> items) {
        factsDAO.insert(items);
    }


    public LiveData<List<Fact>> getAll() {
        return factsDAO.getAll();
    }

    public LiveData<List<Fact>> getAllBookmarked() {
        return factsDAO.getAllBookmarked();
    }

    public LiveData<Fact> getById(String id) {
        return factsDAO.getById(id);
    }

    public LiveData<List<Fact>> getByIds(List<Integer> integers) {
        return factsDAO.getByIds(integers);
    }

    public LiveData<List<Fact>> getByIds(Set<String> integers) {
        return factsDAO.getByIds(integers);
    }

    public Single<List<Fact>> getAllCategories() {
        return factsDAO.getAllCategories();
    }
}
