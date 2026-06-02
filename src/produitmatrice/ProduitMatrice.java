package produitmatrice;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Programme de produit matriciel avec implementations sequentielle et parallele.
 * Compare les performances des deux approches.
 */
public class ProduitMatrice {

    private static final int DEFAULT_SIZE = 500;
    private static final int APERCU_MAX   = 8;
    private static final int MIN_VALUE    = -10;
    private static final int MAX_VALUE    = 10;

    public static void main(String[] args) {
        int n = DEFAULT_SIZE;
        if (args.length >= 1) {
            try {
                n = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Argument invalide, utilisation de la taille par defaut : " + DEFAULT_SIZE);
                n = DEFAULT_SIZE;
            }
        }

        int nbThreads = Runtime.getRuntime().availableProcessors();

        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   PRODUIT DE DEUX MATRICES C = A × B        ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf ("║   Taille   : %d × %d                        ║%n", n, n);
        System.out.printf ("║   Threads  : %d (parallele)                 ║%n", nbThreads);
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
        System.out.flush();

        double[][] A = genererMatrice(n);
        double[][] B = genererMatrice(n);

        System.out.println("Matrices generees avec succes.");
        System.out.println();
        System.out.flush();

        /* ── Affichage de la matrice A ── */
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│  MATRICE A                                  │");
        System.out.println("└─────────────────────────────────────────────┘");
        afficherMatriceFormatee(A, n);
        System.out.println();
        System.out.flush();

        /* ── Affichage de la matrice B ── */
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│  MATRICE B                                  │");
        System.out.println("└─────────────────────────────────────────────┘");
        afficherMatriceFormatee(B, n);
        System.out.println();
        System.out.flush();

        /* ── Multiplication sequentielle ── */
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│  RESOLUTION SEQUENTIELLE (1 thread)         │");
        System.out.println("└─────────────────────────────────────────────┘");
        long debutSeq = System.nanoTime();
        double[][] Cseq = multiplierSequentiel(A, B);
        long finSeq   = System.nanoTime();
        double tempsSeq = (finSeq - debutSeq) / 1_000_000_000.0;
        System.out.printf("Duree : %.4f secondes%n%n", tempsSeq);
        System.out.flush();

        /* ── Multiplication parallele ── */
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.printf ("│  RESOLUTION PARALLELE (%d threads)            │%n", nbThreads);
        System.out.println("└─────────────────────────────────────────────┘");
        long debutPar = System.nanoTime();
        double[][] Cpar = multiplierParallele(A, B, nbThreads);
        long finPar   = System.nanoTime();
        double tempsPar = (finPar - debutPar) / 1_000_000_000.0;
        System.out.printf("Duree : %.4f secondes%n%n", tempsPar);
        System.out.flush();

        /* ── Verification ── */
        boolean correct = comparerMatrices(Cseq, Cpar);

        /* ── Affichage du produit final ── */
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│  RESULTAT : PRODUIT C = A × B               │");
        System.out.println("└─────────────────────────────────────────────┘");
        afficherMatriceFormatee(Cseq, n);
        System.out.println();
        System.out.flush();

        /* ── Bilan comparatif ── */
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│  BILAN COMPARATIF                           │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.printf ("│  Temps sequentiel : %8.4f s               │%n", tempsSeq);
        System.out.printf ("│  Temps parallele  : %8.4f s               │%n", tempsPar);
        System.out.printf ("│  Acceleration     : %8.2f x               │%n", tempsSeq / tempsPar);
        System.out.printf ("│  Resultat correct : %-8s                 │%n", correct ? "OUI" : "NON");
        System.out.println("└─────────────────────────────────────────────┘");
        System.out.flush();
    }

    /**
     * Génère une matrice carrée n×n avec des valeurs aléatoires.
     */
    private static double[][] genererMatrice(int n) {
        Random rand = new Random(42); // graine fixe pour reproductibilité
        double[][] mat = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                mat[i][j] = MIN_VALUE + (MAX_VALUE - MIN_VALUE) * rand.nextDouble();
            }
        }
        return mat;
    }

    /**
     * Multiplication matricielle classique O(n³) — séquentiel.
     */
    public static double[][] multiplierSequentiel(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                double aik = A[i][k];
                for (int j = 0; j < n; j++) {
                    C[i][j] += aik * B[k][j];
                }
            }
        }
        return C;
    }

    /**
     * Multiplication matricielle parallele : les lignes sont reparties
     * entre nbThreads threads (ExecutorService FixedThreadPool).
     */
    public static double[][] multiplierParallele(double[][] A, double[][] B, int nbThreads) {
        int n = A.length;
        double[][] C = new double[n][n];
        ExecutorService executor = Executors.newFixedThreadPool(nbThreads);

        int tailleBloc = Math.max(1, n / nbThreads);
        for (int t = 0; t < nbThreads; t++) {
            final int debut = t * tailleBloc;
            final int fin   = (t == nbThreads - 1) ? n : (t + 1) * tailleBloc;

            executor.submit(() -> {
                for (int i = debut; i < fin; i++) {
                    for (int k = 0; k < n; k++) {
                        double aik = A[i][k];
                        for (int j = 0; j < n; j++) {
                            C[i][j] += aik * B[k][j];
                        }
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execution parallele interrompue.");
        }
        return C;
    }

    /**
     * Vérifie que deux matrices sont égales élément par élément
     * (tolérance epsilon pour les double).
     */
    private static boolean comparerMatrices(double[][] A, double[][] B) {
        int n = A.length;
        double epsilon = 1e-9;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(A[i][j] - B[i][j]) > epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Affiche la matrice avec mise en forme.
     * Pour n <= APERCU_MAX : affichage complet.
     * Pour n >  APERCU_MAX : apercu (premieres et dernieres lignes/colonnes).
     */
    private static void afficherMatriceFormatee(double[][] mat, int n) {
        if (n <= APERCU_MAX) {
            for (int i = 0; i < n; i++) {
                System.out.print("  ");
                for (int j = 0; j < n; j++) {
                    System.out.printf("%8.2f ", mat[i][j]);
                }
                System.out.println();
            }
        } else {
            int k = APERCU_MAX / 2;
            for (int i = 0; i < k; i++) {
                System.out.print("  ");
                for (int j = 0; j < k; j++) {
                    System.out.printf("%8.2f ", mat[i][j]);
                }
                System.out.print("  ...  ");
                for (int j = n - k; j < n; j++) {
                    System.out.printf("%8.2f ", mat[i][j]);
                }
                System.out.println();
            }
            System.out.println("  ...                                                      ...");
            for (int i = n - k; i < n; i++) {
                System.out.print("  ");
                for (int j = 0; j < k; j++) {
                    System.out.printf("%8.2f ", mat[i][j]);
                }
                System.out.print("  ...  ");
                for (int j = n - k; j < n; j++) {
                    System.out.printf("%8.2f ", mat[i][j]);
                }
                System.out.println();
            }
        }
    }
}
