// Angular import
import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { catchError, retry, delay } from 'rxjs/operators';
import { of } from 'rxjs';

// third party import
import { SharedModule } from 'src/app/theme/shared/shared.module';
import { AjouterFichierService } from 'src/app/demo/ajouter-fichier/ajouter-fichier.service';
import { NotificationService, Notification } from 'src/app/services/notification.service';

interface Fichier {
  id: number;
  nomFichier: string;
  codEn: string;
  codeValeur: string;
  createdAt: string;
  natureFichier: string;
  sens: string;
  typeFichier: string;
  updatedAt: string;
  montant: string;
  nomber: string;
  user?: {
    id: number;
    username: string;
    email: string;
    role: string;
  };
}

@Component({
  selector: 'app-nav-right',
  imports: [RouterModule, SharedModule, FormsModule, CommonModule],
  templateUrl: './nav-right.component.html',
  styleUrls: ['./nav-right.component.scss']
})
export class NavRightComponent implements OnInit, OnDestroy {
  userJson: any = null;
  
  // Propriétés pour la recherche globale
  globalSearchTerm = '';
  filteredGlobalResults: Fichier[] = [];
  showSearchResults = false;
  allFichiers: Fichier[] = [];
  subscription: Subscription = new Subscription();
  
  // Propriétés pour la popup de détails
  showFileDetailsPopup = false;
  selectedFile: Fichier | null = null;

  // Propriétés pour les notifications
  notifications: Notification[] = [];
  unreadCount = 0;
  showNotifications = false;

  // État de connexion pour l'interface utilisateur
  isBackendConnected = true;
  retryAttempts = 0;
  maxRetryAttempts = 3;

  constructor(
    private router: Router,
    private ajouterFichierService: AjouterFichierService,
    public notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const userStr = localStorage.getItem('user');
    this.userJson = userStr ? JSON.parse(userStr) : null;
    
    console.log('🔍 DEBUG - Utilisateur chargé:', this.userJson);
    console.log('🔍 DEBUG - Est ADMIN:', this.isAdminUser());
    
    // Charger les notifications depuis le backend avec gestion d'erreur
    this.chargerNotificationsBackend();
    
    // Charger aussi depuis le cache pour une meilleure persistance
    this.chargerNotificationsDepuisCache();
    
    // Charger tous les fichiers pour la recherche globale avec gestion d'erreur
    this.subscription.add(
      this.ajouterFichierService.fichiers$.subscribe({
        next: (data) => {
          this.allFichiers = data || [];
          console.log('Fichiers chargés dans la recherche globale:', this.allFichiers.length);
          console.log('Types de fichiers disponibles:', [...new Set(this.allFichiers.map(f => f.typeFichier))]);
        },
        error: (error) => {
          console.error('❌ Erreur lors de la souscription aux fichiers:', error);
          this.handleConnectionError();
        }
      })
    );
    
    // Charger les fichiers depuis le service
    this.loadAllFichiers();
    
    // Recharger les notifications toutes les 30 secondes pour rester synchronisé (seulement si connecté)
    setInterval(() => {
      if (this.userJson && this.isBackendConnected) {
        this.chargerNotificationsBackend();
      }
    }, 30000);
  }

  /**
   * Gère les erreurs de connexion au backend
   */
  private handleConnectionError(): void {
    this.isBackendConnected = false;
    console.warn('⚠️ Perte de connexion avec le backend - Mode hors ligne activé');
    
    // Charger les données depuis le cache local
    this.chargerDonneesDepuisCache();
    
    // Programmer une tentative de reconnexion
    this.scheduleReconnection();
  }

  /**
   * Programme une tentative de reconnexion
   */
  private scheduleReconnection(): void {
    if (this.retryAttempts < this.maxRetryAttempts) {
      const delayTime = Math.pow(2, this.retryAttempts) * 5000; // Backoff exponentiel
      
      setTimeout(() => {
        this.retryAttempts++;
        console.log(`🔄 Tentative de reconnexion ${this.retryAttempts}/${this.maxRetryAttempts}`);
        this.testConnection();
      }, delayTime);
    }
  }

  /**
   * Teste la connexion au backend
   */
  private testConnection(): void {
    this.ajouterFichierService.getAllFichiers()
      .pipe(
        catchError(error => {
          console.error('❌ Test de connexion échoué:', error);
          this.scheduleReconnection();
          return of([]);
        })
      )
      .subscribe({
        next: (data) => {
          if (data) {
            console.log('✅ Connexion restaurée');
            this.isBackendConnected = true;
            this.retryAttempts = 0;
            this.loadAllFichiers();
            this.chargerNotificationsBackend();
          }
        }
      });
  }

  /**
   * Charge les données depuis le cache local
   */
  private chargerDonneesDepuisCache(): void {
    // Charger les fichiers depuis le cache
    const cachedFichiers = localStorage.getItem('fichiers_cache');
    if (cachedFichiers) {
      try {
        this.allFichiers = JSON.parse(cachedFichiers);
        console.log('📦 Fichiers chargés depuis le cache:', this.allFichiers.length);
      } catch (error) {
        console.error('❌ Erreur lors du chargement du cache des fichiers:', error);
      }
    }
    
    // Charger les notifications depuis le cache
    this.chargerNotificationsDepuisCache();
  }

  /**
   * Sauvegarde les fichiers dans le cache local
   */
  private sauvegarderFichiersCache(fichiers: Fichier[]): void {
    try {
      localStorage.setItem('fichiers_cache', JSON.stringify(fichiers));
      localStorage.setItem('fichiers_timestamp', new Date().toISOString());
    } catch (error) {
      console.error('❌ Erreur lors de la sauvegarde du cache des fichiers:', error);
    }
  }

  // Méthode pour charger les notifications depuis le backend
  private chargerNotificationsBackend(): void {
    if (!this.isBackendConnected) return;
    
    console.log('🔍 DEBUG - Chargement des notifications depuis le backend...');
    
    // Charger TOUTES les notifications (lues et non lues)
    this.notificationService.loadNotifications()
      .pipe(
        retry(2),
        catchError(error => {
          console.error('❌ Erreur lors du chargement des notifications:', error);
          this.handleConnectionError();
          return of([]);
        })
      )
      .subscribe({
        next: (notifications) => {
          this.notifications = notifications;
          // Compter seulement les notifications non lues pour le badge
          this.unreadCount = notifications.filter(n => !n.lu).length;
          console.log('🔍 DEBUG - Toutes les notifications chargées depuis le backend:', notifications.length);
          console.log('🔍 DEBUG - Notifications non lues:', this.unreadCount);
          console.log('🔍 DEBUG - Détails des notifications:', notifications);
          
          // Sauvegarder les notifications dans localStorage pour la persistance
          this.sauvegarderNotificationsLocales();
          
          // Animer le badge si il y a de nouvelles notifications
          if (this.unreadCount > 0) {
            this.animerNotification();
          }
        }
      });
  }
  
  // Méthode pour charger les notifications depuis le cache
  private chargerNotificationsDepuisCache(): void {
    const cachedNotifications = localStorage.getItem('notifications_cache');
    const timestamp = localStorage.getItem('notifications_timestamp');
    
    if (cachedNotifications && timestamp) {
      const cacheAge = new Date().getTime() - new Date(timestamp).getTime();
      const maxAge = 5 * 60 * 1000; // 5 minutes
      
      if (cacheAge < maxAge) {
        try {
          this.notifications = JSON.parse(cachedNotifications);
          this.unreadCount = this.notifications.filter(n => !n.lu).length;
          console.log('🔍 DEBUG - Notifications chargées depuis le cache:', this.notifications.length);
        } catch (error) {
          console.error('❌ Erreur lors du chargement du cache:', error);
        }
      }
    }
  }

  // Méthode pour charger tous les fichiers avec gestion d'erreur améliorée
  loadAllFichiers(): void {
    if (!this.isBackendConnected) {
      this.chargerDonneesDepuisCache();
      return;
    }

    this.ajouterFichierService.getAllFichiers()
      .pipe(
        retry(2),
        catchError(error => {
          console.error('❌ Erreur lors du chargement des fichiers:', error);
          this.handleConnectionError();
          return of([]);
        })
      )
      .subscribe({
        next: (data) => {
          console.log('Données reçues du service:', data?.length || 0);
          if (data && data.length > 0) {
            console.log('Exemples de fichiers:', data.slice(0, 3));
            // Sauvegarder dans le cache
            this.sauvegarderFichiersCache(data);
          }
          this.ajouterFichierService.setFichiers(data);
          this.isBackendConnected = true;
          this.retryAttempts = 0;
        }
      });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  // Méthode pour la recherche globale DYNAMIQUE
  onGlobalSearchChange(): void {
    const term = this.globalSearchTerm?.trim() || '';
    if (term.length >= 1) { // ✅ Recherche dès 1 caractère
      const searchLower = this.normalize(term);
      
      this.filteredGlobalResults = this.allFichiers.filter(fichier => {
        // Vérifier que le fichier existe et a les propriétés nécessaires
        if (!fichier) return false;
        
        // 🔍 Recherche étendue avec tous les champs possibles
        const matchNom = fichier.nomFichier && this.normalize(fichier.nomFichier).includes(searchLower);
        const matchCodEn = fichier.codEn && this.normalize(fichier.codEn).includes(searchLower);
        const matchCodeValeur = fichier.codeValeur && this.normalize(fichier.codeValeur).includes(searchLower);
        const matchNature = fichier.natureFichier && this.normalize(fichier.natureFichier).includes(searchLower);
        const matchType = fichier.typeFichier && this.normalize(fichier.typeFichier).includes(searchLower);
        const matchSens = fichier.sens && this.normalize(fichier.sens).includes(searchLower);
        const matchMontant = fichier.montant && this.normalize(String(fichier.montant)).includes(searchLower);
        const matchNombre = fichier.nomber && this.normalize(String(fichier.nomber)).includes(searchLower);
        
        // ✅ Recherche par user (nom d'utilisateur)
        const matchUser = fichier.user && fichier.user.username && 
          this.normalize(fichier.user.username).includes(searchLower);
        
        // ✅ Recherche par email
        const matchEmail = fichier.user && fichier.user.email && 
          this.normalize(fichier.user.email).includes(searchLower);
        
        // ✅ Recherche par date
        const matchDate = fichier.createdAt && 
          this.normalize(new Date(fichier.createdAt).toLocaleDateString('fr-FR')).includes(searchLower);
        
        // ✅ Mapping intelligent pour les codes (30, 31, 32, 33, 40, 41, 10, 20)
        let matchCodeMapping = false;
        if (searchLower === '30' || searchLower === 'remis' || searchLower === 'remise') {
          matchCodeMapping = fichier.codeValeur === '30' || 
            (fichier.codeValeur && fichier.codeValeur.toUpperCase().includes('CHEQUE'));
        } else if (searchLower === '31' || searchLower === 'cours') {
          matchCodeMapping = fichier.codeValeur === '31';
        } else if (searchLower === '32' || searchLower === 'rejet' || searchLower === 'rejete') {
          matchCodeMapping = fichier.codeValeur === '32' || fichier.codeValeur === '33';
        } else if (searchLower === '33' || searchLower === 'rendu') {
          matchCodeMapping = fichier.codeValeur === '33';
        } else if (searchLower === '40' || searchLower === 'effet') {
          matchCodeMapping = fichier.codeValeur === '40' || fichier.codeValeur === '41' || 
            (fichier.codeValeur && fichier.codeValeur.toUpperCase().includes('EFFET'));
        } else if (searchLower === '10' || searchLower === 'virement') {
          matchCodeMapping = fichier.codeValeur === '10' || 
            (fichier.codeValeur && fichier.codeValeur.toUpperCase().includes('VIREMENT'));
        } else if (searchLower === '20' || searchLower === 'prelevement' || searchLower === 'prélèvement') {
          matchCodeMapping = fichier.codeValeur === '20' || 
            (fichier.codeValeur && fichier.codeValeur.toUpperCase().includes('PRELEVEMENT'));
        }
        
        return matchNom || matchCodEn || matchCodeValeur || matchNature || 
               matchType || matchSens || matchMontant || matchNombre || 
               matchUser || matchEmail || matchDate || matchCodeMapping;
      });
      
      // ✅ Tri par pertinence (nom d'abord, puis code, puis date)
      this.filteredGlobalResults.sort((a, b) => {
        const aMatchNom = a.nomFichier && this.normalize(a.nomFichier).includes(searchLower);
        const bMatchNom = b.nomFichier && this.normalize(b.nomFichier).includes(searchLower);
        
        if (aMatchNom && !bMatchNom) return -1;
        if (!aMatchNom && bMatchNom) return 1;
        
        // Si les deux matchent le nom, trier par date (plus récent d'abord)
        const dateA = new Date(a.createdAt || 0).getTime();
        const dateB = new Date(b.createdAt || 0).getTime();
        return dateB - dateA;
      });
      
      // Debug: afficher les résultats trouvés
      console.log('🔍 Recherche dynamique:', this.globalSearchTerm);
      console.log('📊 Tous les fichiers:', this.allFichiers.length);
      console.log('✅ Résultats trouvés:', this.filteredGlobalResults.length);
      if (this.filteredGlobalResults.length > 0) {
        console.log('📁 Premiers résultats:', this.filteredGlobalResults.slice(0, 5).map(f => ({
          nom: f.nomFichier,
          code: f.codeValeur,
          type: f.typeFichier,
          user: f.user?.username
        })));
      }
      
      this.showSearchResults = true;
    } else {
      this.filteredGlobalResults = [];
      this.showSearchResults = false;
    }
  }

  /**
   * Normalise une chaîne (minuscule + suppression d'accents)
   */
  private normalize(value: string): string {
    return value
      .toString()
      .toLowerCase()
      .normalize('NFD')
      .replace(/\p{Diacritic}+/gu, '');
  }

  // Méthode pour effacer la recherche
  clearGlobalSearch(): void {
    this.globalSearchTerm = '';
    this.filteredGlobalResults = [];
    this.showSearchResults = false;
  }

  // Méthode pour gérer le blur de la recherche
  onSearchBlur(): void {
    setTimeout(() => {
      this.showSearchResults = false;
    }, 200);
  }

  // Méthode pour afficher les détails du fichier
  showFileDetails(fichier: Fichier): void {
    this.selectedFile = fichier;
    this.showFileDetailsPopup = true;
    this.showSearchResults = false;
  }

  // Méthode pour fermer la popup
  closeFileDetailsPopup(): void {
    this.showFileDetailsPopup = false;
    this.selectedFile = null;
  }

  // Méthode pour naviguer vers le fichier sélectionné
  navigateToFile(fichier: Fichier): void {
    let route = '';
    
    switch (fichier.typeFichier) {
      case 'cheque':
        route = `/cheque/${fichier.codeValeur}`;
        break;
      case 'effet':
        route = `/effet/${fichier.codeValeur}`;
        break;
      case 'virement':
        route = `/virement/${fichier.codeValeur}`;
        break;
      case 'prelevement':
        route = `/prlv/${fichier.codeValeur}`;
        break;
      default:
        route = '/default';
    }
    
    // Sauvegarder l'ID du fichier à mettre en évidence
    localStorage.setItem('highlightedFileId', fichier.id.toString());
    
    // Fermer la popup
    this.closeFileDetailsPopup();
    
    // Naviguer vers la page
    this.router.navigate([route]);
  }

  // Méthode pour obtenir l'icône selon le type de fichier
  getFileTypeIcon(typeFichier: string): string {
    switch (typeFichier) {
      case 'cheque':
        return 'ti ti-receipt';
      case 'effet':
        return 'ti ti-file-text';
      case 'virement':
        return 'ti ti-exchange';
      case 'prelevement':
        return 'ti ti-arrow-down';
      default:
        return 'ti ti-file';
    }
  }

  onLogout(): void {
    // Nettoyer les caches
    localStorage.removeItem('notifications_cache');
    localStorage.removeItem('notifications_timestamp');
    localStorage.removeItem('fichiers_cache');
    localStorage.removeItem('fichiers_timestamp');
    localStorage.removeItem('highlightedFileId');
    
    // Déconnexion
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.router.navigate(['/guest/login']);
  }

  // Méthode pour marquer une notification comme lue
  marquerCommeLue(notification: Notification): void {
    if (!this.isBackendConnected) {
      console.warn('⚠️ Mode hors ligne - marquage local uniquement');
      notification.lu = true;
      this.unreadCount = this.notifications.filter(n => !n.lu).length;
      this.sauvegarderNotificationsLocales();
      return;
    }

    console.log('🔍 DEBUG - Marquer comme lue:', notification.id);
    
    // Mettre à jour immédiatement l'interface
    notification.lu = true;
    this.unreadCount = this.notifications.filter(n => !n.lu).length;
    
    // Sauvegarder l'état local immédiatement
    this.sauvegarderNotificationsLocales();
    
    this.notificationService.marquerCommeLue(notification.id)
      .pipe(
        catchError(error => {
          console.error('❌ Erreur lors du marquage de la notification:', error);
          // En cas d'erreur, remettre l'état précédent
          notification.lu = false;
          this.unreadCount = this.notifications.filter(n => !n.lu).length;
          this.sauvegarderNotificationsLocales();
          this.handleConnectionError();
          return of(null);
        })
      )
      .subscribe({
        next: (updatedNotification) => {
          if (updatedNotification) {
            console.log('🔍 DEBUG - Notification marquée comme lue:', updatedNotification);
            // Mettre à jour la notification dans la liste locale
            const index = this.notifications.findIndex(n => n.id === notification.id);
            if (index !== -1) {
              this.notifications[index] = updatedNotification;
            }
            // Recharger depuis le backend pour synchronisation
            this.chargerNotificationsBackend();
          }
        }
      });
  }
  
  // Méthode pour naviguer vers le fichier depuis une notification
  naviguerVersFichierDepuisNotification(notification: Notification): void {
    console.log('🔍 DEBUG - Méthode naviguerVersFichierDepuisNotification appelée');
    console.log('🔍 DEBUG - Notification:', notification);
    
    // Essayer de récupérer le fichier depuis la notification ou depuis le message
    let fichier = notification.fichier;
    
    if (!fichier) {
      console.log('🔍 DEBUG - Aucun fichier direct dans la notification, tentative de récupération...');
      // Essayer de récupérer le fichier depuis le message de la notification
      const message = notification.message;
      console.log('🔍 DEBUG - Message de la notification:', message);
      
      // Chercher dans tous les fichiers disponibles
      const fichierTrouve = this.allFichiers.find(f => 
        message.includes(f.nomFichier) || 
        message.includes(f.codeValeur) ||
        message.includes(f.codEn)
      );
      
      if (fichierTrouve) {
        fichier = fichierTrouve;
        console.log('🔍 DEBUG - Fichier trouvé via recherche dans le message:', fichier);
      }
    }
    
    if (fichier) {
      console.log('🔍 DEBUG - Fichier trouvé dans la notification:', fichier);
      console.log('🔍 DEBUG - Type de fichier:', fichier.typeFichier);
      console.log('🔍 DEBUG - Code valeur:', fichier.codeValeur);
      
      // Sauvegarder l'ID du fichier à mettre en évidence
      localStorage.setItem('highlightedFileId', fichier.id.toString());
      console.log('🔍 DEBUG - ID sauvegardé:', fichier.id);
      
      // Naviguer vers la page appropriée
      let route = '';
      switch (fichier.typeFichier) {
        case 'cheque':
          route = `/cheque/${fichier.codeValeur}`;
          break;
        case 'effet':
          route = `/effet/${fichier.codeValeur}`;
          break;
        case 'virement':
          route = `/virement/${fichier.codeValeur}`;
          break;
        case 'prelevement':
          route = `/prlv/${fichier.codeValeur}`;
          break;
        default:
          route = '/default';
      }
      
      console.log('🔍 DEBUG - Route calculée:', route);
      
      // Fermer les notifications
      this.showNotifications = false;
      
      // Marquer la notification comme lue
      this.marquerCommeLue(notification);
      
      // Naviguer vers la page
      console.log('🔍 DEBUG - Navigation vers:', route);
      this.router.navigate([route]).then(() => {
        console.log('🔍 DEBUG - Navigation réussie vers:', route);
      }).catch(error => {
        console.error('❌ Erreur lors de la navigation:', error);
      });
    } else {
      console.error('❌ Aucun fichier trouvé dans la notification ou dans les fichiers disponibles');
      alert('Impossible de trouver le fichier associé à cette notification.');
    }
  }
  
  // Méthode pour sauvegarder les notifications localement
  private sauvegarderNotificationsLocales(): void {
    try {
      localStorage.setItem('notifications_cache', JSON.stringify(this.notifications));
      localStorage.setItem('notifications_timestamp', new Date().toISOString());
      console.log('🔍 DEBUG - Notifications sauvegardées localement:', this.notifications.length);
    } catch (error) {
      console.error('❌ Erreur lors de la sauvegarde des notifications:', error);
    }
  }

  // Méthode pour marquer toutes les notifications comme lues
  marquerToutesCommeLues(): void {
    if (!this.isBackendConnected) {
      console.warn('⚠️ Mode hors ligne - marquage local uniquement');
      this.notifications.forEach(n => n.lu = true);
      this.unreadCount = 0;
      this.sauvegarderNotificationsLocales();
      return;
    }

    console.log('🔍 DEBUG - Marquer toutes comme lues');
    
    // Mettre à jour immédiatement l'interface
    this.notifications.forEach(n => n.lu = true);
    this.unreadCount = 0;
    
    // Sauvegarder l'état local immédiatement
    this.sauvegarderNotificationsLocales();
    
    this.notificationService.marquerToutesCommeLues()
      .pipe(
        catchError(error => {
          console.error('❌ Erreur lors du marquage de toutes les notifications:', error);
          // En cas d'erreur, remettre l'état précédent
          this.notifications.forEach(n => n.lu = false);
          this.unreadCount = this.notifications.length;
          this.sauvegarderNotificationsLocales();
          this.handleConnectionError();
          return of(null);
        })
      )
      .subscribe({
        next: (result) => {
          if (result !== null) {
            console.log('🔍 DEBUG - Toutes les notifications marquées comme lues');
            // Recharger depuis le backend pour synchronisation
            this.chargerNotificationsBackend();
          }
        }
      });
  }

  // Méthode pour basculer l'affichage des notifications
  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    console.log('🔍 DEBUG - Toggle notifications:', this.showNotifications);
    console.log('🔍 DEBUG - Nombre de notifications:', this.notifications.length);
    console.log('🔍 DEBUG - Notifications non lues:', this.unreadCount);
    console.log('🔍 DEBUG - Utilisateur connecté:', this.userJson);
    
    // Si on ouvre les notifications et qu'il n'y en a pas, recharger
    if (this.showNotifications && this.notifications.length === 0) {
      console.log('🔍 DEBUG - Rechargement forcé des notifications...');
      this.chargerNotificationsBackend();
    }
  }
  
  // Méthode pour forcer l'affichage des notifications (debug)
  forceShowNotifications(): void {
    this.showNotifications = true;
    console.log('🔍 DEBUG - Affichage forcé des notifications activé');
    console.log('🔍 DEBUG - État actuel:', {
      showNotifications: this.showNotifications,
      notificationsCount: this.notifications.length,
      unreadCount: this.unreadCount,
      userJson: this.userJson
    });
  }

  // Méthode pour obtenir le temps écoulé
  getTimeAgo(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / (1000 * 60));
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `Il y a ${minutes} min`;
    if (hours < 24) return `Il y a ${hours} h`;
    return `Il y a ${days} j`;
  }

  // Méthode pour obtenir le titre de la notification
  getNotificationTitle(type: string, fichier: any): string {
    switch (type) {
      case 'ajout':
        return `Nouveau fichier ${fichier.typeFichier}`;
      case 'envoi':
        return `Fichier ${fichier.typeFichier} envoyé`;
      case 'reception':
        return `Fichier ${fichier.typeFichier} reçu`;
      default:
        return 'Notification';
    }
  }

  // Méthode pour obtenir le message de la notification
  getNotificationMessage(type: string, fichier: any, username?: string): string {
    const userInfo = username ? ` par ${username}` : '';
    
    switch (type) {
      case 'ajout':
        return `Le fichier "${fichier.nomFichier}" a été ajouté${userInfo}.`;
      case 'envoi':
        return `Le fichier "${fichier.nomFichier}" a été envoyé${userInfo}.`;
      case 'reception':
        return `Le fichier "${fichier.nomFichier}" a été reçu${userInfo}.`;
      default:
        return 'Nouvelle notification';
    }
  }

  // Méthode pour animer la notification
  animerNotification(): void {
    // Ajouter une classe CSS pour l'animation
    const badge = document.querySelector('.notification-badge');
    if (badge) {
      badge.classList.add('notification-pulse');
      setTimeout(() => {
        badge.classList.remove('notification-pulse');
      }, 1000);
    }
  }

  // Méthode pour vérifier si l'utilisateur actuel est ADMIN
  isAdminUser(): boolean {
    if (!this.userJson) return false;
    
    // Vérifier si l'utilisateur a le rôle ADMIN
    return this.userJson.role === 'ADMIN' || 
           this.userJson.roles?.includes('ADMIN') || 
           this.userJson.username === 'admin' ||
           this.userJson.isAdmin === true;
  }

  // Méthode pour obtenir le nom d'utilisateur actuel
  getCurrentUsername(): string {
    if (!this.userJson) return 'Utilisateur inconnu';
    return this.userJson.username || this.userJson.name || 'Utilisateur inconnu';
  }

  // Méthode pour forcer la reconnexion
  forceReconnection(): void {
    this.retryAttempts = 0;
    this.testConnection();
  }

  // Méthode pour obtenir le statut de connexion
  getConnectionStatus(): string {
    return this.isBackendConnected ? 'Connecté' : 'Hors ligne';
  }
}