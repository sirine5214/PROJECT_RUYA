import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { AuthService } from '../auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export default class LoginComponent {
  loginForm: FormGroup;
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authservice: AuthService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.loginForm.invalid) {
      console.warn('Formulaire de connexion invalide');
      return;
    }

    const user = {
      username: this.f['email'].value,
      email: this.f['email'].value, // Ajouter la propriété email requise par l'interface User
      password: this.f['password'].value
    };

    this.authservice.login(user).subscribe({
      next: (response) => {
        console.log('Connexion réussie');
  
        // ✅ Sauvegarder le token et l'utilisateur dans localStorage
        if (response && response.user) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('user', JSON.stringify(response.user));
    
          // ✅ Rediriger vers la page par défaut
          this.router.navigate(['/default']);
        } else {
          alert('Erreur : utilisateur non trouvé dans la réponse.');
        }
      },
      error: (err) => {
        console.error('Erreur de connexion :', err);
        alert('Erreur : ' + (err.error?.message || 'Identifiants invalides ou serveur inaccessible'));
      }
    });
  }
}
