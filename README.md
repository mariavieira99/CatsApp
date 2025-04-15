# Cats App

An Android app that allows users to explore and manage their favorite cats, built with clean MVVM architecture principles, using Jetpack Compose, Room for local storage, and Retrofit for network communication.

---

## Features

- **Browse Cats**: Displays a list of cats fetched from an API.
- **Search**: Easily search for cat by breed name.
- **Browse Favourite Cats**: Displays a list of favourite cats previously added by the user.
- **Add/Remove Favorites**: Mark/unmark cats as favorites.
- **Cat Details**: View detailed information about each cat.

---

## Hybrid Data Fetching

- The app follows a **hybrid architecture**, combining **network** and **local database (Room)** sources for a balance between responsiveness and offline support.
- On **first launch** (or when Room is empty), cat data is fetched from the Cats API on endpoint - https://api.thecatapi.com/v1/breeds. The response is **saved to Room**.
- **Subsequent reads** are served from Room, not the network.
- This improves performance and resilience to network issues.

## Periodic Sync

- In order to keep the data update while the user is active (app is opened), there is a job running and every 5 minutes it will fetch the latest data and updates the room database and UI in case there were any changes. A 5-minute sync interval ensures the data stays up-to-date with minimal network usage, providing a fresh user experience without overwhelming the system with frequent updates.

## Internet-Aware Syncing

- If the device goes offline, the periodic sync job is cancelled. When comes back online, the app automatically triggers a sync and fetches the updated data from the CatsAPI and updates the room database and UI in case there were any changes. This ensures up-to-date content even after a connection loss.

## Favorites: Network as Source of Truth

- Favorites are managed by the backend (CatsAPI) — all add/remove actions are performed via API requests.
- The app stores the favorite state locally in the Room database using a flag on the CatModel entity. This enables instant UI updates and consistent state across screens.
- Since the backend is the single source of truth, the user cannot modify favorites while offline. This avoids sync conflicts and ensures that local data always matches the server.