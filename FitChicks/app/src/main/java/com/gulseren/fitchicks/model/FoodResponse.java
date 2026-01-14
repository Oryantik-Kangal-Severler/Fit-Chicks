package com.gulseren.fitchicks.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FoodResponse {
    @SerializedName("items")
    private List<FoodModel> items;

    public List<FoodModel> getItems() {return items;}

    public void setItems(List<FoodModel> items){
        this.items = items;
    }


}
