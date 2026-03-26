# Manual de Configuración de pip con Zscaler

## Problema
Cuando intentas instalar paquetes con `pip`, obtienes errores de conexión o certificados SSL porque Zscaler intercepta las conexiones HTTPS.

**Errores típicos:**
```
SSLError(SSLCertVerificationError(1, '[SSL: CERTIFICATE_VERIFY_FAILED]'))
Could not fetch URL https://pypi.org/simple/...
```

---

## Diagnóstico Rápido

### 1. Verificar si Chrome funciona pero pip no
- ✅ Chrome puede acceder a pypi.org
- ❌ `pip install` falla con errores SSL
- ✅ Tienes Zscaler instalado

### 2. Verificar configuración de proxy
```cmd
netsh winhttp show proxy
```

Deberías ver algo como:
```
Script de configuración: http://127.0.0.1:9000/localproxy-xxxxx.pac
```

---

## Solución Paso a Paso

### Opción A: Configuración Permanente (Recomendada)

Esta configuración se hace **una sola vez** y funciona para todos tus proyectos y entornos virtuales.

#### Paso 1: Activar tu entorno virtual
```cmd
cd C:\ruta\a\tu\proyecto
.venv\Scripts\activate
```

#### Paso 2: Configurar pip globalmente
```cmd
pip config set global.trusted-host "pypi.org files.pythonhosted.org pypi.python.org"
pip config set global.proxy http://127.0.0.1:9000
```

#### Paso 3: Verificar la configuración
```cmd
pip config list
```

Deberías ver:
```
global.proxy='http://127.0.0.1:9000'
global.trusted-host='pypi.org files.pythonhosted.org pypi.python.org'
```

#### Paso 4: Probar la instalación
```cmd
pip install numpy
```

✅ **Listo**: Ahora todos tus `pip install` funcionarán automáticamente.

---

### Opción B: Solución Temporal (Por comando)

Si no quieres configurar nada permanentemente, usa estos parámetros en cada instalación:

```cmd
pip install nombre-paquete --proxy http://127.0.0.1:9000 --trusted-host pypi.org --trusted-host files.pythonhosted.org
```

**Ejemplo:**
```cmd
pip install pandas --proxy http://127.0.0.1:9000 --trusted-host pypi.org --trusted-host files.pythonhosted.org
```

---

## Comandos Útiles

### Ver la configuración actual de pip
```cmd
pip config list
```

### Ver dónde está guardada la configuración
```cmd
pip config list -v
```

La configuración global se guarda en:
```
%APPDATA%\pip\pip.ini
```

### Eliminar la configuración (si necesitas resetear)
```cmd
pip config unset global.proxy
pip config unset global.trusted-host
```

### Actualizar pip
```cmd
python -m pip install --upgrade pip
```

---

## Solución de Problemas

### Problema: "pip" no se reconoce como comando

**Causa:** No tienes activado el entorno virtual o pip no está en el PATH.

**Solución 1:** Activa tu entorno virtual
```cmd
.venv\Scripts\activate
```

**Solución 2:** Usa python -m pip
```cmd
python -m pip install paquete
```

### Problema: Sigue fallando después de configurar

**Verifica que Zscaler esté corriendo:**
- El proxy `127.0.0.1:9000` debe estar activo
- Zscaler debe estar conectado

**Verifica la configuración:**
```cmd
pip config list
```

**Prueba con un paquete simple:**
```cmd
pip install requests
```

### Problema: Funciona en un proyecto pero no en otro

**Verifica que estás en el entorno virtual correcto:**
```cmd
where python
```

Debe mostrar la ruta de tu `.venv`, no la de Python global.

---

## Preguntas Frecuentes

### ¿Necesito privilegios de administrador?
**No.** La configuración `global` de pip se guarda en tu perfil de usuario, no requiere permisos de admin.

### ¿Tengo que hacer esto para cada entorno virtual?
**No.** La configuración `global` se aplica a todos los entornos virtuales de tu usuario automáticamente.

### ¿Qué pasa si trabajo sin Zscaler (desde casa)?
Si no estás conectado a Zscaler, puedes:
1. Eliminar la configuración del proxy temporalmente
2. O dejarla, normalmente no causará problemas

### ¿Funciona con requirements.txt?
**Sí.** Una vez configurado:
```cmd
pip install -r requirements.txt
```

### ¿Funciona con poetry o pipenv?
Sí, pero necesitan su propia configuración:

**Poetry:**
```cmd
poetry config http-basic.pypi username password
poetry config certificates.pypi.cert false
```

**Pipenv:**
```cmd
export PIPENV_PYPI_MIRROR=http://127.0.0.1:9000
```

---

## Resumen de Comandos Importantes

```cmd
# Configuración completa (una sola vez)
pip config set global.trusted-host "pypi.org files.pythonhosted.org pypi.python.org"
pip config set global.proxy http://127.0.0.1:9000

# Verificar configuración
pip config list

# Instalar paquetes (después de configurar)
pip install nombre-paquete

# Actualizar pip
python -m pip install --upgrade pip

# Resetear configuración si es necesario
pip config unset global.proxy
pip config unset global.trusted-host
```

---

## Notas Adicionales

- **Zscaler** es un proxy de seguridad corporativo que intercepta conexiones HTTPS
- El proxy local `127.0.0.1:9000` es creado por Zscaler automáticamente
- Chrome usa este proxy automáticamente, pero herramientas de línea de comandos necesitan configuración manual
- Esta configuración es segura y recomendada para entornos corporativos con Zscaler

---

**Fecha de creación:** Febrero 2026  
**Versión:** 1.0  
**Compatible con:** Python 3.x, pip 20+, Zscaler
