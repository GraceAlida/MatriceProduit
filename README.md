# Produit de Matrices — Java (Sequentiel & Parallele)

Programme Java effectuant le produit de deux matrices carrees (**C = A × B**) avec deux implementations : **sequentielle** et **parallele** (multi-threadee via `ExecutorService`). Le programme affiche les matrices, execute les deux resolutions, et presente un bilan comparatif avec verification de l'exactitude.

## Table des matieres

- [Structure du projet](#structure-du-projet)
- [Prerequis](#prerequis)
- [Compilation](#compilation)
- [Execution](#execution)
- [Fonctionnement detaille](#fonctionnement-detaille)
  - [Generation des matrices](#generation-des-matrices)
  - [Multiplication sequentielle](#multiplication-sequentielle)
  - [Multiplication parallele](#multiplication-parallele)
  - [Affichage des matrices](#affichage-des-matrices)
  - [Verification des resultats](#verification-des-resultats)
- [Exemple de sortie](#exemple-de-sortie)
- [Personnalisation](#personnalisation)
- [Analyse des performances](#analyse-des-performances)

---

## Structure du projet

```
ProduitMatrice/
├── src/
│   └── produitmatrice/
│       └── ProduitMatrice.java    # Code source principal
├── build/
│   └── classes/                   # Fichiers .class compiles
├── build.xml                      # Script de build Ant (NetBeans)
├── manifest.mf                    # Manifest du JAR
├── nbproject/                     # Configuration NetBeans
└── README.md                      # Documentation
```

## Prerequis

- **JDK 23** (ou version compatible)
- **NetBeans** (optionnel, pour l'IDE)
- **Ant** (optionnel, pour le build automatise)

## Compilation

### Via javac (ligne de commande)

```powershell
javac -d build/classes src/produitmatrice/ProduitMatrice.java
```

### Via Ant (build NetBeans)

```powershell
ant compile
```

### Via NetBeans

Ouvrir le projet dans NetBeans, clic droit sur le projet → **Clean and Build**, puis **Run** (F6).

> **Important** : si l'affichage ne fonctionne pas dans NetBeans, faites un **Clean and Build** pour forcer la recompilation complete.

## Execution

```powershell
java -cp build/classes produitmatrice.ProduitMatrice [taille]
```

| Argument | Description | Valeur par defaut |
|---|---|---|
| `taille` | Dimension de la matrice carree (n×n) | `500` |

### Exemples

```powershell
# Petite taille : matrices affichees en entier
java -cp build/classes produitmatrice.ProduitMatrice 6

# Taille par defaut (500×500) : apercu des coins
java -cp build/classes produitmatrice.ProduitMatrice

# Taille personnalisee
java -cp build/classes produitmatrice.ProduitMatrice 1000
```

---

## Fonctionnement detaille

Le programme suit les etapes suivantes dans l'ordre :

1. Affichage de l'en-tete (taille, nombre de threads)
2. Generation aleatoire des matrices A et B
3. Affichage de la matrice A
4. Affichage de la matrice B
5. Resolution sequentielle A × B → Cseq et chronometrage
6. Resolution parallele A × B → Cpar et chronometrage
7. Verification de l'egalite Cseq = Cpar
8. Affichage du produit C = A × B
9. Bilan comparatif (temps, acceleration, correction)

### Generation des matrices

```java
private static double[][] genererMatrice(int n)
```

- Chaque matrice est carree de taille n×n.
- Les valeurs sont generees aleatoirement dans l'intervalle **[−10, 10]**.
- Une **graine fixe** (`new Random(42)`) garantit des resultats identiques a chaque execution.
- Complexite : O(n²).

### Multiplication sequentielle

```java
public static double[][] multiplierSequentiel(double[][] A, double[][] B)
```

- Algorithme classique de produit matriciel en **O(n³)**.
- L'ordre des boucles est optimise en **i-k-j** (au lieu de i-j-k) pour une meilleure **localite cache** : la boucle interne (j) parcourt B par lignes, ce qui exploite le prefetching materiel.
- Aucune synchronisation necessaire (un seul thread).

### Multiplication parallele

```java
public static double[][] multiplierParallele(double[][] A, double[][] B, int nbThreads)
```

- Utilise `ExecutorService` avec un **FixedThreadPool** de `nbThreads` threads (egal a `Runtime.getRuntime().availableProcessors()`).
- La matrice est decoupee en **blocs de lignes** equilibres : chaque thread traite un sous-ensemble disjoint de lignes.
- **Aucun conflit d'ecriture** : chaque case `C[i][j]` est modifiee par un seul thread.
- Arret propre du pool avec `shutdown()` puis `awaitTermination()`.

**Strategie de decoupage :**

```
Thread 0 : lignes 0      a (n/p)-1
Thread 1 : lignes n/p    a (2n/p)-1
...
Thread p-1 : lignes (p-1)n/p a n-1
```

Ou p = nombre de threads (= nombre de processeurs disponibles).

### Affichage des matrices

```java
private static void afficherMatriceFormatee(double[][] mat, int n)
```

- **Pour n ≤ 8** : affichage complet de toutes les valeurs.
- **Pour n > 8** : affichage d'un apercu montrant les coins (haut-gauche, haut-droite, bas-gauche, bas-droite) avec `...` pour le centre. Cela evite de noyer la console avec des milliers de nombres.

### Verification des resultats

```java
private static boolean comparerMatrices(double[][] A, double[][] B)
```

- Comparaison element par element de Cseq et Cpar.
- Tolerance **ε = 10⁻⁹** pour absorber les erreurs d'arrondi en virgule flottante (double).

---

## Exemple de sortie

### Avec n = 6 (affichage complet)

```
╔══════════════════════════════════════════════╗
║   PRODUIT DE DEUX MATRICES C = A × B        ║
╠══════════════════════════════════════════════╣
║   Taille   : 6 × 6                        ║
║   Threads  : 4 (parallele)                 ║
╚══════════════════════════════════════════════╝

Matrices generees avec succes.

┌─────────────────────────────────────────────┐
│  MATRICE A                                  │
└─────────────────────────────────────────────┘
      4,55     3,66    -3,83    -4,46     3,31     8,07
     -2,62    -4,49    -0,73     5,66     8,39    -1,27
      5,00    -2,27    -6,45     1,89    -5,80     6,52
     -6,56     1,75     5,03     1,42     1,60     5,05
     -9,37    -2,84     6,36    -1,65     9,48     4,27
     -0,39    -4,17     9,00     6,41     2,73    -2,62

┌─────────────────────────────────────────────┐
│  MATRICE B                                  │
└─────────────────────────────────────────────┘
      4,55     3,66    -3,83    -4,46     3,31     8,07
     -2,62    -4,49    -0,73     5,66     8,39    -1,27
      5,00    -2,27    -6,45     1,89    -5,80     6,52
     -6,56     1,75     5,03     1,42     1,60     5,05
     -9,37    -2,84     6,36    -1,65     9,48     4,27
     -0,39    -4,17     9,00     6,41     2,73    -2,62

┌─────────────────────────────────────────────┐
│  RESOLUTION SEQUENTIELLE (1 thread)         │
└─────────────────────────────────────────────┘
Duree : 0,0000 secondes

┌─────────────────────────────────────────────┐
│  RESOLUTION PARALLELE (4 threads)            │
└─────────────────────────────────────────────┘
Duree : 0,0299 secondes

┌─────────────────────────────────────────────┐
│  RESULTAT : PRODUIT C = A × B               │
└─────────────────────────────────────────────┘
    -12,96   -41,90    75,83    33,15   114,31   -22,38
   -119,00     3,50    88,30   -28,96    43,01    47,48
     35,95    35,76    55,41     6,73     0,78   -31,17
    -35,58   -66,37    54,13    80,36    -4,96   -21,56
    -83,14   -83,62    87,29    47,11     7,16    -9,58
    -12,48    11,20   -27,51   -17,04   -59,44   111,70

┌─────────────────────────────────────────────┐
│  BILAN COMPARATIF                           │
├─────────────────────────────────────────────┤
│  Temps sequentiel :   0,0000 s               │
│  Temps parallele  :   0,0299 s               │
│  Acceleration     :     0,00 x               │
│  Resultat correct : OUI                      │
└─────────────────────────────────────────────┘
```

### Avec n = 500 (apercu des coins)

```
╔══════════════════════════════════════════════╗
║   PRODUIT DE DEUX MATRICES C = A × B        ║
╠══════════════════════════════════════════════╣
║   Taille   : 500 × 500                      ║
║   Threads  : 4 (parallele)                  ║
╚══════════════════════════════════════════════╝

Matrices generees avec succes.

┌─────────────────────────────────────────────┐
│  MATRICE A                                  │
└─────────────────────────────────────────────┘
      4,55     3,66    -3,83    -4,46   ...     -2,70     1,42     9,37     6,34
     -2,62    -4,49    -0,73     5,66   ...      5,16    -7,94     0,25     0,47
      5,00    -2,27    -6,45     1,89   ...      1,09     7,91    -6,49    -1,52
     -6,56     1,75     5,03     1,42   ...      5,20     7,99    -4,02    -0,57
  ...                                                      ...
     -4,22    -7,88     2,55    -6,95   ...     -5,23     9,77     5,58     7,26
      6,73    -7,62     0,95     4,15   ...      4,69     2,36    -1,72    -6,61
      9,89     8,26     1,92    -2,36   ...     -6,61     0,57     2,95     6,57
     -0,95    -0,91    -3,66     7,25   ...      8,35     3,83     5,81     5,72

┌─────────────────────────────────────────────┐
│  MATRICE B                                  │
└─────────────────────────────────────────────┘
      4,55     3,66    -3,83    -4,46   ...     -2,70     1,42     9,37     6,34
     -2,62    -4,49    -0,73     5,66   ...      5,16    -7,94     0,25     0,47
      5,00    -2,27    -6,45     1,89   ...      1,09     7,91    -6,49    -1,52
     -6,56     1,75     5,03     1,42   ...      5,20     7,99    -4,02    -0,57
  ...                                                      ...
     -4,22    -7,88     2,55    -6,95   ...     -5,23     9,77     5,58     7,26
      6,73    -7,62     0,95     4,15   ...      4,69     2,36    -1,72    -6,61
      9,89     8,26     1,92    -2,36   ...     -6,61     0,57     2,95     6,57
     -0,95    -0,91    -3,66     7,25   ...      8,35     3,83     5,81     5,72

┌─────────────────────────────────────────────┐
│  RESOLUTION SEQUENTIELLE (1 thread)         │
└─────────────────────────────────────────────┘
Duree : 0,2743 secondes

┌─────────────────────────────────────────────┐
│  RESOLUTION PARALLELE (4 threads)            │
└─────────────────────────────────────────────┘
Duree : 0,1825 secondes

┌─────────────────────────────────────────────┐
│  RESULTAT : PRODUIT C = A × B               │
└─────────────────────────────────────────────┘
     -5,01   -33,82     1,07    44,94   ...     97,69    49,40   -33,90    61,41
     42,17    13,90   -55,85   -14,40   ...     -6,73    39,36   -30,46    78,44
    -10,94    -0,09    12,24   -24,19   ...     68,95    31,28    28,71    42,03
      4,80    38,40   -68,04    32,84   ...      4,52    43,23    -5,77    -2,60
  ...                                                      ...
    -11,79    63,80   -54,60    14,55   ...    -55,78    -3,95   -28,58    44,93
     25,62   -18,82   -22,51   -18,95   ...    -48,45   -44,20   -45,03    -1,81
      4,84    -4,76    15,21   -28,06   ...     79,30    -3,41   -59,51    58,89
     21,94   -10,37   -48,18     4,48   ...    -57,81   -11,93   -16,04   -38,07

┌─────────────────────────────────────────────┐
│  BILAN COMPARATIF                           │
├─────────────────────────────────────────────┤
│  Temps sequentiel :   0,2743 s               │
│  Temps parallele  :   0,1825 s               │
│  Acceleration     :     1,50 x               │
│  Resultat correct : OUI                      │
└─────────────────────────────────────────────┘
```

## Personnalisation

Les constantes suivantes peuvent etre modifiees directement dans `ProduitMatrice.java` :

| Constante | Ligne | Description |
|---|---|---|
| `DEFAULT_SIZE` | 14 | Taille par defaut de la matrice (500) |
| `APERCU_MAX` | 15 | Seuil d'affichage complet (8) : au-dela, seul un apercu est affiche |
| `MIN_VALUE` | 16 | Borne inferieure des valeurs aleatoires (−10) |
| `MAX_VALUE` | 17 | Borne superieure des valeurs aleatoires (10) |

## Analyse des performances

### Complexite theorique

| Algorithme | Complexite | Espace |
|---|---|---|
| Sequentiel | O(n³) | O(n²) |
| Parallele (p threads) | O(n³ / p) | O(n²) |

### Facteurs influencant l'acceleration

- **Taille de la matrice** : plus n est grand, plus le ratio calcul/overhead est favorable au parallelisme.
- **Nombre de coeurs** : l'acceleration est limitee par `availableProcessors()`.
- **Taille du cache CPU** : les matrices trop grandes (> cache L3) subissent des cache misses.
- **Overhead de thread** : pour n < ~200, le cout de creation des threads domine le gain parallele.

### Acceleration typique (indicatif)

| Taille (n) | Acceleration attendue |
|---|---|
| n < 200 | ~0.5x – 1.0x (overhead dominant) |
| n = 500 | ~1.2x – 1.5x |
| n = 1000 | ~2.0x – 3.0x |
| n = 2000 | ~3.0x – 4.0x |

> Les valeurs exactes dependent du materiel (nombre de coeurs, frequence, cache).
