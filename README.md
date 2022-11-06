# Interview Creation Portal

Android Application for scheduling Interviews built using Kotlin and Firebase in Android Studio

The app has following features and functionalities:
- The home page displays interview list for all the upcoming interviews
- Clicking the left button adds new participant to the database. The admin needs to add valid non-empty name and email-id, and the app throws an error message if the fields are empty. 
- Clicking the right button adds new meeting details. The admin has to give a meeting name, date, start and end time of the interview and select at least two participants from the list of added participants. The app throws an error if start date and time is less than current date and time, end time is less than start time, number of participants selected is less than two or any of the participants already have meetings scheduled during that time.
- On clicking any of the interview schedule gives details of that meeting similar to the add meeting page with an edit and delete option.