package com.jef.justenoughfakepixel.features.storage;

import java.util.HashMap;

public class StorageContainer {

    public HashMap<Integer,String> items;
    public String id;
    public ContainerType type;
    public int page;
    public int xGrid,yGrid;

    public StorageContainer(HashMap<Integer,String> items,ContainerType type,int page,int xGrid,int yGrid){
        this.items=  new HashMap<>(items);
        this.type = type;
        this.page = page;
        this.id = this.type.command + "-" + this.page;
        this.xGrid = xGrid;
        this.yGrid = yGrid;
    }

    public String getCommand(){
        return "/" + this.type.command + " " + this.page;
    }


    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

    }
}
