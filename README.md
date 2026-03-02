**SUMMARY OF CODE:**

Dependencies in my build gradle file:
•	object hunter: for parsing mathematical expressions, which helps me distinguish points from functions on my graphing calculator
•	lifecycle view model compose: helps me store my view in real time—things like zoom level, graph points, etc.

PROGRAM FILES
1. MainActivity: call material theme, which sets my colors, typography, shapes, etc. Then surface, which applies my background colors, etc. Then here is where we call our
3. CalculatorTabsApp: loads my tabs, tracks which tab is selected, defines horizontal tab bar, reloads compose when a new tab is selected.
5. CalculatorViewModel: initiate math parsing (for mathematical expressions), does NOT compute math itself but delegates that responsibility elsewhere. For data handling… my _expression and _results are StateFlows—which are reactive, in-memory streams that are observed by Compose UI. Updates are immediately reflected in the UI.
7. Calculator Engine: parses input for my calculator. Here at fun evaluate, you can see we define a function with input, where “expression” is our input parameter that returns a string.
9. GraphEngine: defines structure to hold/parse functions and user points. To draw the graph, this loops across the screen pixels, converting those pixels to an x-value. Then it connects the lines. This is done hundreds or thousands of times per frame to make the curve appear smooth.
11. CalculatorButtons: for scientific calculator, defines the buttons and recognizes when those buttons are pressed. Has conditions for each of the 3 main operations: add input, clear input, and compute. At Column, we basically define a 2D array filled with buttons. We also define button click logic.
13. FunctionGraph: This handles my graphing UI—i.e., the actual graph drawing.
15. GraphCalculator: Handles other UI elements in my graphing calculator tab—so the layout and state management of the screen. Basically sets the background screen.
17. ScientificCalculator: Handles the UI elements behind my scientific calculator—not the math but the background screen and results. Passes the logic to CalculatorButton to update the state when a button is pressed.
