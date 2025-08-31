package com.pizzaplanner.utils;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001BG\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\t\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e\u00a2\u0006\u0002\u0010\u0010J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\u0015\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\tH\u00c6\u0003J\t\u0010\u001f\u001a\u00020\tH\u00c6\u0003J\t\u0010 \u001a\u00020\fH\u00c6\u0003J\u000f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u00c6\u0003JW\u0010\"\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u0014\b\u0002\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u00052\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\f2\u000e\b\u0002\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u00c6\u0001J\u0013\u0010#\u001a\u00020$2\b\u0010%\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010&\u001a\u00020\fH\u00d6\u0001J\t\u0010\'\u001a\u00020\u0006H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\n\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0014R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u001d\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001b\u00a8\u0006("}, d2 = {"Lcom/pizzaplanner/utils/RecipeTimeline;", "", "recipe", "Lcom/pizzaplanner/data/models/Recipe;", "variableValues", "", "", "", "startTime", "Ljava/time/LocalDateTime;", "targetCompletionTime", "totalDurationMinutes", "", "steps", "", "Lcom/pizzaplanner/utils/StepTimeline;", "(Lcom/pizzaplanner/data/models/Recipe;Ljava/util/Map;Ljava/time/LocalDateTime;Ljava/time/LocalDateTime;ILjava/util/List;)V", "getRecipe", "()Lcom/pizzaplanner/data/models/Recipe;", "getStartTime", "()Ljava/time/LocalDateTime;", "getSteps", "()Ljava/util/List;", "getTargetCompletionTime", "getTotalDurationMinutes", "()I", "getVariableValues", "()Ljava/util/Map;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class RecipeTimeline {
    @org.jetbrains.annotations.NotNull
    private final com.pizzaplanner.data.models.Recipe recipe = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.Map<java.lang.String, java.lang.Double> variableValues = null;
    @org.jetbrains.annotations.NotNull
    private final java.time.LocalDateTime startTime = null;
    @org.jetbrains.annotations.NotNull
    private final java.time.LocalDateTime targetCompletionTime = null;
    private final int totalDurationMinutes = 0;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<com.pizzaplanner.utils.StepTimeline> steps = null;
    
    public RecipeTimeline(@org.jetbrains.annotations.NotNull
    com.pizzaplanner.data.models.Recipe recipe, @org.jetbrains.annotations.NotNull
    java.util.Map<java.lang.String, java.lang.Double> variableValues, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime startTime, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime targetCompletionTime, int totalDurationMinutes, @org.jetbrains.annotations.NotNull
    java.util.List<com.pizzaplanner.utils.StepTimeline> steps) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.pizzaplanner.data.models.Recipe getRecipe() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.Map<java.lang.String, java.lang.Double> getVariableValues() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime getStartTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime getTargetCompletionTime() {
        return null;
    }
    
    public final int getTotalDurationMinutes() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.pizzaplanner.utils.StepTimeline> getSteps() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.pizzaplanner.data.models.Recipe component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.Map<java.lang.String, java.lang.Double> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime component4() {
        return null;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.pizzaplanner.utils.StepTimeline> component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.pizzaplanner.utils.RecipeTimeline copy(@org.jetbrains.annotations.NotNull
    com.pizzaplanner.data.models.Recipe recipe, @org.jetbrains.annotations.NotNull
    java.util.Map<java.lang.String, java.lang.Double> variableValues, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime startTime, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime targetCompletionTime, int totalDurationMinutes, @org.jetbrains.annotations.NotNull
    java.util.List<com.pizzaplanner.utils.StepTimeline> steps) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}