package com.metaopsis.helper;

/**
 * Created by tombennett on 8/17/17.
 */
public class Record
{
    private String connectionName;
    private String password;

    public Record()
    {
    }

    public Record(String connectionName, String password)
    {
        this.connectionName = connectionName;
        this.password = password;
    }

    public String getConnectionName()
    {
        return connectionName;
    }

    public void setConnectionName(String connectionName)
    {
        this.connectionName = connectionName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "Record{" +
                "connectionName='" + connectionName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
