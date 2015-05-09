package com.nkanaev.comics.managers;


public class NavigationItem {
    public String title;
    public final int imageResource;

    public NavigationItem(String x, int imageResourceRef) {
        title = x;
        imageResource = imageResourceRef;
    }
}
