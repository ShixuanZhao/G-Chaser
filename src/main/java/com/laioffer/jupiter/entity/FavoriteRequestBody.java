package com.laioffer.jupiter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

//Json serializeת����java class
public class FavoriteRequestBody {
    private final Item favoriteItem;
   //constructor����annotation��key��favorite��ǰ�ˣ���key��Item��Ҫת���ɵ�
   // java class��
    @JsonCreator
    public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) {
        this.favoriteItem = favoriteItem;
    }

    public Item getFavoriteItem() {
        return favoriteItem;
    }
}
