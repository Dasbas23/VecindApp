# Manual de Configuración de Gradle con Zscaler (Android Studio)

> **Versión:** 1.1 — Marzo 2026  
> **Compatible con:** Android Studio Ladybug+, Gradle 7.x/8.x, Zscaler  
> **Documentos relacionados:** `manual_pip.md`, `manual_paddleocr_zscaler.md`

---

## Problema

Cuando Zscaler está activo, intercepta todas las conexiones HTTPS y presenta su propio certificado SSL. Java/Gradle no reconoce ese certificado y bloquea las descargas de dependencias (Google Maven, Maven Central, JitPack, etc.).

### Errores típicos

```text
PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
unable to find valid certification path to requested target

Got SSL handshake exception during request. It might be caused by SSL misconfiguration

Could not resolve all files for configuration ':app:debugRuntimeClasspath'
```

### Diagnóstico rápido

| Situación | Causa probable |
|---|---|
| Sync OK pero Build falla | El `gradle.properties` del proyecto no tiene las líneas de trustStore |
| Todo falla con timeout (7+ min) | Falta configuración de proxy |
| Todo falla con SSL al instante | Falta el certificado de Zscaler en el cacerts |
| Solo fallan librerías de JitPack | Falta `maven { url = uri("https://jitpack.io") }` en `settings.gradle.kts` |

---

## Fase 1: Exportar el Certificado de Zscaler

1. Abre **Chrome/Edge** y ve a cualquier web HTTPS (ej. `https://google.com`).
2. Haz clic en el **candado** de la barra de direcciones → *La conexión es segura* → *El certificado es válido*.
3. Ve a la pestaña **Ruta de certificación** (o *Detalles*).
4. Selecciona el certificado de **nivel superior** (suele llamarse `Zscaler Root CA`).
5. Haz clic en **Exportar** (o *Copiar en archivo*).
6. Elige formato: **DER binario codificado** (`.cer` o `.der`).
7. Guárdalo como: `ZscalerRootCA.der` en tu carpeta de Descargas.

> **Importante:** Asegúrate de seleccionar el certificado **raíz** (el de arriba del todo en la jerarquía), no el intermedio ni el del sitio web.

---

## Fase 2: Crear el Almacén de Confianza (cacerts)

Vamos a crear una **copia** del almacén de certificados de Java en tu carpeta de usuario e importar el certificado de Zscaler ahí. Así no necesitas permisos de administrador.

### Paso 1: Localizar el keytool de Android Studio

El `keytool.exe` viene con el JDK de Android Studio. Suele estar en una de estas rutas:

```
C:\Users\<USUARIO>\AppData\Local\android-studio\jbr\bin\keytool.exe
C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe
```

Para encontrarlo, ejecuta en PowerShell:

```powershell
Get-ChildItem -Path "$env:LOCALAPPDATA\android-studio\jbr\bin\keytool.exe" -ErrorAction SilentlyContinue
Get-ChildItem -Path "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -ErrorAction SilentlyContinue
```

> En los comandos siguientes, sustituye `<RUTA_KEYTOOL>` por la ruta que hayas encontrado.

### Paso 2: Importar el certificado

```powershell
& "<RUTA_KEYTOOL>" -importcert -file "$env:USERPROFILE\Downloads\ZscalerRootCA.der" -keystore "$env:USERPROFILE\.gradle\cacerts" -alias zscaler_root -storepass changeit
```

Cuando pregunte si confías en el certificado, escribe **`si`** (en español) o **`yes`** (según el idioma del sistema).

> **Nota:** Si el almacén no existe, `keytool` lo crea automáticamente. La contraseña `changeit` es el estándar de Java.

### Paso 3: Verificar la importación

```powershell
& "<RUTA_KEYTOOL>" -list -keystore "$env:USERPROFILE\.gradle\cacerts" -storepass changeit -alias zscaler_root
```

Si ves la **huella SHA-256** del certificado, está correctamente importado.

---

## Fase 3: Configurar Gradle

Este es el paso más crítico. Hay que configurar **dos archivos** `gradle.properties` porque el del proyecto puede sobrescribir al global.

### A. gradle.properties GLOBAL

Ubicación: `C:\Users\<USUARIO>\.gradle\gradle.properties`

Crea o edita el archivo y añade estas **6 líneas**:

```properties
# Proxy de Zscaler
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=9000
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=9000

# Certificados (TrustStore) — usar dobles barras \\
systemProp.javax.net.ssl.trustStore=C:\\Users\\<USUARIO>\\.gradle\\cacerts
systemProp.javax.net.ssl.trustStorePassword=changeit
```

### B. gradle.properties DEL PROYECTO

Ubicación: `C:\Ruta\A\Tu\Proyecto\gradle.properties`

Añade al menos las **2 líneas** del trustStore:

```properties
# Certificados (TrustStore)
systemProp.javax.net.ssl.trustStore=C:\\Users\\<USUARIO>\\.gradle\\cacerts
systemProp.javax.net.ssl.trustStorePassword=changeit
```

> **¿Por qué en los dos sitios?** Gradle lee ambos archivos, pero las propiedades del proyecto tienen prioridad sobre las globales. Si el proyecto define proxy pero no trustStore, el daemon de compilación no sabe dónde encontrar el certificado y falla la descarga.

### C. Reiniciar el Daemon

Gradle mantiene un proceso en segundo plano (daemon) que cachea la configuración. Hay que matarlo para que lea los cambios:

```powershell
cd C:\Ruta\A\Tu\Proyecto
.\gradlew.bat --stop
```

Después, en Android Studio: **Clean Project** → **Rebuild Project**.

---

## Solución de Problemas

### "Android Studio me pregunta si quiero usar la configuración de proxy detectada"

**Evita darle a Sí.** Android Studio sobrescribe el `gradle.properties` global y **borra las líneas del trustStore**. Si le diste a Sí por accidente, vuelve a añadir las líneas del trustStore manualmente.

### "Access Denied al usar keytool"

Estás intentando modificar el `cacerts` original de la instalación de Java (en Archivos de Programa). Este manual crea el archivo en `C:\Users\<USUARIO>\.gradle\cacerts` precisamente para evitar ese problema. Asegúrate de usar la ruta correcta.

### "keytool no se reconoce como un comando"

La ruta al `keytool.exe` no es correcta. Búscalo con:

```powershell
Get-ChildItem -Recurse -Path "$env:LOCALAPPDATA\android-studio" -Filter "keytool.exe" 2>$null
```

### "Trust store file does not exist or is not readable"

Este warning aparece en equipos donde **no tienes Zscaler** (ej. tu PC de casa) porque el `gradle.properties` del proyecto apunta a un `cacerts` que solo existe en el PC del trabajo. **No afecta a la compilación** — Gradle lo ignora y usa el almacén de certificados por defecto de Java, que funciona perfectamente sin Zscaler.

### "Sync funciona pero Build falla"

Causa: El Sync utiliza dependencias cacheadas, pero el Build necesita descargar nuevas y el daemon no tiene configurado el trustStore. **Solución:** Asegúrate de que el `gradle.properties` del proyecto también tiene las líneas del trustStore (Fase 3, apartado B).

### Librerías de JitPack no se descargan

Verifica que tu `settings.gradle.kts` incluye el repositorio de JitPack:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

---

## Cómo Deshacer los Cambios

Si necesitas revertir todo (ej. cambio de PC, desinstalación de Zscaler):

1. **Borrar el cacerts local:**
   ```powershell
   Remove-Item "$env:USERPROFILE\.gradle\cacerts"
   ```

2. **Eliminar las líneas añadidas** de ambos `gradle.properties` (las 6 del global, las 2 del proyecto).

3. **Reiniciar el daemon:**
   ```powershell
   .\gradlew.bat --stop
   ```

---

## Resumen Ejecutivo (Cheatsheet)

Para configurar un equipo nuevo desde cero:

```powershell
# 1. Exportar certificado Zscaler desde Chrome (formato DER, guardar en Downloads)

# 2. Importar al cacerts local
& "<RUTA_KEYTOOL>" -importcert -file "$env:USERPROFILE\Downloads\ZscalerRootCA.der" -keystore "$env:USERPROFILE\.gradle\cacerts" -alias zscaler_root -storepass changeit

# 3. Configurar gradle.properties GLOBAL (~\.gradle\gradle.properties)
#    → Añadir las 6 líneas (proxy + trustStore)

# 4. Configurar gradle.properties DEL PROYECTO
#    → Añadir las 2 líneas (trustStore)

# 5. Reiniciar daemon
.\gradlew.bat --stop

# 6. Clean + Rebuild en Android Studio
```
