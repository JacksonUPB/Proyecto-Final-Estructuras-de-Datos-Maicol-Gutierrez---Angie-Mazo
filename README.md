# ROCKET MISSION CONTROL
Sistema de gestión y simulación de misiones espaciales.

## Integrantes
- Angie Mazo
- Maicol Gutiérrez

## Estructuras de Datos Utilizadas
1. **Tabla Hash**: Búsqueda rápida de cohetes por código único.
2. **Cola FIFO**: Gestión del orden de llegada de solicitudes de lanzamiento.
3. **Cola de Prioridad**: Ordenamiento y atención de incidentes por severidad.


## Guía de uso del programa

Al ejecutar aparece el menú principal. Se escribe el número de la opción y Enter.
Los datos viven solo en memoria: al salir del programa (opción 0) se pierden.

```
=== ROCKET MISSION CONTROL ===
1. Cohetes
2. Misiones
3. Lanzamientos y simulación
4. Incidentes
5. Configuración de reglas
9. Cargar datos de ejemplo
0. Salir
```

### Opción rápida: datos de ejemplo
1. Escribir `9`. Se crean 3 cohetes, 3 misiones y 3 solicitudes en cola.
2. Escribir `3` y luego `3` (procesar siguiente solicitud), tres veces seguidas.
   Resultado esperado, en orden de llegada:
   - **M-001 (Atlas)**: EXITOSA.
   - **M-002 (Falcon)**: ABORTADA, por combustible insuficiente (tiene 100 de 800, o sea 12,5 %).
   - **M-003 (Orion)**: FALLIDA, por fallo del motor.
3. Escribir `4` y luego `1` para ver el historial de incidentes, de mayor a menor severidad
   (primero el fallo del motor, luego el combustible).

### Uso paso a paso (con datos propios)

**1. Registrar un cohete** (menú `1` → `1`)
- Código único: por ejemplo `ATL-01` (se guarda en mayúsculas; no se puede repetir).
- Nombre: por ejemplo `Atlas`.
- Capacidad de combustible: un número mayor que 0, por ejemplo `1000`.

El cohete nace con el tanque lleno, presión 75, motor operativo, sistema eléctrico disponible y comunicación activa.

**2. Consultar, listar, actualizar o eliminar cohetes** (menú `1`)
- `2` Consultar: pide el código.
- `3` Listar: muestra todos los cohetes.
- `4` Actualizar: pide el código y luego el campo a cambiar:
  1 Nombre, 2 Capacidad de combustible, 3 Nivel de combustible (entre 0 y la capacidad),
  4 Presión (no negativa), 5 Motor operativo, 6 Sistema eléctrico, 7 Comunicación
  (en los campos 5, 6 y 7 se responde `s` o `n`).
- `5` Eliminar: no se permite si el cohete tiene misiones programadas o en cola.

**3. Crear una misión** (menú `2` → `1`)
- Nombre de la misión: por ejemplo `Satélite alfa`.
- Código del cohete: debe existir, por ejemplo `ATL-01`.
- Fecha: formato `aaaa-mm-dd`, por ejemplo `2026-12-01`.
- Prioridad: entero de 1 (baja) a 5 (alta).

El sistema asigna el id automáticamente (`M-001`, `M-002`, ...). La misión queda en estado PROGRAMADA.
En ese menú también se puede consultar el estado (`2`, con el id) y listar todas (`3`).

**4. Solicitar y procesar lanzamientos** (menú `3`)
- `1` Solicitar lanzamiento: escribir el id de la misión (por ejemplo `M-001`). Pasa a la cola FIFO y queda EN_COLA. Una misma misión no se puede solicitar dos veces.
- `2` Ver solicitudes pendientes: lista la cola en orden de llegada.
- `3` Procesar siguiente solicitud: atiende la primera de la cola (la que llegó antes, sin importar su prioridad) y simula el lanzamiento según las condiciones del cohete.
- `4` Procesar siguiente eligiendo un fallo manual: igual que la opción 3, pero se fuerza un fallo:
  1 Combustible insuficiente, 2 Fallo del motor, 3 Presión fuera de rango, 4 Falla eléctrica, 5 Pérdida de comunicación, 0 Ninguno.

**5. Ver incidentes** (menú `4`)
- `1` Historial completo, de mayor a menor severidad (empate: orden de llegada).
- `2` Historial de una misión: pide el id (por ejemplo `M-002`).

**6. Cambiar las reglas** (menú `5`)
- `1` Combustible mínimo en % de la capacidad (valor entre 0 y 100; por defecto 30).
- `2` Rango de presión permitido: pide mínima y máxima (por defecto 50 y 100; la máxima debe ser mayor que la mínima).

### Reglas de la simulación
Las etapas se revisan en este orden y la simulación se detiene en el primer fallo:

| Etapa | Fallo (severidad) | Condición | Resultado |
|---|---|---|---|
| Preparación | Combustible insuficiente (3) | nivel por debajo del 30 % de la capacidad | ABORTADA |
| Cuenta regresiva | Presión fuera de rango (3) | presión menor que 50 o mayor que 100 | ABORTADA |
| Despegue | Fallo del motor (5) / Falla eléctrica (4) | motor no operativo / sistema eléctrico no disponible | FALLIDA |
| Ascenso | Pérdida de comunicación (2) | comunicación caída | FALLIDA |

Si no hay ningún fallo, la misión queda EXITOSA. Los valores exactamente en el límite (30 % de combustible, presión 50 o 100) no se consideran error.

### Ejemplos de valores para probar cada resultado
Con un cohete `T-1` de capacidad `1000`:
- **EXITOSA**: no cambiar nada, o dejar combustible en 300 (justo el límite).
- **ABORTADA por combustible**: Actualizar → campo 3 → nivel `100`.
- **ABORTADA por presión**: Actualizar → campo 4 → presión `120`.
- **FALLIDA por motor**: Actualizar → campo 5 → `n`.
- **FALLIDA por falla eléctrica**: Actualizar → campo 6 → `n`.
- **FALLIDA por comunicación**: Actualizar → campo 7 → `n`.

### Mensajes de error frecuentes
- "Ya existe un cohete con el código ...": el código está repetido.
- "No existe un cohete con el código ...": revisar que el cohete esté registrado antes de crear la misión.
- "La prioridad debe estar entre 1 (baja) y 5 (alta)": usar un entero de 1 a 5.
- "No hay solicitudes de lanzamiento pendientes": primero hay que solicitar el lanzamiento de alguna misión (menú 3 → opción 1).

## Estructura del proyecto
- `src/rocket/app`: menú principal (`Main`).
- `src/rocket/modelo`: clases del dominio (cohete, misión, incidente, etapas, estados y tipos de fallo).
- `src/rocket/estructuras`: tabla hash, cola FIFO y cola de prioridad.
- `src/rocket/servicio`: lógica del sistema y simulación de lanzamientos.
- `src/rocket/pruebas`: pruebas automáticas.

## Declaración de uso de Inteligencia Artificial
Se utilizó IA como apoyo complementario para la estructuración inicial de paquetes y revisión de sintaxis en Java, conforme a las directrices del curso. La lógica de negocio, las estructuras de datos y la implementación fueron validadas y ajustadas por el equipo. Adicionalmente, también existió intervención de IA en la construcción del Readme
