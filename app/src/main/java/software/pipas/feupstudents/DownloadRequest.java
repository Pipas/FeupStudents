package software.pipas.feupstudents;

public class DownloadRequest
{
    private String url;
    private String userAgent;
    private String contentDisposition;
    private String mimeType;

    public DownloadRequest(String url, String userAgent, String contentDisposition, String mimeType)
    {
        this.url = url;
        this.userAgent = userAgent;
        this.contentDisposition = contentDisposition;
        this.mimeType = mimeType;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public String getContentDisposition()
    {
        return contentDisposition;
    }

    public String getMimeType()
    {
        return mimeType;
    }
}
