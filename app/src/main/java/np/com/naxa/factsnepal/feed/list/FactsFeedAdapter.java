package np.com.naxa.factsnepal.feed.list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.schedulers.Schedulers;
import np.com.naxa.factsnepal.feed.Fact;
import np.com.naxa.factsnepal.feed.FactsLocalSource;
import np.com.naxa.factsnepal.notification.FactsNotification;
import np.com.naxa.factsnepal.utils.ImageUtils;
import np.com.naxa.factsnepal.common.OnCardItemClickListener;
import np.com.naxa.factsnepal.R;

public class FactsFeedAdapter extends RecyclerView.Adapter<FactsFeedAdapter.FeedCardVH> {

    private ArrayList<Fact> facts;
    private OnCardItemClickListener<Fact> listener;


    public FactsFeedAdapter(ArrayList<Fact> facts, OnCardItemClickListener<Fact> listener) {
        this.facts = facts;
        this.listener = listener;
    }

    public void updateList(List<Fact> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FactsDiffCallback(this.facts, newList));
        facts.clear();
        facts.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void add(Fact mc) {
        this.facts.add(mc);
        notifyItemInserted(this.facts.size() - 1);
    }

    public void addAll(List<Fact> mcList) {
        for (Fact mc : mcList) {
            add(mc);
        }
    }

    public void remove(Fact fact) {
        int position = this.facts.indexOf(fact);
        if (position > -1) {
            this.facts.remove(position);
            notifyItemRemoved(position);
        }
    }


    @NonNull
    @Override
    public FeedCardVH onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_facts_feed_card, parent, false);
        return new FeedCardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedCardVH viewHolder, int i) {
        Context context = viewHolder.imageView.getContext();
        int position = viewHolder.getAdapterPosition();
        Fact fact = facts.get(position);
        ImageUtils.loadRemoteImage(context, fact.getImagePath())
                .fitCenter()
                .into(viewHolder.imageView);

        viewHolder.tvTitle.setText(fact.getTitle());
        viewHolder.tvCategory.setText(fact.getCategory());
        viewHolder.tvSaved.setChecked(fact.isBookmarked());
    }

    @Override
    public int getItemCount() {
        return facts != null ? facts.size() : 0;
    }

    public ArrayList<Fact> getItems() {
        return facts;
    }


    class FeedCardVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTitle, tvCategory;
        ImageView imageView;
        View root;
        CheckBox tvSaved;

        FeedCardVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_feed_card_title);
            tvCategory = itemView.findViewById(R.id.tv_feed_card_category);
            tvSaved = itemView.findViewById(R.id.tv_saved);
            root = itemView.findViewById(R.id.root_item_facts_feed_card);
            imageView = itemView.findViewById(R.id.iv_feed);
            root.setOnClickListener(this);
            tvSaved.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_saved:
                    FactsLocalSource.getINSTANCE().toggleBookMark(facts.get(getAdapterPosition()))
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                    break;
                case R.id.root_item_facts_feed_card:
                    int pos = getAdapterPosition();
                    listener.onCardItemClicked(facts.get(pos), imageView);
                    break;
            }
        }
    }


}
