import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';

export interface Fichier {
  id: number;
  codEn: string;
  codeValeur: string;
  createdAt: string;
  natureFichier: string;
  nomFichier: string;
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

@Injectable({
  providedIn: 'root'
})
export class AjouterFichierService {
  private fichiersSubject = new BehaviorSubject<Fichier[]>([]);
  fichiers$ = this.fichiersSubject.asObservable();

  private fichiersData: Fichier[] = [
    {
      id: 1,
      codEn: 'EN001',
      codeValeur: '30',
      createdAt: '2025-07-20T10:00:00',
      natureFichier: 'Nature A',
      nomFichier: 'fichier1.env',
      sens: 'Émis',
      typeFichier: 'cheque',
      updatedAt: '2025-07-21T12:00:00',
      montant: '1000.50',
      nomber: '12345'
    },
    {
      id: 2,
      codEn: 'EN002',
      codeValeur: '30',
      createdAt: '2025-07-22T09:30:00',
      natureFichier: 'Nature B',
      nomFichier: 'fichier2.rcp',
      sens: 'Reçu',
      typeFichier: 'cheque',
      updatedAt: '2025-07-23T15:45:00',
      montant: '2500.75',
      nomber: '67890'
    },
    // autres fichiers ici...
  ];

  constructor() {
    this.fichiersSubject.next(this.fichiersData);
  }

  getAllFichiers(): Observable<Fichier[]> {
    return of(this.fichiersData);
  }

  setFichiers(fichiers: Fichier[]) {
    this.fichiersData = fichiers;
    this.fichiersSubject.next(fichiers);
  }

  modifierFichier(modFichier: Fichier): Observable<Fichier> {
    const index = this.fichiersData.findIndex(f => f.id === modFichier.id);
    if (index > -1) {
      this.fichiersData[index] = {
        ...modFichier,
        updatedAt: new Date().toISOString()
      };
      this.fichiersSubject.next(this.fichiersData);
      return of(this.fichiersData[index]);
    }
    return of(modFichier);
  }

  supprimerFichier(id: number): Observable<boolean> {
    this.fichiersData = this.fichiersData.filter(f => f.id !== id);
    this.fichiersSubject.next(this.fichiersData);
    return of(true);
  }
}
