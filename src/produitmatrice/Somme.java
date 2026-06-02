/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package produitmatrice;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 *
 * @author mazya
 */
public class Somme {

    /**
     * Calcule la somme de deux matrices en parallèle.
     * @param matA La première matrice
     * @param matB La deuxième matrice
     * @param numThreads Le nombre de threads à utiliser
     * @return La matrice résultante
     * @throws InterruptedException
     * @throws IllegalArgumentException si les matrices n'ont pas la même taille
     */
    public static int[][] sommeParallele(int[][] matA, int[][] matB, int numThreads) throws InterruptedException, IllegalArgumentException {
        if (matA.length == 0 || matB.length == 0 || matA.length != matB.length || matA[0].length != matB[0].length) {
            throw new IllegalArgumentException("Les dimensions des deux matrices doivent être identiques et non nulles.");
        }

        int rows = matA.length;
        int cols = matA[0].length;
        int[][] result = new int[rows][cols];

        Thread[] threads = new Thread[numThreads];
        int rowsPerThread = rows / numThreads;
        
        for (int i = 0; i < numThreads; i++) {
            final int startRow = i * rowsPerThread;
            final int endRow = (i == numThreads - 1) ? rows : (i + 1) * rowsPerThread;
            
            threads[i] = new Thread(() -> {
                for (int r = startRow; r < endRow; r++) {
                    for (int c = 0; c < cols; c++) {
                        result[r][c] = matA[r][c] + matB[r][c];
                    }
                }
            });
            threads[i].start();
        }
        
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }
        
        return result;
    }

    /**
     * Affiche une matrice dans la console si sa taille est raisonnable.
     */
    private static void afficherMatrice(String nom, int[][] matrice) {
        if (matrice.length <= 20 && matrice[0].length <= 20) {
            System.out.println("--- Matrice " + nom + " ---");
            for (int[] ligne : matrice) {
                for (int val : ligne) {
                    System.out.printf("%4d ", val);
                }
                System.out.println();
            }
        } else {
            System.out.println("--- Matrice " + nom + " (trop grande pour être affichée en entier, dimensions: " 
                               + matrice.length + "x" + matrice[0].length + ") ---");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("=== Programme de Somme de Matrices en Parallèle ===");
            System.out.print("Entrez le nombre de lignes : ");
            int rows = scanner.nextInt();
            
            System.out.print("Entrez le nombre de colonnes : ");
            int cols = scanner.nextInt();
            
            if (rows <= 0 || cols <= 0) {
                throw new IllegalArgumentException("Les dimensions doivent être des entiers positifs.");
            }
            
            int[][] matA = new int[rows][cols];
            int[][] matB = new int[rows][cols];
            
            // Initialisation des matrices avec des valeurs d'exemple
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matA[i][j] = i + j;
                    matB[i][j] = i - j;
                }
            }
            
            System.out.println("\nGénération des matrices...");
            afficherMatrice("A", matA);
            System.out.println();
            afficherMatrice("B", matB);
            
            int numThreads = Runtime.getRuntime().availableProcessors();
            System.out.println("\nLancement de la somme parallèle avec " + numThreads + " threads...");
            
            long startTime = System.currentTimeMillis();
            int[][] result = sommeParallele(matA, matB, numThreads);
            long endTime = System.currentTimeMillis();
            
            System.out.println("\nSomme terminée en " + (endTime - startTime) + " ms.");
            
            System.out.println("\nRésultat :");
            afficherMatrice("Résultat (A + B)", result);
            
            // Vérification simple (affichage d'un ou deux éléments clés)
            if (rows > 20 || cols > 20) {
                System.out.println("\nVérification d'échantillons :");
                System.out.println("result[0][0] = " + result[0][0] + " (attendu: 0)");
                System.out.println("result[" + (rows-1) + "][" + (cols-1) + "] = " 
                                   + result[rows-1][cols-1] + " (attendu: " + (2 * (rows-1)) + ")"); 
            }
            
        } catch (InputMismatchException e) {
            System.err.println("\nErreur : Veuillez entrer un nombre entier valide.");
        } catch (IllegalArgumentException e) {
            System.err.println("\nErreur : " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("\nErreur lors de l'exécution parallèle : " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("\nUne erreur inattendue est survenue : " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("\nFin du programme.");
        }
    }
}
