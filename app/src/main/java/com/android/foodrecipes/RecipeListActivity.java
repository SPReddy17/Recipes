package com.android.foodrecipes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.android.foodrecipes.adapters.OnRecipeListener;
import com.android.foodrecipes.adapters.RecipeRecyclerAdapter;
import com.android.foodrecipes.models.Recipe;

import com.android.foodrecipes.util.Testing;
import com.android.foodrecipes.util.VerticalSpacingItemDecorator;
import com.android.foodrecipes.viewmodels.RecipeListViewModel;
import java.util.List;
public class RecipeListActivity extends BaseActivity implements  OnRecipeListener {

    private Button test;

    private RecyclerView mRecyclerView;

    private RecipeListViewModel mRecipeListViewmodel;
    private SearchView mSearchView;

    private static final String TAG = "RecipeListActivity";
    private RecipeRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        mRecyclerView = findViewById(R.id.recipe_list);

        mSearchView = findViewById(R.id.search_view);
        mRecipeListViewmodel = new ViewModelProvider(this).get(RecipeListViewModel.class);
       initRecyclerView();
        subscribeObservers();
        initSearchView();
        //testRetrofitResponse();

        if(!mRecipeListViewmodel.isViewingRecipes()){
             //display search categories
            displaySearchCategories();
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar ));

    }


    private void subscribeObservers(){
        mRecipeListViewmodel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                if (recipes != null){
                    if(mRecipeListViewmodel.isViewingRecipes()) {
                        Testing.printRecipes(recipes, "recipes Test");
                        mRecipeListViewmodel.setIsPerformingQuery(false);
                        mAdapter.setRecipes(recipes);
                    }
                 }
            }
        });
        mRecipeListViewmodel.isQueryExhausted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    Log.d(TAG, "onChanged: query is exhausted");
                    mAdapter.setQueryExhausted();
                }
            }
        });
    }

    private void initRecyclerView(){
        mAdapter = new RecipeRecyclerAdapter(this );
        mRecyclerView.setAdapter(mAdapter);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(30);
        mRecyclerView.addItemDecoration(itemDecorator);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(!mRecyclerView.canScrollVertically(1)){
                    // search the next page...
                    mRecipeListViewmodel.searchNextPage();
                }
            }
        });



    }


    private void initSearchView(){
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                mAdapter.displayLoading();
                mRecipeListViewmodel.searchRecipesApi(s, 1);
                mSearchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
//    private void testRetrofitResponse(){
//
//        searchRecipesApi("chicken breast",1);
//        RecipeApi recipeApi = ServiceGenerator.getRecipeApi();

/*//        Call<RecipeSearchResponse> responseCall = recipeApi
//                .searchRecipe(Constants.API_KEY,
//                        "chicken breast",
//                                "1"
//                );
//        responseCall.enqueue(new Callback<RecipeSearchResponse>() {
//            @Override
//            public void onResponse(Call<RecipeSearchResponse> call, Response<RecipeSearchResponse> response) {
//                Log.d(TAG, "onResponse: server esponse  " + response.toString());
//                if(response.code() == 200){
//                    Log.d(TAG, "onResponse: " + response.body().toString());
//
//                    List<Recipe> recipes = new ArrayList<>(response.body().getRecipes());
//
//                    for(Recipe recipe : recipes){
//                        Log.d(TAG, "onResponse: " + recipe.getTitle());
//                    }
//                }
//                else{
//                    try {
//                        Log.d(TAG, "onResponse: " + response.errorBody().string());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<RecipeSearchResponse> call, Throwable t) {
//
//            }
//        });

//        Call<RecipeResponse> recipeResponseCall = recipeApi
//                .getRecipe(Constants.API_KEY,
//                        "41470"
//                );
//        recipeResponseCall.enqueue(new Callback<RecipeResponse>() {
//            @Override
//            public void onResponse(Call<RecipeResponse> call, Response<RecipeResponse> response) {
//                Log.d(TAG, "onResponse: server response" + response.toString() );
//                if(response.code()== 200){
//                    Log.d(TAG, "onResponse: " +response.body().toString());
//                    Recipe recipe = response.body().getRecipe();
//                    Log.d(TAG, "onResponse: retrieved recipe " + recipe.toString());
//                }
//                else{
//                    try {
//                        Log.d(TAG, "onResponse: " + response.errorBody().string());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<RecipeResponse> call, Throwable t) {
//
//            }
//        });*/

  //  }

    @Override
    public void onRecipeClick(int position) {
        Intent intent =  new Intent(this,RecipeActivity.class);
        intent.putExtra("recipe",mAdapter.getSelectedRecipe(position));
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(String category) {
        mAdapter.displayLoading();
        mRecipeListViewmodel.searchRecipesApi(category, 1);
        mSearchView.clearFocus();
    }

    private void displaySearchCategories(){
        mRecipeListViewmodel.setIsViewingRecipes(false);
        mAdapter.displaySearchCategories();
    }

    @Override
    public void onBackPressed() {

        if(mRecipeListViewmodel.onBackPressed()) {
            super.onBackPressed();
        }
        else{
            displaySearchCategories();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_categories){
            displaySearchCategories();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_search_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}