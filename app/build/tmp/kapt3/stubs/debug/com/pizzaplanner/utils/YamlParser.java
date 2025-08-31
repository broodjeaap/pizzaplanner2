package com.pizzaplanner.utils;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006H\u0002J\u001c\u0010\b\u001a\u00020\t2\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u000bH\u0002J\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\t0\r2\u0006\u0010\u000e\u001a\u00020\u000fJ(\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\r2\u0018\u0010\u0012\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u000b0\rH\u0002J(\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\r2\u0018\u0010\u0015\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u000b0\rH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/pizzaplanner/utils/YamlParser;", "", "()V", "yaml", "Lorg/yaml/snakeyaml/Yaml;", "generateId", "", "name", "parseRecipe", "Lcom/pizzaplanner/data/models/Recipe;", "recipeMap", "", "parseRecipes", "", "inputStream", "Ljava/io/InputStream;", "parseSteps", "Lcom/pizzaplanner/data/models/RecipeStep;", "stepsData", "parseVariables", "Lcom/pizzaplanner/data/models/RecipeVariable;", "variablesData", "app_debug"})
public final class YamlParser {
    @org.jetbrains.annotations.NotNull
    private final org.yaml.snakeyaml.Yaml yaml = null;
    
    public YamlParser() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.pizzaplanner.data.models.Recipe> parseRecipes(@org.jetbrains.annotations.NotNull
    java.io.InputStream inputStream) {
        return null;
    }
    
    private final com.pizzaplanner.data.models.Recipe parseRecipe(java.util.Map<java.lang.String, ? extends java.lang.Object> recipeMap) {
        return null;
    }
    
    private final java.util.List<com.pizzaplanner.data.models.RecipeVariable> parseVariables(java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> variablesData) {
        return null;
    }
    
    private final java.util.List<com.pizzaplanner.data.models.RecipeStep> parseSteps(java.util.List<? extends java.util.Map<java.lang.String, ? extends java.lang.Object>> stepsData) {
        return null;
    }
    
    private final java.lang.String generateId(java.lang.String name) {
        return null;
    }
}