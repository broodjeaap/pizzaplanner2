# 🔔 This app is 'vibe coded' 🔔

My other app, just plain [PizzaPlanner](/broodjeaap/pizzaplanner), was getting warnings from Google that it was becoming outdated, I've been meaning to give it an update, but never had the time...
So I decided it wouldn't be a bad non-trivial app to [vibe code](https://en.wikipedia.org/wiki/Vibe_coding), and... it was.
I've been both impressed and horrified at what it was doing, but here is the end result, not to bad for a few nights of prompting and ~20 dollars worth of credits (OpenRouter/KiloCode)

Anyhow, everything else in this repo is pretty much exclusively created by AI.

# Pizza Planner - Android App

A comprehensive Android application for planning and managing pizza dough preparation with customizable recipes, variable timing, and intelligent alarm system.

## Features

### 🍕 Recipe Management
- **Multiple Pizza Dough Recipes**: Neapolitan, New York Style, Sicilian, and more
- **YAML-Based Recipe Format**: Easy to update and customize recipes without app updates
- **Variable Customization**: Adjust rise time, dough quantity, ambient temperature, and yeast amounts
- **Recipe Download**: Update recipes from remote sources

### ⏰ Smart Planning & Timing
- **DateTime Picker**: Schedule when you want your dough ready
- **Backward Calculation**: Automatically calculates when to start based on your target time
- **Variable-Based Timing**: Recipe steps adjust based on your customized variables
- **Timeline Preview**: See the complete recipe timeline before starting

### 🔔 Advanced Alarm System
- **Full Alarm System**: Works even when the app is closed
- **Multiple Notification Types**: 
  - Step start alarms
  - Step completion reminders
  - Final completion notifications
- **Sound & Vibration**: Customizable alarm tones and vibration patterns
- **Full-Screen Alarms**: Critical steps show full-screen alerts
- **Background Processing**: Uses AlarmManager for reliable timing

### 📱 Modern Android Architecture
- **Material Design 3**: Modern, beautiful UI following Google's design guidelines
- **MVVM Architecture**: Clean separation of concerns with ViewModels and LiveData
- **Navigation Component**: Smooth navigation between app sections
- **Kotlin**: 100% Kotlin codebase with coroutines for async operations

## Technical Specifications

### Requirements
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern

### Key Dependencies
- **AndroidX Libraries**: Core, AppCompat, Material Design
- **Navigation Component**: Fragment navigation
- **Room Database**: Local data persistence
- **WorkManager**: Background task management
- **YAML Parser**: SnakeYAML for recipe parsing
- **OkHttp**: HTTP client for recipe downloads
- **Coroutines**: Asynchronous programming

### Permissions
- `SCHEDULE_EXACT_ALARM`: For precise recipe timing
- `POST_NOTIFICATIONS`: For step notifications
- `WAKE_LOCK`: To ensure alarms work when device is sleeping
- `VIBRATE`: For alarm vibration
- `INTERNET`: For recipe downloads

## Project Structure

```
app/
├── src/main/
│   ├── java/com/pizzaplanner/
│   │   ├── data/
│   │   │   ├── models/          # Recipe, PlannedRecipe, AlarmEvent data classes
│   │   │   ├── repository/      # Data access layer
│   │   │   └── network/         # Network operations
│   │   ├── ui/
│   │   │   ├── recipes/         # Recipe selection screens
│   │   │   ├── planning/        # Recipe planning and customization
│   │   │   ├── active/          # Active recipe tracking
│   │   │   └── settings/        # App settings
│   │   ├── services/            # Alarm, notification, and download services
│   │   ├── utils/               # YAML parser, time calculations
│   │   └── MainActivity.kt
│   ├── assets/
│   │   └── recipes/             # Default YAML recipe files
│   └── res/                     # UI layouts, strings, themes
```

## Recipe Format

Recipes are stored in YAML format with support for variables and formulas:

```yaml
recipes:
  - id: "neapolitan_pizza_dough"
    name: "Neapolitan Pizza Dough"
    description: "Traditional Italian thin crust pizza dough"
    difficulty: "Medium"
    total_time_hours: 24
    variables:
      - name: "rise_time_hours"
        display: "Rise Time (hours)"
        default: 24
        min: 6
        max: 72
        type: "integer"
        unit: "hours"
    steps:
      - id: "mix_ingredients"
        name: "Mix ingredients"
        description: "Combine flour, water, salt, and yeast"
        duration_minutes: 10
        timing: "start"
      - id: "first_rise"
        name: "First rise"
        description: "Let dough rise in covered bowl"
        duration_formula: "rise_time_hours * 0.4 * 60"
        timing: "after_previous"
```

## Key Features Implementation

### Variable Substitution
- Recipe descriptions support variable placeholders: `{variable_name}`
- Duration formulas support mathematical expressions with variables
- Real-time calculation of step timings based on user inputs

### Alarm System
- Uses Android's `AlarmManager` for exact timing
- Supports different alarm types (step start, step end, completion)
- Full-screen alarm activity for critical notifications
- Notification channels for different alarm priorities

### Time Calculations
- Backward calculation from target completion time
- Support for complex timing formulas
- Automatic adjustment based on ambient temperature and other variables

## Installation & Setup

1. **Clone the repository**
2. **Open in Android Studio**
3. **Sync Gradle dependencies**
4. **Run on device or emulator**

### Building
```bash
./gradlew assembleDebug
```

### Testing
```bash
./gradlew test
```

## Usage

1. **Select Recipe**: Browse available pizza dough recipes
2. **Customize Variables**: Adjust rise time, quantity, temperature, etc.
3. **Set Target Time**: Choose when you want your dough ready
4. **Review Timeline**: Check the calculated schedule
5. **Start Recipe**: Begin the process with automatic alarms
6. **Follow Alarms**: Receive notifications for each step

## Future Enhancements

- [ ] Custom recipe creation
- [ ] Recipe sharing between users
- [ ] Integration with smart home devices
- [ ] Weather-based timing adjustments
- [ ] Recipe rating and reviews
- [ ] Ingredient shopping lists
- [ ] Video tutorials for steps

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Pizza dough recipes inspired by traditional Italian and American techniques
- Material Design guidelines from Google
- Android development best practices from the Android team
