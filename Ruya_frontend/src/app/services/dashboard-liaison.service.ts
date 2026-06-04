import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface DashboardEvent {
  type: 'nouveau-fichier' | 'ctr-bo-update' | 'synchronisation' | 'alerte';
  data: any;
  timestamp: Date;
}

export interface FichierEvent {
  id: string;
  nomFichier: string;
  typeFichier: string;
  montant: number;
  sens: string;
  codeValeur: string;
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardLiaisonService {
  private dashboardEventsSubject = new BehaviorSubject<DashboardEvent | null>(null);
  private fichierEventsSubject = new BehaviorSubject<FichierEvent | null>(null);
  private ctrBoStatsSubject = new BehaviorSubject<any>(null);

  // Observables publics
  public dashboardEvents$ = this.dashboardEventsSubject.asObservable();
  public fichierEvents$ = this.fichierEventsSubject.asObservable();
  public ctrBoStats$ = this.ctrBoStatsSubject.asObservable();

  constructor() {
    console.log('🚀 DashboardLiaisonService initialisé');
    this.initialiserEcouteurs();
  }

  // Initialiser les écouteurs d'événements
  private initialiserEcouteurs(): void {
    // Écouter les événements du localStorage
    window.addEventListener('storage', (event) => {
      if (event.key === 'fichiersAjoutes') {
        this.traiterNouveauFichier(event.newValue);
      }
    });

    // Écouter les événements personnalisés
    window.addEventListener('nouveau-fichier-ajoute', (event: any) => {
      this.traiterNouveauFichier(event.detail);
    });

    window.addEventListener('dashboard-update', (event: any) => {
      this.traiterDashboardUpdate(event.detail);
    });

    window.addEventListener('forcer-synchronisation', () => {
      this.forcerSynchronisation();
    });
  }

  // Traiter un nouveau fichier
  private traiterNouveauFichier(fichierData: any): void {
    console.log('📥 Traitement nouveau fichier:', fichierData);
    
    const fichierEvent: FichierEvent = {
      id: fichierData.id || Date.now().toString(),
      nomFichier: fichierData.nomFichier,
      typeFichier: fichierData.typeFichier,
      montant: fichierData.montant,
      sens: fichierData.sens,
      codeValeur: fichierData.codeValeur,
      timestamp: new Date()
    };

    this.fichierEventsSubject.next(fichierEvent);

    // Émettre un événement dashboard
    const dashboardEvent: DashboardEvent = {
      type: 'nouveau-fichier',
      data: fichierEvent,
      timestamp: new Date()
    };

    this.dashboardEventsSubject.next(dashboardEvent);
  }

  // Traiter une mise à jour du dashboard
  private traiterDashboardUpdate(data: any): void {
    console.log('📊 Traitement mise à jour dashboard:', data);
    
    if (data.type === 'ctr-bo-stats') {
      this.ctrBoStatsSubject.next(data.data);
    }

    const dashboardEvent: DashboardEvent = {
      type: 'ctr-bo-update',
      data: data,
      timestamp: new Date()
    };

    this.dashboardEventsSubject.next(dashboardEvent);
  }

  // Forcer la synchronisation
  private forcerSynchronisation(): void {
    console.log('🔄 Forçage de la synchronisation');
    
    const dashboardEvent: DashboardEvent = {
      type: 'synchronisation',
      data: { force: true },
      timestamp: new Date()
    };

    this.dashboardEventsSubject.next(dashboardEvent);
  }

  // Méthodes publiques pour émettre des événements

  // Émettre un nouveau fichier
  emettreNouveauFichier(fichierData: any): void {
    console.log('📤 Émission nouveau fichier:', fichierData);
    
    // Sauvegarder dans localStorage
    const fichiersExistants = localStorage.getItem('fichiersAjoutes') || '[]';
    const fichiers = JSON.parse(fichiersExistants);
    fichiers.push(fichierData);
    localStorage.setItem('fichiersAjoutes', JSON.stringify(fichiers));
    
    // Émettre l'événement
    this.traiterNouveauFichier(fichierData);
  }

  // Émettre une mise à jour CTR/BO
  emettreCtrBoUpdate(stats: any): void {
    console.log('📤 Émission mise à jour CTR/BO:', stats);
    
    const dashboardEvent: DashboardEvent = {
      type: 'ctr-bo-update',
      data: {
        type: 'ctr-bo-stats',
        data: stats
      },
      timestamp: new Date()
    };

    this.dashboardEventsSubject.next(dashboardEvent);
  }

  // Émettre une alerte
  emettreAlerte(alerte: any): void {
    console.log('🚨 Émission alerte:', alerte);
    
    const dashboardEvent: DashboardEvent = {
      type: 'alerte',
      data: alerte,
      timestamp: new Date()
    };

    this.dashboardEventsSubject.next(dashboardEvent);
  }

  // Méthodes pour récupérer les données

  // Récupérer les derniers fichiers ajoutés
  getDerniersFichiers(): FichierEvent[] {
    try {
      const fichiers = localStorage.getItem('fichiersAjoutes');
      if (fichiers) {
        return JSON.parse(fichiers).slice(-10); // Derniers 10 fichiers
      }
    } catch (error) {
      console.error('❌ Erreur récupération derniers fichiers:', error);
    }
    return [];
  }

  // Récupérer les statistiques CTR/BO
  getCtrBoStats(): any {
    return this.ctrBoStatsSubject.value;
  }

  // Méthodes utilitaires

  // Vérifier si un fichier existe déjà
  fichierExiste(nomFichier: string): boolean {
    const fichiers = this.getDerniersFichiers();
    return fichiers.some(f => f.nomFichier === nomFichier);
  }

  // Nettoyer les anciens fichiers
  nettoyerAnciensFichiers(): void {
    try {
      const fichiers = this.getDerniersFichiers();
      const fichiersRecents = fichiers.filter(f => {
        const dateFichier = new Date(f.timestamp);
        const maintenant = new Date();
        const difference = maintenant.getTime() - dateFichier.getTime();
        const jours = difference / (1000 * 3600 * 24);
        return jours <= 7; // Garder seulement les 7 derniers jours
      });
      
      localStorage.setItem('fichiersAjoutes', JSON.stringify(fichiersRecents));
      console.log('🧹 Anciens fichiers nettoyés');
    } catch (error) {
      console.error('❌ Erreur nettoyage fichiers:', error);
    }
  }

  // Obtenir les statistiques de fichiers
  getStatistiquesFichiers(): any {
    const fichiers = this.getDerniersFichiers();
    
    const stats = {
      total: fichiers.length,
      parType: {} as any,
      parSens: {} as any,
      parCode: {} as any,
      montantTotal: 0
    };

    fichiers.forEach(fichier => {
      // Par type
      stats.parType[fichier.typeFichier] = (stats.parType[fichier.typeFichier] || 0) + 1;
      
      // Par sens
      stats.parSens[fichier.sens] = (stats.parSens[fichier.sens] || 0) + 1;
      
      // Par code
      stats.parCode[fichier.codeValeur] = (stats.parCode[fichier.codeValeur] || 0) + 1;
      
      // Montant total
      stats.montantTotal += fichier.montant || 0;
    });

    return stats;
  }
} 