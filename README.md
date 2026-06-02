# Opérations sur Matrices — Java (Produit & Somme)

Ce projet Java implémente deux opérations mathématiques majeures sur des matrices, avec une mise en avant de l'exécution **parallèle** (multi-threadée) pour optimiser les performances :

1. **Produit de Matrices** (`ProduitMatrice.java`) : Compare les performances entre une exécution séquentielle et une exécution parallèle.
2. **Somme de Matrices** (`Somme.java`) : Calcule la somme de deux matrices en parallèle, avec des dimensions saisies dynamiquement par l'utilisateur.

---

## Table des matières

- [Structure du projet](#structure-du-projet)
- [Prérequis](#prérequis)
- [Compilation](#compilation)
- [Exécution](#exécution)
- [Somme de Matrices (Nouveau)](#somme-de-matrices-nouveau)
- [Fonctionnement détaillé - Produit](#fonctionnement-détaillé---produit)
- [Exemples de sortie (Produit)](#exemples-de-sortie-produit)
- [Analyse des performances](#analyse-des-performances)

---

## Structure du projet

```
ProduitMatrice/
├── src/
│   └── produitmatrice/
│       ├── ProduitMatrice.java    # Code pour le produit matriciel
│       └── Somme.java             # Code pour la somme matricielle
├── build/
│   └── classes/                   # Fichiers .class compilés
├── build.xml                      # Script de build Ant (NetBeans)
├── manifest.mf                    # Manifest du JAR
├── nbproject/                     # Configuration NetBeans
└── README.md                      # Documentation globale
```

## Prérequis

- **JDK 23** (ou version compatible)
- **NetBeans** (optionnel, pour l'IDE)
- **Ant** (optionnel, pour le build automatisé)

## Compilation

### Via javac (ligne de commande)
```powershell
javac -d build/classes src/produitmatrice/ProduitMatrice.java src/produitmatrice/Somme.java
```

### Via Ant ou NetBeans
Ouvrez le projet dans NetBeans, faites un clic droit sur le projet → **Clean and Build**.

---

## Exécution

### 1. Produit de Matrices
```powershell
java -cp build/classes produitmatrice.ProduitMatrice [taille]
```

### 2. Somme de Matrices
```powershell
java -cp build/classes produitmatrice.Somme
```
*Le programme `Somme` est interactif et vous demandera de saisir les dimensions des matrices.*

---

## Somme de Matrices (Nouveau)

Le programme `Somme.java` apporte les fonctionnalités suivantes :
- **Saisie utilisateur dynamique** : Utilisation de `Scanner` pour définir les lignes et colonnes au lancement.
- **Exécution parallèle** : Le calcul de la somme est réparti sur plusieurs threads via la classe `Thread` (le nombre s'adapte aux cœurs du processeur).
- **Affichage intelligent** : Affichage complet pour des dimensions ≤ 20x20, sinon un aperçu est présenté avec validation d'un échantillon (ex: `result[0][0]`).
- **Gestion robuste des exceptions** : Validation des entrées (`InputMismatchException`, `IllegalArgumentException`) et gestion des interruptions (`InterruptedException`).

---

## Fonctionnement détaillé - Produit

Le programme `ProduitMatrice.java` suit ces étapes dans l'ordre :
1. Génération aléatoire des matrices A et B (valeurs entre -10 et 10).
2. Résolution **séquentielle** avec ordre de boucle optimisé (i-k-j) pour le cache.
3. Résolution **parallèle** via `ExecutorService` (découpage par blocs de lignes).
4. Vérification de l'égalité des résultats.
5. Affichage d'un aperçu complet (n ≤ 8) ou des coins (n > 8).

---

## Exemples de sortie (Produit)

### Avec n = 6 (affichage complet)

```
╔══════════════════════════════════════════════╗
║   PRODUIT DE DEUX MATRICES C = A × B         ║
╠══════════════════════════════════════════════╣
║   Taille   : 6 × 6                           ║
║   Threads  : 4 (parallele)                   ║
╚══════════════════════════════════════════════╝

Matrices generees avec succes.

┌─────────────────────────────────────────────┐
│  MATRICE A                                  │
└─────────────────────────────────────────────┘
      4,55     3,66    -3,83    -4,46     3,31     8,07
     -2,62    -4,49    -0,73     5,66     8,39    -1,27
...
```
*(Le reste de l'affichage détaillé a été tronqué ici pour la lisibilité)*

---

## Analyse des performances

### Complexité théorique (Produit)
| Algorithme | Complexité | Espace |
|---|---|---|
| Séquentiel | O(n³) | O(n²) |
| Parallèle (p threads) | O(n³ / p) | O(n²) |

### Complexité théorique (Somme)
La somme est en $O(n^2)$ séquentiellement et $O(n^2 / p)$ en parallèle. L'accélération sur la somme est souvent moindre que sur le produit car l'opération est davantage limitée par la bande passante mémoire (Memory Bound).

### Facteurs influençant l'accélération
- **Taille de la matrice** : plus n est grand, plus le ratio calcul/overhead est favorable.
- **Nombre de cœurs** : l'accélération est limitée par `availableProcessors()`.
- **Taille du cache CPU** : les matrices trop grandes subissent des cache misses.
- **Overhead de thread** : pour les petites matrices, le coût de création des threads domine le gain parallèle.
