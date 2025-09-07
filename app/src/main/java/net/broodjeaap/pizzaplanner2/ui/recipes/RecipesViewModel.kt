package net.broodjeaap.pizzaplanner2.ui.recipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.broodjeaap.pizzaplanner2.data.models.Recipe

class RecipesViewModel : ViewModel() {

    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> = _recipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        _recipes.value = emptyList()
        _isLoading.value = false
        _error.value = null
    }

    fun setRecipes(recipes: List<Recipe>) {
        _isLoading.value = false
        _recipes.value = recipes
        _error.value = null
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun setError(errorMessage: String) {
        _isLoading.value = false
        _error.value = errorMessage
    }

    fun clearError() {
        _error.value = null
    }
}
