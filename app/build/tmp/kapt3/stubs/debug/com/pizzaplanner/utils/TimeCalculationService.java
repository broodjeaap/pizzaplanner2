package com.pizzaplanner.utils;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J*\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b2\u0006\u0010\u000b\u001a\u00020\fJ$\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\bH\u0002J$\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u00122\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u00122\u0006\u0010\u0016\u001a\u00020\fH\u0002J\u0016\u0010\u0017\u001a\u00020\u000e2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u0012H\u0002J$\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\t2\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\bH\u0002J\u0010\u0010\u001b\u001a\u00020\n2\u0006\u0010\u001c\u001a\u00020\tH\u0002J\u001c\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\u00122\u0006\u0010\u001f\u001a\u00020\t2\u0006\u0010 \u001a\u00020\u0004J0\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00150\u00122\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00100\u00122\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\bH\u0002J$\u0010\"\u001a\u00020\t2\u0006\u0010#\u001a\u00020\t2\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\bH\u0002\u00a8\u0006$"}, d2 = {"Lcom/pizzaplanner/utils/TimeCalculationService;", "", "()V", "calculateRecipeTimeline", "Lcom/pizzaplanner/utils/RecipeTimeline;", "recipe", "Lcom/pizzaplanner/data/models/Recipe;", "variableValues", "", "", "", "targetCompletionTime", "Ljava/time/LocalDateTime;", "calculateStepDuration", "", "step", "Lcom/pizzaplanner/data/models/RecipeStep;", "calculateStepTimeline", "", "Lcom/pizzaplanner/utils/StepTimeline;", "steps", "Lcom/pizzaplanner/utils/ProcessedStep;", "startTime", "calculateTotalDuration", "evaluateFormula", "formula", "variables", "evaluateSimpleExpression", "expression", "generateAlarmEvents", "Lcom/pizzaplanner/data/models/AlarmEvent;", "plannedRecipeId", "timeline", "processSteps", "substituteVariables", "text", "app_debug"})
public final class TimeCalculationService {
    
    public TimeCalculationService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.pizzaplanner.utils.RecipeTimeline calculateRecipeTimeline(@org.jetbrains.annotations.NotNull
    com.pizzaplanner.data.models.Recipe recipe, @org.jetbrains.annotations.NotNull
    java.util.Map<java.lang.String, java.lang.Double> variableValues, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime targetCompletionTime) {
        return null;
    }
    
    private final java.util.List<com.pizzaplanner.utils.ProcessedStep> processSteps(java.util.List<com.pizzaplanner.data.models.RecipeStep> steps, java.util.Map<java.lang.String, java.lang.Double> variableValues) {
        return null;
    }
    
    private final int calculateStepDuration(com.pizzaplanner.data.models.RecipeStep step, java.util.Map<java.lang.String, java.lang.Double> variableValues) {
        return 0;
    }
    
    private final double evaluateFormula(java.lang.String formula, java.util.Map<java.lang.String, java.lang.Double> variables) {
        return 0.0;
    }
    
    private final double evaluateSimpleExpression(java.lang.String expression) {
        return 0.0;
    }
    
    private final java.lang.String substituteVariables(java.lang.String text, java.util.Map<java.lang.String, java.lang.Double> variables) {
        return null;
    }
    
    private final int calculateTotalDuration(java.util.List<com.pizzaplanner.utils.ProcessedStep> steps) {
        return 0;
    }
    
    private final java.util.List<com.pizzaplanner.utils.StepTimeline> calculateStepTimeline(java.util.List<com.pizzaplanner.utils.ProcessedStep> steps, java.time.LocalDateTime startTime) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.pizzaplanner.data.models.AlarmEvent> generateAlarmEvents(@org.jetbrains.annotations.NotNull
    java.lang.String plannedRecipeId, @org.jetbrains.annotations.NotNull
    com.pizzaplanner.utils.RecipeTimeline timeline) {
        return null;
    }
}