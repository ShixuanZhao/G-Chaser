package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

//Json serialize转换成java class
public class FavoriteRequestBody {
    private final Item favoriteItem;
   //constructor加上annotation。key是favorite（前端），key是Item（要转换成的
   // java class）
    @JsonCreator
    public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) {
        this.favoriteItem = favoriteItem;
    }

    public Item getFavoriteItem() {
        return favoriteItem;
    }
}
