# Real-Time Route Tracking Android App

A native Android application built with Java that allows users to select a pickup and drop-off location, view a calculated route, and track their real-time movement along the path.

---

##Overview

This application enables users to:

- Select pickup and drop-off points using map taps 
- View a calculated route between the two points
- Track their real-time movement along the route
- See live updates of estimated time and distance remaining

---


### Features
1. Location Selection
   - Select pickup/drop-off locations via map tap
   

2. **Route Display**
   - Use Google Directions API to draw a route on the map

3. **Real-Time Movement Tracking**
   - Continuously track the user's GPS position
   - Dynamically draw their movement on the map

4. **ETA & Distance Calculation**
   - Display estimated time and distance remaining in real-time

---

## ⚙Technical Stack

| Component         | Technology                      |
|------------------|----------------------------------|
| Language          | Java                            |
| Architecture      | MVVM                            |
| Maps              | Google Maps SDK + Directions API|
| Location Tracking | FusedLocationProviderClient     |
| UI                | ViewBinding                     |
| Background Tasks  | Foreground Service              |
| Minimum SDK       | 23+                             |

---




---

## 🧪 Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/crowuory/TrackerTest 
   cd your-repo-name
2. Open in Android Studio

Open the project using Android Studio.

3. Google Maps & API Keys

Add your API keys for Google Maps, Directions, and Places API.

## 🔑 API Keys Required

This project uses the following Google APIs:
- Maps SDK
- Directions API
- Places API

To run the app, create your own API key via [Google Cloud Console](https://console.cloud.google.com/), enable the required APIs, and add the keys


4. Run the App

Connect a device or use an emulator

Click Run in Android Studio
