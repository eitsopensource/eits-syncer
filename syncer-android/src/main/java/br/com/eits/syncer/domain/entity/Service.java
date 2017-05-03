package br.com.eits.syncer.domain.entity;

/**
 *
 */
public class Service
{
    public static Service of(String serviceName)
    {
        //TODO get from a static list
        return new Service(serviceName, null);
    }

    /**
     *
     */
    private String name;
    /**
     *
     */
    private java.net.URL url;

    /**
     *
     * @param name
     * @param url
     */
    public Service(String name, java.net.URL url)
    {
        this.name = name;
        this.url = url;
    }

    /**
     *
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     *
     * @return
     */
    public java.net.URL getUrl()
    {
        return url;
    }
}