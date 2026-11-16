# PartidosPolíticos - App Android Nativa en Java

Aplicación móvil nativa para Android desarrollada en **Java puro** con el SDK oficial de Android, siguiendo la arquitectura **MVC (Modelo - Vista - Controlador)** y las guías didácticas del profesor John Carlos Arrieta.

---

## 📌 ¿Qué hace la aplicación?

La aplicación permite la administración completa de partidos políticos y la gestión de usuarios conectándose a un backend REST:
- **Autenticación y Cuentas:**
  - Inicio de sesión seguro con JWT en memoria (`ActividadLogin`).
  - Registro de nuevos usuarios con rol `MEMBER` (`ActividadRegistro`).
  - Solicitud de restablecimiento de contraseña (`ActividadRecuperarClave`).
- **Navegación:** Menú principal con accesos rápidos (`ActividadPrincipal`).
- **CRUD de Partidos Políticos:** Guardar, buscar con diálogo personalizado, modificar, eliminar y limpiar campos (`ActividadCrudPartido`).
- **Listado Interactivo:** Visualización de todos los partidos con `ListView` y `ArrayAdapter`. Al tocar un partido se abre el formulario CRUD con todos sus datos cargados (`ActividadListaPartidos`).
- **Reportes Estadísticos:** Filtrado de partidos por país y por número mínimo de alcaldes (`ActividadReportes`).
- **Sensores de Hardware:** Integración con Acelerómetro (Shake) y Sensor de Proximidad.

---

## 🌐 Backend y Credenciales de Prueba

- **Base URL:** `https://partidos-api.onrender.com`
  > *Nota: Al estar alojado en el plan gratuito de Render, el backend puede tardar aproximadamente 1 minuto en despertar en la primera petición. La aplicación tiene configurados timeouts de 60 segundos.*
- **Usuario Administrador de Prueba:**
  - **Correo:** `admin@example.com`
  - **Contraseña:** `Admin1234!`

---

## 📱 Sensores de Hardware y Cómo Probarlos

La app implementa dos sensores nativos usando `SensorManager` y `SensorEventListener`, registrándolos en `onResume()` y desregistrándolos en `onPause()`. Si el dispositivo carece del sensor, se muestra un aviso didáctico sin fallar ni cerrarse.

### 1. Acelerómetro (Detección de Agitación / Shake)
- **Pantalla:** `ActividadCrudPartido` (Formulario).
- **Comportamiento:** Al agitar el teléfono, se ejecuta automáticamente el método `limpiarCampos(null)` vaciando el formulario y desplegando el Toast: `"Formulario limpiado"`.
- **Configuración:** Umbral de aceleración neta de `13.0 m/s²` y anti-rebote de `1 segundo`.
- **Cómo probarlo en el Emulador de Android Studio:**
  1. Abrir el emulador en Android Studio.
  2. En la barra lateral derecha del emulador, hacer clic en los tres puntos (`...`) **Extended controls**.
  3. Ir a la pestaña **Virtual sensors** > **Device pose** (o **Accelerometer**).
  4. Mover rápidamente los deslizadores de los ejes X, Y, Z o rotar bruscamente el dispositivo virtual para simular la sacudida. El formulario se limpiará de inmediato.

### 2. Sensor de Proximidad
- **Pantalla:** `ActividadPrincipal` (Menú Principal).
- **Comportamiento:** Al acercar la mano o un objeto a la pantalla (parte frontal superior), la app detecta la cercanía y muestra el Toast: `"Sensor de proximidad detectado"`.
- **Configuración:** Detección de distancia menor al rango máximo y anti-rebote de `2 segundos`.
- **Cómo probarlo en el Emulador de Android Studio:**
  1. Abrir el emulador.
  2. En la barra lateral derecha del emulador, hacer clic en los tres puntos (`...`) **Extended controls**.
  3. Ir a la pestaña **Virtual sensors**.
  4. En la tarjeta **Proximity**, cambiar el control deslizante de `5 cm` (Far) a `0 cm` (Near). Aparecerá el Toast en pantalla.

---

## 📦 Instalación del APK

El APK generado se encuentra copiado en la raíz del proyecto en:
```bash
apk/PartidosPoliticos-debug.apk
```

Para instalarlo en un dispositivo o emulador conectado mediante ADB:
```bash
adb install apk/PartidosPoliticos-debug.apk
```
O simplemente arrastre y suelte el archivo `PartidosPoliticos-debug.apk` dentro de la ventana de su emulador de Android Studio.

---

## 🏛️ Decisiones de Diseño e Implementación

En concordancia con los lineamientos del profesor y la regla de no realizar preguntas, se tomaron las siguientes decisiones:

1. **Java Puro y SDK Nativo:** Se utilizó exclusivamente Java con las librerías oficiales de Android (`HttpURLConnection`, `org.json`, `SensorManager`, `AlertDialog`, `ListView`, `ArrayAdapter`), sin dependencias de terceros como Retrofit, OkHttp, Volley ni Gson.
2. **Arquitectura MVC:**
   - **Modelo:** Entidades `Partido` y `Usuario` en el paquete `com.puelloemiro.partidos.entidades`.
   - **Acceso a Datos (DAO):** `ConexionApi`, `DaoPartido` y `DaoUsuario` en el paquete `com.puelloemiro.partidos.dao`, reemplazando el SQLite tradicional por llamadas directas a la API REST.
   - **Controlador:** Activities en `com.puelloemiro.partidos` gestionando ciclo de vida, eventos de red y sensores.
   - **Vista:** Layouts XML con botones configurados mediante atributos `android:onClick` que invocan métodos públicos con firma `void metodo(View v)`.
3. **Manejo de Sesión en Memoria:** La clase `Sesion.java` mantiene el `accessToken` en una variable estática en memoria RAM (`public static String token = null;`). No se utiliza `SharedPreferences`, base de datos local ni almacenamiento en disco. Al cerrar sesión o reiniciar el proceso, el token se destruye.
4. **PUT sin ID en el cuerpo:** Siguiendo la restricción del backend, en `DaoPartido.modificarPartido(Partido)` el ID se concatena en la URL (`PUT /api/partidos-politicos/{id}`) y el método `p.toJSON(false)` omite la clave `"id"` en el JSON enviado.
5. **Registro de Usuarios:** En `DaoUsuario.registrar(Usuario)` el ID del usuario se asigna con el mismo valor de su `email` y el rol asignado por defecto es `"MEMBER"`.
6. **Manejo Estricto de Cifras Electorales:** Los 5 contadores (`numPresidentes`, `numGobernadores`, `numAlcaldes`, `numConsejales`, `numCongresistas`) se validan como números enteros mayores o iguales a cero y se envían/reciben en el JSON como números (`int`/`Number`), respetando además la grafía exacta `"numConsejales"` devuelta por el servidor.
7. **Respuesta 204 en DELETE:** El backend responde con `204 No Content` tras una eliminación exitosa; `ConexionApi` reconoce el rango 200–299 como exitoso y maneja el flujo sin cuerpo adecuadamente.
8. **Manejo Centralizado de Errores:** Cuando el servidor devuelve un código de error, `ConexionApi` lee el `getErrorStream()` y parsea el objeto JSON `{"status": int, "message": "..."}`, extrayendo el mensaje descriptivo para propagarlo en una excepción que se muestra en un `Toast` claro al usuario.
9. **Operaciones Asíncronas Seguras:** Todas las peticiones HTTP se ejecutan en hilos secundarios gestionados con `ExecutorService`, y las actualizaciones de la interfaz y despliegue de mensajes se despachan mediante `runOnUiThread()`. Los botones de acción se deshabilitan y se despliega un `ProgressBar` durante las operaciones de red.
10. **Tolerancia a Dispositivos sin Sensores:** Ambas actividades con sensores verifican la disponibilidad del hardware con `getDefaultSensor()`. Si un dispositivo o emulador no cuenta con el sensor, muestra un Toast informativo sin generar fallos ni excepciones.
