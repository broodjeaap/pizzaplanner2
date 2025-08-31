package com.pizzaplanner.data.models;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0019\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B[\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007\u0012\u0006\u0010\t\u001a\u00020\u0007\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0007\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u0010 \u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u0010\"\u001a\u00020\fH\u00c6\u0003J\u000b\u0010#\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003Ji\u0010$\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\t\u001a\u00020\u00072\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\u000b\u001a\u00020\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\t\u0010%\u001a\u00020&H\u00d6\u0001J\u0013\u0010\'\u001a\u00020(2\b\u0010)\u001a\u0004\u0018\u00010*H\u00d6\u0003J\t\u0010+\u001a\u00020&H\u00d6\u0001J\t\u0010,\u001a\u00020\u0003H\u00d6\u0001J\u0019\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u0002002\u0006\u00101\u001a\u00020&H\u00d6\u0001R\u0013\u0010\n\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0010R\u0011\u0010\t\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0010R\u0013\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0014R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0014R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0014\u00a8\u00062"}, d2 = {"Lcom/pizzaplanner/data/models/RecipeProgress;", "Landroid/os/Parcelable;", "plannedRecipeId", "", "stepId", "stepName", "scheduledStartTime", "Ljava/time/LocalDateTime;", "actualStartTime", "estimatedEndTime", "actualEndTime", "status", "Lcom/pizzaplanner/data/models/StepStatus;", "notes", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/time/LocalDateTime;Ljava/time/LocalDateTime;Ljava/time/LocalDateTime;Ljava/time/LocalDateTime;Lcom/pizzaplanner/data/models/StepStatus;Ljava/lang/String;)V", "getActualEndTime", "()Ljava/time/LocalDateTime;", "getActualStartTime", "getEstimatedEndTime", "getNotes", "()Ljava/lang/String;", "getPlannedRecipeId", "getScheduledStartTime", "getStatus", "()Lcom/pizzaplanner/data/models/StepStatus;", "getStepId", "getStepName", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "describeContents", "", "equals", "", "other", "", "hashCode", "toString", "writeToParcel", "", "parcel", "Landroid/os/Parcel;", "flags", "app_debug"})
@kotlinx.parcelize.Parcelize
public final class RecipeProgress implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String plannedRecipeId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String stepId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String stepName = null;
    @org.jetbrains.annotations.NotNull
    private final java.time.LocalDateTime scheduledStartTime = null;
    @org.jetbrains.annotations.Nullable
    private final java.time.LocalDateTime actualStartTime = null;
    @org.jetbrains.annotations.NotNull
    private final java.time.LocalDateTime estimatedEndTime = null;
    @org.jetbrains.annotations.Nullable
    private final java.time.LocalDateTime actualEndTime = null;
    @org.jetbrains.annotations.NotNull
    private final com.pizzaplanner.data.models.StepStatus status = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String notes = null;
    
    public RecipeProgress(@org.jetbrains.annotations.NotNull
    java.lang.String plannedRecipeId, @org.jetbrains.annotations.NotNull
    java.lang.String stepId, @org.jetbrains.annotations.NotNull
    java.lang.String stepName, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime scheduledStartTime, @org.jetbrains.annotations.Nullable
    java.time.LocalDateTime actualStartTime, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime estimatedEndTime, @org.jetbrains.annotations.Nullable
    java.time.LocalDateTime actualEndTime, @org.jetbrains.annotations.NotNull
    com.pizzaplanner.data.models.StepStatus status, @org.jetbrains.annotations.Nullable
    java.lang.String notes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getPlannedRecipeId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStepId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getStepName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime getScheduledStartTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.time.LocalDateTime getActualStartTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime getEstimatedEndTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.time.LocalDateTime getActualEndTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.pizzaplanner.data.models.StepStatus getStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.time.LocalDateTime component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.time.LocalDateTime component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.time.LocalDateTime component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.pizzaplanner.data.models.StepStatus component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.pizzaplanner.data.models.RecipeProgress copy(@org.jetbrains.annotations.NotNull
    java.lang.String plannedRecipeId, @org.jetbrains.annotations.NotNull
    java.lang.String stepId, @org.jetbrains.annotations.NotNull
    java.lang.String stepName, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime scheduledStartTime, @org.jetbrains.annotations.Nullable
    java.time.LocalDateTime actualStartTime, @org.jetbrains.annotations.NotNull
    java.time.LocalDateTime estimatedEndTime, @org.jetbrains.annotations.Nullable
    java.time.LocalDateTime actualEndTime, @org.jetbrains.annotations.NotNull
    com.pizzaplanner.data.models.StepStatus status, @org.jetbrains.annotations.Nullable
    java.lang.String notes) {
        return null;
    }
    
    @java.lang.Override
    public int describeContents() {
        return 0;
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
    
    @java.lang.Override
    public void writeToParcel(@org.jetbrains.annotations.NotNull
    android.os.Parcel parcel, int flags) {
    }
}