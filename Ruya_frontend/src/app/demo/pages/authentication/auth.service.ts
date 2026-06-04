import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Utilisateur } from '../gestion-utilisateur/gestion-utilisateur.component';
export interface User {
  id?: number;
  username: string;
  email: string;
  password: string;
  role?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/users'; // URL de l'API mise à jour

  constructor(private http: HttpClient) {}

  register(user: User): Observable<any> {
    return this.http.post(`${this.apiUrl}`, user);
  }
  login(user: User): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user); // adapte l'endpoint si nécessaire
  }
resetPassword(email: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/reset-password`, { email });
}

resetPasswordConfirm(code: string, newPassword: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/confirm-reset-password`, {
    resetCode: code,
    newPassword: newPassword
  });
}
getAllUsers(): Observable<Utilisateur[]> {
  return this.http.get<Utilisateur[]>(this.apiUrl);
}
updateUserStatus(id: number, active: boolean): Observable<Utilisateur> {
  return this.http.put<Utilisateur>(`${this.apiUrl}/${id}/status?active=${active}`, null);
}

deleteUser(id: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/${id}`);
}

updateUser(id: number, userData: { username: string; email: string; role: string }): Observable<Utilisateur> {
  return this.http.put<Utilisateur>(`${this.apiUrl}/${id}`, userData);
}

// Profile management
updateProfile(id: number, profileData: { username: string; email: string }): Observable<Utilisateur> {
  return this.http.put<Utilisateur>(`${this.apiUrl}/${id}/profile`, profileData);
}

changePassword(id: number, passwordData: { currentPassword: string; newPassword: string }): Observable<any> {
  return this.http.put(`${this.apiUrl}/${id}/change-password`, passwordData);
}

getCurrentUserProfile(userId: number): Observable<Utilisateur> {
  return this.http.get<Utilisateur>(`${this.apiUrl}/profile/me?userId=${userId}`);
}

  
}
