import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service'; // assure-toi que ce chemin est correct
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HttpClientModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent {
  resetForm: FormGroup;
  submitted = false;
  errorMsg = '';
  successMsg = '';
  loading = false;
  email = '';
  expectedCode = ''; // plus besoin si on utilise un vrai backend

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
    this.resetForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

  }

  get f() { return this.resetForm.controls; }

onSubmit() {
  this.submitted = true;
  this.errorMsg = '';
  this.successMsg = '';

  if (this.resetForm.invalid) return;

  this.loading = true;
  const code = this.f['code'].value;
  const password = this.f['password'].value;

  this.authService.resetPasswordConfirm(code, password).subscribe({
    next: () => {
      this.successMsg = 'Mot de passe changé avec succès !';
      this.loading = false;
      setTimeout(() => this.router.navigate(['/guest/login']), 2000);
    },
    error: (err) => {
      this.loading = false;
      this.errorMsg = err.status === 400
        ? 'Code invalide ou expiré.'
        : 'Erreur lors de la réinitialisation.';
    }
  });
}

}
