# Maps_Application

First activity signs into the app using Google Sign In intent. Second activity has two fragments, one to display Google Map and other to display list of locations which has been marked as favorite.
Tab layout is fixed to switch between the two fragments. 
 
In the map fragment, user can search for address or places with the search bar on top of the screen. It displays a list of possible places retrieved from Places API. 
Clicking on a place then zooms the camera to that location and places a marker there. User can click on the marker and add it to favorites. User can also return to their device location by clicking on blue icon on the right hand side.
 
In the favorite fragment, user can view the list of places that has been marked as favorite. User can also remove any place from the list.

Libraries used-
1) Google Play Services
2) Places API
3) Room Database
4) Coroutines
5) Recycler view
