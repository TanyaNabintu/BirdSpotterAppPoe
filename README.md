# BirdSpotterAp
Bird Project

## Step 1:
1. Download the code from this link: https://github.com/TanyaNabintu/BirdSpotterAppPoe.git
2. Open the downloaded folder in Android Studio.
   -OR-
   Open Android Studio, click on File -> New -> Project from Version Control -> Paste the link: https://github.com/TanyaNabintu/BirdSpotterAppPoe.git

## Step 2:
Once the download is complete, Android Studio will automatically download dependencies.
On the top bar, find the "Run" menu, click on it, and select "Run 'app'."

## Step 3:
If the process runs successfully, it will install the app on the emulator, bringing you to the login screen. If it's your first time, click on "Register" and fill in three fields:
   - Email
   - Password
   - Confirm Password
Afterward, go back to the login page and fill in:
   - Email
   - Password

## Step 4:
After successful login, you'll be redirected to the home page displaying a list of birds. You can sort by rarity, name, and date. Additionally, you can hotspot them to view their location on Google Maps.

Click on a picture to view bird details.

## Step 5:
On the bottom right, there's a plus (+) button. Clicking it opens a screen to add a new bird. Try entering the following fields:
   - Bird Name
   - Rarity (select from common, rare, extremely rare)
   - Notes
   - Upload Images
   - Click "Add"
The bird will be added to Firebase Firestore.

## Step 6:
For updating or deleting a bird, you must be the owner (the one who added it). Click on the bird, and two buttons will appear at the bottom - one for updating and the other for deleting. You can edit fields or delete as needed.

## Step 7:
At the top of the list, there's a search bar allowing you to search by name, rarity, or address. Click on the item to view details with a simple click.

## Step 8:
To add a bird to the map's hotspots, press and hold the click button. Once the message pops up, add the bird's information. Click "Save," and it will be added to the map. To view descriptions of other birds, click on the red pin to display all the information.
