package com.natech.roja.MenuCategories;

/**
 * Created by Tshepo on 2015/06/17. Class that holds the values for the menu categories
 */
@SuppressWarnings("SameParameterValue")
public class Category {

    private final String category, description, photoDir;
    private final int catID;
    private boolean hasExtras = false;
    private boolean isNested = false;
    public Category(String category, String description, String photoDir,int catID,int hasExtras,boolean isNested)
    {
        this.category = category;
        this.description = description;
        this.photoDir = photoDir;
        this.catID = catID;
        this.hasExtras = hasExtras == 1;
        this.isNested = isNested;

    }

    public String getCategory(){
        return category;
    }

    public String getDescription(){
        return description;
    }

    public String getPhotoDir(){
        return photoDir;
    }
    public int getCatID()
    {
        return catID;
    }

    public Boolean isSpecials(){
        return category.equalsIgnoreCase("Specials");
    }

    public boolean hasExtras(){
        return hasExtras;
    }

    public boolean isNested(){
        return isNested;
    }
}
