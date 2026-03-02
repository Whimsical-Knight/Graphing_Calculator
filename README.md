Summary of code:

CalculatorButtons: class for handling calculator button UI

CalculatorEngine.kt: calculator logic
CalculatorViewModel.kt: receives input, decides what to do accordingly
GraphEngine: for generating graphs


In build.gradle.kts (:app), added dependency to allow for parsing mathematical expression
In MainActivity.kt, built calculator (see main file for notes). Compose based. No fragments used.