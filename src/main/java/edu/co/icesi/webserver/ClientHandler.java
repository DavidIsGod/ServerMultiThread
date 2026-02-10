package edu.co.icesi.webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Manejador de solicitudes HTTP en un hilo independiente.
 * Procesa solicitudes HTTP/1.0 GET y responde con la estructura HTTP correcta.
 */
public class ClientHandler implements Runnable {
    
    private static final String CRLF = "\r\n";
    private static final String HTTP_VERSION = "HTTP/1.0";
    private static final String SERVER_NAME = "MiServidorWeb/1.0";
    
    private final Socket clientSocket;
    private final String webRoot;
    
    /**
     * Constructor del manejador de cliente.
     * @param clientSocket Socket de la conexión del cliente
     * @param webRoot Directorio raíz del servidor web
     */
    public ClientHandler(Socket clientSocket, String webRoot) {
        this.clientSocket = clientSocket;
        this.webRoot = webRoot;
    }
    
    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, false)
        ) {
            // Leer y mostrar la línea de solicitud
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }
            
            System.out.println("\n========== SOLICITUD RECIBIDA ==========");
            System.out.println("[LINEA DE SOLICITUD] " + requestLine);
            
            // Leer y mostrar todos los encabezados HTTP
            System.out.println("[ENCABEZADOS]");
            String headerLine;
            while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
                System.out.println("  " + headerLine);
            }
            System.out.println("=========================================\n");
            
            // Parsear la línea de solicitud: METODO RECURSO PROTOCOLO
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3) {
                sendErrorResponse(writer, outputStream, 400, "Bad Request");
                return;
            }
            
            String method = requestParts[0];
            String resource = requestParts[1];
            String protocol = requestParts[2];
            
            System.out.println("[INFO] Método: " + method);
            System.out.println("[INFO] Recurso: " + resource);
            System.out.println("[INFO] Protocolo: " + protocol);
            
            // Verificar que sea HTTP/1.0 o compatible
            if (!protocol.startsWith("HTTP/")) {
                sendErrorResponse(writer, outputStream, 400, "Bad Request");
                return;
            }
            
            // Solo soportamos el método GET
            if (!method.equalsIgnoreCase("GET")) {
                sendErrorResponse(writer, outputStream, 501, "Not Implemented");
                return;
            }
            
            // Procesar la solicitud GET
            processGetRequest(resource, writer, outputStream);
            
        } catch (IOException e) {
            System.err.println("[ERROR] Error procesando solicitud: " + e.getMessage());
        } finally {
            // Cerrar el socket del cliente
            closeSocket();
        }
    }
    
    /**
     * Procesa una solicitud GET y envía el archivo solicitado.
     */
    private void processGetRequest(String resource, PrintWriter writer, OutputStream outputStream) 
            throws IOException {
        
        // Si el recurso es "/", servir index.html
        if (resource.equals("/")) {
            resource = "/index.html";
        }
        
        // Prevenir ataques de path traversal
        if (resource.contains("..")) {
            sendErrorResponse(writer, outputStream, 403, "Forbidden");
            return;
        }
        
        // Construir la ruta del archivo
        Path filePath = Paths.get(webRoot, resource).normalize();
        File file = filePath.toFile();
        
        System.out.println("[INFO] Buscando archivo: " + filePath.toAbsolutePath());
        
        // Verificar si el archivo existe y es legible
        if (!file.exists() || !file.isFile()) {
            System.out.println("[WARN] Archivo no encontrado: " + resource);
            send404Response(writer, outputStream);
            return;
        }
        
        if (!file.canRead()) {
            sendErrorResponse(writer, outputStream, 403, "Forbidden");
            return;
        }
        
        // Determinar el tipo MIME
        String mimeType = getMimeType(resource);
        long fileSize = file.length();
        
        System.out.println("[INFO] Tipo MIME: " + mimeType);
        System.out.println("[INFO] Tamaño: " + fileSize + " bytes");
        
        // Enviar la respuesta HTTP exitosa
        sendResponse(writer, outputStream, 200, "OK", mimeType, file);
    }
    
    /**
     * Determina el tipo MIME basado en la extensión del archivo.
     */
    private String getMimeType(String resource) {
        String lowerResource = resource.toLowerCase();
        
        if (lowerResource.endsWith(".html") || lowerResource.endsWith(".htm")) {
            return "text/html";
        } else if (lowerResource.endsWith(".jpg") || lowerResource.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerResource.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerResource.endsWith(".png")) {
            return "image/png";
        } else if (lowerResource.endsWith(".css")) {
            return "text/css";
        } else if (lowerResource.endsWith(".js")) {
            return "application/javascript";
        } else if (lowerResource.endsWith(".txt")) {
            return "text/plain";
        } else {
            return "application/octet-stream";
        }
    }
    
    /**
     * Envía una respuesta HTTP con un archivo.
     */
    private void sendResponse(PrintWriter writer, OutputStream outputStream, 
            int statusCode, String statusMessage, String mimeType, File file) throws IOException {
        
        // Línea de estado
        writer.print(HTTP_VERSION + " " + statusCode + " " + statusMessage + CRLF);
        
        // Encabezados HTTP
        writer.print("Server: " + SERVER_NAME + CRLF);
        writer.print("Date: " + getHttpDate() + CRLF);
        writer.print("Content-Type: " + mimeType + CRLF);
        writer.print("Content-Length: " + file.length() + CRLF);
        writer.print("Connection: close" + CRLF);
        
        // Línea vacía que separa headers del cuerpo
        writer.print(CRLF);
        writer.flush();
        
        // Enviar el cuerpo (contenido del archivo)
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
        
        System.out.println("[RESPUESTA] " + statusCode + " " + statusMessage + " - " + file.getName());
    }
    
    /**
     * Envía una respuesta de error 404 con archivo de error.
     */
    private void send404Response(PrintWriter writer, OutputStream outputStream) throws IOException {
        // Buscar archivo de error 404 personalizado
        Path errorFilePath = Paths.get(webRoot, "404.html");
        File errorFile = errorFilePath.toFile();
        
        if (errorFile.exists() && errorFile.isFile()) {
            sendResponse(writer, outputStream, 404, "Not Found", "text/html", errorFile);
        } else {
            // Enviar respuesta 404 por defecto
            String body = "<!DOCTYPE html>" + CRLF +
                    "<html><head><title>404 Not Found</title></head>" + CRLF +
                    "<body><h1>404 - Archivo No Encontrado</h1>" + CRLF +
                    "<p>El recurso solicitado no existe en este servidor.</p>" + CRLF +
                    "<hr><p><em>" + SERVER_NAME + "</em></p></body></html>";
            
            sendErrorResponse(writer, outputStream, 404, "Not Found", body);
        }
    }
    
    /**
     * Envía una respuesta de error HTTP genérica.
     */
    private void sendErrorResponse(PrintWriter writer, OutputStream outputStream, 
            int statusCode, String statusMessage) throws IOException {
        String body = "<!DOCTYPE html>" + CRLF +
                "<html><head><title>" + statusCode + " " + statusMessage + "</title></head>" + CRLF +
                "<body><h1>" + statusCode + " - " + statusMessage + "</h1>" + CRLF +
                "<hr><p><em>" + SERVER_NAME + "</em></p></body></html>";
        
        sendErrorResponse(writer, outputStream, statusCode, statusMessage, body);
    }
    
    /**
     * Envía una respuesta de error HTTP con cuerpo personalizado.
     */
    private void sendErrorResponse(PrintWriter writer, OutputStream outputStream, 
            int statusCode, String statusMessage, String body) throws IOException {
        
        byte[] bodyBytes = body.getBytes("UTF-8");
        
        // Línea de estado
        writer.print(HTTP_VERSION + " " + statusCode + " " + statusMessage + CRLF);
        
        // Encabezados HTTP
        writer.print("Server: " + SERVER_NAME + CRLF);
        writer.print("Date: " + getHttpDate() + CRLF);
        writer.print("Content-Type: text/html; charset=UTF-8" + CRLF);
        writer.print("Content-Length: " + bodyBytes.length + CRLF);
        writer.print("Connection: close" + CRLF);
        
        // Línea vacía que separa headers del cuerpo
        writer.print(CRLF);
        writer.flush();
        
        // Enviar cuerpo
        outputStream.write(bodyBytes);
        outputStream.flush();
        
        System.out.println("[RESPUESTA] " + statusCode + " " + statusMessage);
    }
    
    /**
     * Obtiene la fecha actual en formato HTTP.
     */
    private String getHttpDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(new Date());
    }
    
    /**
     * Cierra el socket del cliente de forma segura.
     */
    private void closeSocket() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("[INFO] Socket cerrado correctamente.");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Error cerrando socket: " + e.getMessage());
        }
    }
}
