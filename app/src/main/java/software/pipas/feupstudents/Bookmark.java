package software.pipas.feupstudents;

public class Bookmark
{
    private int id;
    private String url;
    private String title;

    public Bookmark(){}

    public Bookmark(String title, String url)
    {
        super();
        this.title = title;
        this.url = url;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return title;
    }
}
