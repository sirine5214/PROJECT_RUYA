# Système de Notifications - RUya

## Vue d'ensemble
Le système de notifications permet aux administrateurs de recevoir des notifications en temps réel lorsqu'un fichier est ajouté, envoyé ou reçu dans l'application.

## Fonctionnalités

### ✅ Fonctionnalités existantes
- **Visibilité ADMIN uniquement** : Les notifications ne sont visibles que pour les utilisateurs ayant le rôle ADMIN
- **Informations utilisateur** : Chaque notification affiche le nom de l'utilisateur qui a effectué l'action
- **Types de notifications** : Ajout, envoi et réception de fichiers
- **Interface utilisateur** : Badge avec compteur, dropdown avec liste des notifications
- **Animations** : Effets visuels lors de la réception de nouvelles notifications

### ✅ Nouvelles fonctionnalités ajoutées
- **Persistance des notifications** : L'état "lu/non lu" est sauvegardé dans localStorage et persiste entre les sessions
- **Nom d'utilisateur depuis la base de données** : Les notifications affichent le nom d'utilisateur récupéré directement depuis la base de données
- **Sauvegarde automatique** : Les notifications sont automatiquement sauvegardées lors de leur création ou modification

## Implémentation technique

### Fichiers modifiés

#### Frontend (Angular)
1. **`nav-right.component.ts`**
   - Ajout de méthodes `sauvegarderNotifications()` et `chargerNotifications()`
   - Mise à jour de `marquerCommeLue()` et `marquerToutesCommeLues()` pour sauvegarder l'état
   - Modification de `ajouterNotification()` et `creerNotificationFromEvent()` pour utiliser les noms d'utilisateur de la base de données
   - Mise à jour de l'interface `Fichier` pour inclure les informations utilisateur

2. **`ajouter-fichier.service.ts`**
   - Mise à jour de l'interface `Fichier` pour inclure les informations utilisateur
   - Les événements de fichiers incluent déjà les informations utilisateur

3. **Tous les composants de fichiers** (cheque30, cheque31, cheque32, cheque33, prlv20, effet40, effet41, virement10)
   - Mise à jour de l'interface `Fichier` pour inclure les informations utilisateur

#### Backend (Spring Boot)
1. **`Fichier.java`** - Déjà configuré avec la relation `@ManyToOne` vers `User`
2. **`FichierController.java`** - Retourne automatiquement les informations utilisateur grâce à `FetchType.EAGER`

### Logique ADMIN
```typescript
isAdminUser(): boolean {
  if (!this.userJson) return false;
  
  return this.userJson.role === 'ADMIN' || 
         this.userJson.roles?.includes('ADMIN') || 
         this.userJson.username === 'admin' ||
         this.userJson.isAdmin === true;
}
```

### Structure des notifications
```typescript
interface Notification {
  id: number;
  type: 'ajout' | 'envoi' | 'reception';
  titre: string;
  message: string;
  fichier?: Fichier;
  timestamp: Date;
  lu: boolean;
  icon: string;
  username?: string; // Nom de l'utilisateur depuis la base de données
}
```

### Persistance des données
- **localStorage** : Les notifications sont sauvegardées dans `localStorage` avec la clé `'notifications'`
- **Format JSON** : Les notifications sont sérialisées en JSON avec conversion des dates
- **Chargement automatique** : Les notifications sont rechargées au démarrage de l'application

### Récupération des noms d'utilisateur
- **Priorité base de données** : `fichier.user?.username` (si disponible)
- **Fallback localStorage** : `getCurrentUsername()` (utilisateur actuel)
- **Fallback par défaut** : "Utilisateur inconnu"

## Utilisation

### Pour les développeurs
1. **Ajouter une notification** :
   ```typescript
   this.ajouterNotification(fichier); // Utilise automatiquement le nom d'utilisateur de la base de données
   ```

2. **Marquer comme lue** :
   ```typescript
   this.marquerCommeLue(notification); // Sauvegarde automatiquement
   ```

3. **Marquer toutes comme lues** :
   ```typescript
   this.marquerToutesCommeLues(); // Sauvegarde automatiquement
   ```

### Pour les utilisateurs
1. **Voir les notifications** : Cliquer sur l'icône de cloche dans la barre de navigation
2. **Marquer comme lue** : Cliquer sur une notification individuelle
3. **Marquer toutes comme lues** : Cliquer sur "Marquer toutes comme lues"
4. **Persistance** : L'état des notifications est conservé entre les sessions

## Personnalisation

### Modifier les messages
```typescript
getNotificationMessage(type: string, fichier: any, username?: string): string {
  const userInfo = username ? ` par ${username}` : '';
  
  switch (type) {
    case 'ajout':
      return `Le fichier "${fichier.nomFichier}" a été ajouté${userInfo}.`;
    // ... autres cas
  }
}
```

### Modifier la logique ADMIN
```typescript
isAdminUser(): boolean {
  // Personnaliser selon vos besoins
  return this.userJson?.role === 'ADMIN';
}
```

### Modifier la persistance
```typescript
private sauvegarderNotifications() {
  // Changer pour une API backend si nécessaire
  localStorage.setItem('notifications', JSON.stringify(this.notifications));
}
```

## Notes importantes
- Les notifications ne sont visibles que pour les utilisateurs ADMIN
- L'état "lu/non lu" persiste entre les sessions grâce à localStorage
- Les noms d'utilisateur proviennent directement de la base de données
- Le système est extensible pour d'autres types de notifications 