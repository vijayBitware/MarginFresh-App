package com.marginfresh.Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Admin on 6/22/2017.
 */

public class GetNearBy implements Serializable
{
    private String message;

    private String status;

    private ArrayList<Nearby_store_array> nearby_store_array;

    public String getMessage ()
    {
        return message;
    }

    public void setMessage (String message)
    {
        this.message = message;
    }

    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }

    public ArrayList<Nearby_store_array> getNearby_store_array ()
    {
        return nearby_store_array;
    }

    public void setNearby_store_array (ArrayList<Nearby_store_array> nearby_store_array)
    {
        this.nearby_store_array = nearby_store_array;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [message = "+message+", status = "+status+", nearby_store_array = "+nearby_store_array+"]";
    }


    public class Nearby_store_array
    {
        private String store_image_url;

        private String store_id;

        private String store_name;

        public String getStore_image_url ()
        {
            return store_image_url;
        }

        public void setStore_image_url (String store_image_url)
        {
            this.store_image_url = store_image_url;
        }

        public String getStore_id ()
        {
            return store_id;
        }

        public void setStore_id (String store_id)
        {
            this.store_id = store_id;
        }

        public String getStore_name ()
        {
            return store_name;
        }

        public void setStore_name (String store_name)
        {
            this.store_name = store_name;
        }

        @Override
        public String toString()
        {
            return "ClassPojo [store_image_url = "+store_image_url+", store_id = "+store_id+", store_name = "+store_name+"]";
        }
    }

}
