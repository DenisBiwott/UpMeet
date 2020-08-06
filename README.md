
UpMeet
==================
Initially called BookSpace, UpMeet is an android based platform that aims at connecting venue owners - (Could be owners of conference rooms, Board rooms, Party venues, Lounges, Open air spaces e.t.c) and Venue Seekers. In essence it aids in management of facilities and boosts customer reach on owners' side and easy search and online booking on seekers' side.

Upmeet is written mainly on Java using android studio and cloud functions on NodeJS 8 deployed to Firebase

Setup for development
---------------------
- Clone the project from the repostitory
- Install Android studio
- Build project in an android phone
- Setup nodeJS for management and deploying cloud functions
- Deploy cloud functions using `firebase deploy`
- Develop!

Cloud functions
---------------
- Mpesa runs nodejs 8. This along with a notification function happens in Firebase cloud functions.
- The source code is found in the parent directory titled *UpMeetCloudFunctions*

Features
--------
- These are the main feaures of UpMeet.
    - Add venues and display on map
    - Search for nearby venues
    - Reservation of facilities
    - Payment for venue facilities via M-pesa
    - Generate reservation tickets

Contributing
------------
Pull requests and improvement suggestions are welcome. For major changes, please open an issue first to discuss what you would like to change.


