package com.pizzaplanner.services;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0014\u0010\u000b\u001a\u00020\b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rJ\u000e\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\u000eJ\u0014\u0010\u0011\u001a\u00020\b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/pizzaplanner/services/AlarmService;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "alarmManager", "Landroid/app/AlarmManager;", "cancelAlarm", "", "alarmId", "", "cancelAllAlarms", "alarmEvents", "", "Lcom/pizzaplanner/data/models/AlarmEvent;", "scheduleAlarm", "alarmEvent", "scheduleMultipleAlarms", "app_debug"})
public final class AlarmService {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final android.app.AlarmManager alarmManager = null;
    
    public AlarmService(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    public final void scheduleAlarm(@org.jetbrains.annotations.NotNull
    com.pizzaplanner.data.models.AlarmEvent alarmEvent) {
    }
    
    public final void cancelAlarm(@org.jetbrains.annotations.NotNull
    java.lang.String alarmId) {
    }
    
    public final void scheduleMultipleAlarms(@org.jetbrains.annotations.NotNull
    java.util.List<com.pizzaplanner.data.models.AlarmEvent> alarmEvents) {
    }
    
    public final void cancelAllAlarms(@org.jetbrains.annotations.NotNull
    java.util.List<com.pizzaplanner.data.models.AlarmEvent> alarmEvents) {
    }
}