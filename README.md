# Train Rush

## Description

Train rush is a simple Android application, inspired by the original snake game, where players could control the train to collect passengers while avoiding crashing into walls or the train itself. To run on Android Studio, we target Android API level 33. PLease configure the virtual simulator device to match this API.

## Controls
|Control | Description |
|-----------------|-----------------|
| L      | Move train to the left direction (POV of train head) |
| R      | Move train to the right direction (POV of train head)|

## Rules

In the Snake Game, players control a train that must pick up passengers within a limited time frame. Here are the rules regarding passenger pickup and time constraints:
- The train must pick up a passenger within 7 seconds of starting the game or after the previous passenger pickup
- A progress bar simulates the time available for passenger pickup. Once the time limit of 7 seconds elapses, certain consequences occur:
  - If the train successfully picks up a passenger within the time limit, the time resets to 7 seconds
  - If the train fails to pick up a passenger within the time limit and there is currently no passenger available, the game ends
  - If the train fails to pick up a passenger within the time limit but there is a passenger available, the train decreases in size
 
