package com.android.foodrecipes.util;

import android.util.Log;

import com.android.foodrecipes.models.Recipe;

import java.util.List;

public class Testing {
    public static  void printRecipes(List<Recipe> list, String tag){
        for (Recipe recipe : list){
            Log.d(tag, "printRecipes: "+ recipe.getTitle() + " image url" + recipe.getImage_url());
        }
    }
}
