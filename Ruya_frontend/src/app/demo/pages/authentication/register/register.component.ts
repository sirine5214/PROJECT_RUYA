import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormGroup, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export default class RegisterComponent {

  registerForm: FormGroup;
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authservice: AuthService
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      terms: [false, Validators.requiredTrue]
    });
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.registerForm.invalid) {
      console.warn('Formulaire invalide');
      return;
    }

    const user = {
      username: this.f['username'].value,
      email: this.f['email'].value,
      password: this.f['password'].value
    };


    // ✅ Appel API backend + redirection
    this.authservice.register(user).subscribe({
      next: () => {
        console.log('Enregistrement réussi');
        this.router.navigate(['/guest/login']);
      },
      error: (err) => {
        console.error('Erreur lors de l\'enregistrement :', err);
        alert('Erreur : ' + (err.error?.message || 'Serveur inaccessible'));
      }
    });
  }
}
