import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../authentication/auth.service';

export interface Utilisateur {
  id: number;           
  username: string;
  email: string;
  role: string;
  password: string;
  isActive: boolean;
  createdAt: Date;        
  updatedAt: Date;
}

@Component({
  selector: 'app-gestion-utilisateur',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-utilisateur.component.html',
  styleUrls: ['./gestion-utilisateur.component.scss']
})
export class GestionUtilisateurComponent implements OnInit {
  utilisateurs: Utilisateur[] = [];
  utilisateursFiltres: Utilisateur[] = [];

  // Pour modal d'ajout : username au lieu de nom/prenom séparés
  newUser = { username: '', email: '', password: '', role: '' };
  isModalOpen = false;
  errorMsg = '';
  successMsg = '';
  searchTerm = '';

  // Pour modal de modification
  isEditModalOpen = false;
  editUser: Utilisateur | null = null;
  editForm = { username: '', email: '', role: '' };

  // Pagination
  currentPage = 1;
  itemsPerPage = 5;
  totalPages = 1;

  constructor(private userService: AuthService) {}

  ngOnInit() {
    this.chargerUtilisateurs();
  }

  chargerUtilisateurs() {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.utilisateurs = users;
        this.appliquerFiltres();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        this.afficherErreur('Erreur lors du chargement des utilisateurs');
      }
    });
  }

  appliquerFiltres() {
    if (!this.searchTerm.trim()) {
      this.utilisateursFiltres = [...this.utilisateurs];
    } else {
      const term = this.searchTerm.toLowerCase();
      this.utilisateursFiltres = this.utilisateurs.filter(u => 
        u.username.toLowerCase().includes(term) || 
        u.email.toLowerCase().includes(term)
      );
    }
    this.calculerPagination();
  }

  calculerPagination() {
    this.totalPages = Math.ceil(this.utilisateursFiltres.length / this.itemsPerPage);
    if (this.currentPage > this.totalPages) {
      this.currentPage = Math.max(1, this.totalPages);
    }
  }

  get utilisateursPagines(): Utilisateur[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.utilisateursFiltres.slice(startIndex, startIndex + this.itemsPerPage);
  }

  changerPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  supprimerUtilisateur(id: number) {
    const user = this.utilisateurs.find(u => u.id === id);
    if (!user) return;

    const confirmation = confirm(`Voulez-vous vraiment supprimer l'utilisateur "${user.username}" ?\n\nCette action est irréversible.`);
    if (!confirmation) return;

    this.userService.deleteUser(id).subscribe({
      next: () => {
        this.utilisateurs = this.utilisateurs.filter(u => u.id !== id);
        this.appliquerFiltres();
        this.afficherSucces(`Utilisateur "${user.username}" supprimé avec succès`);
      },
      error: (err) => {
        console.error('Erreur lors de la suppression', err);
        this.afficherErreur('Erreur lors de la suppression de l\'utilisateur');
      }
    });
  }

  activerUtilisateur(id: number) {
    const user = this.utilisateurs.find(u => u.id === id);
    if (!user) return;

    const confirmation = confirm(`Voulez-vous activer l'utilisateur "${user.username}" ?`);
    if (!confirmation) return;

    this.userService.updateUserStatus(id, true).subscribe({
      next: updatedUser => {
        const userIndex = this.utilisateurs.findIndex(u => u.id === id);
        if (userIndex !== -1) {
          this.utilisateurs[userIndex].isActive = updatedUser.isActive;
          this.appliquerFiltres();
          this.afficherSucces(`Utilisateur "${user.username}" activé avec succès`);
        }
      },
      error: err => {
        console.error('Erreur activation', err);
        this.afficherErreur('Erreur lors de l\'activation de l\'utilisateur');
      }
    });
  }

  desactiverUtilisateur(id: number) {
    const user = this.utilisateurs.find(u => u.id === id);
    if (!user) return;

    const confirmation = confirm(`Voulez-vous désactiver l'utilisateur "${user.username}" ?\n\nL'utilisateur ne pourra plus se connecter.`);
    if (!confirmation) return;

    this.userService.updateUserStatus(id, false).subscribe({
      next: updatedUser => {
        const userIndex = this.utilisateurs.findIndex(u => u.id === id);
        if (userIndex !== -1) {
          this.utilisateurs[userIndex].isActive = updatedUser.isActive;
          this.appliquerFiltres();
          this.afficherSucces(`Utilisateur "${user.username}" désactivé avec succès`);
        }
      },
      error: err => {
        console.error('Erreur désactivation', err);
        this.afficherErreur('Erreur lors de la désactivation de l\'utilisateur');
      }
    });
  }

  ouvrirModal() {
    this.isModalOpen = true;
    this.newUser = { username: '', email: '', password: '', role: '' };
    this.errorMsg = '';
  }

  fermerModal() {
    this.isModalOpen = false;
    this.errorMsg = '';
  }

  ajouterUtilisateur() {
    this.errorMsg = '';

    if (!this.newUser.username || !this.newUser.email || !this.newUser.password || !this.newUser.role) {
      this.errorMsg = 'Tous les champs sont obligatoires.';
      return;
    }

    if (!/^\S+@\S+\.\S+$/.test(this.newUser.email)) {
      this.errorMsg = 'Format d\'email invalide.';
      return;
    }

    if (this.newUser.password.length < 6) {
      this.errorMsg = 'Le mot de passe doit contenir au moins 6 caractères.';
      return;
    }

    const nouvelUtilisateur = {
      username: this.newUser.username,
      email: this.newUser.email,
      password: this.newUser.password,
      role: this.newUser.role
    };

    this.userService.register(nouvelUtilisateur).subscribe({
      next: (createdUser) => {
        this.utilisateurs.push(createdUser);
        this.appliquerFiltres();
        this.fermerModal();
        this.afficherSucces(`Utilisateur "${createdUser.username}" créé avec succès`);
      },
      error: (err) => {
        console.error('Erreur lors de l\'ajout utilisateur', err);
        if (err.error?.error) {
          this.errorMsg = err.error.error;
        } else {
          this.errorMsg = 'Erreur lors de l\'ajout de l\'utilisateur. Vérifiez que le nom d\'utilisateur et l\'email ne sont pas déjà utilisés.';
        }
      }
    });
  }

  afficherSucces(message: string) {
    this.successMsg = message;
    setTimeout(() => {
      this.successMsg = '';
    }, 5000);
  }

  afficherErreur(message: string) {
    this.errorMsg = message;
    setTimeout(() => {
      this.errorMsg = '';
    }, 5000);
  }

  onSearchChange() {
    this.currentPage = 1;
    this.appliquerFiltres();
  }

  formaterDate(date: Date | string): string {
    if (!date) return 'N/A';
    const d = new Date(date);
    return d.toLocaleDateString('fr-FR', { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ADMIN' ? 'badge-admin' : 'badge-user';
  }

  // Fonctions pour la modification d'utilisateur
  ouvrirModalEdit(utilisateur: Utilisateur) {
    this.editUser = { ...utilisateur };
    this.editForm = {
      username: utilisateur.username,
      email: utilisateur.email,
      role: utilisateur.role
    };
    this.isEditModalOpen = true;
    this.errorMsg = '';
  }

  fermerModalEdit() {
    this.isEditModalOpen = false;
    this.editUser = null;
    this.errorMsg = '';
  }

  modifierUtilisateur() {
    this.errorMsg = '';

    if (!this.editForm.username || !this.editForm.email || !this.editForm.role) {
      this.errorMsg = 'Tous les champs sont obligatoires.';
      return;
    }

    if (!/^\S+@\S+\.\S+$/.test(this.editForm.email)) {
      this.errorMsg = 'Format d\'email invalide.';
      return;
    }

    if (!this.editUser) return;

    const utilisateurModifie = {
      username: this.editForm.username,
      email: this.editForm.email,
      role: this.editForm.role
    };

    this.userService.updateUser(this.editUser.id, utilisateurModifie).subscribe({
      next: (updatedUser) => {
        const index = this.utilisateurs.findIndex(u => u.id === this.editUser!.id);
        if (index !== -1) {
          this.utilisateurs[index] = { ...this.utilisateurs[index], ...updatedUser };
          this.appliquerFiltres();
        }
        this.fermerModalEdit();
        this.afficherSucces(`Utilisateur "${updatedUser.username}" modifié avec succès`);
      },
      error: (err) => {
        console.error('Erreur lors de la modification', err);
        if (err.error?.error) {
          this.errorMsg = err.error.error;
        } else {
          this.errorMsg = 'Erreur lors de la modification de l\'utilisateur. Vérifiez que le nom d\'utilisateur et l\'email ne sont pas déjà utilisés.';
        }
      }
    });
  }
}
