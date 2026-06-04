import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  // Statistiques par statut
  getStatsByStatus(): Observable<any> {
    return this.http.get(`${this.baseUrl}/fichiers/stats/status`);
  }

  // Statistiques mensuelles
  getMonthlyStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/fichiers/stats/monthly`);
  }

  // Tous les fichiers
  getAllFichiers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/fichiers`);
  }

  // Fichiers avec filtres
  getFichiersWithFilters(params: any): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/fichiers/filter`, { params });
  }

  // Alertes
  getAlerts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/fichiers/alerts`);
  }

  // Fichiers en attente
  getPendingFichiers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/fichiers/pending`);
  }

  // Fichiers récents
  getRecentFichiers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/fichiers/recent`);
  }

  // Montants par type
  getAmountsByType(): Observable<any> {
    return this.http.get(`${this.baseUrl}/fichiers/stats/amounts`);
  }

  // Statistiques par utilisateur
  getStatsByUser(userId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/fichiers/stats/user/${userId}`);
  }

  // Créer un fichier
  createFichier(fichier: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/fichiers`, fichier);
  }

  // Mettre à jour un fichier
  updateFichier(id: number, fichier: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/fichiers/${id}`, fichier);
  }

  // Supprimer un fichier
  deleteFichier(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/fichiers/${id}`);
  }
}