# Taller GA — Implementación y comparativa ORM vs. SQL puro

**Asignatura:** Aplicaciones Web (5.º semestre)
**Docente:** Dr. Gleiston Cicerón Guerrero Ulloa, Ph.D.
**Autor:** Alucard — UTEQ, Ingeniería de Software

## Descripción

Este repositorio implementa el mismo CRUD (listar, crear, eliminar) de dos formas distintas sobre PostgreSQL 16, para comparar rendimiento, mantenibilidad y seguridad:

- **`/`** (raíz) — Módulo **JDBC puro** con `PreparedStatement` (java.sql).
- **`/taller-jpa-puro`** — Módulo **Spring Data JPA + Hibernate**.

Ambos módulos operan sobre la misma base de datos `taller_db` y la misma tabla `productos` (100 registros sembrados con `generate_series`).

## Tabla comparativa final

| Criterio | JDBC puro con PreparedStatement | Spring Data JPA + Hibernate |
|---|---|---|
| **(1) Líneas de código** (repositorio + conexión) | 87 líneas (`Conexion.java` + `ProductoRepositorioJdbc.java`, incluye método extra de búsqueda segura) | 33 líneas (`Producto.java` con getters/setters manuales + `ProductoRepository.java`) |
| **(2) Tiempo del listado** (100 filas, mediana de 3–4 corridas) | nanoTime: **49.7 ms** · StopWatch: **103.3 ms** | nanoTime: **214.3 ms** · StopWatch: **9.0 ms** |
| **(3) Facilidad de mantenimiento** | El SQL está escrito en cadenas dentro del código Java: cualquier cambio de esquema exige retocar cada consulta manualmente. El mapeo `ResultSet → objeto` es código repetitivo (boilerplate) que se repite en cada método. | El mapeo objeto-relacional lo hace Hibernate mediante anotaciones (`@Entity`, `@Column`); los métodos CRUD básicos vienen dados por `JpaRepository` sin escribir una sola línea de SQL. Boilerplate mínimo. |
| **(4) Prevención de SQL Injection** | Se previene siempre que se use `PreparedStatement` con marcadores `?` y `setXxx(...)`. Se demostró en el proyecto que la versión con concatenación de cadenas (`ProductoRepositorioInseguro`) es vulnerable al ataque `' OR '1'='1`, devolviendo las 100 filas; la versión parametrizada (`buscarPorNombreSeguro`) devuelve 0 filas ante el mismo ataque. | Se previene por defecto: Hibernate genera siempre sentencias `PreparedStatement` parametrizadas internamente, incluso para consultas derivadas o `@Query`, sin que el desarrollador pueda concatenar cadenas por accidente. |

### Nota metodológica sobre los tiempos

Se observó que el tiempo medido con `StopWatch` en JDBC es más alto e inconsistente que en JPA. Esto se debe a que cada llamada a `listar()` en el módulo JDBC abre una conexión nueva contra PostgreSQL (`Conexion.abrir()`), mientras que el módulo JPA reutiliza el pool de conexiones de Hibernate una vez inicializado el `EntityManagerFactory`. La primera consulta en JPA es más lenta (arranque en frío de Hibernate + JIT warm-up), pero las siguientes son notablemente más rápidas.

Equipo de pruebas: Windows, PostgreSQL 16, ejecución local vía IntelliJ IDEA.

## Cómo ejecutar

### Módulo JDBC puro
```bash
cd taller-jdbc-puro
mvn compile exec:java -Dexec.mainClass="ec.edu.uteq.taller.Main"
```

### Módulo Spring Data JPA
```bash
cd taller-jpa-puro
mvn compile exec:java -Dexec.mainClass="ec.edu.uteq.taller.Main"
```

Ambos requieren PostgreSQL 16 corriendo localmente con la base `taller_db`, el rol `taller` (contraseña `taller`) y la tabla `productos` con 100 registros sembrados.
