import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

export interface FileEvent {
  type: 'ajout' | 'envoi' | 'reception';
  fichier: any;
  timestamp: Date;
  username?: string; // Nom de l'utilisateur qui a effectué l'action
}

@Injectable({
  providedIn: 'root'
})
export class AjouterFichierService {
  private isModalOpen = new BehaviorSubject<boolean>(false);
  isModalOpen$ = this.isModalOpen.asObservable();

  public baseUrl = 'http://localhost:8081/api/fichiers';

  // Observable de la liste des fichiers
  private fichiersSubject = new BehaviorSubject<any[]>([]);
  fichiers$ = this.fichiersSubject.asObservable();

  // Observable pour notifier ajout fichier
  fichierAjoute$ = new Subject<void>();

  // Observable pour les événements de fichiers
  private fileEventsSubject = new Subject<FileEvent>();
  fileEvents$ = this.fileEventsSubject.asObservable();

  constructor(
    private http: HttpClient
  ) {}

  openModal() {
    // Vérifier si l'utilisateur est connecté avant d'ouvrir le modal
    const userStr = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    
    if (!userStr || !token) {
      console.error('❌ Utilisateur non connecté - Impossible d\'ouvrir le modal');
      alert('Vous devez être connecté pour ajouter un fichier.');
      return;
    }
    
    this.isModalOpen.next(true);
  }

  closeModal() {
    this.isModalOpen.next(false);
  }

  /**
   * Récupère l'utilisateur connecté depuis localStorage
   */
  getCurrentUser(): any {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      return null;
    }
    
    try {
      return JSON.parse(userStr);
    } catch (e) {
      console.error('❌ Erreur lors du parsing de l\'utilisateur:', e);
      return null;
    }
  }

  /**
   * Vérifie si l'utilisateur est connecté
   */
  isUserLoggedIn(): boolean {
    const userStr = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    return !!(userStr && token);
  }

  getAllFichiers(): Observable<any[]> {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const id = user.id;
    if (!id) {
      // Pas d'utilisateur → fallback global
      return this.http.get<any[]>(`${this.baseUrl}`);
    }
    return this.http
      .get<any[]>(`${this.baseUrl}/getallbyuser/${id}`)
      .pipe(
        // Fallback sur la liste globale en cas d'erreur 500
        catchError(() => this.http.get<any[]>(`${this.baseUrl}`))
      );
  }

  ajouterFichier(fichier: any): Observable<any> {
    console.log('🔄 Ajout de fichier en base de données:', fichier);
    
    return this.http.post<any>(this.baseUrl, fichier).pipe(
      tap((nouveauFichier) => {
        console.log('✅ Fichier sauvegardé en base:', nouveauFichier);
        console.log('🔍 DEBUG - Fichier créé avec utilisateur:', nouveauFichier.user);
        
        // Obtenir le nom d'utilisateur depuis localStorage
        const userStr = localStorage.getItem('user');
        const userJson = userStr ? JSON.parse(userStr) : null;
        const username = userJson?.username || userJson?.name || 'Utilisateur inconnu';
        
        console.log('🔍 DEBUG - Nom d\'utilisateur récupéré:', username);
        console.log('🔍 DEBUG - Utilisateur JSON:', userJson);
        
        // Émettre un événement de notification avec le nom d'utilisateur
        const fileEvent: FileEvent = {
          type: 'ajout',
          fichier: nouveauFichier,
          timestamp: new Date(),
          username: username
        };
        
        console.log('🔍 DEBUG - Émission d\'événement de fichier:', fileEvent);
        console.log('🔍 DEBUG - Type d\'événement:', fileEvent.type);
        console.log('🔍 DEBUG - Fichier avec utilisateur:', fileEvent.fichier);
        console.log('🔍 DEBUG - Username dans l\'événement:', fileEvent.username);
        this.fileEventsSubject.next(fileEvent);
        
        // Notifier l'ajout
        this.fichierAjoute$.next();
      })
    );
  }

  setFichiers(fichiers: any[]) {
    this.fichiersSubject.next(fichiers);
  }

  // Ajoute la modification d'un fichier
  modifierFichier(fichier: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${fichier.id}`, fichier);
  }

  // Ajoute la suppression d'un fichier
  supprimerFichier(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${id}`);
  }

  // Méthode pour émettre un événement d'envoi de fichier
  emettreEnvoiFichier(fichier: any) {
    // Obtenir le nom d'utilisateur depuis localStorage
    const userStr = localStorage.getItem('user');
    const userJson = userStr ? JSON.parse(userStr) : null;
    const username = userJson?.username || userJson?.name || 'Utilisateur inconnu';
    
    const fileEvent: FileEvent = {
      type: 'envoi',
      fichier: fichier,
      timestamp: new Date(),
      username: username
    };
    
    this.fileEventsSubject.next(fileEvent);
  }

  // Méthode pour émettre un événement de réception de fichier
  emettreReceptionFichier(fichier: any) {
    // Obtenir le nom d'utilisateur depuis localStorage
    const userStr = localStorage.getItem('user');
    const userJson = userStr ? JSON.parse(userStr) : null;
    const username = userJson?.username || userJson?.name || 'Utilisateur inconnu';
    
    const fileEvent: FileEvent = {
      type: 'reception',
      fichier: fichier,
      timestamp: new Date(),
      username: username
    };
    
    this.fileEventsSubject.next(fileEvent);
  }
}
