package np.com.naxa.factsnepal.network.facts;

public class Category {
    private boolean isSelected;

    private String id;

    private String title;

    private String slug;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }


    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", title = " + title + ", slug = " + slug + "]";
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}