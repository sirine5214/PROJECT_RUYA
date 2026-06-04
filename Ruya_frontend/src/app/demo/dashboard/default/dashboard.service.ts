 // dashboard.service.ts - Interfaces alignées avec les DTOs Spring Boot
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, timer, EMPTY, of } from 'rxjs';
import { catchError, retry, tap, switchMap, startWith, delay, filter, take } from 'rxjs/operators';

// Interfaces alignées avec les DTOs Spring Boot
export interface DataRow {
  label: string;
  value: number | string;
  amount?: string;
  status?: 'success' | 'warning' | 'danger';
}

export interface CardData {
  title: string;
  icon: string;
  type: 'primary' | 'success' | 'warning' | 'default' | 'info';
  data: DataRow[];
}

export interface StatCard {
  number: string;
  label: string;
  amount?: string;
  status?: 'success' | 'warning' | 'danger';
}

export interface DashboardResponse {
  cardData: CardData[];
  globalStats: StatCard[];
  lastUpdate: string;
  connected: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  
  // Configuration de l'API - CORRIGÉE
  private readonly API_BASE_URL = 'http://localhost:8081/api/dashboard';
  private readonly MAX_CONSECUTIVE_ERRORS = 3;
  private readonly BASE_RETRY_DELAY = 5000;
  
  // Sujets pour la gestion d'état réactive
  private dashboardDataSubject = new BehaviorSubject<DashboardResponse | null>(null);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new BehaviorSubject<string | null>(null);
  private autoRefreshEnabledSubject = new BehaviorSubject<boolean>(true);
  
  private consecutiveErrors = 0;
  
  // Observables publics
  public dashboardData$ = this.dashboardDataSubject.asObservable();
  public loading$ = this.loadingSubject.asObservable();
  public error$ = this.errorSubject.asObservable();
  public autoRefreshEnabled$ = this.autoRefreshEnabledSubject.asObservable();
  
  constructor() {
    this.startAutoRefresh();
  }

  /**
   * Récupère les données du dashboard depuis l'API Spring Boot
   */
  getDashboardData(): Observable<DashboardResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);
    
    return this.http.get<DashboardResponse>(`${this.API_BASE_URL}/data`).pipe(
      retry({
        count: 2,
        delay: (error, retryCount) => {
          console.warn(`Tentative ${retryCount} après erreur:`, error);
          return timer(1000 * retryCount);
        }
      }),
      tap(data => {
        console.log('Données reçues du backend:', data);
        this.dashboardDataSubject.next(data);
        this.loadingSubject.next(false);
        this.consecutiveErrors = 0;
        this.autoRefreshEnabledSubject.next(true);
      }),
      catchError(this.handleError.bind(this))
    );
  }

  /**
   * Rafraîchit manuellement les données
   */
  refreshData(): Observable<DashboardResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);
    
    return this.http.post<DashboardResponse>(`${this.API_BASE_URL}/refresh`, {}).pipe(
      tap(data => {
        console.log('Données rafraîchies:', data);
        this.dashboardDataSubject.next(data);
        this.loadingSubject.next(false);
        this.consecutiveErrors = 0;
        this.autoRefreshEnabledSubject.next(true);
      }),
      catchError(this.handleError.bind(this))
    );
  }

  /**
   * Récupère les données pour une période spécifique
   */
  getDashboardDataForPeriod(startDate: string, endDate: string): Observable<DashboardResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);
    
    const params = { startDate, endDate };
    
    return this.http.get<DashboardResponse>(`${this.API_BASE_URL}/data/period`, { params }).pipe(
      tap(data => {
        console.log('Données période reçues:', data);
        this.dashboardDataSubject.next(data);
        this.loadingSubject.next(false);
        this.consecutiveErrors = 0;
      }),
      catchError(this.handleError.bind(this))
    );
  }

  /**
   * Démarre le rafraîchissement automatique
   */
  private startAutoRefresh(): void {
    timer(0, 30000).pipe(
      filter(() => this.autoRefreshEnabledSubject.value),
      switchMap(() => {
        if (this.consecutiveErrors >= this.MAX_CONSECUTIVE_ERRORS) {
          const pauseDuration = this.BASE_RETRY_DELAY * Math.pow(2, this.consecutiveErrors - this.MAX_CONSECUTIVE_ERRORS);
          console.warn(`Pause du rafraîchissement automatique pendant ${pauseDuration/1000}s après ${this.consecutiveErrors} erreurs consécutives`);
          
          return timer(pauseDuration).pipe(
            switchMap(() => this.healthCheck()),
            switchMap(() => {
              console.log('Serveur de nouveau disponible, reprise du rafraîchissement');
              this.consecutiveErrors = 0;
              return this.getDashboardData();
            }),
            catchError(error => {
              console.error('Serveur toujours indisponible:', error);
              this.consecutiveErrors++;
              return EMPTY;
            })
          );
        }
        
        return this.getDashboardData().pipe(
          catchError(error => {
            this.consecutiveErrors++;
            console.error(`Erreur lors du rafraîchissement automatique (${this.consecutiveErrors}/${this.MAX_CONSECUTIVE_ERRORS}):`, error);
            
            if (this.consecutiveErrors >= this.MAX_CONSECUTIVE_ERRORS) {
              this.autoRefreshEnabledSubject.next(false);
              console.warn('Rafraîchissement automatique mis en pause après trop d\'erreurs consécutives');
            }
            
            return EMPTY;
          })
        );
      })
    ).subscribe();
  }

  /**
   * Force la reprise du rafraîchissement automatique
   */
  resumeAutoRefresh(): void {
    this.consecutiveErrors = 0;
    this.autoRefreshEnabledSubject.next(true);
    this.clearError();
    console.log('Rafraîchissement automatique repris manuellement');
  }

  /**
   * Arrête temporairement le rafraîchissement automatique
   */
  pauseAutoRefresh(): void {
    this.autoRefreshEnabledSubject.next(false);
    console.log('Rafraîchissement automatique mis en pause manuellement');
  }

  /**
   * Vérifie la santé du service
   */
  healthCheck(): Observable<string> {
    return this.http.get(`${this.API_BASE_URL}/health`, { 
      observe: 'body',
      responseType: 'text'
    }).pipe(
      catchError(error => {
        console.warn('Health check échoué:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Test de connectivité simple
   */
  testConnection(): Observable<boolean> {
    return this.healthCheck().pipe(
      take(1),
      tap(() => console.log('Test de connexion réussi')),
      switchMap(() => of(true)),
      catchError(() => {
        console.warn('Test de connexion échoué');
        return of(false);
      })
    );
  }

  /**
   * Gère les erreurs HTTP avec plus de détails
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur est survenue';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      switch (error.status) {
        case 0:
          errorMessage = 'Impossible de contacter le serveur backend Spring Boot. Vérifiez que le serveur est démarré sur le port 8081.';
          break;
        case 404:
          errorMessage = 'Endpoint /api/dashboard non trouvé. Vérifiez le contrôleur Spring Boot.';
          break;
        case 500:
          errorMessage = 'Erreur interne du serveur Spring Boot. Vérifiez les logs backend.';
          break;
        case 503:
          errorMessage = 'Service Spring Boot temporairement indisponible.';
          break;
        default:
          errorMessage = `Erreur serveur: ${error.status} - ${error.message}`;
      }
    }
    
    this.errorSubject.next(errorMessage);
    this.loadingSubject.next(false);
    console.error('Erreur Dashboard Service:', errorMessage, error);
    
    return throwError(() => errorMessage);
  }

  /**
   * Méthodes utilitaires pour les composants
   */
  getCurrentData(): DashboardResponse | null {
    return this.dashboardDataSubject.value;
  }

  isLoading(): boolean {
    return this.loadingSubject.value;
  }

  getLastError(): string | null {
    return this.errorSubject.value;
  }

  getConsecutiveErrorCount(): number {
    return this.consecutiveErrors;
  }

  isAutoRefreshEnabled(): boolean {
    return this.autoRefreshEnabledSubject.value;
  }

  clearError(): void {
    this.errorSubject.next(null);
  }
}