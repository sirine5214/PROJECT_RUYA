import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router'; 

@Component({
  selector: 'app-forget-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forget-password.component.html',
  styleUrls: ['./forget-password.component.scss']
})
export default class ForgetPasswordComponent implements OnInit {
  form: FormGroup;
  submitted = false;
  successMessage = '';
  errorMessage = '';
  isLoading = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
    
    // S'assurer qu'il n'y a aucune valeur par défaut
    this.form.patchValue({
      email: ''
    });
  }

  ngOnInit() {
    // Réinitialiser le formulaire au chargement de la page
    this.form.reset();
    this.form.patchValue({
      email: ''
    });
    
    // Nettoyer complètement le champ email
    setTimeout(() => {
      const emailInput = document.getElementById('email') as HTMLInputElement;
      if (emailInput) {
        emailInput.value = '';
        emailInput.setAttribute('autocomplete', 'new-password');
        emailInput.setAttribute('readonly', 'readonly');
      }
    }, 100);
  }

  // Méthode pour nettoyer le champ quand on clique dessus
  onEmailFocus(event: any) {
    const input = event.target;
    input.removeAttribute('readonly');
    input.value = '';
    this.form.patchValue({ email: '' });
  }

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.successMessage = '';
    this.errorMessage = '';

    if (this.form.invalid) return;

    const email = this.f['email'].value;
    this.isLoading = true;

    // Simuler un délai pour montrer l'icône de chargement
    setTimeout(() => {
      this.authService.resetPassword(email).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = 'Un lien de réinitialisation a été envoyé.';
          this.router.navigate(['/guest/reset-password']);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.status === 404
            ? 'Adresse e-mail non reconnue.'
            : 'Erreur lors de la réinitialisation.';
        }
      });
    }, 2000); // Délai de 2 secondes pour montrer l'icône de chargement
  }
}
