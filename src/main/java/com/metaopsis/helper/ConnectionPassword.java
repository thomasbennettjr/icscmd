package com.metaopsis.helper;

import com.metaopsis.icsapi.impl.InformaticaCloudException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by tombennett on 8/17/17.
 */
public class ConnectionPassword
{
    private Path path;

    public ConnectionPassword(Path path)
    {
        this.path = path;
    }

    public Record[] getRecords() throws InformaticaCloudException
    {
        ArrayList<Record> records = new ArrayList<Record>();
        try(BufferedReader reader = Files.newBufferedReader(this.path, Charset.forName("UTF-8")))
        {
            String currentLine = null;
            while ((currentLine = reader.readLine()) != null)
            {
                String[] parts = currentLine.split(",");
                records.add(new Record(parts[0], parts[1]));
            }
        } catch(IOException e)
        {
            throw(new InformaticaCloudException(e.getMessage()));
        }


        return records.toArray(new Record[0]);
    }
}
