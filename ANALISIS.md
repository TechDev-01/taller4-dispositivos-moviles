# Análisis — Taller 4: Pantalla de perfil de usuario en Jetpack Compose
**Estudiante:** Steban Sepúlveda  
**Asignatura:** Programación para Dispositivos Móviles — Universidad de Caldas  

---

## 1. Predicción inicial 

1. **Si uso `remember { mutableStateOf(0) }` para un contador y roto la pantalla, ¿el valor se mantiene o se pierde? ¿Por qué?**
   * *Prediccion:* El valor se pierde y regresa a 0. Esto ocurre porque la rotación de pantalla causa una cambio de configuración (*configuration change*), destruyendo y recreando la `Activity`. Aunque `remember` conserva el estado durante las recomposiciones ordinarias del árbol de composables, este vive ligado al ciclo de vida del composable en memoria de la UI actual. Al recrearse la `Activity`, el árbol se destruye por completo y `remember` se inicializa de nuevo desde su valor inicial.

2. **Si tengo una lista creada con `remember { mutableListOf(...) }` y le agrego un elemento con `.add()`, ¿la pantalla se actualiza? ¿Por qué?**
   * *Prediccion:* La pantalla no se actualiza. Aunque la lista en memoria de JVM sí se muta internamente y suma el nuevo elemento, `mutableListOf` crea una instancia estándar de Java/Kotlin (`ArrayList`) que no es observable por el runtime de Jetpack Compose. Al no existir un mecanismo de suscripción a cambios ni un `State<T>` involucrado, Compose nunca se entera de que la lista cambió y no desencadena una recomposición.

3. **¿Qué diferencia hay entre `remember` y `rememberSaveable`?**
   * *Prediccion:* `remember` retiene un valor a través de las recomposiciones del marco de Compose mientras el nodo no se remueva de la jerarquía visual. `rememberSaveable` no solo retiene el valor durante la recomposicion, sino que además persiste automaticamente dicho estado en un `Bundle` (a través del `SavedStateRegistry`), sobreviviendo a la destruccion y recreacion de la `Activity` por cambios de configuración (como la rotación del dispositivo) o muerte de proceso iniciada por el sistema.

---

## 2. Verificación de la predicción

* **Pregunta 1 (Contador y rotación):** **Confirmada.** Al usar `remember { mutableStateOf(0) }` y rotar el emulador de Vertical a Horizontal, el contador se reinició de 5 a 0.
* **Pregunta 2 (Mutación de `mutableListOf`):** **Confirmada.** Al presionar "Agregar logro", la UI permanecía estática. Sin embargo, al inspeccionar mediante `Log.d`, se verificó que la lista en memoria pasó de tamaño 1 a 2, 3, etc.
* **Pregunta 3 (`remember` vs `rememberSaveable`):** **Confirmada.** `rememberSaveable` salvó automáticamente el estado entero en el `Bundle` durante la recreación de la `Activity`, manteniendo intacto el valor del contador tras la rotación.

---

## 3. Diagnóstico del bug (Parte 2)

### Síntoma
Al hacer clic sobre el botón "Agregar logro", la lista visual de logros no muestra el nuevo elemento en la pantalla y parece no reaccionar.

### ¿Por qué ocurre?
Compose basa sus actualizaciones de pantalla en el concepto de **recomposición**, la cual solo se dispara si se lee un tipo observable de estado reconocido por el marco, como `State<T>`. 
`remember { mutableListOf(...) }` instancia un `ArrayList` común. Al invocar `.add()`, se modifica la estructura interna del objeto en la JVM, pero no se emite ninguna notificación hacia el rastreador de instantáneas de Compose (*Snapshot system*). Por ende, Compose asume que nada ha cambiado y omite la fase de renderizado.

### Evidencia
Se agregó la traza de diagnóstico en el evento `onClick`:
```kotlin
OutlinedButton(onClick = {
    logros.add("Nuevo logro ${logros.size + 1}")
    Log.d("Taller4_Debug", "Lista interna actualizada. Tamaño actual: ${logros.size}")
})