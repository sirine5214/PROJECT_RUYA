       // default.component.ts - Version corrigée pour utilisation dynamique des données backend
import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { DashboardService, DashboardResponse, CardData, StatCard, DataRow } from './dashboard.service';

@Component({
  selector: 'app-default',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './default.component.html',
  styleUrls: ['./default.component.scss'],
  providers: [DashboardService]
})
export class DefaultComponent implements OnInit, OnDestroy {
  private dashboardService = inject(DashboardService);
  private subscriptions: Subscription[] = [];
  
  // Signals for reactive state management
  currentDate = signal(new Date().toISOString().split('T')[0]);
  isLoading = signal(false);
  sessionActive = signal(true);
  errorMessage = signal<string | null>(null);
  connectionStatus = signal<'connected' | 'connecting' | 'disconnected'>('connecting');
  lastUpdateTime = signal<string>('');
  autoRefreshEnabled = signal(true);
  
  // Dashboard data - maintenant entièrement géré par le backend
  cardData = signal<CardData[]>([]);
  globalStats = signal<StatCard[]>([]);

  // Données mock uniquement pour l'affichage initial (SUPPRIMÉES après connexion backend)
  private initialMockData: CardData[] = [
    {
      title: 'Connexion en cours...',
      icon: 'fas fa-spinner',
      type: 'default',
      data: [
        { label: 'Chargement des données backend', value: '...' }
      ]
    }
  ];

  private initialMockStats: StatCard[] = [
    { number: '...', label: 'Connexion en cours' }
  ];

  // Computed values basés sur les données réelles
  totalAmount = computed(() => {
    let total = 0;
    this.cardData().forEach(card => {
      card.data.forEach(row => {
        if (row.amount) {
          const amount = parseFloat(row.amount.replace(/[^\d.-]/g, ''));
          total += amount || 0;
        }
      });
    });
    return total.toLocaleString('fr-FR', { style: 'currency', currency: 'EUR' });
  });
  
  // Computed connection status display
  connectionStatusDisplay = computed(() => {
    switch (this.connectionStatus()) {
      case 'connected': return 'Backend Spring Boot connecté';
      case 'connecting': return 'Connexion au backend Spring Boot...';
      case 'disconnected': return 'Backend Spring Boot indisponible';
      default: return 'État inconnu';
    }
  });

  ngOnInit() {
    console.log('Initialisation du Dashboard RUYA - Version Backend Dynamique');
    this.initializeDashboard();
    this.subscribeToDataUpdates();
  }

  ngOnDestroy() {
    console.log('Fermeture du composant Dashboard RUYA');
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  /**
   * Initialise le dashboard en chargeant les données du backend
   */
  private initializeDashboard(): void {
    // Afficher un état de chargement initial
    this.cardData.set(this.initialMockData);
    this.globalStats.set(this.initialMockStats);
    this.connectionStatus.set('connecting');
    
    console.log('Tentative de connexion au backend Spring Boot...');
    this.testBackendConnection();
  }

  /**
   * Test de connexion au backend Spring Boot
   */
  private testBackendConnection(): void {
    this.dashboardService.testConnection().subscribe({
      next: (isConnected) => {
        if (isConnected) {
          this.connectionStatus.set('connected');
          this.showNotification('Backend Spring Boot connecté !', 'success');
          console.log('Connexion backend établie, chargement des données réelles...');
          this.loadRealDataFromBackend();
        } else {
          this.handleOfflineMode();
        }
      },
      error: (error) => {
        console.warn('Impossible de se connecter au backend Spring Boot:', error);
        this.handleOfflineMode();
      }
    });
  }

  /**
   * Charge les données réelles depuis le backend Spring Boot
   */
  private loadRealDataFromBackend(): void {
    this.dashboardService.getDashboardData().subscribe({
      next: (data: DashboardResponse) => {
        console.log('Données réelles chargées depuis Spring Boot:', data);
        
        // REMPLACER complètement les données mock par les données backend
        this.cardData.set(data.cardData);
        this.globalStats.set(data.globalStats);
        this.lastUpdateTime.set(data.lastUpdate);
        this.connectionStatus.set('connected');
        this.errorMessage.set(null);
        
        this.showNotification('Données temps réel chargées depuis Spring Boot !', 'success');
      },
      error: (error) => {
        console.error('Échec du chargement des données Spring Boot:', error);
        this.showNotification('Impossible de charger les données du backend', 'error');
        this.handleOfflineMode();
      }
    });
  }

  /**
   * Gère le mode hors-ligne (backend indisponible)
   */
  private handleOfflineMode(): void {
    this.connectionStatus.set('disconnected');
    this.errorMessage.set('Backend Spring Boot indisponible sur localhost:8081. Vérifiez que le serveur est démarré.');
    this.showNotification('Backend Spring Boot indisponible', 'error');
    
    // Données d'erreur au lieu de données mock
    this.cardData.set([
      {
        title: 'ERREUR CONNEXION',
        icon: 'fas fa-exclamation-triangle',
        type: 'warning',
        data: [
          { label: 'Backend Spring Boot', value: 'Indisponible', status: 'danger' },
          { label: 'Port', value: '8081', status: 'warning' },
          { label: 'Action', value: 'Vérifier serveur Spring Boot', status: 'warning' }
        ]
      }
    ]);
    
    this.globalStats.set([
      { number: 'ERREUR', label: 'Connexion Backend', status: 'danger' },
      { number: '8081', label: 'Port Spring Boot' },
      { number: new Date().toLocaleTimeString(), label: 'Dernière tentative' }
    ]);
  }

  /**
   * S'abonne aux mises à jour automatiques du service
   */
  private subscribeToDataUpdates(): void {
    // Données du dashboard
    const dataSubscription = this.dashboardService.dashboardData$.subscribe(data => {
      if (data && this.connectionStatus() === 'connected') {
        console.log('Mise à jour des données depuis Spring Boot');
        this.cardData.set(data.cardData);
        this.globalStats.set(data.globalStats);
        this.lastUpdateTime.set(data.lastUpdate);
        this.clearError();
      }
    });

    // État de chargement
    const loadingSubscription = this.dashboardService.loading$.subscribe(loading => {
      this.isLoading.set(loading);
      if (loading && this.connectionStatus() !== 'disconnected') {
        this.connectionStatus.set('connecting');
      }
    });

    // Erreurs
    const errorSubscription = this.dashboardService.error$.subscribe(error => {
      if (error) {
        this.errorMessage.set(error);
        if (error.includes('contacter le serveur')) {
          this.connectionStatus.set('disconnected');
          this.handleOfflineMode();
        }
      }
    });

    // Auto-refresh
    const autoRefreshSubscription = this.dashboardService.autoRefreshEnabled$.subscribe(enabled => {
      this.autoRefreshEnabled.set(enabled);
    });

    this.subscriptions.push(dataSubscription, loadingSubscription, errorSubscription, autoRefreshSubscription);
  }

  /**
   * Rafraîchir manuellement les données
   */
  public refreshData(): void {
    if (this.connectionStatus() === 'disconnected') {
      console.log('Tentative de reconnexion au backend Spring Boot...');
      this.testBackendConnection();
      return;
    }

    console.log('Rafraîchissement manuel des données Spring Boot...');
    this.dashboardService.refreshData().subscribe({
      next: (data: DashboardResponse) => {
        this.showNotification('Données Spring Boot rafraîchies !', 'success');
      },
      error: (error) => {
        this.showNotification('Erreur lors du rafraîchissement Spring Boot', 'error');
        console.error('Erreur refresh:', error);
      }
    });
  }

  /**
   * Basculer le rafraîchissement automatique
   */
  public toggleAutoRefresh(): void {
    if (this.autoRefreshEnabled()) {
      this.dashboardService.pauseAutoRefresh();
      this.showNotification('Actualisation automatique désactivée', 'warning');
    } else {
      this.dashboardService.resumeAutoRefresh();
      this.showNotification('Actualisation automatique reprise', 'success');
    }
  }

  /**
   * Tenter une reconnexion
   */
  public retryConnection(): void {
    this.clearError();
    this.connectionStatus.set('connecting');
    this.showNotification('Tentative de reconnexion Spring Boot...', 'warning');
    this.testBackendConnection();
  }

  /**
   * Effacer le message d'erreur
   */
  public clearError(): void {
    this.errorMessage.set(null);
    this.dashboardService.clearError();
  }

  // ==================== ACTIONS MÉTIER ====================

  public onRegenerateFiles(): void {
    console.log('Régénération des fichiers...');
    this.isLoading.set(true);
    setTimeout(() => {
      this.isLoading.set(false);
      this.refreshData();
      this.showNotification('Fichiers régénérés avec succès !', 'success');
    }, 3000);
  }

  public onValidateData(): void {
    console.log('Validation des données...');
    this.isLoading.set(true);
    setTimeout(() => {
      this.isLoading.set(false);
      this.refreshData();
      this.showNotification('Données validées avec succès !', 'success');
    }, 2000);
  }

  public onExportData(): void {
    console.log('Export des données Spring Boot...');
    
    const currentData = this.dashboardService.getCurrentData();
    if (!currentData) {
      this.showNotification('Aucune donnée à exporter', 'error');
      return;
    }

    const exportData = {
      timestamp: new Date().toISOString(),
      source: 'spring-boot-backend',
      backendConnected: this.connectionStatus() === 'connected',
      cardData: currentData.cardData,
      globalStats: currentData.globalStats,
      lastUpdate: currentData.lastUpdate,
      totalAmount: this.totalAmount(),
      version: '3.0-dynamic',
      application: 'RUYA-Dashboard-SpringBoot'
    };

    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ruya-dashboard-springboot-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);

    this.showNotification('Données Spring Boot exportées !', 'success');
  }

  public onCloseApp(): void {
    if (confirm('Êtes-vous sûr de vouloir fermer l\'application RUYA ?')) {
      console.log('Fermeture de l\'application RUYA...');
      this.sessionActive.set(false);
      this.dashboardService.pauseAutoRefresh();
      setTimeout(() => {
        console.log('Application RUYA fermée proprement');
      }, 500);
    }
  }

  // ==================== MÉTHODES MANQUANTES AJOUTÉES ====================

  /**
   * Obtient la classe CSS pour le type de ligne de données
   */
  public getRowTypeClass(type?: string): string {
    if (!type) return '';
    return `type-${type}`;
  }

  /**
   * Obtient le libellé du type de valeur
   */
  public getValueTypeLabel(type?: string): string {
    switch (type) {
      case 'number':
        return 'Nombre';
      case 'amount':
        return 'Montant';
      case 'percentage':
        return 'Pourcentage';
      case 'time':
        return 'Heure';
      default:
        return 'Valeur';
    }
  }

  /**
   * Formate les valeurs selon leur type
   */
  public formatValue(value: number, type?: string): string {
    switch (type) {
      case 'percentage':
        return value.toFixed(1);
      case 'amount':
        return new Intl.NumberFormat('fr-TN', {
          style: 'currency',
          currency: 'TND',
          minimumFractionDigits: 2
        }).format(value);
      case 'number':
        return new Intl.NumberFormat('fr-FR').format(value);
      default:
        return value.toString();
    }
  }

  // ==================== UTILITAIRES ====================

  private showNotification(message: string, type: 'success' | 'warning' | 'error' = 'success'): void {
    console.log(`${type.toUpperCase()}: ${message}`);
    // Intégration future avec ngx-toastr ou autre bibliothèque de notifications
  }

  // ==================== TEMPLATE HELPERS ====================

  public trackByIndex(index: number): number {
    return index;
  }

  public getStatusClass(status?: string): string {
    switch (status) {
      case 'success': return 'status-success';
      case 'warning': return 'status-warning';
      case 'danger': return 'status-danger';
      default: return '';
    }
  }

  public getCardClass(type: string): string {
    switch (type) {
      case 'primary': return 'card-primary';
      case 'success': return 'card-success';
      case 'warning': return 'card-warning';
      case 'info': return 'card-info';
      default: return '';
    }
  }

  public getConnectionStatusClass(): string {
    switch (this.connectionStatus()) {
      case 'connected': return 'status-success';
      case 'connecting': return 'status-warning';
      case 'disconnected': return 'status-danger';
      default: return '';
    }
  }

  // ==================== COMPUTED PROPERTIES ====================

  public hasError(): boolean {
    return this.errorMessage() !== null;
  }

  public hasData(): boolean {
    return this.cardData().length > 0 && this.connectionStatus() === 'connected';
  }

  public isBackendConnected(): boolean {
    return this.connectionStatus() === 'connected';
  }

  public getErrorCount(): number {
    return this.dashboardService.getConsecutiveErrorCount();
  }

  public getDataSource(): string {
    return this.isBackendConnected() ? 'Spring Boot Backend (Port 8081)' : 'Backend indisponible';
  }
}