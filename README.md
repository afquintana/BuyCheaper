# BuyCheaper

Aplicación Android en Kotlin con Clean Architecture + MVVM + Compose.

## Stack
- Kotlin + Coroutines + StateFlow
- Jetpack Compose
- Hilt
- Retrofit
- Firebase Auth, Firestore, Analytics y Crashlytics

## Configuración Firebase
1. Crea un proyecto de Firebase.
2. Activa Authentication (email/password).
3. Crea Firestore en modo producción o test según necesites.
4. Descarga `google-services.json` y colócalo en `app/google-services.json`.
5. Añade reglas de Firestore para `sections`, `products` y `supermarkets`.

## Arquitectura
- `domain`: modelos, repositorios y casos de uso.
- `data`: implementaciones Firebase + API Retrofit.
- `presentation`: pantallas Compose y ViewModels.
- `di`: módulo Hilt.
