# FridgeHelper

**Ostateczna zmiana Readme: 10.05.2026r**

FridgeHelper to projekt zaliczeniowy PAM (Projekt Aplikacji Mobilnej) 2026.  
Aplikacja mobilna na system Android służąca do zarządzania zawartością lodówki.  
Umożliwia skanowanie produktów, śledzenie dat ważności, otrzymywanie powiadomień oraz uzyskiwanie propozycji przepisów na podstawie posiadanych składników.

---

# WYKORZYSTANE TECHNOLOGIE

- **Kotlin / Java** – główny język programowania aplikacji  
- **Android Studio** – środowisko programistyczne  
- **SQLite / Room Database** – lokalna baza danych do przechowywania produktów  
- **Firebase** – obsługa powiadomień i synchronizacji danych  
- **ML Kit / Barcode Scanner** – skanowanie kodów kreskowych  
- **Gradle** – system budowania projektu  

---

# STRUKTURA PROJEKTU

```text
MainActivity.kt         - główne okno aplikacji
Product.kt              - model produktu
ProductAdapter.kt       - adapter do wyświetlania listy produktów
DatabaseManager.kt      - obsługa lokalnej bazy danych
NotificationHelper.kt   - system powiadomień
ScannerActivity.kt      - moduł skanowania kodów kreskowych
RecipeManager.kt        - generowanie propozycji przepisów
build.gradle            - konfiguracja budowania projektu
```

---

# INSTALACJA I URUCHOMIENIE

## Wymagania

- Android Studio  
- Android SDK  
- Gradle  
- Urządzenie z systemem Android lub emulator  

---

## Kroki instalacji

### 1. Sklonuj repozytorium

```bash
git clone https://github.com/user/FridgeHelper.git
cd FridgeHelper
```

### 2. Otwórz projekt w Android Studio

Uruchom Android Studio i wybierz opcję:

```text
Open an Existing Project
```

Następnie wskaż folder projektu `FridgeHelper`.

---

### 3. Zbuduj projekt

Projekt zostanie automatycznie zsynchronizowany z Gradle.  
W razie potrzeby wybierz:

```text
Build → Make Project
```

---

### 4. Uruchom aplikację

Uruchom emulator Androida lub podłącz urządzenie mobilne, a następnie wybierz:

```text
Run 'app'
```
