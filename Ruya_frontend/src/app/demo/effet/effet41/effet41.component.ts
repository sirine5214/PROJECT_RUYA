import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AjouterFichierService } from '../../ajouter-fichier/ajouter-fichier.service';

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
  selector: 'app-effet41',
  templateUrl: './effet41.html',
  styleUrls: ['./effet41.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class Effet41Component implements OnInit, OnDestroy {
  fichiers: Fichier[] = [];
  filteredFichiers: Fichier[] = [];
  fichierSelectionne: Fichier | null = null;
  isEditModalOpen = false;
  nomFichierBase = '';
  extensionFichier = '';
  subscription: Subscription;
  
  // Propriétés pour la recherche et le tri
  searchTerm = '';
  sortOrder = 'all';
  
  // Propriétés pour la popup de détails
  showFileDetailsPopup = false;
  selectedFile: Fichier | null = null;
  
  // 🔐 Utilisateur actuel
  currentUser: any = null;

  constructor(private ajouterFichierService: AjouterFichierService) {}
  
  /**
   * 🔐 Vérifier si l'utilisateur actuel est ADMIN
   */
  isAdmin(): boolean {
    return this.currentUser && this.currentUser.role === 'ADMIN';
  }

  ngOnInit() {
    // 🔐 Récupérer l'utilisateur actuel
    const userStr = localStorage.getItem('currentUser');
    if (userStr) {
      this.currentUser = JSON.parse(userStr);
    }
    
    this.subscription = this.ajouterFichierService.fichiers$.subscribe(data => {
      console.log('🔍 DEBUG Effet41 - Tous les fichiers reçus:', data);
      
      this.fichiers = (data || []).filter(f => {
        // Gère camelCase et SNAKE_CASE pour typeFichier et codeValeur
        const type = (f.typeFichier || f.TYPE_FICHIER || '').toString().toLowerCase();
        const code = (f.codeValeur || f.CODE_VALEUR || '').toString();
        const nomFichier = (f.nomFichier || f.NOM_FICHIER || '').toString().toLowerCase();
        
        console.log(`🔍 Fichier: ${f.nomFichier} | Type: "${type}" | Code: "${code}"`);
        
        // Vérifie le type et le code
        // Accepte 'effet' OU 'manuel' (ancienne valeur backend) avec code 41
        // Accepte UNIQUEMENT le code 41
        const isValidCode = code === '41';
        
        // 🔐 ADMIN voit tous les fichiers, SIMPLE_USER voit seulement les siens
        const matchesUser = this.isAdmin() || 
          (this.currentUser && f.user && f.user.id === this.currentUser.id);
        
        const match = isValidCode && matchesUser;
        
        if (match) {
          console.log('✅ Fichier correspond aux critères effet41');
        }
        
        return match;
      });
      
      console.log('📊 Fichiers filtrés pour effet41:', this.fichiers);
      this.applyFilters();
      
      // Vérifier s'il y a un fichier à mettre en évidence
      this.checkHighlightedFile();
    });
    this.ajouterFichierService.getAllFichiers().subscribe({
      next: (data) => this.ajouterFichierService.setFichiers(data),
      error: (err) => console.error('Erreur lors du chargement des fichiers', err)
    });
  }
  
  // Méthode pour vérifier et mettre en évidence un fichier
  checkHighlightedFile() {
    const highlightedFileId = localStorage.getItem('highlightedFileId');
    if (highlightedFileId) {
      const fileId = parseInt(highlightedFileId);
      const fileToHighlight = this.filteredFichiers.find(f => f.id === fileId);
      
      if (fileToHighlight) {
        console.log('🔍 DEBUG - Fichier à mettre en évidence trouvé:', fileToHighlight.nomFichier);
        
        // Faire défiler vers le fichier
        setTimeout(() => {
          this.scrollToFile(fileId);
        }, 500);
        
        // Supprimer l'ID du localStorage après utilisation
        localStorage.removeItem('highlightedFileId');
      }
    }
  }
  
  // Méthode pour faire défiler vers un fichier spécifique
  scrollToFile(fileId: number) {
    const element = document.querySelector(`[data-file-id="${fileId}"]`);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
      
      // Ajouter une classe temporaire pour l'animation
      element.classList.add('highlighted-file');
      setTimeout(() => {
        element.classList.remove('highlighted-file');
      }, 3000);
    }
  }

  // Méthode pour appliquer les filtres et le tri
  applyFilters() {
    let filtered = [...this.fichiers];
    
    // Filtrage par recherche
    if (this.searchTerm.trim()) {
      const searchLower = this.searchTerm.toLowerCase();
      filtered = filtered.filter(fichier => 
        fichier.nomFichier.toLowerCase().includes(searchLower) ||
        fichier.codEn.toLowerCase().includes(searchLower) ||
        fichier.codeValeur.toLowerCase().includes(searchLower) ||
        fichier.natureFichier.toLowerCase().includes(searchLower) ||
        fichier.typeFichier.toLowerCase().includes(searchLower) ||
        fichier.sens.toLowerCase().includes(searchLower)
      );
    }
    
    // Tri par date
    if (this.sortOrder === 'recent') {
      filtered.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    } else if (this.sortOrder === 'oldest') {
      filtered.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }
    
    this.filteredFichiers = filtered;
  }

  // Méthode appelée lors du changement de recherche
  onSearchChange() {
    this.applyFilters();
  }

  // Méthode appelée lors du changement de tri
  onSortChange() {
    this.applyFilters();
  }

  // Méthode pour réinitialiser les filtres
  resetFilters() {
    this.searchTerm = '';
    this.sortOrder = 'all';
    this.applyFilters();
  }

  // Méthode pour afficher les détails du fichier
  showFileDetails(fichier: Fichier) {
    this.selectedFile = fichier;
    this.showFileDetailsPopup = true;
  }

  // Méthode pour fermer la popup
  closeFileDetailsPopup() {
    this.showFileDetailsPopup = false;
    this.selectedFile = null;
  }

  ngOnDestroy() {
    if (this.subscription) this.subscription.unsubscribe();
  }

  modifierFichier(fichier: Fichier) {
    this.fichierSelectionne = {...fichier}; // clone pour éviter modif directe
    this.isEditModalOpen = true;
    const parts = fichier.nomFichier.split('.');
    this.extensionFichier = parts.length > 1 ? parts.pop()! : '';
    this.nomFichierBase = parts.join('.');
  }

  fermerEditModal() {
    this.isEditModalOpen = false;
    this.fichierSelectionne = null;
    this.nomFichierBase = '';
    this.extensionFichier = '';
  }

  onBackdropClick(event: Event) {
    if (event.target === event.currentTarget) {
      this.fermerEditModal();
    }
  }

  sauvegarderNomFichier(nouveauNomBase: string) {
    if (this.fichierSelectionne) {
      // S'assurer que l'extension est préservée (.env ou .rcp)
      const extension = this.extensionFichier || '';
      const nouveauNomComplet = nouveauNomBase + (extension ? '.' + extension : '');
      
      const fichierModifie = {...this.fichierSelectionne, nomFichier: nouveauNomComplet};
      this.ajouterFichierService.modifierFichier(fichierModifie).subscribe(() => {
        this.fermerEditModal();
      });
    }
  }

  supprimerFichier(fichier: Fichier) {
    if (confirm('Voulez-vous vraiment supprimer ce fichier ?')) {
      this.ajouterFichierService.supprimerFichier(fichier.id).subscribe(() => {
        // La suppression se répercute automatiquement via BehaviorSubject
      });
    }
  }
}
