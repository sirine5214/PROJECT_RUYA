import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../authentication/auth.service';

interface UserProfile {
  id: number;
  username: string;
  email: string;
  role: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  currentUser: UserProfile | null = null;
  isEditMode = false;
  isChangingPassword = false;

  // Edit form data
  editForm = {
    username: '',
    email: ''
  };

  // Password change form
  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  // Messages
  successMessage = '';
  errorMessage = '';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.loadUserProfile();
  }

  loadUserProfile() {
    const userJson = localStorage.getItem('user');
    if (userJson) {
      this.currentUser = JSON.parse(userJson);
      this.editForm = {
        username: this.currentUser?.username || '',
        email: this.currentUser?.email || ''
      };
    }
  }

  toggleEditMode() {
    this.isEditMode = !this.isEditMode;
    if (!this.isEditMode) {
      // Reset form if cancelled
      this.editForm = {
        username: this.currentUser?.username || '',
        email: this.currentUser?.email || ''
      };
    }
    this.clearMessages();
  }

  togglePasswordChange() {
    this.isChangingPassword = !this.isChangingPassword;
    if (!this.isChangingPassword) {
      this.resetPasswordForm();
    }
    this.clearMessages();
  }

  updateProfile() {
    if (!this.currentUser) return;

    this.clearMessages();

    // Validation
    if (!this.editForm.username || !this.editForm.email) {
      this.errorMessage = 'Tous les champs sont obligatoires';
      return;
    }

    if (!/^\S+@\S+\.\S+$/.test(this.editForm.email)) {
      this.errorMessage = 'Email invalide';
      return;
    }

    this.authService.updateProfile(this.currentUser.id, this.editForm).subscribe({
      next: (updatedUser) => {
        this.currentUser = updatedUser;
        // Update localStorage
        localStorage.setItem('user', JSON.stringify(updatedUser));
        this.successMessage = 'Profil mis à jour avec succès';
        this.isEditMode = false;
      },
      error: (err) => {
        console.error('Erreur mise à jour profil:', err);
        this.errorMessage = err.error?.error || 'Erreur lors de la mise à jour du profil';
      }
    });
  }

  changePassword() {
    if (!this.currentUser) return;

    this.clearMessages();

    // Validation
    if (!this.passwordForm.currentPassword || !this.passwordForm.newPassword || !this.passwordForm.confirmPassword) {
      this.errorMessage = 'Tous les champs sont obligatoires';
      return;
    }

    if (this.passwordForm.newPassword.length < 6) {
      this.errorMessage = 'Le nouveau mot de passe doit contenir au moins 6 caractères';
      return;
    }

    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas';
      return;
    }

    this.authService.changePassword(this.currentUser.id, {
      currentPassword: this.passwordForm.currentPassword,
      newPassword: this.passwordForm.newPassword
    }).subscribe({
      next: () => {
        this.successMessage = 'Mot de passe modifié avec succès';
        this.isChangingPassword = false;
        this.resetPasswordForm();
      },
      error: (err) => {
        console.error('Erreur changement mot de passe:', err);
        this.errorMessage = err.error?.error || 'Erreur lors du changement de mot de passe';
      }
    });
  }

  resetPasswordForm() {
    this.passwordForm = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  }

  clearMessages() {
    this.successMessage = '';
    this.errorMessage = '';
  }

  getRoleBadgeClass(): string {
    if (this.currentUser?.role === 'ADMIN') {
      return 'badge-admin';
    }
    return 'badge-user';
  }

  getStatusBadgeClass(): string {
    return this.currentUser?.isActive ? 'badge-active' : 'badge-inactive';
  }

  formatDate(date: Date | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
