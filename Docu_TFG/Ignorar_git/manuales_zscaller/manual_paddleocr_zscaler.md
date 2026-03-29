# Manual de Configuración de PaddleOCR con Zscaler

## Problema
Cuando intentas ejecutar aplicaciones que usan PaddleOCR u otras librerías Python que descargan datos de internet (requests, urllib3, etc.), obtienes errores de conexión o certificados SSL porque Zscaler intercepta las conexiones HTTPS.

**Errores típicos:**
```
ConnectTimeout: Connection to paddleocr.bj.bcebos.com timed out
SSLError: [SSL: CERTIFICATE_VERIFY_FAILED] certificate verify failed
urllib3.exceptions.MaxRetryError: HTTPSConnectionPool(...): Max retries exceeded
```

---

## Diagnóstico Rápido

### 1. Síntomas del problema
- ✅ pip funciona (ya configurado con proxy)
- ❌ Tu aplicación Python falla al descargar datos
- ❌ Librerías como PaddleOCR, requests, urllib3 no pueden conectarse
- ✅ Chrome puede descargar los archivos sin problema
- ✅ Tienes Zscaler instalado

### 2. Diferencia clave
- **pip**: Usa su propia configuración (ya lo arreglamos en el manual anterior)
- **Librerías Python (requests, etc.)**: Usan variables de entorno del sistema

---

## Solución Principal: Variables de Entorno

Las librerías Python como `requests`, `urllib3` (que usa PaddleOCR internamente) necesitan que configures el proxy mediante variables de entorno.

### Para PowerShell (Temporal - Solo esa sesión)

```powershell
$env:HTTP_PROXY="http://127.0.0.1:9000"
$env:HTTPS_PROXY="http://127.0.0.1:9000"
$env:REQUESTS_CA_BUNDLE=""
$env:CURL_CA_BUNDLE=""
python main.py
```

### Para CMD (Temporal - Solo esa sesión)

```cmd
set HTTP_PROXY=http://127.0.0.1:9000
set HTTPS_PROXY=http://127.0.0.1:9000
set REQUESTS_CA_BUNDLE=
set CURL_CA_BUNDLE=
python main.py
```

### Explicación de las variables

| Variable | Propósito |
|----------|-----------|
| `HTTP_PROXY` | Configura el proxy para conexiones HTTP |
| `HTTPS_PROXY` | Configura el proxy para conexiones HTTPS |
| `REQUESTS_CA_BUNDLE` | Al dejarlo vacío (""), desactiva la verificación de certificados SSL en requests |
| `CURL_CA_BUNDLE` | Al dejarlo vacío (""), desactiva la verificación de certificados SSL en curl |

**Nota:** Desactivar la verificación SSL es necesario porque Zscaler intercepta las conexiones con su propio certificado, que Python no reconoce por defecto.

---

## Configuración Permanente

Si trabajas regularmente con estas librerías, configura las variables de forma permanente para no tener que escribirlas cada vez.

### Paso 1: Abrir Configuración de Variables de Entorno

1. Presiona `Win + S` y busca "Variables de entorno"
2. Selecciona "Editar las variables de entorno del sistema"
3. Haz clic en "Variables de entorno..."

### Paso 2: Añadir Variables de Usuario

En la sección **"Variables del usuario para [tu nombre]"**, haz clic en "Nueva..." y añade:

**Variable 1:**
- Nombre: `HTTP_PROXY`
- Valor: `http://127.0.0.1:9000`

**Variable 2:**
- Nombre: `HTTPS_PROXY`
- Valor: `http://127.0.0.1:9000`

**Variable 3:**
- Nombre: `REQUESTS_CA_BUNDLE`
- Valor: (dejar vacío - solo crea la variable sin valor)

**Variable 4:**
- Nombre: `CURL_CA_BUNDLE`
- Valor: (dejar vacío - solo crea la variable sin valor)

### Paso 3: Aplicar cambios

1. Haz clic en "Aceptar" en todas las ventanas
2. **Reinicia** tu terminal (PowerShell, CMD, o VSCode)
3. Ejecuta tu aplicación normalmente:
   ```powershell
   python main.py
   ```

✅ **Ahora funcionará automáticamente** sin necesidad de configurar variables cada vez.

---

## Solución Alternativa: Script de Inicio

Si prefieres no modificar las variables de entorno del sistema, crea un script que configure todo automáticamente.

### Para PowerShell

Crea un archivo `run.ps1` en la raíz de tu proyecto:

```powershell
# Configurar proxy y certificados
$env:HTTP_PROXY="http://127.0.0.1:9000"
$env:HTTPS_PROXY="http://127.0.0.1:9000"
$env:REQUESTS_CA_BUNDLE=""
$env:CURL_CA_BUNDLE=""

# Activar entorno virtual (si lo usas)
.\.venv\Scripts\Activate.ps1

# Ejecutar aplicación
python main.py
```

**Uso:**
```powershell
.\run.ps1
```

### Para CMD

Crea un archivo `run.bat` en la raíz de tu proyecto:

```batch
@echo off
REM Configurar proxy y certificados
set HTTP_PROXY=http://127.0.0.1:9000
set HTTPS_PROXY=http://127.0.0.1:9000
set REQUESTS_CA_BUNDLE=
set CURL_CA_BUNDLE=

REM Activar entorno virtual (si lo usas)
call .venv\Scripts\activate.bat

REM Ejecutar aplicación
python main.py
```

**Uso:**
```cmd
run.bat
```

---

## Verificar Configuración

### Comprobar que las variables están configuradas

**En PowerShell:**
```powershell
echo $env:HTTP_PROXY
echo $env:HTTPS_PROXY
echo $env:REQUESTS_CA_BUNDLE
echo $env:CURL_CA_BUNDLE
```

Debe mostrar:
```
http://127.0.0.1:9000
http://127.0.0.1:9000
(vacío)
(vacío)
```

**En CMD:**
```cmd
echo %HTTP_PROXY%
echo %HTTPS_PROXY%
echo %REQUESTS_CA_BUNDLE%
echo %CURL_CA_BUNDLE%
```

### Probar con requests directamente

Crea un archivo `test_proxy.py`:
```python
import requests
import os

print("Proxy configurado:", os.environ.get('HTTPS_PROXY'))
print("CA Bundle:", os.environ.get('REQUESTS_CA_BUNDLE'))

try:
    response = requests.get('https://www.google.com', timeout=5)
    print("✅ Conexión exitosa:", response.status_code)
except Exception as e:
    print("❌ Error:", e)
```

Ejecuta:
```powershell
python test_proxy.py
```

Si sale "✅ Conexión exitosa", todo está bien configurado.

---

## Solución de Problemas

### Problema 1: Sigue sin funcionar después de configurar variables permanentes

**Causa:** La terminal no ha recargado las variables de entorno.

**Solución:**
1. Cierra **completamente** tu terminal (PowerShell, CMD, VSCode)
2. Abre una nueva terminal
3. Verifica con `echo $env:HTTP_PROXY` que la variable existe
4. Intenta de nuevo

### Problema 2: Funciona en una terminal pero no en otra

**Causa:** Cada tipo de terminal (PowerShell vs CMD) puede tener diferentes variables.

**Solución:** Configura las variables de entorno del **sistema** (no solo de la sesión) siguiendo los pasos de "Configuración Permanente".

### Problema 3: Error "execution of scripts is disabled on this system"

**Causa:** PowerShell tiene restricciones de ejecución de scripts.

**Solución temporal:**
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run.ps1
```

**Solución permanente (requiere admin):**
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
```

### Problema 4: PaddleOCR descarga modelos muy lento

**Causa:** El servidor de PaddleOCR está en China y puede ser lento.

**Solución:**
1. Descarga manualmente los modelos desde Chrome
2. Colócalos en la carpeta correcta:
   ```
   C:\Users\[tu_usuario]\.paddleocr\whl\
   ```
3. PaddleOCR detectará que ya están descargados

**Modelos comunes de PaddleOCR:**
- Detección: `https://paddleocr.bj.bcebos.com/PP-OCRv3/english/en_PP-OCRv3_det_infer.tar`
- Reconocimiento: `https://paddleocr.bj.bcebos.com/PP-OCRv3/english/en_PP-OCRv3_rec_infer.tar`
- Clasificación: `https://paddleocr.bj.bcebos.com/dygraph_v2.0/ch/ch_ppocr_mobile_v2.0_cls_infer.tar`

### Problema 5: Funciona en desarrollo pero no en producción/ejecutable

**Causa:** Al crear un .exe con PyInstaller u otras herramientas, las variables de entorno pueden no transmitirse.

**Solución:** Configura las variables en el código Python directamente:
```python
import os

# Configurar proxy al inicio del script
os.environ['HTTP_PROXY'] = 'http://127.0.0.1:9000'
os.environ['HTTPS_PROXY'] = 'http://127.0.0.1:9000'
os.environ['REQUESTS_CA_BUNDLE'] = ''
os.environ['CURL_CA_BUNDLE'] = ''

# Luego importar PaddleOCR
from paddleocr import PaddleOCR
```

---

## Librerías Afectadas

Estas son algunas librerías Python que necesitan la misma configuración de proxy:

- ✅ **requests** - Cliente HTTP
- ✅ **urllib3** - Base de bajo nivel para HTTP
- ✅ **PaddleOCR** - OCR con deep learning
- ✅ **httpx** - Cliente HTTP moderno
- ✅ **aiohttp** - Cliente HTTP asíncrono
- ✅ **wget** - Descarga de archivos
- ✅ **transformers** (Hugging Face) - Modelos de IA
- ✅ **torch.hub** - Descarga de modelos PyTorch
- ✅ **tensorflow** - Descarga de modelos

**Todas estas librerías funcionarán** una vez configuradas las variables de entorno.

---

## Comandos de Referencia Rápida

### PowerShell - Configuración temporal
```powershell
$env:HTTP_PROXY="http://127.0.0.1:9000"; $env:HTTPS_PROXY="http://127.0.0.1:9000"; $env:REQUESTS_CA_BUNDLE=""; $env:CURL_CA_BUNDLE=""; python main.py
```

### CMD - Configuración temporal
```cmd
set HTTP_PROXY=http://127.0.0.1:9000 && set HTTPS_PROXY=http://127.0.0.1:9000 && set REQUESTS_CA_BUNDLE= && set CURL_CA_BUNDLE= && python main.py
```

### Verificar variables (PowerShell)
```powershell
Get-ChildItem Env: | Where-Object {$_.Name -like "*PROXY*" -or $_.Name -like "*BUNDLE*"}
```

### Verificar variables (CMD)
```cmd
set | findstr /I "PROXY BUNDLE"
```

---

## Comparación de Métodos

| Método | Duración | Ventajas | Desventajas |
|--------|----------|----------|-------------|
| **Variables temporales** | Solo esa sesión | Rápido para probar | Hay que repetirlo cada vez |
| **Variables permanentes** | Para siempre | Una vez y olvidar | Afecta a todo (puede causar problemas sin Zscaler) |
| **Script de inicio** | Controlado | Balance perfecto | Hay que ejecutar el script |
| **Hardcoded en código** | En la app | Funciona en ejecutables | Menos flexible |

**Recomendación:** Usa **variables permanentes** si siempre trabajas con Zscaler, o **script de inicio** si solo a veces necesitas el proxy.

---

## Seguridad y Consideraciones

### ¿Es seguro desactivar la verificación SSL?

**En entorno corporativo con Zscaler:** Sí, es necesario y relativamente seguro porque:
- Zscaler es tu proxy de seguridad corporativo
- Ya está validando las conexiones por ti
- Es la única forma de que funcionen estas librerías

**Fuera del entorno corporativo:** No desactives la verificación SSL, ya que te expone a ataques man-in-the-middle.

### ¿Qué pasa si trabajo desde casa sin Zscaler?

Si desconectas Zscaler o trabajas desde casa:
1. Las conexiones **pueden seguir funcionando** (el proxy 127.0.0.1:9000 simplemente no responderá y Python usará conexión directa)
2. Si da problemas, simplemente elimina las variables temporalmente
3. O usa un script condicional que detecte si Zscaler está activo

---

## Notas Adicionales

- El proxy `127.0.0.1:9000` es creado automáticamente por Zscaler
- Chrome y otros navegadores usan este proxy automáticamente vía configuración PAC
- Herramientas de línea de comandos (Python, curl, git) necesitan configuración manual
- Esta configuración también funciona para git, curl, wget y otras herramientas CLI

---

## Resumen Ejecutivo

**Para empezar ahora mismo (PowerShell):**
```powershell
$env:HTTP_PROXY="http://127.0.0.1:9000"
$env:HTTPS_PROXY="http://127.0.0.1:9000"
$env:REQUESTS_CA_BUNDLE=""
$env:CURL_CA_BUNDLE=""
python main.py
```

**Para no tener que hacerlo más:**
1. Abre "Variables de entorno" en Windows
2. Añade las 4 variables en "Variables del usuario"
3. Reinicia tu terminal
4. ¡Listo para siempre!

---

**Fecha de creación:** Febrero 2026  
**Versión:** 1.0  
**Compatible con:** Python 3.x, PaddleOCR, requests, urllib3, Zscaler  
**Documento relacionado:** manual_pip.md
