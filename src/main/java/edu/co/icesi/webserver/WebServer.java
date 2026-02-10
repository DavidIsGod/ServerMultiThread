package edu.co.icesi.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor web multi-hilos HTTP/1.0
 * Escucha en un puerto TCP configurable (>1024) y crea un hilo por cada solicitud.
 */
public class WebServer {
    
    private final int port;
    private final String webRoot;
    private volatile boolean running;
    
    /**
     * Constructor del servidor web.
     * @param port Puerto TCP donde escuchar (debe ser > 1024)
     * @param webRoot Directorio raíz para servir archivos
     */
    public WebServer(int port, String webRoot) {
        if (port <= 1024) {
            throw new IllegalArgumentException("El puerto debe ser mayor a 1024");
        }
        this.port = port;
        this.webRoot = webRoot;
        this.running = false;
    }
    
    /**
     * Inicia el servidor web y escucha conexiones de forma continua.
     */
    public void start() {
        running = true;
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("========================================");
            System.out.println("Servidor Web HTTP/1.0 iniciado");
            System.out.println("Puerto: " + port);
            System.out.println("Directorio raíz: " + webRoot);
            System.out.println("========================================");
            System.out.println("Esperando conexiones...\n");
            
            while (running) {
                try {
                    // Acepta conexión entrante
                    Socket clientSocket = serverSocket.accept();
                    
                    System.out.println("[CONEXION] Nueva conexión desde: " 
                        + clientSocket.getInetAddress().getHostAddress() 
                        + ":" + clientSocket.getPort());
                    
                    // Crea un nuevo hilo para manejar la solicitud
                    ClientHandler handler = new ClientHandler(clientSocket, webRoot);
                    Thread thread = new Thread(handler);
                    thread.start();
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[ERROR] Error aceptando conexión: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("[ERROR FATAL] No se pudo iniciar el servidor en el puerto " 
                + port + ": " + e.getMessage());
        }
    }
    
    /**
     * Detiene el servidor web.
     */
    public void stop() {
        running = false;
        System.out.println("\n[INFO] Servidor detenido.");
    }
    
    /**
     * Punto de entrada principal.
     * Uso: java WebServer [puerto] [webroot]
     * Puerto por defecto: 8080
     * WebRoot por defecto: ./wwwroot
     */
    public static void main(String[] args) {
        int port = 8080; // Puerto por defecto
        String webRoot = "wwwroot"; // Directorio raíz por defecto
        
        // Parsear argumentos de línea de comandos
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido. Usando puerto por defecto: " + port);
            }
        }
        
        if (args.length >= 2) {
            webRoot = args[1];
        }
        
        // Crear e iniciar el servidor
        WebServer server = new WebServer(port, webRoot);
        
        // Agregar hook para cierre graceful
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Cerrando servidor...");
            server.stop();
        }));
        
        server.start();
    }
}
