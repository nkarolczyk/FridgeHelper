# FridgeHelper
Ostateczna zmiana Readme: 10.05.2026r

Projekt zaliczeniowy PAM (Projekt Aplikacji Mobilnej) 2026
Aplikacja na Androida do zarządzania zawartością lodówki. Możliwe skanowanie produktów, śledzenie ich daty ważności, otrzymywanie powiadomień i uzyskiwanie propozycji przepisów na podstawie posiadanych produktów. 

# WYKORZYSTANE TECHNOLOGIE:
Kotlin / Java – główny język programowania aplikacji.
Android Studio – środowisko programistyczne.
SQLite / Room Database – lokalna baza danych do przechowywania produktów.
Firebase – obsługa powiadomień i synchronizacji danych.
ML Kit / Barcode Scanner – skanowanie kodów kreskowych.
Gradle – system budowania projektu.

# STRUKTURA PROJEKTU:
MainActivity.kt – główne okno aplikacji.
Product.kt – model produktu.
ProductAdapter.kt – adapter do wyświetlania listy produktów.
DatabaseManager.kt – obsługa lokalnej bazy danych.
NotificationHelper.kt – system powiadomień.
ScannerActivity.kt – moduł skanowania kodów kreskowych.
RecipeManager.kt – generowanie propozycji przepisów.
build.gradle – konfiguracja budowania projektu.


# INSTALACJA I URUCHOMIENIE
Wymagania
Android Studio
Android SDK
Gradle
Urządzenie z systemem Android lub emulator

# Kroki instalacji
Sklonuj repozytorium:
git clone https://github.com/user/FridgeHelper.git
cd FridgeHelper
Otwórz projekt w Android Studio.
Zbuduj projekt przy użyciu Gradle.
Uruchom aplikację na emulatorze lub urządzeniu mobilnym:
Run 'app'
