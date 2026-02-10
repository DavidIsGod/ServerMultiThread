# Servidor Web HTTP/1.0 Multi-hilos

Servidor web HTTP/1.0 implementado en Java que escucha conexiones TCP en un puerto configurable (>1024) y opera de forma continua. Soporta solicitudes GET y sirve archivos estáticos (HTML, imágenes, etc.).

## 📋 Características

- ✅ Escucha conexiones TCP en puerto configurable (mayor a 1024)
- ✅ Operación continua (servidor siempre activo)
- ✅ Multi-hilos (un hilo por cada solicitud)
- ✅ Soporta método HTTP GET
- ✅ Sirve archivos estáticos (HTML, JPG, GIF, PNG, CSS, JS)
- ✅ Manejo de errores 404 personalizado
- ✅ Muestra solicitudes y headers en consola

## 🔧 Requisitos

- **Java 21** o superior
- **Maven** (opcional, para compilar con Maven)

## 🚀 Cómo Ejecutar

### Opción 1: Usando `java -cp` (Línea de comandos)

#### Paso 1: Compilar el proyecto

```bash
mvn compile
```

O manualmente:
```bash
javac -d target/classes src/main/java/edu/co/icesi/webserver/*.java
```

#### Paso 2: Ejecutar el servidor

```bash
java -cp target/classes edu.co.icesi.webserver.WebServer [puerto] [webroot]
```

**Ejemplo:**
```bash
java -cp target/classes edu.co.icesi.webserver.WebServer 8080 wwwroot
```

**Parámetros:**
- `puerto` (opcional): Puerto TCP donde escuchar. Por defecto: `8080`
- `webroot` (opcional): Directorio raíz para servir archivos. Por defecto: `wwwroot`

---

### ⚙️ ¿Por qué necesitas `java -cp`?

El parámetro `-cp` (o `-classpath`) le dice a Java **dónde buscar las clases compiladas** (archivos `.class`) y las **dependencias** (librerías externas).

#### El problema sin `-cp`:

Si ejecutas simplemente:
```bash
java edu.co.icesi.webserver.WebServer
```

Java no sabrá dónde encontrar:
1. **Tus clases compiladas**: Los archivos `.class` están en `target/classes/`
2. **Las dependencias**: Librerías como Jakarta Servlet API

Resultado: `ClassNotFoundException` o `NoClassDefFoundError`

#### La solución con `-cp`:

```bash
java -cp target/classes edu.co.icesi.webserver.WebServer
```

Esto le dice a Java:
- Busca las clases en: `target/classes/`
- La clase principal es: `edu.co.icesi.webserver.WebServer`

#### Estructura del classpath:

```
target/classes/          ← Aquí están tus .class compilados
  └── edu/
      └── co/
          └── icesi/
              └── webserver/
                  ├── WebServer.class
                  └── ClientHandler.class
```

Cuando Java busca `edu.co.icesi.webserver.WebServer`, busca:
1. `target/classes/edu/co/icesi/webserver/WebServer.class` ✅

#### Si tuvieras dependencias externas:

```bash
java -cp "target/classes:lib/servlet-api.jar" edu.co.icesi.webserver.WebServer
```

El `:` (Linux/Mac) o `;` (Windows) separa múltiples rutas en el classpath.

---

### Opción 2: Usando Maven (Recomendado)

Maven maneja el classpath automáticamente:

```bash
mvn compile exec:java -Dexec.mainClass="edu.co.icesi.webserver.WebServer" -Dexec.args="8080 wwwroot"
```

O puedes agregar el plugin `exec-maven-plugin` al `pom.xml` y ejecutar:
```bash
mvn exec:java
```

---

### Opción 3: Desde IntelliJ IDEA

1. Abre el proyecto en IntelliJ IDEA
2. Haz clic derecho en `WebServer.java`
3. Selecciona **"Run 'WebServer.main()'"**
4. IntelliJ configura el classpath automáticamente

---

## 📁 Estructura del Proyecto

```
CeI2-Code2/
├── src/
│   └── main/
│       └── java/
│           └── edu/
│               └── co/
│                   └── icesi/
│                       └── webserver/
│                           ├── WebServer.java      # Servidor principal
│                           └── ClientHandler.java   # Manejador de solicitudes
├── wwwroot/                                        # Archivos estáticos
│   ├── index.html                                  # Página principal
│   ├── test.html                                   # Página de prueba
│   ├── 404.html                                    # Página de error 404
│   └── gatitomiaumiau.gif                          # Imágenes
├── target/
│   └── classes/                                    # Clases compiladas (.class)
├── pom.xml                                         # Configuración Maven
└── README.md                                       # Este archivo
```

## 🌐 Uso

Una vez que el servidor esté corriendo:

1. Abre tu navegador
2. Ve a: `http://localhost:8080`
3. Verás la página principal con las imágenes y enlaces de prueba

### Endpoints disponibles:

- `http://localhost:8080/` - Página principal (index.html)
- `http://localhost:8080/test.html` - Página de prueba
- `http://localhost:8080/gatitomiaumiau.gif` - Imagen GIF
- `http://localhost:8080/noexiste.html` - Prueba de error 404

## 📝 Notas Técnicas

- El servidor valida que el puerto sea mayor a 1024 (puertos privilegiados)
- Cada solicitud se maneja en un hilo independiente
- El servidor muestra en consola todas las solicitudes y headers HTTP recibidos
- Soporta tipos MIME: HTML, JPG, GIF, PNG, CSS, JS, TXT

## 🛑 Detener el Servidor

Presiona `Ctrl + C` en la terminal donde está corriendo el servidor.

## 👨‍💻 Autor

Desarrollado para el curso de Comunicaciones e Internet II - Universidad Icesi

## 📄 Licencia

Este proyecto es de uso educativo.
