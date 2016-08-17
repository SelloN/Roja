package com.natech.roja.HomeContent;

/**
 * Created by Tshepo on 2015/07/09.
 */
@SuppressWarnings("DefaultFileTemplate")
public class Promotions  {

    private final String photoDir, promoLink, caption,restID,restName,largePhotoDir,description;
    private final int promoType;

    public Promotions(String caption, String photoDir, String promoLink, String restID, String restName,
                      int promoType,String largePhotoDir, String description)
    {
        this.caption = caption;
        this.photoDir = photoDir;
        this.promoLink = promoLink;
        this.restID = restID;
        this.restName = restName;
        this.promoType = promoType;
        this.largePhotoDir = largePhotoDir;
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public String getLargePhotoDir(){
        return largePhotoDir;
    }

    public int getPromoType(){
        return promoType;
    }

    public String getPhotoDir(){
        return photoDir;
    }

    public String getPromoLink(){
        return promoLink;
    }

    public String getCaption(){
        return caption;
    }

    public String getRestID(){
        return restID;
    }

    public String getRestName(){
        return restName;
    }


}
