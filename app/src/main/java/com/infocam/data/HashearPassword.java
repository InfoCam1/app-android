package com.infocam.data;

import java.security.MessageDigest; // Esta es la clase necesaria para crear hashes (como SHA-256).
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets; // Para usar UTF-8 de forma estándar.

public class HashearPassword {
    public static String hashPassword(String password) { // Creamos un método estático que recibe la contraseña y devuelve su hash.
        try {
            // MessageDigest es la clase de Java que implementa algoritmos de hash.
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); // Creamos una instancia del algoritmo SHA-256.

            // digest() toma los bytes de la contraseña y devuelve el hash en bytes.
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8)); // getBytes(StandardCharsets.UTF_8) convierte el String a bytes usando UTF-8.

            // Los bytes no son legibles como texto, por lo que hay que convertirlos a hexadecimal.
            StringBuilder hexString = new StringBuilder(); // Para construir el string hexadecimal.

            for (byte b : hash) { // Recorremos cada byte del hash.
                // "0xff & b" convierte el byte (que puede ser negativo) a un número positivo entre 0-255.
                String hex = Integer.toHexString(0xff & b); // Convierte el byte a hexadecimal.

                if (hex.length() == 1) // Si es un solo dígito, agregamos un 0 delante (ej: "a" -> "0a").
                    hexString.append('0');
                hexString.append(hex); // Agregamos el valor hexadecimal al resultado.
            }

            return hexString.toString(); // Devolvemos el hash completo en formato hexadecimal.

        } catch (NoSuchAlgorithmException e) { // Cuando una aplicación solicita un algoritmo criptográfico específico (como SHA-256) que no está disponible en el entorno de ejecución actual (JVM), saltaria este error.
            throw new RuntimeException(e);
        }
    }
}