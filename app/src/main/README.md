## Understanding the project
Explanation of key files for folks who want to learn or use this code


**activity/AllInOneActivity.java** The main activity of the project. Holds the viewpager for the four fragments.

**controller/GeofenceController.java** Handles setting up geofences, removing geofences, and all the callbacks.

**data/** The directory holds helper functions for sqlitedatabase interactions. There are two databases in this project, one for user messages/history and another that is a repository of all stored insults.

**receiver/AlarmManagerBroadcastReceiver** Responsible for all the scheduled stuff like when to show a notification

**util/ReminderOracle** The code for deciding what notification to show

Everything else is pretty much UI code and can be found under the appropriate adapter/fragment/etc directories.
