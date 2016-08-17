package com.natech.roja;

/**
 * Created by Tshepo on 2015/07/14.
 */
@SuppressWarnings("DefaultFileTemplate")
class User {

    private final String name, surname, email, photoDir;

    public User(String name, String surname, String email, String photoDir)
    {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.photoDir = photoDir;
    }

    public String getName()
    {
        return name;
    }
    public String getSurname()
    {
        return surname;
    }
    public String getEmail()
    {
        return email;
    }
    public String getPhotoDir()
    {
        return photoDir;
    }
}
